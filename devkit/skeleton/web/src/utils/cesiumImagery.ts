/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Cesium 底图配置。
 *
 * 通过 .env 中的 VITE_MAP_PROVIDER 切换底图，无需改代码：
 *   VITE_MAP_PROVIDER=natural-earth  Cesium 内置 NaturalEarthII 低分辨率底图（从 CDN 加载）
 *   VITE_MAP_PROVIDER=esri           Esri 卫星影像（需外网）
 *   VITE_MAP_PROVIDER=osm            OpenStreetMap 矢量底图（需外网）
 *   VITE_MAP_PROVIDER=bing           Bing Maps 卫星（需外网 + VITE_BING_KEY）
 *   VITE_MAP_PROVIDER=tianditu       天地图卫星+中文标注（需外网 + VITE_TIANDITU_KEY）
 */

import { getRuntimeConfig } from '@/utils/runtimeConfig'

type BuiltinProvider = 'natural-earth' | 'esri' | 'osm' | 'bing' | 'tianditu'
type Provider = BuiltinProvider | (string & Record<never, never>)

function buildEsriProvider(): Cesium.ImageryProvider[] {
  return [
    new Cesium.UrlTemplateImageryProvider({
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      credit: new Cesium.Credit('Esri World Imagery'),
      maximumLevel: 19,
    }),
    // 地名/道路标注叠加层
    new Cesium.UrlTemplateImageryProvider({
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}',
      credit: new Cesium.Credit('Esri'),
      maximumLevel: 19,
    }),
  ]
}

function buildOsmProvider(): Cesium.ImageryProvider[] {
  return [
    new Cesium.UrlTemplateImageryProvider({
      url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
      credit: new Cesium.Credit('© OpenStreetMap contributors'),
      maximumLevel: 19,
    }),
  ]
}

async function buildBingProvider(): Promise<Cesium.ImageryProvider[]> {
  const key = getRuntimeConfig('BING_KEY')
  if (!key) {
    console.warn('[cesiumImagery] VITE_BING_KEY 未配置，回退到离线底图')
    return buildNaturalEarthProvider()
  }
  // Cesium 1.100+ 废弃了同步构造函数，使用异步工厂 fromUrl 进行 key 验证
  const provider = await Cesium.BingMapsImageryProvider.fromUrl(
    'https://dev.virtualearth.net',
    { key, mapStyle: Cesium.BingMapsStyle.AERIAL_WITH_LABELS_ON_DEMAND },
  )
  return [provider]
}

async function buildTiandituProvider(): Promise<Cesium.ImageryProvider[]> {
  const key = getRuntimeConfig('TIANDITU_KEY')
  if (!key) {
    console.warn('[cesiumImagery] VITE_TIANDITU_KEY 未配置，回退到离线底图')
    return buildNaturalEarthProvider()
  }
  const subdomains = ['0', '1', '2', '3', '4', '5', '6', '7']
  // WebMapTileServiceImageryProvider 不支持 URL 中的 {s} 替换，
  // 通过随机选取子域名前缀来分散请求（每次构造随机，非每瓦片随机）
  const pick = (): string => subdomains[Math.floor(Math.random() * subdomains.length)]
  const imgUrl = `https://t${pick()}.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={TileMatrix}&TILEROW={TileRow}&TILECOL={TileCol}&tk=${key}`
  const ciaUrl = `https://t${pick()}.tianditu.gov.cn/cia_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cia&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={TileMatrix}&TILEROW={TileRow}&TILECOL={TileCol}&tk=${key}`
  return [
    new Cesium.WebMapTileServiceImageryProvider({
      url: imgUrl,
      layer: 'img',
      style: 'default',
      format: 'tiles',
      tileMatrixSetID: 'w',
      credit: new Cesium.Credit('© 天地图'),
      maximumLevel: 18,
    }),
    new Cesium.WebMapTileServiceImageryProvider({
      url: ciaUrl,
      layer: 'cia',
      style: 'default',
      format: 'tiles',
      tileMatrixSetID: 'w',
      credit: new Cesium.Credit('© 天地图'),
      maximumLevel: 18,
    }),
  ]
}

async function buildNaturalEarthProvider(): Promise<Cesium.ImageryProvider[]> {
  const baseUrl = (window as unknown as { CESIUM_BASE_URL?: string }).CESIUM_BASE_URL ?? '/cesium/'
  const provider = await Cesium.TileMapServiceImageryProvider.fromUrl(
    `${baseUrl}Assets/Textures/NaturalEarthII`,
  )
  return [provider]
}

const DEFAULT_PROVIDER: BuiltinProvider = 'esri'

const BUILDERS: Record<string, () => Cesium.ImageryProvider[] | Promise<Cesium.ImageryProvider[]>> = {
  'natural-earth': buildNaturalEarthProvider,
  esri: buildEsriProvider,
  osm: buildOsmProvider,
  bing: buildBingProvider,
  tianditu: buildTiandituProvider,
}

/**
 * 为 Cesium Viewer 应用底图。
 * 优先从 window.__FH_CONFIG__.MAP_PROVIDER 读取（运行时注入），
 * 未配置则 fallback 到 import.meta.env.VITE_MAP_PROVIDER（本地开发），
 * 两者都未配置则默认 esri（Esri 卫星影像，需外网）。
 */
export async function applyBaseImagery(viewer: Cesium.Viewer): Promise<void> {
  const provider = getRuntimeConfig('MAP_PROVIDER', DEFAULT_PROVIDER) as Provider
  const build = BUILDERS[provider] ?? BUILDERS[DEFAULT_PROVIDER]

  viewer.imageryLayers.removeAll()
  const providers = await build()
  for (const p of providers) {
    viewer.imageryLayers.addImageryProvider(p)
  }
}
