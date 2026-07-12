// import $ from 'jquery'
import { ElMessage } from "element-plus";

export default {
  handlerDis: {},
  handlerArea: {},
  handlerHeight: {},
  active: false,
  /**
     * 创建测量控件
     */
  init: function(viewer) {
    // 注册测距、测高、测面积控件
    this.measureDistance(viewer)
    this.measureHeight(viewer)
    this.measureArea(viewer)
  },
  /**
     * 测距离
     * @param {Object} Cesium
     * @param {Object} viewer
     */
  measureDistance: function(viewer) {
    this.handlerDis = new Cesium.MeasureHandler(viewer, Cesium.MeasureMode.Distance, 0)
    this.handlerDis.measureEvt.addEventListener((result) => {
      if(Number(result.distance) == 0) {
        this.deActiveAllMeasure3DControl();
        ElMessage.warning("至少选择两个且不同的点");
        return
      }
      const distance = result.distance > 1000 ? (result.distance / 1000).toFixed(2) + 'km' : Number(result.distance).toFixed(2) + 'm'
      this.handlerDis.disLabel.text = '距离:' + distance + `\n右键结束测量`
    })
    this.handlerDis.activeEvt.addEventListener((isActive) => {
      if (isActive === true) {
        viewer.enableCursorStyle = false
        viewer._element.style.cursor = ''
        $('body').removeClass('measureCur').addClass('measureCur')
        
      } else {
        viewer.enableCursorStyle = true
        $('body').removeClass('measureCur')
        setTimeout(() => {
          this.handlerDis.disLabel.text = this.handlerDis.disLabel.text.replaceAll("\n右键结束测量","")
        }, 10);
        Map3D.popupClose()
      }
    })
  },
  /**
     * 测高度
     * @param {Object} Cesium
     * @param {Object} viewer
     */
  measureHeight: function(viewer) {
    this.handlerHeight = new Cesium.MeasureHandler(viewer, Cesium.MeasureMode.DVH)
    this.handlerHeight.measureEvt.addEventListener((result) => {
      const distance = result.distance > 1000 ? (result.distance / 1000).toFixed(2) + 'km' : Number(result.distance).toFixed(2) + 'm'
      const vHeight = result.verticalHeight > 1000 ? (result.verticalHeight / 1000).toFixed(2) + 'km' : Number(result.verticalHeight).toFixed(2) + 'm'
      const hDistance = result.horizontalDistance > 1000 ? (result.horizontalDistance / 1000).toFixed(2) + 'km' : Number(result.horizontalDistance).toFixed(2) + 'm'
      this.handlerHeight.disLabel.text = '空间距离:' + distance
      this.handlerHeight.vLabel.text = '垂直高度:' + vHeight
      this.handlerHeight.hLabel.text = '水平距离:' + hDistance
    })
    this.handlerHeight.activeEvt.addEventListener((isActive) => {
      if (isActive == true) {
        viewer.enableCursorStyle = false
        viewer._element.style.cursor = ''
        $('body').removeClass('measureCur').addClass('measureCur')
      } else {
        viewer.enableCursorStyle = true
        $('body').removeClass('measureCur')
        this.active = false
        Map3D.popupClose()
      }
    })
  },
  // /**
  //    * 测面积
  //    * @param {Object} Cesium
  //    * @param {Object} viewer
  //    */
  // measureArea: function(viewer) {
  //   this.handlerArea = new Cesium.MeasureHandler(viewer, Cesium.MeasureMode.Area, 2)
  //   let that=this;
  //   this.handlerArea.measureEvt.addEventListener((result) => {
  //     const area = result.area > 1000000 ? (result.area / 1000000).toFixed(2) + 'km²' : Number(result.area).toFixed(2) + '㎡'
  //     this.handlerArea.areaLabel.text = '面积:' + area + `\n右键结束测量`
      
  //   })
  //   this.handlerArea.activeEvt.addEventListener(function(isActive) {
  //     if (isActive == true) {
  //       viewer.enableCursorStyle = false
  //       viewer._element.style.cursor = ''
  //       $('body').removeClass('measureCur').addClass('measureCur')
  //     } else {
  //       viewer.enableCursorStyle = true
  //       Map3D.popupClose()
  //       $('body').removeClass('measureCur')
  //       setTimeout(() => {
  //         that.handlerArea.areaLabel.text = that.handlerArea.areaLabel.text.replaceAll("\n右键结束测量","")
  //       }, 10);
  //       if(that.handlerArea.polylineCoord.length<=2) {
  //         that.deActiveAllMeasure3DControl();
  //         ElMessage.warning("至少选择三个且不同的点");
  //       }
        
        
  //     }
  //   })
  // },
  /**
     * 测面积
     * @param {Object} Cesium
     * @param {Object} viewer
     */
  measureArea: function(viewer) {
    this.handlerArea = new Cesium.MeasureHandler(viewer, Cesium.MeasureMode.Area, 2)
    this.handlerArea.measureEvt.addEventListener(result => {
      const area = result.area > 1000000 ? (result.area / 1000000).toFixed(2) + 'km²' : Number(result.area).toFixed(2) + '㎡'
      this.handlerArea.areaLabel.text = '面积:' + area + `\n右键结束测量`
    })
    this.handlerArea.activeEvt.addEventListener(isActive=> {
      if (isActive == true) {
        viewer.enableCursorStyle = false
        viewer._element.style.cursor = ''
        $('body').removeClass('measureCur').addClass('measureCur')
      } else {
        Map3D.popupClose()
        viewer.enableCursorStyle = true
        $('body').removeClass('measureCur')
        setTimeout(() => {
          this.handlerArea.areaLabel.text = this.handlerArea.areaLabel.text.replaceAll("\n右键结束测量","")
        }, 10);
        if(this.handlerArea.polylineCoord.length<=2) {
          // this.deActiveAllMeasure3DControl();
          ElMessage.warning("至少选择三个且不同的点");
        }
      }
    })
  },
  
  /**
     * 激活测量控件
     * @param {Object} type：控件类型（DIS--测距；AREA--面积；HEIGHT--高度）
     */
  activateMeasure3DControl: function(type) {
    this.deActiveAllMeasure3DControl()
    switch (type) {
      case 'DIS':
        this.handlerDis && this.handlerDis.activate()
        this.active = true
        break
      case 'AREA':
        this.handlerArea && this.handlerArea.activate()
        this.active = true
        break
      case 'HEIGHT':
        this.handlerHeight && this.handlerHeight.activate()
        this.active = true
        break
      default:
        console.log("未匹配到");
    }
  },
  /**
     * 销毁测量控件,并清除所有要素
     */
  deActiveAllMeasure3DControl: function() {
    this.active = false
    this.handlerDis && this.handlerDis.deactivate && this.handlerDis.deactivate()
    this.handlerArea && this.handlerArea.deactivate && this.handlerArea.deactivate()
    this.handlerHeight && this.handlerHeight.deactivate && this.handlerHeight.deactivate()
    this.handlerDis && this.handlerDis.clear && this.handlerDis.clear()
    this.handlerArea && this.handlerArea.clear && this.handlerArea.clear()
    this.handlerHeight && this.handlerHeight.clear && this.handlerHeight.clear()
  },
  deActiveBtNtClear: function() {
    this.active = false
    this.handlerDis && this.handlerDis.deactivate()
    this.handlerArea && this.handlerArea.deactivate()
    this.handlerHeight && this.handlerHeight.deactivate()
  }
}
