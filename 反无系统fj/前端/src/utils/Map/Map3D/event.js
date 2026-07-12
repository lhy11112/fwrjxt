export default {
    viewer: {},
    handler: {},

    // 初始化 viewer
    init(viewer) {
        this.viewer = viewer
    },

    // 场景点击事件
    initSceneEvent(eventType, result) {
        const that = this;
        this.handler = new Cesium.ScreenSpaceEventHandler(this.viewer.canvas)
        this.handler.setInputAction(e => {
            if (e.position) {
                const position = viewer.scene.pickPosition(e.position);
                result(e, position);
            } else if (e.endPosition && !isNaN(e.endPosition.x) && !isNaN(e.endPosition.y)) {
                const position = this.viewer.scene.pickPosition(e.endPosition);
                result(e, position);
            }
        }, Cesium.ScreenSpaceEventType[eventType])
    },

    removeEvent(eventType) {
        this.handler.removeInputAction(Cesium.ScreenSpaceEventType[eventType])//移除事件
        this.viewer.screenSpaceEventHandler.removeInputAction(Cesium.ScreenSpaceEventType[eventType]);
    }

}
