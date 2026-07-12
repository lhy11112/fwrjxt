let viewShed3D, viewer, pointHandler, handler, viewPosition;

export default {
    viewShed3D,
    viewer,
    pointHandler,
    handler,
    viewPosition,

    /**
     * 可视域分析封装
     */
    init: function (viewer, scene) {
        const that = this
        that.viewer = viewer
        that.viewShed3D = new Cesium.ViewShed3D(scene)
        // 先将此标记置为true，不激活鼠标移动事件中对可视域分析对象的操作
        scene.viewFlag = true
        that.pointHandler = new Cesium.DrawHandler(viewer, Cesium.DrawMode.Point)
        that.pointHandler.drawEvt.addEventListener(function (result) {
            const position = result.object.position
            that.viewPosition = position

            // 将获取的点的位置转化成经纬度
            const cartographic = Cesium.Cartographic.fromCartesian(position)
            const longitude = Cesium.Math.toDegrees(cartographic.longitude)
            const latitude = Cesium.Math.toDegrees(cartographic.latitude)
            const height = cartographic.height

            if (that.viewer.scene.viewFlag) {
                // 设置视口位置
                that.viewShed3D.viewPosition = [longitude, latitude, height]
                that.viewShed3D.build()
                // 将标记置为false以激活鼠标移动回调里面的设置可视域操作
                that.viewer.scene.viewFlag = false
            }
        })
        that.handler = new Cesium.ScreenSpaceEventHandler(scene.canvas)
        // 鼠标移动时间回调
        that.handler.setInputAction(function (e) {
            // 若此标记为false，则激活对可视域分析对象的操作
            if (!that.viewer.scene.viewFlag) {
                //获取鼠标屏幕坐标,并将其转化成笛卡尔坐标
                const position = e.endPosition
                const last = that.viewer.scene.pickPosition(position)

                //计算该点与视口位置点坐标的距离
                const distance = Cesium.Cartesian3.distance(that.viewPosition, last)

                if (distance > 0) {
                    // 将鼠标当前点坐标转化成经纬度
                    const cartographic = Cesium.Cartographic.fromCartesian(last)
                    const longitude = Cesium.Math.toDegrees(cartographic.longitude)
                    const latitude = Cesium.Math.toDegrees(cartographic.latitude)
                    const height = cartographic.height
                    // 通过该点设置可视域分析对象的距离及方向
                    that.viewShed3D.setDistDirByPoint([longitude, latitude, height])
                }
            }
        }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
        that.handler.setInputAction(function (e) {
            //鼠标右键事件回调，不再执行鼠标移动事件中对可视域的操作
            scene.viewFlag = true
            // $("#wrapper").show();
            // viewModel.direction = viewshed3D.direction;
            // viewModel.pitch = viewshed3D.pitch;
            // viewModel.distance = viewshed3D.distance;
            // viewModel.horizontalFov = viewshed3D.horizontalFov;
            // viewModel.verticalFov = viewshed3D.verticalFov;
        }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
    },
    // 绘制可视域
    chooseView() {
        const that = this
        if (that.pointHandler.active) {
            return
        }
        //先清除之前的可视域分析
        // viewer.entities.removeAll();
        that.viewShed3D.distance = 0.1
        that.viewer.scene.viewFlag = true

        //激活绘制点类
        that.pointHandler.activate()
    },
    // 清除
    clear() {
        const that = this
        // that.viewShed3D.removeAllClipRegion()

        // 清除观察点
        that.pointHandler.clear()
        that.viewShed3D.distance = 0.1
        that.viewer.scene.viewFlag = true
    }
}
