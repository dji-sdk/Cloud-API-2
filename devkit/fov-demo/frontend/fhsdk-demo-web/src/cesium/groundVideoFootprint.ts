// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * WHEP 直播贴地：细分网格 + 显式 UV（与视锥 TL/TR/BR/BL 对齐）；
 * 普通 Primitive + polygonOffset 解决 z-fighting（GroundPrimitive 无法自定义 UV）。
 */
import * as Cesium from 'cesium'
import { terrainSurfaceHeight } from '@/cesium/seiRaycast'

const GRID = 20
/** 亚米级抬升 + polygonOffset，减轻与 globe 的深度冲突 */
const HEIGHT_OFFSET_M = 0.5
const MIN_EDGE_M = 0.3

const VIDEO_RENDER_STATE = {
  depthTest: { enabled: true },
  polygonOffset: { enabled: true, factor: -4, units: -4 },
}

function bilinearCartographic(
  tl: Cesium.Cartographic,
  tr: Cesium.Cartographic,
  br: Cesium.Cartographic,
  bl: Cesium.Cartographic,
  u: number,
  v: number,
): Cesium.Cartographic {
  const w00 = (1 - u) * (1 - v)
  const w10 = u * (1 - v)
  const w11 = u * v
  const w01 = (1 - u) * v
  return new Cesium.Cartographic(
    w00 * tl.longitude + w10 * tr.longitude + w11 * br.longitude + w01 * bl.longitude,
    w00 * tl.latitude + w10 * tr.latitude + w11 * br.latitude + w01 * bl.latitude,
    w00 * tl.height + w10 * tr.height + w11 * br.height + w01 * bl.height,
  )
}

function footprintKey(corners: Cesium.Cartesian3[]): string {
  return corners.map((p) => `${p.x.toFixed(2)},${p.y.toFixed(2)},${p.z.toFixed(2)}`).join('|')
}

function isValidFootprint(corners: Cesium.Cartesian3[]): boolean {
  for (const c of corners) {
    if (!Number.isFinite(c.x) || !Number.isFinite(c.y) || !Number.isFinite(c.z)) return false
    if (Cesium.Cartesian3.magnitudeSquared(c) < 1e10) return false
  }
  for (let i = 0; i < 4; i++) {
    if (Cesium.Cartesian3.distance(corners[i]!, corners[(i + 1) % 4]!) < MIN_EDGE_M) return false
  }
  return true
}

function buildDrapedMesh(
  viewer: Cesium.Viewer,
  corners: Cesium.Cartesian3[],
): {
  positions: Float64Array
  sts: Float32Array
  indices: Uint16Array
} | null {
  const cartos = corners.map((c) => Cesium.Cartographic.fromCartesian(c))
  const [tl, tr, br, bl] = cartos as [
    Cesium.Cartographic,
    Cesium.Cartographic,
    Cesium.Cartographic,
    Cesium.Cartographic,
  ]

  const verts = GRID + 1
  const positions = new Float64Array(verts * verts * 3)
  const sts = new Float32Array(verts * verts * 2)

  let pi = 0
  let si = 0
  for (let row = 0; row < verts; row++) {
    const v = row / GRID
    for (let col = 0; col < verts; col++) {
      const u = col / GRID
      const carto = bilinearCartographic(tl, tr, br, bl, u, v)
      const hBilinear = carto.height
      const hTerrain = terrainSurfaceHeight(viewer, carto)
      const h = Math.max(hBilinear, hTerrain) + HEIGHT_OFFSET_M
      if (!Number.isFinite(h)) return null
      const pos = Cesium.Cartesian3.fromRadians(carto.longitude, carto.latitude, h)
      if (!Number.isFinite(pos.x) || !Number.isFinite(pos.y) || !Number.isFinite(pos.z)) return null
      positions[pi++] = pos.x
      positions[pi++] = pos.y
      positions[pi++] = pos.z
      // TL(0,0) TR(1,0) BR(1,1) BL(0,1)；Cesium ST 的 t 轴向上
      sts[si++] = u
      sts[si++] = 1 - v
    }
  }

  const indices: number[] = []
  for (let row = 0; row < GRID; row++) {
    for (let col = 0; col < GRID; col++) {
      const i = row * verts + col
      indices.push(i, i + 1, i + verts)
      indices.push(i + 1, i + verts + 1, i + verts)
    }
  }

  return { positions, sts, indices: new Uint16Array(indices) }
}

export class GroundVideoFootprint {
  private mesh: Cesium.Primitive | undefined
  private show = false
  private dirty = true
  private lastKey = ''
  private readonly corners = [
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
  ]
  private readonly material: Cesium.Material

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly viewer: Cesium.Viewer,
  ) {
    const legacy = viewer.entities.getById('sei-footprint-video')
    if (legacy) viewer.entities.remove(legacy)

    const placeholder = document.createElement('canvas')
    placeholder.width = 2
    placeholder.height = 2
    const ctx = placeholder.getContext('2d')!
    ctx.fillStyle = '#000'
    ctx.fillRect(0, 0, 2, 2)
    this.material = Cesium.Material.fromType('Image', { image: placeholder })
  }

  bindVideo(video: HTMLVideoElement | null | undefined) {
    if (video) {
      this.material.uniforms.image = video
    }
  }

  update(
    _viewer: Cesium.Viewer,
    positions: Cesium.Cartesian3[] | null,
    video: HTMLVideoElement | null | undefined,
  ) {
    if (!positions || positions.length < 4) return

    this.bindVideo(video)
    const key = footprintKey(positions)
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
      for (let i = 0; i < 4; i++) {
        Cesium.Cartesian3.clone(positions[i]!, this.corners[i]!)
      }
    }
    this.show = true
    this.sync()
  }

  hide(_viewer: Cesium.Viewer) {
    if (!this.show && !this.mesh) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync()
  }

  private removeMesh() {
    if (this.mesh) {
      this.collection.remove(this.mesh)
      this.mesh = undefined
    }
  }

  private sync() {
    if (!this.show) {
      this.removeMesh()
      return
    }
    if (!this.dirty && this.mesh) return

    this.removeMesh()

    if (!isValidFootprint(this.corners)) {
      this.dirty = true
      return
    }

    const meshData = buildDrapedMesh(this.viewer, this.corners)
    if (!meshData) {
      this.dirty = true
      return
    }

    this.dirty = false
    const { positions, sts, indices } = meshData
    const boundingSphere = Cesium.BoundingSphere.fromVertices(positions)

    this.mesh = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.Geometry({
            attributes: {
              position: new Cesium.GeometryAttribute({
                componentDatatype: Cesium.ComponentDatatype.DOUBLE,
                componentsPerAttribute: 3,
                values: positions,
              }),
              st: new Cesium.GeometryAttribute({
                componentDatatype: Cesium.ComponentDatatype.FLOAT,
                componentsPerAttribute: 2,
                values: sts,
              }),
            } as Cesium.GeometryAttributes,
            indices,
            primitiveType: Cesium.PrimitiveType.TRIANGLES,
            boundingSphere,
          }),
        }),
        appearance: new Cesium.MaterialAppearance({
          material: this.material,
          flat: true,
          faceForward: true,
          translucent: false,
          renderState: VIDEO_RENDER_STATE,
        }),
        asynchronous: false,
      }),
    )
    this.collection.raiseToTop(this.mesh)
  }

  destroy(_viewer: Cesium.Viewer) {
    this.removeMesh()
  }
}
