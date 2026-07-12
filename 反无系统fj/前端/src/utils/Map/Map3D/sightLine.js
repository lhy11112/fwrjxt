import { Cartesian2toDegrees } from './common'
let sightLine,
    handlerPoint,
    handler,
    num = 0,
    viewer;
//   scene:any

const addViewFlag = false, // 添加观察点
    addTargetFlag = false, // 添加目标点
    couldRemove = false; // 能否移出目标点


export default {
    sightLine,
    handlerPoint,
    handler,
    addViewFlag,
    addTargetFlag,
    couldRemove,
    num,
    viewer,
    /**
     * 通视分析封装
     */
    init: function (scene, viewer) {
        const that = this
        that.viewer = viewer
        that.sightLine = new Cesium.Sightline(scene)
        that.sightLine.lineWidth = 3; // 设置通视线的宽度
        that.sightLine.hiddenColor =  Cesium.Color.fromCssColorString("rgb(255, 0, 0)"); // 设置不可见部分颜色
        that.sightLine.visibleColor = Cesium.Color.fromCssColorString("rgb(0, 0, 255)");  // 设置可见部分颜色
        that.sightLine.build()
        that.handlerPoint = new Cesium.DrawHandler(viewer, Cesium.DrawMode.Point)
        that.handler = new Cesium.ScreenSpaceEventHandler(scene.canvas)
        that.handlerPoint.drawEvt.addEventListener(function (result) {
            //添加观察点
            if (that.addViewFlag) {
                const position = result.object.position

                //将获取的点的位置转化成经纬度
                const cartographic = Cartesian2toDegrees(position)

                //设置观察点
                that.sightLine.viewPosition = cartographic
                that.addViewFlag = false
            }
            that.handlerPoint.deactivate()
        })
    },
    //添加通视点
    addTarget(CartesianPosition) {
        const that = this
        if (that.addViewFlag === false && that.addTargetFlag) {
            num += 1
            //将获取的点的位置转化成经纬度
            const cartographic = Cartesian2toDegrees(CartesianPosition)
            //添加目标点
            const name = 'point' + num
            that.sightLine.addTargetPoint({
                position: cartographic,
                name: name
            })
            that.couldRemove = true
        }
    },
    // 添加观察点点击
    addViewPointClick() {
        const that = this
        that.addViewFlag = true
        if (that.handlerPoint.active) {
            return
        }
        if (couldRemove) {
            that.sightLine.removeAllTargetPoint()
        }
        that.handlerPoint.activate()
    },
    // 添加目标点点击
    addTargetPointClick() {
        const that = this
        that.addViewFlag = false
        that.addTargetFlag = true

        //鼠标点击事件，添加点
        that.handler.setInputAction(function (e) {
            const position = that.viewer.scene.pickPosition(e.position)
            that.addTarget(position)
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

        //鼠标右键事件，结束
        that.handler.setInputAction(function () {
            that.handler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK)
        }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
    },
    clear() {
        const that = this
        that.addViewFlag = false
        that.addTargetFlag = false
        that.handlerPoint.clear()
        that.num = 0
        if (that.couldRemove) {
            that.sightLine.removeAllTargetPoint()
            that.couldRemove = false
        }
    }
}
