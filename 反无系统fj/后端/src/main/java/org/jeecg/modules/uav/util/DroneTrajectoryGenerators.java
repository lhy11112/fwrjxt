package org.jeecg.modules.uav.util;
import cn.hutool.core.date.DateUnit;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.geom.Point2D;
public class DroneTrajectoryGenerators {
    /**
     * 航点类，包含地理位置信息和停留时间
     */
    public static class Waypoint {
        private final double longitude;  // 经度
        private final double latitude;   // 纬度
        private final double altitude;   // 高度(米)
        private final long stayTime;     // 停留时间(秒)

        public Waypoint(double longitude, double latitude, double altitude) {
            this(longitude, latitude, altitude, 0);
        }

        public Waypoint(double longitude, double latitude, double altitude, long stayTime) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.altitude = altitude;
            this.stayTime = stayTime;
        }

        // Getters
        public double getLongitude() { return longitude; }
        public double getLatitude() { return latitude; }
        public double getAltitude() { return altitude; }
        public long getStayTime() { return stayTime; }
    }

    /**
     * 轨迹点类，包含完整的状态信息
     */
    public static class TrajectoryPoint {
        public double longitude;
        public double latitude;
        public double altitude;
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        public Date time;
        public double speed;       // 米/秒
        public double heading;     // 航向角(度，0-360)

        public TrajectoryPoint(double longitude, double latitude, double altitude,
                               Date time, double speed, double heading) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.altitude = altitude;
            this.time = time;
            this.speed = speed;
            this.heading = heading;
        }
    }

    /**
     * 无人机参数类，存储性能限制（基础性能，无全局速度）
     */
    public static class DroneParameters {
        private final double maxAcceleration;  // 最大加速度(米/秒²)
        private final double turnRadius;       // 最小转弯半径(米)

        public DroneParameters(double maxAcceleration, double turnRadius) {
            this.maxAcceleration = maxAcceleration;
            this.turnRadius = turnRadius;
        }

        // Getters
        public double getMaxAcceleration() { return maxAcceleration; }
        public double getTurnRadius() { return turnRadius; }
    }

    /**
     * 新增：多航段速度配置类（核心适配多途经点）
     * 航段数规则：起点→途1→途2→…→途N→终点 → 共 N+1 个航段
     * 速度列表需与航段一一对应：[航段1速度, 航段2速度, ..., 航段N+1速度]
     */
    public static class MultiSegmentSpeedConfig {
        private final List<Double> segmentMaxSpeeds; // 各航段最大速度(m/s)，按航段顺序排列

        /**
         * 构造方法：传入所有航段的速度列表
         * @param segmentMaxSpeeds 航段速度列表，长度=途经点数量+1
         */
        public MultiSegmentSpeedConfig(List<Double> segmentMaxSpeeds) {
            // 校验1：速度列表不能为空
            if (segmentMaxSpeeds == null || segmentMaxSpeeds.isEmpty()) {
                throw new IllegalArgumentException("航段速度列表不能为空");
            }
            // 校验2：所有速度必须大于0
            for (double speed : segmentMaxSpeeds) {
                if (speed <= 0) {
                    throw new IllegalArgumentException("所有航段的最大速度必须大于0，当前存在无效速度：" + speed);
                }
            }
            this.segmentMaxSpeeds = new ArrayList<>(segmentMaxSpeeds);
        }

        // 根据航段索引获取对应速度（索引从0开始）
        public double getSegmentSpeed(int segmentIndex) {
            if (segmentIndex < 0 || segmentIndex >= segmentMaxSpeeds.size()) {
                throw new IndexOutOfBoundsException("航段索引越界，当前航段数：" + segmentMaxSpeeds.size() + "，请求索引：" + segmentIndex);
            }
            return segmentMaxSpeeds.get(segmentIndex);
        }

        // 获取航段总数
        public int getSegmentCount() {
            return segmentMaxSpeeds.size();
        }
    }

    // 加加速度限制（米/秒³），控制加速度变化率
    private static final double MAX_JERK = 0.5;

    // 地球半径(米)，用于距离计算
    private static final double EARTH_RADIUS = 6371000;

    // ===== 优化新增常量 =====
    /**
     * GPS水平噪声标准差（米），模拟真实传感器误差。
     * 仅在调用 generateTrajectory(..., enableGpsNoise=true) 时生效，默认关闭。
     */
    private static final double GPS_NOISE_H = 0.8;
    /** GPS垂直噪声标准差（米），高度传感器精度略低 */
    private static final double GPS_NOISE_V = 0.4;
    /** 航向前瞻系数：前瞻距离 = 当前速度 × 该系数（秒），使航向领先于当前位置 */
    private static final double HEADING_LOOKAHEAD_SEC = 2.0;
    /** 随机数生成器（固定种子保证可复现，生产中可改为 new Random()） */
    private static final Random RANDOM = new Random(12345);

    /**
     * 核心方法：多途经点轨迹生成，逐航段自定义速度，精准计算各航段时间
     *
     * 【v2.0 优化说明】
     * ① 位置物理一致性：位置通过速度积分（∑v·Δt）推导，与速度完全一致，消除原版时间比例插值误差
     * ② 基于剩余距离的梯形速度规划：提前预判减速点，杜绝航段末尾速度不归零的问题
     * ③ 路径长度修正：使用含转弯弧度的实际路径总长（而非点对点直线距离）做速度规划基准
     * ④ 高度S型曲线改为距离比例驱动：高度变化与实际飞行进度同步，加减速时高度不再虚假线性
     * ⑤ 前瞻航向计算：无人机航向指向前方 lookahead 距离处，更贴近真实飞控行为
     * ⑥ GPS噪声模型（可选）：通过 enableGpsNoise 控制，默认关闭；开启后叠加高斯扰动复现传感器抖动
     *
     * @param startPoint    起点
     * @param waypoints     途经点列表（支持0个/1个/多个，0个则为起点→终点单航段）
     * @param endPoint      终点
     * @param startTime     轨迹开始时间
     * @param speedConfig   多航段速度配置（速度列表长度=途经点数量+1）
     * @param droneParams   无人机基础性能参数（加速度、转弯半径）
     * @param enableGpsNoise 是否叠加GPS噪声（true=模拟传感器抖动，false=输出纯净理论轨迹）
     * @return 完整平滑轨迹点列表
     */
    public static List<TrajectoryPoint> generateTrajectory(
            Waypoint startPoint, List<Waypoint> waypoints, Waypoint endPoint,
            Date startTime, MultiSegmentSpeedConfig speedConfig, DroneParameters droneParams,
            boolean enableGpsNoise) {

        // 初始化完整航点路径：起点 → 所有途经点 → 终点
        List<Waypoint> fullPath = new ArrayList<>();
        fullPath.add(startPoint);
        if (waypoints != null && !waypoints.isEmpty()) {
            fullPath.addAll(waypoints);
        }
        fullPath.add(endPoint);

        // 核心参数：航段总数 = 完整路径点数量 - 1 = 途经点数量 + 1
        int totalSegment = fullPath.size() - 1;
        // 校验：速度配置的航段数必须与实际航段数一致
        if (speedConfig.getSegmentCount() != totalSegment) {
            throw new IllegalArgumentException(
                    "速度配置航段数与实际航段数不匹配！" +
                            "实际航段数（起点→途N→终点）：" + totalSegment +
                            "，配置速度航段数：" + speedConfig.getSegmentCount() +
                            "（规则：速度列表长度=途经点数量+1）"
            );
        }

        // ---------------------- 第一步：预计算所有航段和停留的基础参数 ----------------------
        double[] segmentDistances = new double[totalSegment]; // 各航段距离（米）
        long[] segmentStayTimes = new long[totalSegment];     // 各航段起点的停留时间（秒，仅途经点有停留）
        long totalStayTime = 0;                                // 所有途经点总停留时间
        StringBuilder segmentInfo = new StringBuilder("===== 多航段基础参数汇总 =====\n");

        // 逐航段计算距离，逐途经点统计停留时间
        for (int i = 0; i < totalSegment; i++) {
            Waypoint from = fullPath.get(i);
            Waypoint to = fullPath.get(i + 1);
            // 计算当前航段的三维实际距离（含高度差），用于飞行时间计算
            // 若起终点高度不同（爬升/下降），三维距离 > 水平距离，时间更准确
            segmentDistances[i] = calculateDistance3D(from, to);
            // 停留时间规则：仅「途经点」有停留（即路径中除了起点和终点的点）
            boolean isWaypoint = i > 0 && i < fullPath.size() - 1;
            segmentStayTimes[i] = isWaypoint ? from.getStayTime() : 0;
            if (isWaypoint) {
                totalStayTime += segmentStayTimes[i];
            }
            // 拼接航段信息
            segmentInfo.append(String.format(
                    "航段%d：%s → %s | 距离：%.2f米 | 航段起点停留时间：%d秒 | 配置最大速度：%.1fm/s%n",
                    i+1,
                    getPointDesc(i, from),
                    getPointDesc(i+1, to),
                    segmentDistances[i],
                    segmentStayTimes[i],
                    speedConfig.getSegmentSpeed(i)
            ));
        }
        segmentInfo.append(String.format("所有途经点总停留时间：%d秒%n", totalStayTime));
        System.out.println(segmentInfo);

        // ---------------------- 第二步：逐航段计算飞行时间，推导总耗时 ----------------------
        long[] segmentFlightTimes = new long[totalSegment]; // 各航段飞行时间（秒，向上取整）
        long totalFlightTime = 0;                           // 所有航段总飞行时间（不含停留）
        StringBuilder timeCalcInfo = new StringBuilder("===== 多航段时间计算汇总 =====\n");

        for (int i = 0; i < totalSegment; i++) {
            double distance = segmentDistances[i];
            double speed = speedConfig.getSegmentSpeed(i);
            // 航段飞行时间 = 航段距离 / 航段自定义速度 ，向上取整（确保覆盖全程）
            segmentFlightTimes[i] = (long) Math.ceil(distance / speed);
            totalFlightTime += segmentFlightTimes[i];
            timeCalcInfo.append(String.format(
                    "航段%d飞行时间：%d秒（计算公式：%.2f米 ÷ %.1fm/s = %.2f秒 → 向上取整）%n",
                    i+1, segmentFlightTimes[i], distance, speed, distance/speed
            ));
        }
        // 总耗时 = 总飞行时间 + 所有途经点总停留时间
        long totalTimeSec = totalFlightTime + totalStayTime;
        // 轨迹结束时间 = 开始时间 + 总耗时
        Date endTime = new Date(startTime.getTime() + totalTimeSec * 1000);
        timeCalcInfo.append(String.format(
                "总飞行时间：%d秒 | 总停留时间：%d秒 | 轨迹总耗时：%d秒%n",
                totalFlightTime, totalStayTime, totalTimeSec
        ));
        timeCalcInfo.append(String.format(
                "轨迹时间范围：%s → %s%n",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime)
        ));
        System.out.println(timeCalcInfo);

        // ---------------------- 第三步：初始化轨迹生成基础变量 ----------------------
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        Date currentTime = new Date(startTime.getTime()); // 当前轨迹时间
        double currentSpeed = 0;                          // 无人机当前速度（m/s）
        double currentAcceleration = 0;                   // 无人机当前加速度（m/s²）

        // 添加起点轨迹点（航向指向第一个航段的终点）
        double startHeading = calculateHeading(startPoint, fullPath.get(1));
        trajectory.add(new TrajectoryPoint(
                startPoint.getLongitude(),
                startPoint.getLatitude(),
                startPoint.getAltitude(),
                new Date(currentTime.getTime()),
                currentSpeed,
                startHeading
        ));

        // ---------------------- 第四步：逐航段生成平滑轨迹（v2.0核心优化逻辑） ----------------------
        for (int segIndex = 0; segIndex < totalSegment; segIndex++) {
            Waypoint fromPoint = fullPath.get(segIndex);   // 当前航段起点
            Waypoint toPoint = fullPath.get(segIndex + 1); // 当前航段终点
            double segMaxSpeed = speedConfig.getSegmentSpeed(segIndex); // 当前航段自定义速度
            long segFlightTime = segmentFlightTimes[segIndex]; // 当前航段飞行时间
            long segStayTime = segmentStayTimes[segIndex];   // 当前航段起点的停留时间（仅途经点有）

            // ########## 步骤1：处理当前航段起点的停留（仅途经点触发，起点/终点无停留） ##########
            if (segStayTime > 0) {
                System.out.printf("===== 开始处理【%s】停留，时长：%d秒 =====%n", getPointDesc(segIndex, fromPoint), segStayTime);
                double stayHeading = calculateHeading(fromPoint, toPoint); // 停留时航向指向下一航段
                for (long s = 1; s <= segStayTime; s++) {
                    currentTime.setTime(currentTime.getTime() + 1000);
                    // 停留期间：速度0，位置不变，航向固定；可选叠加GPS抖动模拟真实静止噪声
                    double stayLon = fromPoint.getLongitude();
                    double stayLat = fromPoint.getLatitude();
                    double stayAlt = fromPoint.getAltitude();
                    if (enableGpsNoise) {
                        double[] stayNoise = addGpsNoise(stayLon, stayLat, stayAlt);
                        stayLon = stayNoise[0]; stayLat = stayNoise[1]; stayAlt = stayNoise[2];
                    }
                    trajectory.add(new TrajectoryPoint(
                            stayLon,
                            stayLat,
                            stayAlt,
                            new Date(currentTime.getTime()),
                            0,
                            stayHeading
                    ));
                }
                // 停留后重置速度和加速度
                currentSpeed = 0;
                currentAcceleration = 0;
            }

            // ########## 步骤2：当前航段基础参数初始化 ##########
            double segDistance = calculateDistance(fromPoint, toPoint);
            double segHeading = calculateHeading(fromPoint, toPoint);
            // 适配航段速度的加速度：避免加速度过大导致瞬间达到最大速度，取「基础加速度」和「航段速度/2」的最小值
            double segAcceleration = Math.min(droneParams.getMaxAcceleration(), segMaxSpeed / 2);

            // ########## 步骤3：生成当前航段的转弯平滑路径点 ##########
            List<Waypoint> segmentPoints = new ArrayList<>();
            if (segIndex > 0) { // 非第一个航段，需要处理转弯（前一航段到当前航段的航向变化）
                Waypoint prevFromPoint = fullPath.get(segIndex - 1);
                double previousHeading = calculateHeading(prevFromPoint, fromPoint);
                Point2D.Double startPlane = new Point2D.Double(0, 0);
                Point2D.Double endPlane = toPlaneCoordinates(fromPoint, toPoint);
                // 生成转弯过渡点（平面坐标）
                List<Point2D.Double> turnPoints = generateTurnPoints(
                        startPlane, endPlane, previousHeading, segHeading, droneParams.getTurnRadius());

                // 转弯点水平总距离（用于按比例插值高度）
                double horizTotal = calculateDistance(fromPoint, toPoint);
                double altDiff = toPoint.getAltitude() - fromPoint.getAltitude();

                for (Point2D.Double turnPoint : turnPoints) {
                    // 将平面坐标还原为经纬度（水平位置）
                    Waypoint horzWp = toGeographicCoordinates(fromPoint, turnPoint);
                    // 计算该转弯点距起点的水平距离，按比例插值高度
                    // 保证转弯段的高度平滑连续，不再全部取 fromPoint.getAltitude()
                    double horizDistFromStart = Math.sqrt(turnPoint.x * turnPoint.x + turnPoint.y * turnPoint.y);
                    double altRatio = (horizTotal > 1e-9) ? Math.min(1.0, horizDistFromStart / horizTotal) : 0.0;
                    double interpolatedAlt = fromPoint.getAltitude() + altDiff * altRatio;
                    segmentPoints.add(new Waypoint(horzWp.getLongitude(), horzWp.getLatitude(), interpolatedAlt));
                }
            }
            segmentPoints.add(toPoint); // 航段终点加入路径点（携带正确的目标高度）

            // ===== 【v2.0优化】计算含转弯弧度的路径实际总长度 =====
            // 原版直接用起终点直线距离，转弯弧度会被忽略，导致位置插值与实际路径不符
            double pathTotalDist = computePathLength(fromPoint, segmentPoints);
            if (pathTotalDist < 0.001) pathTotalDist = Math.max(segDistance, 0.001);

            // ########## 步骤4：逐秒生成当前航段的平滑轨迹点（v2.0：速度积分驱动位置） ##########
            // 关键变量：通过速度积分（Σv·Δt）追踪实际行驶距离，保证位置与速度物理一致
            double traveledDist = 0.0;
            // 前一时刻的位置（用于计算实际运动方向→航向）
            Waypoint prevPos = fromPoint;
            // 平滑航向：用低通滤波避免航向剧烈抖动
            double smoothedHeading = segHeading;

            for (long t = 1; t <= segFlightTime; t++) {
                currentTime.setTime(currentTime.getTime() + 1000);

                // ===== 4.1 【v2.0优化】基于剩余距离的梯形速度规划 =====
                // 原版按时间比例计算目标速度，与位置脱钩；
                // 新版实时计算"从当前速度减速到0所需距离"，提前减速，确保末尾归零
                double remaining = pathTotalDist - traveledDist;
                // 从当前速度减速到0所需的最短距离（不考虑jerk限制，用于判断减速时机）
                double minDecDist = (currentSpeed * currentSpeed) / (2.0 * segAcceleration);

                double targetSpeed;
                if (remaining <= minDecDist + 0.5) {
                    // 进入减速段：目标速度由剩余距离反算，保证在终点速度归零
                    targetSpeed = Math.sqrt(Math.max(0, 2.0 * segAcceleration * remaining));
                } else {
                    // 加速或匀速段：目标速度为当前航段最大速度
                    targetSpeed = segMaxSpeed;
                }
                targetSpeed = Math.max(0, Math.min(targetSpeed, segMaxSpeed));

                // 4.2 平滑加速度（限制加加速度，避免加速度突变）
                currentAcceleration = calculateSmoothAcceleration(
                        currentSpeed, targetSpeed, segAcceleration, 1.0, currentAcceleration);
                currentSpeed += currentAcceleration;
                currentSpeed = Math.max(0, Math.min(currentSpeed, segMaxSpeed));

                // ===== 4.3 【v2.0优化】位置更新：速度积分（物理一致性核心） =====
                // 原版：position = lerp(start, end, t/totalTime)，与速度无关
                // 新版：traveledDist += v·Δt，再按距离在路径上插值，物理完全一致
                traveledDist += currentSpeed * 1.0; // Δt = 1秒
                traveledDist = Math.min(traveledDist, pathTotalDist);

                // 按实际行驶距离在路径上插值，得到当前位置
                Waypoint posWaypoint = interpolatePositionByDistance(
                        fromPoint, segmentPoints, pathTotalDist, traveledDist);

                // ===== 4.4 高度：直接来自 interpolatePositionByDistance() 的 3D 插值结果 =====
                // posWaypoint 已通过 3D 距离参数化路径插值得到正确高度：
                // - 直线段：起终点高度线性插值
                // - 转弯段：转弯点已预先按水平比例分配高度，插值连续平滑
                // 原版此处用 Smoothstep 强行覆盖高度，导致航点 altitude 参数实际无效，现已移除
                double finalAlt = posWaypoint.getAltitude();

                // ===== 4.5 【v2.0优化】前瞻航向计算 =====
                // 原版：从当前位置指向预设路径点，存在索引越界和方向跳变问题
                // 新版：沿路径向前看 lookaheadDist 处，无人机"提前转向"，贴近真实飞控
                double lookaheadDist = Math.max(5.0, currentSpeed * HEADING_LOOKAHEAD_SEC);
                double lookaheadTraveledDist = Math.min(traveledDist + lookaheadDist, pathTotalDist);
                Waypoint lookaheadWp = interpolatePositionByDistance(
                        fromPoint, segmentPoints, pathTotalDist, lookaheadTraveledDist);

                double rawHeading;
                double distToPrevPos = calculateDistance(prevPos, posWaypoint);
                if (distToPrevPos > 0.5) {
                    double lookaheadHeading = calculateHeading(posWaypoint, lookaheadWp);
                    rawHeading = lookaheadHeading;
                } else {
                    rawHeading = segHeading;
                }
                // 低通滤波平滑航向（α=0.2：新航向占20%，历史航向占80%，消除抖动）
                smoothedHeading = smoothHeading(smoothedHeading, rawHeading, 0.2);

                // ===== 4.6 【v2.0优化】可选叠加GPS噪声（高斯随机扰动）=====
                // enableGpsNoise=true 时模拟GPS传感器误差、IMU漂移，使轨迹更自然；
                // enableGpsNoise=false（默认）时输出纯净理论轨迹，避免曲线偏移
                double outLon = posWaypoint.getLongitude();
                double outLat = posWaypoint.getLatitude();
                double outAlt = finalAlt;
                if (enableGpsNoise) {
                    double[] noisyPos = addGpsNoise(outLon, outLat, outAlt);
                    outLon = noisyPos[0]; outLat = noisyPos[1]; outAlt = noisyPos[2];
                }

                // 4.7 添加当前轨迹点到结果集
                trajectory.add(new TrajectoryPoint(
                        outLon,
                        outLat,
                        outAlt,
                        new Date(currentTime.getTime()),
                        currentSpeed,
                        smoothedHeading
                ));

                prevPos = posWaypoint; // 更新前一时刻位置
            }
            System.out.printf("===== 航段%d（%s→%s）轨迹生成完成，共生成%d个轨迹点 =====%n",
                    segIndex+1, getPointDesc(segIndex, fromPoint), getPointDesc(segIndex+1, toPoint), segFlightTime);
        }

        // 输出轨迹生成总结果
        System.out.printf("===== 多航段轨迹生成完成 =====%n");
        System.out.printf("总轨迹点数量：%d个%n", trajectory.size());
        System.out.printf("实际轨迹开始时间：%s%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trajectory.get(0).time));
        System.out.printf("实际轨迹结束时间：%s%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trajectory.get(trajectory.size()-1).time));

        return trajectory;
    }

    /**
     * 重载方法：默认关闭GPS噪声（向后兼容原有调用方式）
     */
    public static List<TrajectoryPoint> generateTrajectory(
            Waypoint startPoint, List<Waypoint> waypoints, Waypoint endPoint,
            Date startTime, MultiSegmentSpeedConfig speedConfig, DroneParameters droneParams) {
        return generateTrajectory(startPoint, waypoints, endPoint, startTime, speedConfig, droneParams, false);
    }

    /**
     * 辅助方法：生成航点描述（如「起点」「途经点1」「途经点2」「终点」）
     */
    private static String getPointDesc(int index, Waypoint point) {
        if (index == 0) {
            return "起点";
        } else if (index == Integer.MAX_VALUE) {
            return "终点";
        } else {
            return "途经点" + index;
        }
    }

    /**
     * 计算路径（含转弯点）的三维实际总长度
     * 使用 calculateDistance3D 逐段累加，同时考虑水平位移和高度变化，
     * 保证含爬升/下降航段的路径长度准确，进而使飞行时间和速度规划正确
     *
     * @param start        路径起点
     * @param pathPoints   路径点列表（含转弯中间点和终点，各点已设置正确高度）
     * @return 路径三维实际总长度（米）
     */
    private static double computePathLength(Waypoint start, List<Waypoint> pathPoints) {
        double total = 0;
        Waypoint prev = start;
        for (Waypoint wp : pathPoints) {
            total += calculateDistance3D(prev, wp);
            prev = wp;
        }
        return total;
    }

    /**
     * 按三维实际行驶距离在路径上插值，得到对应位置（含正确高度）
     *
     * 使用 calculateDistance3D 做距离参数化，保证：
     * - 水平位置与高度同步推进（爬升陡的地方水平走得少）
     * - 高度随飞行进度线性变化，完全由航点 altitude 参数决定
     *
     * @param fromPoint       路径起点
     * @param segmentPoints   路径点列表（含转弯点和终点，各点已设置正确高度）
     * @param totalPathDist   路径三维实际总长度（由 computePathLength 计算）
     * @param targetDist      目标行驶距离（0 ~ totalPathDist）
     * @return 对应位置的 Waypoint（经纬度 + 正确高度）
     */
    private static Waypoint interpolatePositionByDistance(
            Waypoint fromPoint, List<Waypoint> segmentPoints,
            double totalPathDist, double targetDist) {
        // 边界处理
        if (targetDist <= 0) return fromPoint;
        if (targetDist >= totalPathDist || segmentPoints.isEmpty()) {
            return segmentPoints.isEmpty() ? fromPoint : segmentPoints.get(segmentPoints.size() - 1);
        }

        double cumDist = 0;
        Waypoint prev = fromPoint;
        for (Waypoint wp : segmentPoints) {
            // 使用 3D 距离：高度差大的子段会占用更多"距离预算"，高度随之正确变化
            double segLen = calculateDistance3D(prev, wp);
            if (cumDist + segLen >= targetDist - 1e-9) {
                // 目标点在 [prev, wp] 段内，按 3D 比例线性插值经纬度和高度
                double ratio = (segLen < 1e-9) ? 0 : (targetDist - cumDist) / segLen;
                ratio = Math.max(0, Math.min(1, ratio));
                double lon = prev.getLongitude() + ratio * (wp.getLongitude() - prev.getLongitude());
                double lat = prev.getLatitude()  + ratio * (wp.getLatitude()  - prev.getLatitude());
                double alt = prev.getAltitude()  + ratio * (wp.getAltitude()  - prev.getAltitude());
                return new Waypoint(lon, lat, alt);
            }
            cumDist += segLen;
            prev = wp;
        }
        // 超出路径长度则返回终点
        return segmentPoints.get(segmentPoints.size() - 1);
    }

    /**
     * 【v2.0新增】按行进距离比例计算平滑高度（Smoothstep曲线）
     *
     * Smoothstep = 3t² - 2t³，在 t=0 和 t=1 处导数均为0（无高度突变），
     * 比原版"分段二次曲线"更简洁且数学性质更优
     *
     * @param startAlt  起点高度（米）
     * @param endAlt    终点高度（米）
     * @param distRatio 行进距离比例（0.0 ~ 1.0），由速度积分驱动
     * @return 当前时刻应有的高度（米）
     */
    private static double calculateSmoothAltitudeByDistRatio(double startAlt, double endAlt, double distRatio) {
        double t = Math.max(0, Math.min(1, distRatio));
        // Smoothstep：起止处切线水平，保证高度变化自然，无急剧爬升或俯冲感
        double smooth = t * t * (3.0 - 2.0 * t);
        return startAlt + (endAlt - startAlt) * smooth;
    }

    /**
     * 【v2.0新增】添加GPS噪声，模拟真实无人机传感器误差
     *
     * 真实无人机GPS水平精度约 ±1~3m（RTK可到±0.1m），
     * 气压高度计精度约 ±0.5~1m；此处用高斯分布近似
     *
     * @param lon 经度
     * @param lat 纬度
     * @param alt 高度（米）
     * @return [加噪后经度, 加噪后纬度, 加噪后高度]
     */
    private static double[] addGpsNoise(double lon, double lat, double alt) {
        // 水平噪声：先转换为度（1米对应多少经纬度）
        double latNoiseMeter = RANDOM.nextGaussian() * GPS_NOISE_H;
        double lonNoiseMeter = RANDOM.nextGaussian() * GPS_NOISE_H;
        double altNoise = RANDOM.nextGaussian() * GPS_NOISE_V;

        // 将米转换为经纬度偏移量
        double latNoiseDeg = latNoiseMeter / EARTH_RADIUS * (180.0 / Math.PI);
        double lonNoiseDeg = lonNoiseMeter / (EARTH_RADIUS * Math.cos(Math.toRadians(lat))) * (180.0 / Math.PI);

        return new double[]{lon + lonNoiseDeg, lat + latNoiseDeg, alt + altNoise};
    }

    /**
     * 【v2.0新增】航向角低通滤波（平滑过渡，消除抖动）
     *
     * 处理航向角的圆周连续性问题（如 350° → 10° 应视为+20°而非-340°），
     * 再按权重加权，保证航向平滑渐变
     *
     * @param currentHeading  当前（历史）平滑航向（度）
     * @param newHeading      本时刻计算的原始新航向（度）
     * @param alpha           新值权重（0~1），越大响应越快，越小越平滑
     * @return 低通滤波后的平滑航向（度）
     */
    private static double smoothHeading(double currentHeading, double newHeading, double alpha) {
        // 计算角差，处理0/360跨越
        double diff = newHeading - currentHeading;
        // 归一化到 [-180, 180]
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        double smoothed = currentHeading + alpha * diff;
        return (smoothed + 360) % 360;
    }

    /**
     * 三次样条插值计算两点间的平滑位置（保留，供外部或其他场景使用）
     */
    private static Waypoint cubicSplineInterpolation(Waypoint from, Waypoint to, double t) {
        double s = 2 * Math.pow(t, 3) - 3 * Math.pow(t, 2) + 1;
        double invS = 1 - s;
        double lon = from.getLongitude() * s + to.getLongitude() * invS;
        double lat = from.getLatitude() * s + to.getLatitude() * invS;
        double alt = from.getAltitude() * s + to.getAltitude() * invS;
        return new Waypoint(lon, lat, alt);
    }

    /**
     * 生成平滑的转弯路径点（基于最小转弯半径）
     */
    private static List<Point2D.Double> generateTurnPoints(Point2D.Double start, Point2D.Double end,
                                                           double startHeading, double endHeading,
                                                           double turnRadius) {
        List<Point2D.Double> turnPoints = new ArrayList<>();

        double headingDiff = endHeading - startHeading;
        headingDiff = (headingDiff + 360) % 360;
        boolean isLeftTurn = headingDiff > 180;
        if (isLeftTurn) headingDiff -= 360;

        // 航向变化小于3度则视为直线，不生成转弯点
        if (Math.abs(headingDiff) < 3) {
            return turnPoints;
        }

        double turnAngle = Math.toRadians(Math.abs(headingDiff));
        double startAngle = Math.toRadians(startHeading + (isLeftTurn ? 90 : -90));
        double endAngle = startAngle + (isLeftTurn ? -turnAngle : turnAngle);

        Point2D.Double center = new Point2D.Double(
                start.x + turnRadius * Math.cos(startAngle),
                start.y + turnRadius * Math.sin(startAngle)
        );

        // 每0.5度生成一个转弯点，保证转弯平滑度
        int steps = (int) Math.ceil(Math.toDegrees(turnAngle) * 2);
        for (int i = 1; i < steps; i++) {
            double angle = startAngle + (isLeftTurn ? -i : i) * Math.toRadians(0.5);
            Point2D.Double point = new Point2D.Double(
                    center.x - turnRadius * Math.cos(angle),
                    center.y - turnRadius * Math.sin(angle)
            );
            turnPoints.add(point);
        }

        return turnPoints;
    }

    /**
     * 计算平滑的加速度曲线（限制加加速度，避免加速度突变）
     */
    private static double calculateSmoothAcceleration(double currentSpeed, double targetSpeed,
                                                      double maxAcceleration, double deltaTime,
                                                      double currentAcceleration) {
        double desiredAcc = (targetSpeed - currentSpeed) / deltaTime;
        double maxAccChange = MAX_JERK * deltaTime; // 加速度最大变化量

        // 先限制加速度变化率，再限制最大加速度
        double newAcc = desiredAcc;
        if (desiredAcc > currentAcceleration + maxAccChange) {
            newAcc = currentAcceleration + maxAccChange;
        } else if (desiredAcc < currentAcceleration - maxAccChange) {
            newAcc = currentAcceleration - maxAccChange;
        }

        // 加速度边界：-maxAcceleration ~ +maxAcceleration
        return Math.max(-maxAcceleration, Math.min(maxAcceleration, newAcc));
    }

    /**
     * 计算平滑的高度变化曲线（S型曲线，加速→匀速→减速，保留供外部调用）
     */
    private static double calculateSmoothAltitude(double startAlt, double endAlt,
                                                  double totalTime, double currentTime) {
        double t = currentTime / totalTime;
        if (t <= 0.2) {
            return startAlt + (endAlt - startAlt) * Math.pow(t / 0.2, 2);
        } else if (t >= 0.8) {
            return startAlt + (endAlt - startAlt) * (1 - Math.pow((1 - t) / 0.2, 2));
        } else {
            return startAlt + (endAlt - startAlt) * (0.25 + (t - 0.2) * 0.75 / 0.6);
        }
    }

    /**
     * 计算两点之间的三维真实距离（米）
     * 在水平球面距离基础上叠加高度差，用于：
     * - 航段飞行时间计算（无人机实际飞行路径长度）
     * - 路径总长计算（含爬升/下降的真实路径）
     * - 位置插值（3D距离参数化，使高度随飞行进度线性变化）
     *
     * 公式：sqrt(horizDist² + altDiff²)
     */
    private static double calculateDistance3D(Waypoint a, Waypoint b) {
        double horizDist = calculateDistance(a, b);
        double altDiff = b.getAltitude() - a.getAltitude();
        return Math.sqrt(horizDist * horizDist + altDiff * altDiff);
    }

    /**
     * 计算两点之间的水平球面距离（米）- 哈维正弦公式
     * 仅用于：航向计算、转弯点生成等需要纯水平距离的场景
     */
    private static double calculateDistance(Waypoint a, Waypoint b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lon1 = Math.toRadians(a.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double lon2 = Math.toRadians(b.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double aHaversine = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(aHaversine), Math.sqrt(1 - aHaversine));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算两点之间的航向角（度，0-360范围）
     */
    private static double calculateHeading(Waypoint from, Waypoint to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double bearing = Math.atan2(y, x);

        // 转换为度并调整到0-360范围
        bearing = Math.toDegrees(bearing);
        return (bearing + 360) % 360;
    }

    /**
     * 将地理坐标转换为平面坐标（以参考点为原点），用于转弯点计算
     */
    private static Point2D.Double toPlaneCoordinates(Waypoint reference, Waypoint point) {
        double distance = calculateDistance(reference, point);
        double heading = calculateHeading(reference, point);

        double radians = Math.toRadians(heading);
        double x = distance * Math.sin(radians);
        double y = distance * Math.cos(radians);

        return new Point2D.Double(x, y);
    }

    /**
     * 将平面坐标转换为地理坐标，用于转弯点还原为经纬度
     */
    private static Waypoint toGeographicCoordinates(Waypoint reference, Point2D.Double planePoint) {
        double distance = Math.sqrt(planePoint.x * planePoint.x + planePoint.y * planePoint.y);
        if (distance < 0.001) { // 距离过近，视为同一位置
            return new Waypoint(reference.getLongitude(), reference.getLatitude(), reference.getAltitude());
        }

        double heading = Math.toDegrees(Math.atan2(planePoint.x, planePoint.y));
        heading = (heading + 360) % 360;

        // 将平面距离和航向转换为经纬度变化
        double lat1 = Math.toRadians(reference.getLatitude());
        double lon1 = Math.toRadians(reference.getLongitude());
        double angularDistance = distance / EARTH_RADIUS;
        double headingRad = Math.toRadians(heading);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(headingRad));

        double lon2 = lon1 + Math.atan2(Math.sin(headingRad) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

        return new Waypoint(Math.toDegrees(lon2), Math.toDegrees(lat2), reference.getAltitude());
    }
}
