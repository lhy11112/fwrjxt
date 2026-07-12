use ndarray::Array1;
use ndarray::Array2;

/// 扩展卡尔曼滤波器 (EKF)
///
/// 用于目标状态估计：位置、速度、加速度
/// 状态向量: [x, y, z, vx, vy, vz]^T
#[derive(Debug, Clone)]
pub struct ExtendedKalmanFilter {
    /// 状态向量 (6维)
    pub state: Array1<f64>,
    /// 协方差矩阵 (6x6)
    pub covariance: Array2<f64>,
    /// 过程噪声矩阵
    pub process_noise: Array2<f64>,
    /// 测量噪声
    pub measurement_noise: Array2<f64>,
    /// 时间步长 (秒)
    pub dt: f64,
}

impl ExtendedKalmanFilter {
    pub fn new(dt: f64) -> Self {
        let state = Array1::zeros(6);
        let covariance = Array2::eye(6) * 100.0;
        let process_noise = Array2::eye(6) * 0.01;
        let measurement_noise = Array2::eye(3) * 1.0;

        Self {
            state,
            covariance,
            process_noise,
            measurement_noise,
            dt,
        }
    }

    /// 使用初始测量初始化状态
    pub fn initialize(&mut self, x: f64, y: f64, z: f64) {
        self.state[0] = x;
        self.state[1] = y;
        self.state[2] = z;
    }

    /// 预测步骤
    pub fn predict(&mut self) {
        let dt = self.dt;
        // 状态转移矩阵 F
        let mut f = Array2::eye(6);
        f[[0, 3]] = dt;
        f[[1, 4]] = dt;
        f[[2, 5]] = dt;

        // 先验估计: x = F * x
        self.state = f.dot(&self.state);

        // 先验协方差: P = F * P * F^T + Q
        let ft = f.t();
        self.covariance = f.dot(&self.covariance).dot(&ft) + &self.process_noise;
    }

    /// 更新步骤（使用雷达测量：距离、方位、俯仰）
    pub fn update(&mut self, range: f64, azimuth: f64, elevation: f64) {
        let x = self.state[0];
        let y = self.state[1];
        let z = self.state[2];

        // 预测测量
        let pred_range = (x * x + y * y + z * z).sqrt();
        let pred_azimuth = y.atan2(x);
        let pred_elevation = z.atan2((x * x + y * y).sqrt());

        // 测量残差
        let mut innovation = Array1::zeros(3);
        innovation[0] = range - pred_range;
        innovation[1] = azimuth - pred_azimuth;
        innovation[2] = elevation - pred_elevation;

        // 计算雅可比矩阵 H
        let mut h = Array2::zeros((3, 6));
        let r = pred_range.max(1e-6);
        let r_xy = (x * x + y * y).sqrt().max(1e-6);

        h[[0, 0]] = x / r;
        h[[0, 1]] = y / r;
        h[[0, 2]] = z / r;
        h[[1, 0]] = -y / (r_xy * r_xy);
        h[[1, 1]] = x / (r_xy * r_xy);
        h[[2, 0]] = -x * z / (r * r * r_xy);
        h[[2, 1]] = -y * z / (r * r * r_xy);
        h[[2, 2]] = r_xy / (r * r);

        // 卡尔曼增益: K = P * H^T * (H * P * H^T + R)^-1
        let ht = h.t();
        let s = h.dot(&self.covariance).dot(&ht) + &self.measurement_noise;
        let k = self.covariance.dot(&ht).dot(&self.inverse_3x3(&s));

        // 更新状态: x = x + K * innovation
        self.state = &self.state + k.dot(&innovation);

        // 更新协方差: P = (I - K*H) * P
        let kh = k.dot(&h);
        let i = Array2::eye(6);
        self.covariance = (&i - &kh).dot(&self.covariance);
    }

    /// 3x3 矩阵求逆
    fn inverse_3x3(&self, m: &Array2<f64>) -> Array2<f64> {
        let det = m[[0, 0]] * (m[[1, 1]] * m[[2, 2]] - m[[1, 2]] * m[[2, 1]])
            - m[[0, 1]] * (m[[1, 0]] * m[[2, 2]] - m[[1, 2]] * m[[2, 0]])
            + m[[0, 2]] * (m[[1, 0]] * m[[2, 1]] - m[[1, 1]] * m[[2, 0]]);

        if det.abs() < 1e-12 {
            return Array2::eye(3) * 1e6; // 返回大矩阵避免奇异
        }

        let inv_det = 1.0 / det;
        let mut inv = Array2::zeros((3, 3));

        inv[[0, 0]] = (m[[1, 1]] * m[[2, 2]] - m[[1, 2]] * m[[2, 1]]) * inv_det;
        inv[[0, 1]] = (m[[0, 2]] * m[[2, 1]] - m[[0, 1]] * m[[2, 2]]) * inv_det;
        inv[[0, 2]] = (m[[0, 1]] * m[[1, 2]] - m[[0, 2]] * m[[1, 1]]) * inv_det;
        inv[[1, 0]] = (m[[1, 2]] * m[[2, 0]] - m[[1, 0]] * m[[2, 2]]) * inv_det;
        inv[[1, 1]] = (m[[0, 0]] * m[[2, 2]] - m[[0, 2]] * m[[2, 0]]) * inv_det;
        inv[[1, 2]] = (m[[0, 2]] * m[[1, 0]] - m[[0, 0]] * m[[1, 2]]) * inv_det;
        inv[[2, 0]] = (m[[1, 0]] * m[[2, 1]] - m[[1, 1]] * m[[2, 0]]) * inv_det;
        inv[[2, 1]] = (m[[0, 1]] * m[[2, 0]] - m[[0, 0]] * m[[2, 1]]) * inv_det;
        inv[[2, 2]] = (m[[0, 0]] * m[[1, 1]] - m[[0, 1]] * m[[1, 0]]) * inv_det;

        inv
    }

    pub fn get_position(&self) -> (f64, f64, f64) {
        (self.state[0], self.state[1], self.state[2])
    }

    pub fn get_velocity(&self) -> (f64, f64, f64) {
        (self.state[3], self.state[4], self.state[5])
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_ekf_predict_update() {
        let mut ekf = ExtendedKalmanFilter::new(0.1);
        ekf.initialize(1000.0, 500.0, 100.0);
        ekf.predict();
        ekf.update(1100.0, 0.5, 0.15);

        let (x, y, z) = ekf.get_position();
        assert!(x > 0.0);
        assert!(y > 0.0);
        assert!(z > 0.0);
    }

    #[test]
    fn test_inverse_3x3() {
        let ekf = ExtendedKalmanFilter::new(0.1);
        let m = Array2::from_shape_vec((3, 3), vec![2.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 2.0]).unwrap();
        let inv = ekf.inverse_3x3(&m);
        let identity = m.dot(&inv);
        for i in 0..3 {
            for j in 0..3 {
                if i == j {
                    assert!((identity[[i, j]] - 1.0).abs() < 1e-6);
                } else {
                    assert!(identity[[i, j]].abs() < 1e-6);
                }
            }
        }
    }
}
