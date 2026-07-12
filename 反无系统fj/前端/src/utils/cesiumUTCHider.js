// cesiumUTCHider.js
export default {
  // 隐藏 UTC 时间显示
  hideUTC(viewer) {
    if (!viewer || !viewer.animation) {
      console.warn('Viewer 或 Animation 不存在');
      return;
    }

    // 等待 DOM 渲染完成
    setTimeout(() => {
      this._hideUTCElements(viewer);
      
      // 监听时钟变化，防止动态重新显示
      if (viewer.clock) {
        viewer.clock.onTick.addEventListener(() => {
          this._hideUTCElements(viewer);
        });
      }
    }, 100);
  },

  // 执行隐藏操作
  _hideUTCElements(viewer) {
    const animationContainer = viewer.animation.container;
    if (!animationContainer) return;

    // 1. 隐藏 SVG 文本元素
    const svgTexts = animationContainer.querySelectorAll('.cesium-animation-svgText');
    svgTexts.forEach(text => {
      text.style.display = 'none';
    });

    // 2. 隐藏 UTC 标签
    const utcLabels = animationContainer.querySelectorAll('.cesium-animation-utcLabel');
    utcLabels.forEach(label => {
      label.style.display = 'none';
    });

    // 3. 隐藏 timeLabel
    const timeLabels = animationContainer.querySelectorAll('.cesium-animation-timeLabel');
    timeLabels.forEach(label => {
      label.style.display = 'none';
    });

    // 4. 检查 tspan 中的 UTC 文本并隐藏
    const tspans = animationContainer.querySelectorAll('tspan');
    tspans.forEach(tspan => {
      if (tspan.textContent && tspan.textContent.includes('UTC')) {
        tspan.style.display = 'none';
        // 或者清空内容
        // tspan.textContent = '';
      }
    });
  },

  // 显示本地时间（可选）
  showLocalTime(viewer, containerId = 'localTimeDisplay') {
    if (!viewer || !viewer.animation) return;

    setTimeout(() => {
      const animationContainer = viewer.animation.container;
      
      // 检查是否已存在
      if (animationContainer.querySelector(`#${containerId}`)) {
        return;
      }

      const localTimeEl = document.createElement('div');
      localTimeEl.id = containerId;
      localTimeEl.style.cssText = `
        position: absolute;
        bottom: 30px;
        left: 50%;
        transform: translateX(-50%);
        color: #fff;
        font-size: 12px;
        font-weight: bold;
        text-align: center;
        font-family: 'Microsoft YaHei', sans-serif;
      `;

      animationContainer.appendChild(localTimeEl);

      // 更新时间
      const updateTime = () => {
        const currentTime = viewer.clock.currentTime;
        const isoString = Cesium.JulianDate.toIso8601(currentTime);
        const localDate = new Date(isoString);
        
        // 格式化为本地时间
        const timeStr = localDate.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        });
        
        localTimeEl.textContent = timeStr.replace(/\//g, '-');
      };

      viewer.clock.onTick.addEventListener(updateTime);
      updateTime();
    }, 200);
  },

  // 销毁
  destroy(viewer) {
    if (viewer && viewer.clock) {
      viewer.clock.onTick.removeAllListeners();
    }
  }
}
