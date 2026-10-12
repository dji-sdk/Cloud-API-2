// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * 高频折线/视锥：scene.primitives + 同步重建。
 */
import * as Cesium from 'cesium'
import { buildPerspectiveFrustum, type SeiFovAngles } from '@/cesium/seiFrustum'

function positionsEqual(a: Cesium.Cartesian3, b: Cesium.Cartesian3, eps = 0.01): boolean {
  return Cesium.Cartesian3.distanceSquared(a, b) < eps * eps
}

/** 端点变化时才重建 Primitive */
export class DynamicPolyline {
  private primitive: Cesium.Primitive | undefined
  private readonly start = new Cesium.Cartesian3()
  private readonly end = new Cesium.Cartesian3()
  private show = false
  private dirty = true

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly color: Cesium.Color,
    private readonly width: number,
  ) {}

  setEndpoints(start: Cesium.Cartesian3, end: Cesium.Cartesian3) {
    const moved = !positionsEqual(this.start, start) || !positionsEqual(this.end, end)
    Cesium.Cartesian3.clone(start, this.start)
    Cesium.Cartesian3.clone(end, this.end)
    if (!this.show) {
      this.show = true
      this.dirty = true
    } else if (moved) {
      this.dirty = true
    }
    this.sync()
  }

  hide() {
    if (!this.show && !this.primitive) return
    this.show = false
    this.dirty = true
    this.sync()
  }

  sync() {
    if (!this.show) {
      if (this.primitive) {
        this.collection.remove(this.primitive)
        this.primitive = undefined
      }
      return
    }
    if (!this.dirty && this.primitive) return

    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
    this.dirty = false

    this.primitive = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.PolylineGeometry({
            positions: [this.start, this.end],
            width: this.width,
            vertexFormat: Cesium.PolylineColorAppearance.VERTEX_FORMAT,
            arcType: Cesium.ArcType.NONE,
          }),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.color),
          },
        }),
        appearance: new Cesium.PolylineColorAppearance(),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
  }
}

export interface FrustumState {
  origin: Cesium.Cartesian3
  orientation: Cesium.Quaternion
  fov: SeiFovAngles | null
  far?: number
}

/** 视锥线框 + 半透明填充 */
export class DynamicFrustum {
  private outline: Cesium.Primitive | undefined
  private fill: Cesium.Primitive | undefined
  private show = false
  private dirty = true
  private lastKey = ''

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly outlineColor: Cesium.Color,
    private readonly fillColor: Cesium.Color,
  ) {}

  update(state: FrustumState | null) {
    if (!state) {
      this.hide()
      return
    }
    const key = [
      state.origin.x, state.origin.y, state.origin.z,
      state.orientation.x, state.orientation.y, state.orientation.z, state.orientation.w,
      state.fov?.horizontalRad ?? 0,
      state.fov?.verticalRad ?? 0,
      state.far ?? 0,
    ].join(',')
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
    }
    this.show = true
    this.sync(state)
  }

  hide() {
    if (!this.show && !this.outline && !this.fill) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync(null)
  }

  private sync(state: FrustumState | null) {
    if (!this.show || !state) {
      if (this.outline) {
        this.collection.remove(this.outline)
        this.outline = undefined
      }
      if (this.fill) {
        this.collection.remove(this.fill)
        this.fill = undefined
      }
      return
    }
    if (!this.dirty && this.outline && this.fill) return

    if (this.outline) {
      this.collection.remove(this.outline)
      this.outline = undefined
    }
    if (this.fill) {
      this.collection.remove(this.fill)
      this.fill = undefined
    }
    this.dirty = false

    const geometryOptions = {
      frustum: buildPerspectiveFrustum(state.fov, state.far),
      origin: state.origin,
      orientation: state.orientation,
    }

    this.fill = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.FrustumGeometry(geometryOptions),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.fillColor),
          },
        }),
        appearance: new Cesium.PerInstanceColorAppearance({ flat: true }),
        asynchronous: false,
      }),
    )

    this.outline = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.FrustumOutlineGeometry(geometryOptions),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.outlineColor),
          },
        }),
        appearance: new Cesium.PerInstanceColorAppearance({ flat: true }),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.outline) this.collection.remove(this.outline)
    if (this.fill) this.collection.remove(this.fill)
    this.outline = undefined
    this.fill = undefined
  }
}

/** 闭合折线（视锥地面交线） */
export class DynamicClosedPolyline {
  private primitive: Cesium.Primitive | undefined
  private readonly points: Cesium.Cartesian3[] = [
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
  ]
  private show = false
  private dirty = true
  private lastKey = ''

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly color: Cesium.Color,
    private readonly width: number,
  ) {}

  update(positions: Cesium.Cartesian3[] | null) {
    if (!positions || positions.length < 4) {
      this.hide()
      return
    }
    const key = positions.map((p) => `${p.x},${p.y},${p.z}`).join('|')
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
      for (let i = 0; i < 4; i++) {
        Cesium.Cartesian3.clone(positions[i]!, this.points[i]!)
      }
    }
    this.show = true
    this.sync()
  }

  hide() {
    if (!this.show && !this.primitive) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync()
  }

  private sync() {
    if (!this.show) {
      if (this.primitive) {
        this.collection.remove(this.primitive)
        this.primitive = undefined
      }
      return
    }
    if (!this.dirty && this.primitive) return

    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
    this.dirty = false

    const loop = [
      this.points[0]!,
      this.points[1]!,
      this.points[2]!,
      this.points[3]!,
      this.points[0]!,
    ]

    this.primitive = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.PolylineGeometry({
            positions: loop,
            width: this.width,
            vertexFormat: Cesium.PolylineColorAppearance.VERTEX_FORMAT,
            arcType: Cesium.ArcType.NONE,
          }),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.color),
          },
        }),
        appearance: new Cesium.PolylineColorAppearance(),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
  }
}

/** 闭合贴地折线（视锥地面交线，Classification TERRAIN） */
export class DynamicGroundClosedPolyline {
  private primitive: Cesium.GroundPolylinePrimitive | undefined
  private readonly points: Cesium.Cartesian3[] = [
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
  ]
  private show = false
  private dirty = true
  private lastKey = ''

  constructor(
    private readonly viewer: Cesium.Viewer,
    private readonly color: Cesium.Color,
    private readonly width: number,
  ) {}

  update(positions: Cesium.Cartesian3[] | null) {
    if (!positions || positions.length < 4) {
      this.hide()
      return
    }
    const key = positions.map((p) => `${p.x},${p.y},${p.z}`).join('|')
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
      for (let i = 0; i < 4; i++) {
        Cesium.Cartesian3.clone(positions[i]!, this.points[i]!)
      }
    }
    this.show = true
    this.sync()
  }

  hide() {
    if (!this.show && !this.primitive) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync()
  }

  private sync() {
    if (!this.show) {
      if (this.primitive) {
        this.viewer.scene.groundPrimitives.remove(this.primitive)
        this.primitive = undefined
      }
      return
    }
    if (!this.dirty && this.primitive) return

    if (this.primitive) {
      this.viewer.scene.groundPrimitives.remove(this.primitive)
      this.primitive = undefined
    }
    this.dirty = false

    if (!Cesium.GroundPolylinePrimitive.isSupported(this.viewer.scene)) return

    try {
      this.primitive = this.viewer.scene.groundPrimitives.add(
        new Cesium.GroundPolylinePrimitive({
          geometryInstances: new Cesium.GeometryInstance({
            geometry: new Cesium.GroundPolylineGeometry({
              positions: this.points,
              loop: true,
              width: this.width,
            }),
            attributes: {
              color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.color),
            },
          }),
          appearance: new Cesium.PolylineColorAppearance(),
          classificationType: Cesium.ClassificationType.TERRAIN,
          asynchronous: false,
        }),
      )
    } catch {
      this.primitive = undefined
      this.dirty = true
    }
  }

  destroy() {
    if (this.primitive) {
      this.viewer.scene.groundPrimitives.remove(this.primitive)
      this.primitive = undefined
    }
  }
}
