// 开敞度分析
let scene,
    viewer,
    opennessHandler;
const opennessViewDomeArray = [];
const opennessData = {
    X: 0,
    Y: 0,
    Z: 0,
    observationRadius: 100,
    startingAngle: 0,
    endAngle: 360,
    visualColor: '#00B7EF',
    hideColor: '#E36C09',
    checked: false,
    displayTypeOptions: [
        {
            value: 0,
            label: '可视部分'
        },
        {
            value: 1,
            label: '隐藏部分'
        },
        {
            value: 2,
            label: '全部显示'
        }
    ],
    displayType: 0 // 与value类型保持一致
}
export default {
    opennessViewDomeArray,
    viewer,
    scene,
    opennessHandler,
    opennessData,
    init(viewer, scene) {
        const that = this
        that.viewer = viewer
        that.scene = scene
    },
    // 执行开敞度分析
    updateViewDome(p, opennessData) {
        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].viewPosition = p
        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].build() //执行开敞度分析
        this.opennessAddpoint(this.opennessData)
    },
    // 开敞度分析添加点
    opennessAddpoint(opennessData) {
        if (this.opennessData.X == undefined || this.opennessData.Y == undefined || this.opennessData.Z == undefined) {
            return
        }
        //首先移除之前添加的点
        // viewer.entities.removeAll();
        this.viewer.entities.removeById('opennessPoint')

        //在位置添加对应点
        this.viewer.entities.add(
            new Cesium.Entity({
                id: 'opennessPoint',
                point: new Cesium.PointGraphics({
                    color: new Cesium.Color(1, 0, 0),
                    pixelSize: 6,
                    outlineColor: new Cesium.Color(0, 1, 1)
                }),
                position: Cesium.Cartesian3.fromDegrees(this.opennessData.X, this.opennessData.Y,  this.opennessData.Z)
            })
        )
    },
    // 开敞度分析结果清除
    opennessClear(opennessData) {
        // viewer.entities.removeAll();
        this.viewer.entities.removeById('opennessPoint')

        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].destroy() //释放对象
        this.opennessViewDomeArray.pop();
        // if (this.opennessViewDomeArray.length == 0) {
        //     return
        // } else {
        //     let v = this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1]
        //     this.opennessData.X = v.viewPosition[0]
        //     this.opennessData.Y = v.viewPosition[1]
        //     this.opennessData.Z = v.viewPosition[2]
        //     this.opennessAddpoint()
        // }
    },
    // 开敞度分析清除按键
    clearOpennessAnalysis(opennessData) {
        if (this.opennessViewDomeArray.length == 0) {
            return
        }
        this.opennessClear(this.opennessData)
        this.opennessHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK)
    },
    // 开敞度各值改变
    opennessAttributeChange(opennessData) {
        const that = this;
        that.opennessData = opennessData;
        this.opennessData = opennessData;
        if (!this.opennessData.X || !this.opennessData.Y || !this.opennessData.Z || !this.opennessViewDomeArray.length || !this.opennessData.observationRadius) {
            // 半径不能为0，角度可为0
            return
        }
        //可视部颜色
        const VisibleColor = Cesium.Color.fromCssColorString(this.opennessData.visualColor)
        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].visibleAreaColor = Cesium.Color.fromAlpha(VisibleColor, 0.5)
        //隐藏部颜色
        const HiddenColor = Cesium.Color.fromCssColorString(this.opennessData.hideColor)
        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].hiddenAreaColor = Cesium.Color.fromAlpha(HiddenColor, 0.5)
        // 坐标 半径 角度 改变
        if (that.opennessData.X && that.opennessData.Y && that.opennessData.Z) {
            that.opennessViewDomeArray[that.opennessViewDomeArray.length - 1].viewPosition = [that.opennessData.X, that.opennessData.Y, that.opennessData.Z];
        }
        that.opennessViewDomeArray[that.opennessViewDomeArray.length - 1].distance = Number(that.opennessData.observationRadius);
        that.opennessViewDomeArray[that.opennessViewDomeArray.length - 1].startAngle = Number(that.opennessData.startingAngle);
        that.opennessViewDomeArray[that.opennessViewDomeArray.length - 1].endAngle = Number(that.opennessData.endAngle);
        that.opennessAddpoint(this.opennessData)
    },
    // 开敞度显示类型改变
    opennessDisplayTypeChange(opennessData) {
        const index = this.opennessData.displayType
        switch (index) {
            case 0:
                this.opennessData.displayType = Cesium.ViewDomeType.VISIBLEDOME
                break
            case 1:
                this.opennessData.displayType = Cesium.ViewDomeType.HIDDENDOME
                break
            case 2:
                this.opennessData.displayType = Cesium.ViewDomeType.ALLDOME
                break
            default:
                break
        }
        if (this.opennessViewDomeArray.length == 0) {
            return
        }
        this.opennessViewDomeArray[this.opennessViewDomeArray.length - 1].domeType = this.opennessData.displayType
    },
    initViewDome(opennessData) {
        const VisibleColor = Cesium.Color.fromCssColorString(this.opennessData.visualColor)
        const HiddenColor = Cesium.Color.fromCssColorString(this.opennessData.hideColor)
        const viewDome = new Cesium.ViewDome(this.scene) //构造新的开敞度分析对象
        // viewDome.viewPosition = [longitude, latitude, height]; //视点位置，这里用的是CBD示例的中心位置
        viewDome.distance = Number(this.opennessData.observationRadius) //可视距离
        viewDome.domeType = this.opennessData.displayType //开敞度类型,分为可视部分、不可视部分, 全部显示
        viewDome.visibleAreaColor = Cesium.Color.fromAlpha(VisibleColor, 0.5) //可视部颜色
        viewDome.hiddenAreaColor = Cesium.Color.fromAlpha(HiddenColor, 0.5) //隐藏部分颜色
        viewDome.startAngle = Number(this.opennessData.startingAngle) //起始角度
        viewDome.endAngle = Number(this.opennessData.endAngle) //终止角度
        viewDome.isClosed = this.opennessData.checked //封口
        // viewDome.build(); //执行开敞度分析
        this.opennessViewDomeArray.push(viewDome)
    },
    // 开敞度分析
    startOpennessAnalysis(opennessData) {
        // this.mousestyle(); //鼠标样式
        this.initViewDome(this.opennessData)
        this.opennessHandler = new Cesium.ScreenSpaceEventHandler(this.scene.canvas)
        this.opennessHandler.setInputAction((e) => {
            this.viewer.enableCursorStyle = true
            // $("body").removeClass("drawCur");
            //获取点击位置笛卡尔坐标
            const position = this.scene.pickPosition(e.position)
            //将笛卡尔坐标转化为经纬度坐标
            const positions = []
            const cartographic = Cesium.Cartographic.fromCartesian(position)
            this.opennessData.X = Cesium.Math.toDegrees(cartographic.longitude)
            this.opennessData.Y = Cesium.Math.toDegrees(cartographic.latitude)
            this.opennessData.Z = cartographic.height
            if (this.opennessData.Z < 0) {
                this.opennessData.Z = 0
            }
            if (positions.indexOf(this.opennessData.X) == -1 && positions.indexOf(this.opennessData.Y) == -1) {
                positions.push(this.opennessData.X)
                positions.push(this.opennessData.Y)
                positions.push(this.opennessData.Z)
            }
            this.updateViewDome(positions, this.opennessData)
            this.opennessHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK)
        }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
    }
}
