import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  type ModelEntry, type ModelStats, type ModelParameter,
  ModelCategory, type ModelStatus,
} from '@/types'

/** 类别元数据 */
export const CATEGORY_META: Record<string, { label: string; icon: string }> = {
  [ModelCategory.SignalProcessing]:    { label: '信号处理与预处理', icon: '🔊' },
  [ModelCategory.DetectionRecognition]:{ label: '检测与识别',     icon: '🔍' },
  [ModelCategory.FusionAssociation]:   { label: '融合与关联',     icon: '🔗' },
  [ModelCategory.Classification]:      { label: '分类与识别',     icon: '🏷️' },
  [ModelCategory.IntentThreat]:        { label: '意图与威胁评估', icon: '⚠️' },
  [ModelCategory.PathPlanning]:        { label: '路径规划',       icon: '🗺️' },
  [ModelCategory.TaskScheduling]:      { label: '任务分配与调度', icon: '📋' },
  [ModelCategory.Simulation]:          { label: '仿真与验证',     icon: '🧪' },
}

/** 状态标签 */
const STATUS_LABELS: Record<string, string> = {
  deployed: '已部署', testing: '测试中', development: '开发中', deprecated: '已废弃',
}

// ==================== 所有模型数据 ====================
const ALL_MODELS: ModelEntry[] = [
  // ===== 1. 信号处理与预处理 =====
  {
    model_id: 'sig-kalman', name: '卡尔曼滤波', name_en: 'Kalman Filter',
    category: ModelCategory.SignalProcessing, kind: 'algorithm', status: 'deployed', version: 'v2.1.0',
    description: '传感器级去噪滤波器，消除设备系统误差与环境噪声，提供状态最优估计',
    parameters: [
      { key: 'process_noise', label: '过程噪声Q', type: 'number', default: 0.01, value: 0.01, min: 0.001, max: 1.0, step: 0.001, description: '过程噪声协方差' },
      { key: 'measure_noise', label: '测量噪声R', type: 'number', default: 0.1, value: 0.1, min: 0.001, max: 1.0, step: 0.001, description: '测量噪声协方差' },
      { key: 'dim_state', label: '状态维度', type: 'number', default: 6, value: 6, min: 2, max: 12, step: 1, description: '状态向量维度（位置+速度）' },
    ],
    metrics: { accuracy: 0.92, latency_ms: 0.5 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '原始传感器测量值', output_desc: '滤波后的状态估计',
    references: ['R.E. Kalman, 1960'],
  },
  {
    model_id: 'sig-wavelet', name: '小波变换去噪', name_en: 'Wavelet Denoising',
    category: ModelCategory.SignalProcessing, kind: 'algorithm', status: 'deployed', version: 'v1.3.0',
    description: '对非平稳信号进行多尺度分解与重构，去除高频噪声分量',
    parameters: [
      { key: 'wavelet_type', label: '小波基', type: 'select', default: 'db4', value: 'db4', options: [
        { label: 'Daubechies 4', value: 'db4' }, { label: 'Daubechies 6', value: 'db6' },
        { label: 'Symlet 8', value: 'sym8' }, { label: 'Coiflet 5', value: 'coif5' },
      ]},
      { key: 'level', label: '分解层数', type: 'number', default: 4, value: 4, min: 1, max: 8, step: 1 },
      { key: 'threshold_mode', label: '阈值模式', type: 'select', default: 'soft', value: 'soft', options: [
        { label: '软阈值', value: 'soft' }, { label: '硬阈值', value: 'hard' },
      ]},
    ],
    metrics: { accuracy: 0.88, latency_ms: 2.1 },
    dependencies: [], applicable_devices: ['rf', 'acoustic'],
    input_desc: '频谱/声纹信号', output_desc: '去噪信号',
    references: ['Mallat S., 1989'],
  },
  {
    model_id: 'sig-interp', name: '高斯过程回归插值', name_en: 'GP Regression Interpolation',
    category: ModelCategory.SignalProcessing, kind: 'algorithm', status: 'testing', version: 'v0.9.0',
    description: '填补短时信号中断造成的缺失数据，基于高斯过程对缺失采样点进行预测',
    parameters: [
      { key: 'kernel', label: '核函数', type: 'select', default: 'rbf', value: 'rbf', options: [
        { label: 'RBF', value: 'rbf' }, { label: 'Matern32', value: 'matern32' }, { label: 'Matern52', value: 'matern52' },
      ]},
      { key: 'length_scale', label: '长度尺度', type: 'number', default: 1.0, value: 1.0, min: 0.1, max: 10, step: 0.1 },
    ],
    metrics: { accuracy: 0.85 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '含缺失的时序数据', output_desc: '补全后的连续时序',
    references: ['Rasmussen & Williams, 2006'],
  },
  {
    model_id: 'sig-sync', name: 'PTP时间同步', name_en: 'Precision Time Protocol',
    category: ModelCategory.SignalProcessing, kind: 'algorithm', status: 'deployed', version: 'v1.0.0',
    description: 'IEEE 1588 PTP 高精度时钟同步，统一多传感器时间基准到GPS时钟',
    parameters: [
      { key: 'sync_interval', label: '同步间隔(s)', type: 'number', default: 1, value: 1, min: 0.1, max: 60, step: 0.1 },
      { key: 'max_offset_ns', label: '最大偏差(ns)', type: 'number', default: 100, value: 100, min: 10, max: 10000, step: 10 },
    ],
    metrics: { accuracy: 0.999, latency_ms: 0.05 },
    dependencies: [], applicable_devices: ['radar', 'rf', 'eoir'],
    input_desc: '各传感器时间戳', output_desc: '统一GPS时间基准的时间戳',
    references: ['IEEE 1588-2019'],
  },

  // ===== 2. 检测与识别 =====
  {
    model_id: 'det-yolov8', name: 'YOLOv8 目标检测', name_en: 'YOLOv8 Object Detection',
    category: ModelCategory.DetectionRecognition, kind: 'model', status: 'deployed', version: 'v8.2.0',
    description: '基于YOLOv8的EO/IR图像实时无人机检测，支持多尺度目标识别',
    parameters: [
      { key: 'conf_threshold', label: '置信度阈值', type: 'number', default: 0.5, value: 0.5, min: 0.1, max: 0.95, step: 0.05 },
      { key: 'iou_threshold', label: 'IoU NMS阈值', type: 'number', default: 0.45, value: 0.45, min: 0.1, max: 0.9, step: 0.05 },
      { key: 'img_size', label: '输入尺寸', type: 'select', default: '640', value: '640', options: [
        { label: '320×320', value: '320' }, { label: '640×640', value: '640' }, { label: '1280×1280', value: '1280' },
      ]},
      { key: 'model_variant', label: '模型变体', type: 'select', default: 'nano', value: 'nano', options: [
        { label: 'Nano', value: 'nano' }, { label: 'Small', value: 'small' }, { label: 'Medium', value: 'medium' },
      ]},
    ],
    metrics: { accuracy: 0.94, precision: 0.92, recall: 0.91, latency_ms: 12, f1_score: 0.915 },
    dependencies: [], applicable_devices: ['eoir'],
    input_desc: '可见光/红外图像帧', output_desc: '检测框 (x,y,w,h,class,conf)',
    references: ['Ultralytics YOLOv8, 2023'],
  },
  {
    model_id: 'det-rtdetr', name: 'RT-DETR 检测器', name_en: 'RT-DETR Detector',
    category: ModelCategory.DetectionRecognition, kind: 'model', status: 'testing', version: 'v1.0.0',
    description: '基于Transformer的实时目标检测，RT-DETR替代NMS后处理，端到端检测',
    parameters: [
      { key: 'conf_threshold', label: '置信度阈值', type: 'number', default: 0.5, value: 0.5, min: 0.1, max: 0.95, step: 0.05 },
      { key: 'backbone', label: '骨干网络', type: 'select', default: 'resnet50', value: 'resnet50', options: [
        { label: 'ResNet-50', value: 'resnet50' }, { label: 'HGNetv2', value: 'hgnetv2' },
      ]},
    ],
    metrics: { accuracy: 0.95, latency_ms: 15 },
    dependencies: [], applicable_devices: ['eoir'],
    input_desc: '图像帧', output_desc: '检测框+类别',
    references: ['Zhao et al., 2023'],
  },
  {
    model_id: 'det-rf-fp', name: 'RF指纹识别', name_en: 'RF Fingerprint Recognition',
    category: ModelCategory.DetectionRecognition, kind: 'model', status: 'deployed', version: 'v2.0.0',
    description: '基于SDR频谱数据的无人机射频指纹特征提取与型号识别',
    parameters: [
      { key: 'fft_size', label: 'FFT点数', type: 'number', default: 4096, value: 4096, min: 512, max: 16384, step: 512 },
      { key: 'freq_range', label: '频率范围(GHz)', type: 'string', default: '2.4-5.8', value: '2.4-5.8' },
      { key: 'db_threshold', label: '信号检测阈(dB)', type: 'number', default: -80, value: -80, min: -120, max: -30, step: 1 },
    ],
    metrics: { accuracy: 0.89, recall: 0.87, precision: 0.91, latency_ms: 8 },
    dependencies: [], applicable_devices: ['rf'],
    input_desc: 'I/Q采样数据', output_desc: '型号+频点+带宽+调制方式',
    references: ['Soltani et al., 2020'],
  },
  {
    model_id: 'det-acoustic-cnn', name: '声学特征CNN分类', name_en: 'Acoustic CNN Classifier',
    category: ModelCategory.DetectionRecognition, kind: 'model', status: 'development', version: 'v0.5.0',
    description: '基于MFCC特征+CNN的无人机声学信号分类，识别旋翼/固定翼/直升机',
    parameters: [
      { key: 'n_mfcc', label: 'MFCC系数', type: 'number', default: 13, value: 13, min: 8, max: 40, step: 1 },
      { key: 'win_ms', label: '窗口(ms)', type: 'number', default: 25, value: 25, min: 10, max: 100, step: 5 },
    ],
    metrics: { accuracy: 0.82, latency_ms: 25 },
    dependencies: [], applicable_devices: ['acoustic'],
    input_desc: '音频流(16kHz)', output_desc: '无人机类型分类概率',
    references: ['Bernardini et al., 2017'],
  },

  // ===== 3. 融合与关联 =====
  {
    model_id: 'fus-gnn', name: '全局最近邻关联', name_en: 'Global Nearest Neighbor',
    category: ModelCategory.FusionAssociation, kind: 'algorithm', status: 'deployed', version: 'v3.0.0',
    description: '基于Mahalanobis距离的门限关联，稀疏场景首选快速关联算法',
    parameters: [
      { key: 'gate_threshold', label: '关联门限', type: 'number', default: 3.0, value: 3.0, min: 1.0, max: 10.0, step: 0.5, description: 'Mahalanobis距离门限(σ)' },
      { key: 'max_age', label: '航迹最大寿命', type: 'number', default: 30, value: 30, min: 5, max: 120, step: 1, description: '未关联帧数超过后航迹终结' },
    ],
    metrics: { accuracy: 0.93, latency_ms: 0.3 },
    dependencies: ['sig-kalman'], applicable_devices: ['radar'],
    input_desc: '点迹列表', output_desc: '关联后的航迹',
    references: ['Blackman & Popoli, 1999'],
  },
  {
    model_id: 'fus-jpda', name: '联合概率数据关联', name_en: 'JPDA',
    category: ModelCategory.FusionAssociation, kind: 'algorithm', status: 'deployed', version: 'v2.2.0',
    description: '密集多目标场景的关联算法，计算每个量测-航迹的联合关联概率',
    parameters: [
      { key: 'gate_threshold', label: '关联门限', type: 'number', default: 3.0, value: 3.0, min: 1.0, max: 10.0, step: 0.5 },
      { key: 'pd', label: '检测概率Pd', type: 'number', default: 0.9, value: 0.9, min: 0.5, max: 0.99, step: 0.01 },
      { key: 'clutter_density', label: '杂波密度', type: 'number', default: 1e-6, value: 1e-6, min: 1e-9, max: 1e-3, step: 1e-7 },
    ],
    metrics: { accuracy: 0.95, latency_ms: 2.5 },
    dependencies: ['sig-kalman'], applicable_devices: ['radar'],
    input_desc: '多目标点迹', output_desc: '多对多关联概率矩阵',
    references: ['Bar-Shalom & Fortmann, 1988'],
  },
  {
    model_id: 'fus-mht', name: '多假设跟踪', name_en: 'Multiple Hypothesis Tracking',
    category: ModelCategory.FusionAssociation, kind: 'algorithm', status: 'testing', version: 'v1.5.0',
    description: '延迟决策的多假设跟踪，回溯多帧消除关联歧义，适合高杂波场景',
    parameters: [
      { key: 'max_hypotheses', label: '最大假设数', type: 'number', default: 200, value: 200, min: 10, max: 1000, step: 10 },
      { key: 'n_scan_back', label: '回溯帧数', type: 'number', default: 5, value: 5, min: 2, max: 15, step: 1 },
    ],
    metrics: { accuracy: 0.97, latency_ms: 15 },
    dependencies: ['fus-jpda'], applicable_devices: ['radar'],
    input_desc: '时序点迹序列', output_desc: '全局最优航迹集合',
    references: ['Reid, 1979'],
  },
  {
    model_id: 'fus-imm', name: '交互多模型滤波器', name_en: 'IMM Filter',
    category: ModelCategory.FusionAssociation, kind: 'algorithm', status: 'deployed', version: 'v1.8.0',
    description: '多模态切换跟踪滤波器，在匀速/机动/悬停模型间自适应切换',
    parameters: [
      { key: 'models', label: '运动模型', type: 'select', default: 'CV_CA_CT', value: 'CV_CA_CT', options: [
        { label: 'CV+CA+CT', value: 'CV_CA_CT' }, { label: 'CV+CA', value: 'CV_CA' }, { label: 'CV+CT', value: 'CV_CT' },
      ]},
      { key: 'transition_matrix', label: '转移概率矩阵', type: 'string', default: '[[0.95,0.025,0.025],[0.025,0.95,0.025],[0.025,0.025,0.95]]', value: '[[0.95,0.025,0.025],[0.025,0.95,0.025],[0.025,0.025,0.95]]' },
    ],
    metrics: { accuracy: 0.96, latency_ms: 1.2 },
    dependencies: ['sig-kalman'], applicable_devices: ['radar'],
    input_desc: '量测序列', output_desc: '融合状态估计(位置+速度+模型概率)',
    references: ['Blom & Bar-Shalom, 1988'],
  },

  // ===== 4. 分类与识别 =====
  {
    model_id: 'cls-lightgbm', name: 'LightGBM 行为分类', name_en: 'LightGBM Behavior Classifier',
    category: ModelCategory.Classification, kind: 'model', status: 'deployed', version: 'v2.3.0',
    description: '梯度提升决策树行为分类器，低延迟、高可解释性，对航迹特征进行分类',
    parameters: [
      { key: 'n_estimators', label: '树数量', type: 'number', default: 200, value: 200, min: 50, max: 500, step: 10 },
      { key: 'max_depth', label: '最大深度', type: 'number', default: 8, value: 8, min: 3, max: 16, step: 1 },
      { key: 'learning_rate', label: '学习率', type: 'number', default: 0.05, value: 0.05, min: 0.01, max: 0.3, step: 0.01 },
    ],
    metrics: { accuracy: 0.91, latency_ms: 0.8, f1_score: 0.90 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '航迹特征向量', output_desc: '行为类别概率(侦察/攻击/诱饵/监视/运输)',
    references: ['Ke et al., NIPS 2017'],
  },
  {
    model_id: 'cls-lstm', name: 'LSTM 时序分类', name_en: 'LSTM Temporal Classifier',
    category: ModelCategory.Classification, kind: 'model', status: 'deployed', version: 'v1.6.0',
    description: '双向LSTM+Attention对无人机完整航迹序列进行行为模式分类',
    parameters: [
      { key: 'hidden_size', label: '隐层维度', type: 'number', default: 128, value: 128, min: 32, max: 512, step: 32 },
      { key: 'num_layers', label: 'LSTM层数', type: 'number', default: 2, value: 2, min: 1, max: 4, step: 1 },
      { key: 'seq_length', label: '输入序列长度', type: 'number', default: 60, value: 60, min: 10, max: 300, step: 10 },
      { key: 'bidirectional', label: '双向', type: 'boolean', default: true, value: true },
    ],
    metrics: { accuracy: 0.93, precision: 0.91, recall: 0.90, latency_ms: 45 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '60帧历史航迹序列', output_desc: '行为意图标签',
    references: ['Hochreiter & Schmidhuber, 1997'],
  },
  {
    model_id: 'cls-transformer', name: 'Transformer 分类器', name_en: 'Transformer Encoder Classifier',
    category: ModelCategory.Classification, kind: 'model', status: 'testing', version: 'v0.8.0',
    description: '自注意力机制对长程航迹依赖建模，捕捉复杂战术意图',
    parameters: [
      { key: 'd_model', label: '模型维度', type: 'number', default: 256, value: 256, min: 64, max: 768, step: 64 },
      { key: 'n_heads', label: '注意力头数', type: 'number', default: 8, value: 8, min: 2, max: 16, step: 2 },
      { key: 'n_layers', label: '编码层数', type: 'number', default: 4, value: 4, min: 1, max: 12, step: 1 },
    ],
    metrics: { accuracy: 0.94, latency_ms: 80 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '长航时行为序列(>100帧)', output_desc: '战术意图分类',
    references: ['Vaswani et al., 2017'],
  },
  {
    model_id: 'cls-rule', name: '规则引擎', name_en: 'Rule Engine (Drools/SRE)',
    category: ModelCategory.Classification, kind: 'rule_engine', status: 'deployed', version: 'v3.1.0',
    description: '基于前向推理规则引擎的快速态势判断，冷启动+兜底方案，确保可解释性',
    parameters: [
      { key: 'rule_set', label: '规则集版本', type: 'select', default: 'v3_baseline', value: 'v3_baseline', options: [
        { label: 'v3 基线规则', value: 'v3_baseline' }, { label: 'v3 扩展规则', value: 'v3_extended' },
      ]},
      { key: 'salience_mode', label: '优先级模式', type: 'select', default: 'threat_first', value: 'threat_first', options: [
        { label: '威胁优先', value: 'threat_first' }, { label: '距离优先', value: 'distance_first' },
      ]},
    ],
    metrics: { accuracy: 0.88, latency_ms: 0.1 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '态势事件', output_desc: '分类+建议操作',
    references: ['Drools 8.x / 自研SRE'],
  },

  // ===== 5. 意图与威胁评估 =====
  {
    model_id: 'threat-lstm-attn', name: 'LSTM+Attention 意图识别', name_en: 'LSTM-Attention Intent Recognizer',
    category: ModelCategory.IntentThreat, kind: 'model', status: 'deployed', version: 'v2.0.0',
    description: '结合加权Attention机制的LSTM意图识别模型，主推理引擎',
    parameters: [
      { key: 'hidden_size', label: '隐层维度', type: 'number', default: 128, value: 128, min: 32, max: 512, step: 32 },
      { key: 'attn_type', label: 'Attention类型', type: 'select', default: 'dot', value: 'dot', options: [
        { label: 'Dot-product', value: 'dot' }, { label: 'Additive', value: 'additive' }, { label: 'Multi-head', value: 'multihead' },
      ]},
    ],
    metrics: { accuracy: 0.93, latency_ms: 30 },
    dependencies: ['cls-lstm'], applicable_devices: ['radar', 'rf'],
    input_desc: '航迹+通信信号特征', output_desc: '意图概率分布',
    references: ['Luong et al., 2015'],
  },
  {
    model_id: 'threat-pomdp', name: 'POMDP 意图推断', name_en: 'POMDP Intent Inference',
    category: ModelCategory.IntentThreat, kind: 'model', status: 'testing', version: 'v0.6.0',
    description: '部分可观马尔可夫决策过程，处理高不确定性环境下的无人机意图推断',
    parameters: [
      { key: 'horizon', label: '规划视野', type: 'number', default: 10, value: 10, min: 3, max: 30, step: 1 },
      { key: 'discount', label: '折扣因子', type: 'number', default: 0.95, value: 0.95, min: 0.8, max: 0.99, step: 0.01 },
    ],
    metrics: { accuracy: 0.87, latency_ms: 150 },
    dependencies: [], applicable_devices: ['radar', 'rf'],
    input_desc: '部分观测量', output_desc: '隐含意图状态分布',
    references: ['Kaelbling et al., 1998'],
  },
  {
    model_id: 'threat-score', name: '加权威胁评分模型', name_en: 'Weighted Threat Scoring',
    category: ModelCategory.IntentThreat, kind: 'algorithm', status: 'deployed', version: 'v2.5.0',
    description: 'ThreatScore = w1·Intent + w2·Distance + w3·Speed + w4·Altitude + w5·Model + w6·Maneuver，综合评分后按红/黄/绿分级',
    parameters: [
      { key: 'w_intent', label: '意图权重w1', type: 'number', default: 0.30, value: 0.30, min: 0, max: 1, step: 0.05 },
      { key: 'w_distance', label: '距离权重w2', type: 'number', default: 0.25, value: 0.25, min: 0, max: 1, step: 0.05 },
      { key: 'w_speed', label: '速度权重w3', type: 'number', default: 0.15, value: 0.15, min: 0, max: 1, step: 0.05 },
      { key: 'w_altitude', label: '高度权重w4', type: 'number', default: 0.10, value: 0.10, min: 0, max: 1, step: 0.05 },
      { key: 'w_model', label: '机型权重w5', type: 'number', default: 0.10, value: 0.10, min: 0, max: 1, step: 0.05 },
      { key: 'w_maneuver', label: '机动权重w6', type: 'number', default: 0.10, value: 0.10, min: 0, max: 1, step: 0.05 },
      { key: 'red_threshold', label: '红色阈值', type: 'number', default: 0.75, value: 0.75, min: 0.5, max: 0.95, step: 0.01 },
      { key: 'yellow_threshold', label: '黄色阈值', type: 'number', default: 0.40, value: 0.40, min: 0.2, max: 0.7, step: 0.01 },
    ],
    metrics: { accuracy: 0.92, latency_ms: 1.5 },
    dependencies: ['cls-lightgbm', 'threat-lstm-attn'], applicable_devices: ['all'],
    input_desc: '意图+距离+速度+高度+机型+机动频率', output_desc: '0-1威胁分数+红/黄/绿等级',
    references: ['HSimC2 Design Doc §3.3.1'],
  },

  // ===== 6. 路径规划 =====
  {
    model_id: 'path-rrtstar', name: 'RRT* 路径规划', name_en: 'RRT* Path Planner',
    category: ModelCategory.PathPlanning, kind: 'algorithm', status: 'deployed', version: 'v1.7.0',
    description: '渐进最优的随机采样路径规划，带地形约束和禁飞区避障',
    parameters: [
      { key: 'step_size', label: '扩展步长(m)', type: 'number', default: 50, value: 50, min: 10, max: 200, step: 5 },
      { key: 'max_iter', label: '最大迭代', type: 'number', default: 5000, value: 5000, min: 500, max: 20000, step: 500 },
      { key: 'rewire_radius', label: '重连半径(m)', type: 'number', default: 200, value: 200, min: 50, max: 1000, step: 10 },
      { key: 'goal_bias', label: '目标偏向', type: 'number', default: 0.1, value: 0.1, min: 0.01, max: 0.5, step: 0.01 },
    ],
    metrics: { accuracy: 0.98, latency_ms: 8 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '起点/终点/禁飞区列表/地形高程', output_desc: '无碰撞最优路径',
    references: ['Karaman & Frazzoli, 2011'],
  },
  {
    model_id: 'path-hybrid-a', name: 'Hybrid A* 路径规划', name_en: 'Hybrid A* Planner',
    category: ModelCategory.PathPlanning, kind: 'algorithm', status: 'deployed', version: 'v1.4.0',
    description: '考虑运动学约束的启发式搜索，优先生成动力学可行的平滑轨迹',
    parameters: [
      { key: 'grid_resolution', label: '栅格分辨率(m)', type: 'number', default: 2, value: 2, min: 0.5, max: 10, step: 0.5 },
      { key: 'turning_radius', label: '最小转弯半径(m)', type: 'number', default: 30, value: 30, min: 5, max: 100, step: 5 },
      { key: 'heuristic_weight', label: '启发权重', type: 'number', default: 1.2, value: 1.2, min: 1.0, max: 2.0, step: 0.05 },
    ],
    metrics: { accuracy: 0.99, latency_ms: 5 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '起点/终点/地形栅格/运动约束', output_desc: '动力学可行路径',
    references: ['Dolgov et al., 2010'],
  },
  {
    model_id: 'path-dubins', name: 'Dubins曲线规划', name_en: 'Dubins Curve Planner',
    category: ModelCategory.PathPlanning, kind: 'algorithm', status: 'deployed', version: 'v1.2.0',
    description: '固定翼/高速拦截的最短路径，考虑最小转弯半径的圆弧-直线-圆弧',
    parameters: [
      { key: 'turning_radius', label: '转弯半径(m)', type: 'number', default: 50, value: 50, min: 10, max: 300, step: 10 },
      { key: 'curve_type', label: '曲线类型', type: 'select', default: 'RSR', value: 'RSR', options: [
        { label: 'RSR (右-直-右)', value: 'RSR' }, { label: 'LSL (左-直-左)', value: 'LSL' },
        { label: 'RSL', value: 'RSL' }, { label: 'LSR', value: 'LSR' },
      ]},
    ],
    metrics: { accuracy: 0.99, latency_ms: 0.3 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '起点姿态/终点姿态', output_desc: 'Dubins最短路径',
    references: ['Dubins, 1957'],
  },
  {
    model_id: 'path-vo', name: '速度障碍法冲突消解', name_en: 'Velocity Obstacle Resolution',
    category: ModelCategory.PathPlanning, kind: 'algorithm', status: 'testing', version: 'v1.1.0',
    description: '多机轨迹冲突消解算法，基于VO构造速度避碰锥，实时调整速度矢量',
    parameters: [
      { key: 'safety_radius', label: '安全半径(m)', type: 'number', default: 20, value: 20, min: 5, max: 100, step: 5 },
      { key: 'time_horizon', label: '时间视野(s)', type: 'number', default: 5, value: 5, min: 1, max: 30, step: 1 },
    ],
    metrics: { accuracy: 0.98, latency_ms: 1.0 },
    dependencies: ['path-rrtstar'], applicable_devices: ['all'],
    input_desc: '本机+邻居轨迹', output_desc: '避碰速度矢量',
    references: ['Fiorini & Shiller, 1998'],
  },

  // ===== 7. 任务分配与调度 =====
  {
    model_id: 'task-pso', name: 'PSO 粒子群任务分配', name_en: 'PSO Task Assignment',
    category: ModelCategory.TaskScheduling, kind: 'algorithm', status: 'deployed', version: 'v2.1.0',
    description: '群体智能优化算法，多拦截器-多目标的全局最优任务分配',
    parameters: [
      { key: 'swarm_size', label: '粒子群规模', type: 'number', default: 50, value: 50, min: 10, max: 200, step: 10 },
      { key: 'c1', label: '认知系数c1', type: 'number', default: 2.0, value: 2.0, min: 0.5, max: 3.0, step: 0.1 },
      { key: 'c2', label: '社会系数c2', type: 'number', default: 2.0, value: 2.0, min: 0.5, max: 3.0, step: 0.1 },
      { key: 'w_inertia', label: '惯性权重', type: 'number', default: 0.7, value: 0.7, min: 0.2, max: 1.2, step: 0.05 },
      { key: 'max_iter', label: '最大迭代', type: 'number', default: 300, value: 300, min: 50, max: 2000, step: 50 },
    ],
    metrics: { accuracy: 0.95, latency_ms: 150 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '拦截器列表+目标列表+约束矩阵', output_desc: '最优分配方案',
    references: ['Kennedy & Eberhart, 1995'],
  },
  {
    model_id: 'task-aco', name: 'ACO 蚁群任务分配', name_en: 'ACO Task Assignment',
    category: ModelCategory.TaskScheduling, kind: 'algorithm', status: 'testing', version: 'v1.0.0',
    description: '蚁群优化算法解决多机协同拦截任务分配，适合大规模蜂群对抗',
    parameters: [
      { key: 'n_ants', label: '蚂蚁数量', type: 'number', default: 100, value: 100, min: 20, max: 500, step: 10 },
      { key: 'alpha', label: '信息素因子α', type: 'number', default: 1.0, value: 1.0, min: 0.5, max: 3.0, step: 0.1 },
      { key: 'beta', label: '启发因子β', type: 'number', default: 2.0, value: 2.0, min: 0.5, max: 5.0, step: 0.1 },
      { key: 'evap_rate', label: '蒸发率', type: 'number', default: 0.1, value: 0.1, min: 0.01, max: 0.5, step: 0.01 },
    ],
    metrics: { accuracy: 0.93, latency_ms: 250 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '任务图+距离矩阵', output_desc: '近优分配方案',
    references: ['Dorigo et al., 1996'],
  },
  {
    model_id: 'task-milp', name: 'MILP 集中式分配', name_en: 'MILP Centralized Assignment',
    category: ModelCategory.TaskScheduling, kind: 'algorithm', status: 'deployed', version: 'v1.5.0',
    description: '混合整数线性规划求解器，开阔场景全局最优任务分配',
    parameters: [
      { key: 'time_limit', label: '求解时限(s)', type: 'number', default: 3.0, value: 3.0, min: 0.1, max: 10.0, step: 0.1 },
      { key: 'gap_tolerance', label: '最优间隙', type: 'number', default: 0.05, value: 0.05, min: 0.01, max: 0.2, step: 0.01 },
    ],
    metrics: { accuracy: 0.99, latency_ms: 800 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '拦截器-目标成本矩阵+约束', output_desc: '全局最优解',
    references: ['Gurobi/CPLEX Solver'],
  },
  {
    model_id: 'task-hungarian', name: '匈牙利算法', name_en: 'Hungarian Algorithm',
    category: ModelCategory.TaskScheduling, kind: 'algorithm', status: 'deployed', version: 'v2.0.0',
    description: '多项式时间复杂度的最优一对一匹配，适合等数量拦截器-目标场景',
    parameters: [
      { key: 'cost_mode', label: '成本模式', type: 'select', default: 'distance', value: 'distance', options: [
        { label: '最小距离', value: 'distance' }, { label: '最小时间', value: 'time' }, { label: '最小威胁', value: 'threat' },
      ]},
    ],
    metrics: { accuracy: 0.99, latency_ms: 5 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: 'N×N成本矩阵', output_desc: '一对一最优匹配',
    references: ['Kuhn, 1955'],
  },
  {
    model_id: 'task-cnp', name: '合同网协议', name_en: 'Contract Net Protocol',
    category: ModelCategory.TaskScheduling, kind: 'rule_engine', status: 'development', version: 'v0.4.0',
    description: '分布式协商协议，通信受限场景中各拦截节点自主投标分配任务',
    parameters: [
      { key: 'bid_timeout', label: '投标超时(ms)', type: 'number', default: 200, value: 200, min: 50, max: 1000, step: 50 },
      { key: 'min_bidders', label: '最少投标数', type: 'number', default: 2, value: 2, min: 1, max: 5, step: 1 },
    ],
    metrics: { accuracy: 0.90, latency_ms: 50 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '任务公告', output_desc: '协商分配结果',
    references: ['Smith, 1980'],
  },

  // ===== 8. 仿真与验证 =====
  {
    model_id: 'sim-montecarlo', name: 'Monte Carlo 仿真验证', name_en: 'Monte Carlo Simulation',
    category: ModelCategory.Simulation, kind: 'algorithm', status: 'deployed', version: 'v1.2.0',
    description: '大规模随机采样评估算法性能，生成准确率/召回率/延迟等统计指标',
    parameters: [
      { key: 'n_samples', label: '采样数', type: 'number', default: 10000, value: 10000, min: 100, max: 100000, step: 100 },
      { key: 'confidence', label: '置信水平', type: 'number', default: 0.95, value: 0.95, min: 0.8, max: 0.99, step: 0.01 },
    ],
    metrics: { accuracy: 0.99, latency_ms: -1 },
    dependencies: [], applicable_devices: ['all'],
    input_desc: '算法+测试数据集', output_desc: '性能统计+置信区间',
    references: ['Metropolis & Ulam, 1949'],
  },
  {
    model_id: 'sim-sitl', name: 'SITL 软件在环仿真', name_en: 'Software In The Loop',
    category: ModelCategory.Simulation, kind: 'model', status: 'deployed', version: 'v2.0.0',
    description: 'PX4/ArduPilot SITL仿真环境，验证规划算法在飞控层面的可行性',
    parameters: [
      { key: 'vehicle', label: '飞行器类型', type: 'select', default: 'quad', value: 'quad', options: [
        { label: '四旋翼', value: 'quad' }, { label: '固定翼', value: 'fixedwing' }, { label: '六旋翼', value: 'hexa' },
      ]},
      { key: 'world', label: '仿真场景', type: 'select', default: 'qinling', value: 'qinling', options: [
        { label: '秦岭山地', value: 'qinling' }, { label: '城市开阔', value: 'urban' }, { label: '电磁对抗', value: 'em_warfare' },
      ]},
      { key: 'speed_factor', label: '仿真速度', type: 'number', default: 1, value: 1, min: 1, max: 10, step: 1 },
    ],
    metrics: { accuracy: 0.97 },
    dependencies: ['path-rrtstar'], applicable_devices: ['all'],
    input_desc: '飞行计划', output_desc: '飞行日志+传感器模拟数据',
    references: ['PX4 SITL / ArduPilot SITL'],
  },
]

export const useModelStore = defineStore('model', () => {
  const models = ref<ModelEntry[]>(ALL_MODELS)
  const selectedModelId = ref<string | null>(null)
  const filterCategory = ref<string | null>(null)
  const filterStatus = ref<ModelStatus | null>(null)
  const searchText = ref('')

  // 计算属性
  const groupedByCategory = computed(() => {
    const groups: Record<string, ModelEntry[]> = {}
    for (const m of models.value) {
      if (!groups[m.category]) groups[m.category] = []
      groups[m.category].push(m)
    }
    return Object.entries(groups).map(([cat, items]) => ({
      key: cat, label: CATEGORY_META[cat]?.label || cat,
      icon: CATEGORY_META[cat]?.icon || '📦', items,
    }))
  })

  const selectedModel = computed(() =>
    models.value.find(m => m.model_id === selectedModelId.value) ?? null
  )

  const stats = computed((): ModelStats => {
    const by_category: Record<string, number> = {}
    const by_status: Record<string, number> = {}
    for (const m of models.value) {
      by_category[m.category] = (by_category[m.category] || 0) + 1
      by_status[m.status] = (by_status[m.status] || 0) + 1
    }
    return { total: models.value.length, by_category, by_status }
  })

  // 动作
  function selectModel(id: string | null) { selectedModelId.value = id }
  function setFilterCategory(cat: string | null) { filterCategory.value = cat }
  function setFilterStatus(s: ModelStatus | null) { filterStatus.value = s }
  function setSearchText(t: string) { searchText.value = t }

  function updateModelParam(modelId: string, paramKey: string, value: any) {
    const m = models.value.find(x => x.model_id === modelId)
    if (m) {
      const p = m.parameters.find(x => x.key === paramKey)
      if (p) p.value = value
    }
  }

  function getFilteredModels(): ModelEntry[] {
    let list = models.value
    if (filterCategory.value) list = list.filter(m => m.category === filterCategory.value)
    if (filterStatus.value) list = list.filter(m => m.status === filterStatus.value)
    if (searchText.value) {
      const q = searchText.value.toLowerCase()
      list = list.filter(m =>
        m.name.includes(q) || m.name_en.toLowerCase().includes(q) ||
        m.description.includes(q) || m.category.includes(q)
      )
    }
    return list
  }

  return {
    models, selectedModelId, filterCategory, filterStatus, searchText,
    groupedByCategory, selectedModel, stats,
    selectModel, setFilterCategory, setFilterStatus, setSearchText,
    updateModelParam, getFilteredModels,
    CATEGORY_META, STATUS_LABELS,
  }
})
