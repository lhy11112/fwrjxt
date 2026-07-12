/**
 * 3D 无人机飞行回放模块
 * 移植自源项目 window.Map3D.wrjFly
 * 提供基于 CesiumJS 的无人机飞行路径生成、模型渲染与播放控制
 */

// Cesium type references (imported lazily to avoid build issues)
let Cesium: any = null

export interface FlightPoint {
  dron_lat?: number
  dron_lng?: number
  altitude?: number
  height?: number
  data_time?: string
  sd?: number
  angle?: number
}

export interface PlaybackOptions {
  pathColor?: string
  pathWidth?: number
  modelUri?: string
  playbackSpeed?: number // multiplier, default 1
}

const DEFAULT_OPTIONS: PlaybackOptions = {
  pathColor: '#4db4ff',
  pathWidth: 3,
  modelUri: '',
  playbackSpeed: 1,
}

export class DroneFlightPlayback {
  private viewer: any
  private options: PlaybackOptions
  private pathEntities: any[] = []
  private modelEntity: any = null
  private labelEntity: any = null
  private controller: any = null
  private positionProperty: any = null
  private onDataUpdate: ((item: FlightPoint) => void) | null = null
  private onDetailUpdate: ((detail: any) => void) | null = null

  constructor(viewer: any, options?: Partial<PlaybackOptions>) {
    this.viewer = viewer
    this.options = { ...DEFAULT_OPTIONS, ...options }
  }

  /** 异步初始化 Cesium 引用 */
  static async getCesium(): Promise<any> {
    if (!Cesium) {
      Cesium = await import('cesium')
    }
    return Cesium
  }

  /** 生成无人机飞行路径（Cesium SampledPositionProperty） */
  async generateDronePath(data: FlightPoint[]): Promise<any> {
    const C = await DroneFlightPlayback.getCesium()
    const positionProperty = new C.SampledPositionProperty()

    const validPoints = data.filter(
      (p) => p.dron_lat != null && p.dron_lng != null,
    )

    for (const point of validPoints) {
      const time = C.JulianDate.fromDate(
        new Date(point.data_time || Date.now()),
      )
      const position = C.Cartesian3.fromDegrees(
        point.dron_lng!,
        point.dron_lat!,
        (point.height || point.altitude || 100) + 50,
      )
      positionProperty.addSample(time, position)
    }

    this.positionProperty = positionProperty

    // Draw path polyline
    if (validPoints.length >= 2) {
      const positions = validPoints.map((p) =>
        C.Cartesian3.fromDegrees(
          p.dron_lng!,
          p.dron_lat!,
          (p.height || p.altitude || 100) + 50,
        ),
      )
      const pathEntity = this.viewer.entities.add({
        polyline: {
          positions,
          width: this.options.pathWidth,
          material: C.Color.fromCssColorString(this.options.pathColor!),
          clampToGround: false,
        },
      })
      this.pathEntities.push(pathEntity)
    }

    return positionProperty
  }

  /** 创建无人机 3D 模型 */
  async createDroneModel(data: FlightPoint[]): Promise<any> {
    const C = await DroneFlightPlayback.getCesium()
    if (data.length === 0) return null

    const first = data[0]
    const position = C.Cartesian3.fromDegrees(
      first.dron_lng || 0,
      first.dron_lat || 0,
      (first.height || first.altitude || 100) + 50,
    )

    // Billboard-based drone marker (can be upgraded to glTF model)
    this.modelEntity = this.viewer.entities.add({
      position,
      billboard: {
        image: '/static/marker/drone-3d.png',
        width: 32,
        height: 32,
      },
      label: {
        text: `无人机`,
        font: '12px sans-serif',
        fillColor: C.Color.WHITE,
        outlineColor: C.Color.BLACK,
        outlineWidth: 2,
        verticalOrigin: C.VerticalOrigin.BOTTOM,
        pixelOffset: new C.Cartesian2(0, -20),
      },
    })

    return this.modelEntity
  }

  /** 播放控制器 */
  async createPlaybackController(
    positionProperty: any,
    rowData: FlightPoint[],
    startTime?: any,
    stopTime?: any,
  ): Promise<any> {
    const C = await DroneFlightPlayback.getCesium()

    // Move model entity to use position property
    if (this.modelEntity) {
      this.modelEntity.position = positionProperty
    }

    const start = startTime || positionProperty._property._values[0]?.[0]
    const stop =
      stopTime ||
      positionProperty._property._values[
        positionProperty._property._values.length - 1
      ]?.[0]

    if (start && stop) {
      this.viewer.clock.startTime = start.clone()
      this.viewer.clock.stopTime = stop.clone()
      this.viewer.clock.currentTime = start.clone()
      this.viewer.clock.clockRange = C.ClockRange.LOOP_STOP
      this.viewer.clock.multiplier = this.options.playbackSpeed || 1
    }

    // Store controller state
    this.controller = {
      positionProperty,
      rowData,
      start,
      stop,
      playing: false,
    }

    return this.controller
  }

  /** 播放/暂停切换 */
  togglePlayPause(play?: boolean): boolean {
    if (!this.viewer) return false
    const wasPlaying = this.viewer.clock.shouldAnimate
    this.viewer.clock.shouldAnimate =
      play !== undefined ? play : !wasPlaying
    return this.viewer.clock.shouldAnimate
  }

  /** 设置相机视角 */
  async setCameraPerspective(mode: number): Promise<void> {
    if (!this.modelEntity) return
    const C = await DroneFlightPlayback.getCesium()

    const position = this.modelEntity.position?.getValue(
      this.viewer.clock.currentTime,
    )
    if (!position) return

    if (mode === 1) {
      // Top-down view
      const offset = new C.Cartesian3(0, 0, 500)
      const target = C.SceneTransforms.wgs84ToWindowCoordinates(
        this.viewer.scene,
        position,
      )
      if (target) {
        this.viewer.camera.lookAt(
          position,
          new C.HeadingPitchRange(0, C.Math.toRadians(-90), 500),
        )
      }
    } else if (mode === 3) {
      // Follow-behind view
      this.viewer.trackedEntity = this.modelEntity
    }
  }

  /** 清除所有图层 */
  clearAllLayers(): void {
    for (const entity of this.pathEntities) {
      this.viewer.entities.remove(entity)
    }
    this.pathEntities = []
    if (this.modelEntity) {
      this.viewer.entities.remove(this.modelEntity)
      this.modelEntity = null
    }
    if (this.labelEntity) {
      this.viewer.entities.remove(this.labelEntity)
      this.labelEntity = null
    }
    this.controller = null
    this.positionProperty = null
    this.viewer.clock.shouldAnimate = false
  }

  /** 获取当前回放状态 */
  get isPlaying(): boolean {
    return this.viewer?.clock?.shouldAnimate ?? false
  }

  /** 获取路径点数量 */
  get pathPointCount(): number {
    return this.controller?.rowData?.length ?? 0
  }
}
