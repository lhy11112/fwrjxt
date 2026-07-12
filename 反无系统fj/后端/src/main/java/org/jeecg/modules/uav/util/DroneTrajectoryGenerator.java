package org.jeecg.modules.uav.util;

import cn.hutool.core.date.DateUnit;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.geom.Point2D;

/**
 * 无人机平滑轨迹生成器
 * 支持指定开始和结束时间，采用Date类处理时间
 */
public class DroneTrajectoryGenerator {

    /**
     * 航点类，包含地理位置信息和停留时间
     */
    public static class Waypoint {
        private  double longitude;  // 经度
        private  double latitude;   // 纬度
        private  double altitude;   // 高度(米)
        private  long stayTime;     // 停留时间(秒)

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
        @DateTimeFormat(
                pattern = "yyyy-MM-dd HH:mm:ss"
        )
        @JsonFormat(
                timezone = "GMT+8",
                pattern = "yyyy-MM-dd HH:mm:ss"
        )
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
     * 无人机参数类，存储性能限制
     */
    public static class DroneParameters {
        private final double maxSpeed;      // 最大速度(米/秒)
        private final double acceleration;  // 最大加速度(米/秒²)
        private final double turnRadius;    // 最小转弯半径(米)

        public DroneParameters(double maxSpeed, double acceleration, double turnRadius) {
            this.maxSpeed = maxSpeed;
            this.acceleration = acceleration;
            this.turnRadius = turnRadius;
        }

        // Getters
        public double getMaxSpeed() { return maxSpeed; }
        public double getAcceleration() { return acceleration; }
        public double getTurnRadius() { return turnRadius; }
    }

    // 加加速度限制（米/秒³），控制加速度变化率
    private static final double MAX_JERK = 0.5;

    // 地球半径(米)，用于距离计算
    private static final double EARTH_RADIUS = 6371000;

    /**
     * 生成平滑的无人机轨迹，支持指定开始和结束时间
     * @param startPoint 起点
     * @param waypoints 途经点列表
     * @param endPoint 终点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param droneParams 无人机参数
     * @return 生成的轨迹点列表
     */
    public static List<TrajectoryPoint> generateTrajectory(
            Waypoint startPoint, List<Waypoint> waypoints, Waypoint endPoint,
            Date startTime, Date endTime, DroneParameters droneParams) {

        // 计算总可用时间（毫秒）
        long totalAvailableTimeMs = endTime.getTime() - startTime.getTime();
        if (totalAvailableTimeMs <= 0) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
        long totalAvailableTimeSec = totalAvailableTimeMs / 1000;

        List<TrajectoryPoint> trajectory = new ArrayList<>();
        List<Waypoint> fullPath = new ArrayList<>();
        fullPath.add(startPoint);
        fullPath.addAll(waypoints);
        fullPath.add(endPoint);

        // 计算总距离和总停留时间
        double totalDistance = 0;
        long totalStayTime = 0;
        for (int i = 0; i < fullPath.size() - 1; i++) {
            totalDistance += calculateDistance(fullPath.get(i), fullPath.get(i + 1));
            if (i > 0) {
                totalStayTime += fullPath.get(i).getStayTime();
            }
        }

        // 计算可用飞行时间（扣除停留时间）
        long availableFlightTime = totalAvailableTimeSec - totalStayTime;
        if (availableFlightTime <= 0) {
            throw new IllegalArgumentException("可用飞行时间不足，无法完成轨迹");
        }

        // 根据可用时间调整最大速度（如果需要）
        double requiredSpeed = totalDistance / availableFlightTime;
        DroneParameters adjustedParams = droneParams;
        if (requiredSpeed < droneParams.getMaxSpeed()) {
            adjustedParams = new DroneParameters(
                    requiredSpeed,
                    Math.min(droneParams.getAcceleration(), requiredSpeed / 2),  // 调整加速度
                    droneParams.getTurnRadius()
            );
        }

        Date currentTime = new Date(startTime.getTime());
        double currentSpeed = 0;
        double currentAcceleration = 0;
        Waypoint currentPosition = startPoint;

        // 添加起点
        trajectory.add(new TrajectoryPoint(
                currentPosition.getLongitude(),
                currentPosition.getLatitude(),
                currentPosition.getAltitude(),
                new Date(currentTime.getTime()),
                currentSpeed,
                0
        ));

        for (int i = 0; i < fullPath.size() - 1; i++) {
            Waypoint fromPoint = fullPath.get(i);
            Waypoint toPoint = fullPath.get(i + 1);

            // 途经点停留处理
            if (i > 0) {
                long stayTime = fromPoint.getStayTime();
                // 检查停留时间是否会导致超时
                long expectedEndTimeMs = currentTime.getTime() + stayTime * 1000;
                if (expectedEndTimeMs > endTime.getTime()) {
                    throw new IllegalStateException("停留时间过长，将导致轨迹超出指定结束时间");
                }

                for (long s = 1; s <= stayTime; s++) {
                    currentTime.setTime(currentTime.getTime() + 1000);
                    trajectory.add(new TrajectoryPoint(
                            fromPoint.getLongitude(),
                            fromPoint.getLatitude(),
                            fromPoint.getAltitude(),
                            new Date(currentTime.getTime()),
                            0,
                            calculateHeading(fromPoint, toPoint)
                    ));
                }
                currentSpeed = 0;
                currentAcceleration = 0;
            }

            double distance = calculateDistance(fromPoint, toPoint);
            double heading = calculateHeading(fromPoint, toPoint);

            if (i == 0) {
                trajectory.get(0).heading = heading;
            }

            // 按总可用飞行时间占比分配航段时间（修复核心）
            double segmentDistanceRatio = distance / totalDistance;
            long segmentTime = (long) Math.ceil(segmentDistanceRatio * availableFlightTime);

            // 预判当前航段时间是否会导致超时
            long currentElapsedTime = (currentTime.getTime() - startTime.getTime()) / 1000;
            long estimatedTotalTime = currentElapsedTime + segmentTime;
            if (estimatedTotalTime > availableFlightTime + totalStayTime) {
                segmentTime = availableFlightTime + totalStayTime - currentElapsedTime;
                if (segmentTime <= 0) {
                    throw new IllegalStateException("剩余时间不足，无法完成当前航段");
                }
            }

            // 生成转弯路径点
            List<Waypoint> segmentPoints = new ArrayList<>();
            if (i > 0) {
                double previousHeading = calculateHeading(fullPath.get(i-1), fromPoint);
                Point2D.Double startPlane = new Point2D.Double(0, 0);
                Point2D.Double endPlane = toPlaneCoordinates(fromPoint, toPoint);
                List<Point2D.Double> turnPoints = generateTurnPoints(
                        startPlane, endPlane, previousHeading, heading, adjustedParams.getTurnRadius());
                for (Point2D.Double turnPoint : turnPoints) {
                    segmentPoints.add(toGeographicCoordinates(fromPoint, turnPoint));
                }
            }
            segmentPoints.add(toPoint);

            // 生成轨迹点（使用三次样条插值）
            currentPosition = fromPoint;
            double totalSegmentDistance = distance;

            for (long t = 1; t <= segmentTime; t++) {
                currentTime.setTime(currentTime.getTime() + 1000);
                // 检查是否超出结束时间
                if (currentTime.after(endTime)) {
                    throw new IllegalStateException("生成的轨迹时间超出指定的结束时间");
                }

                double ratio = (double)t / segmentTime;

                // 计算目标速度
                double targetSpeed;
                double timeToMaxSpeed = adjustedParams.getMaxSpeed() / adjustedParams.getAcceleration();
                boolean canReachMaxSpeed = segmentTime > 2 * timeToMaxSpeed;

                if (canReachMaxSpeed) {
                    if (t <= timeToMaxSpeed) {
                        targetSpeed = adjustedParams.getAcceleration() * t;
                    } else if (t > segmentTime - timeToMaxSpeed) {
                        targetSpeed = adjustedParams.getMaxSpeed() -
                                adjustedParams.getAcceleration() * (t - (segmentTime - timeToMaxSpeed));
                    } else {
                        targetSpeed = adjustedParams.getMaxSpeed();
                    }
                } else {
                    double midTime = segmentTime / 2.0;
                    if (t <= midTime) {
                        targetSpeed = adjustedParams.getAcceleration() * t;
                    } else {
                        targetSpeed = adjustedParams.getAcceleration() * (segmentTime - t);
                    }
                }
                targetSpeed = Math.min(targetSpeed, adjustedParams.getMaxSpeed());

                // 平滑加速度计算（已修复）
                currentAcceleration = calculateSmoothAcceleration(
                        currentSpeed, targetSpeed, adjustedParams.getAcceleration(), 1,currentAcceleration);
                currentSpeed += currentAcceleration * 1;
                currentSpeed = Math.max(0, Math.min(currentSpeed, adjustedParams.getMaxSpeed()));

                // 位置计算（使用三次样条插值）
                Waypoint currentWaypoint;
                if (segmentPoints.isEmpty() || ratio >= 1.0) {
                    currentWaypoint = toPoint;
                } else {
                    int segmentIndex = 0;
                    double cumulativeDistance = 0;
                    double prevDistance = 0;

                    for (int j = 0; j < segmentPoints.size(); j++) {
                        cumulativeDistance += calculateDistance(
                                j == 0 ? fromPoint : segmentPoints.get(j-1),
                                segmentPoints.get(j));
                        if (cumulativeDistance / totalSegmentDistance >= ratio) {
                            segmentIndex = j;
                            break;
                        }
                        prevDistance = cumulativeDistance;
                    }

                    Waypoint segmentStart = segmentIndex == 0 ? fromPoint : segmentPoints.get(segmentIndex - 1);
                    Waypoint segmentEnd = segmentPoints.get(segmentIndex);
                    double segmentLength = calculateDistance(segmentStart, segmentEnd);
                    double segmentRatio = (ratio * totalSegmentDistance - prevDistance) / segmentLength;

                    // 使用三次样条插值
                    currentWaypoint = cubicSplineInterpolation(segmentStart, segmentEnd, segmentRatio);
                    // 应用平滑高度变化
                    double smoothAlt = calculateSmoothAltitude(
                            fromPoint.getAltitude(), toPoint.getAltitude(), segmentTime, t);
                    currentWaypoint = new Waypoint(
                            currentWaypoint.getLongitude(),
                            currentWaypoint.getLatitude(),
                            smoothAlt
                    );
                }

                // 航向平滑过渡
                double currentHeading = heading;
                if (t < segmentTime && !segmentPoints.isEmpty()) {
                    int nextSegmentIndex = (int)(ratio * segmentPoints.size()) + 1;
                    nextSegmentIndex = Math.min(nextSegmentIndex, segmentPoints.size() - 1);
                    Waypoint nextPoint = segmentPoints.get(nextSegmentIndex);
                    currentHeading = calculateHeading(currentWaypoint, nextPoint);
                }

                trajectory.add(new TrajectoryPoint(
                        currentWaypoint.getLongitude(),
                        currentWaypoint.getLatitude(),
                        currentWaypoint.getAltitude(),
                        new Date(currentTime.getTime()),
                        currentSpeed,
                        currentHeading
                ));

                currentPosition = currentWaypoint;
            }
        }

        return trajectory;
    }

    /**
     * 三次样条插值计算两点间的平滑位置
     */
    private static Waypoint cubicSplineInterpolation(Waypoint from, Waypoint to, double t) {
        // 三次样条曲线：s(t) = 2t³ - 3t² + 1 (起点到终点平滑过渡)
        double s = 2 * Math.pow(t, 3) - 3 * Math.pow(t, 2) + 1;
        double invS = 1 - s;

        double lon = from.getLongitude() * s + to.getLongitude() * invS;
        double lat = from.getLatitude() * s + to.getLatitude() * invS;
        double alt = from.getAltitude() * s + to.getAltitude() * invS;

        return new Waypoint(lon, lat, alt);
    }

    /**
     * 生成平滑的转弯路径
     */
    private static List<Point2D.Double> generateTurnPoints(Point2D.Double start, Point2D.Double end,
                                                           double startHeading, double endHeading,
                                                           double turnRadius) {
        List<Point2D.Double> turnPoints = new ArrayList<>();

        double headingDiff = endHeading - startHeading;
        headingDiff = (headingDiff + 360) % 360;
        boolean isLeftTurn = headingDiff > 180;
        if (isLeftTurn) headingDiff -= 360;

        // 航向变化小于3度则视为直线
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

        // 每0.5度一个点，提高转弯平滑度
        int steps = (int)Math.ceil(Math.toDegrees(turnAngle) * 2);
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
     * 计算平滑的加速度曲线（限制加加速度）
     * 已修复变量作用域问题
     */
    private static double calculateSmoothAcceleration(double currentSpeed, double targetSpeed,
                                                      double maxAcceleration, double deltaTime,
                                                      double currentAcceleration) {  // 添加当前加速度作为参数
        double desiredAcc = (targetSpeed - currentSpeed) / deltaTime;
        // 限制加速度变化率（加加速度）
        double maxAccChange = MAX_JERK * deltaTime;

        // 先限制加速度变化率，再限制最大加速度
        double newAcc = desiredAcc;
        if (desiredAcc > currentAcceleration + maxAccChange) {
            newAcc = currentAcceleration + maxAccChange;
        } else if (desiredAcc < currentAcceleration - maxAccChange) {
            newAcc = currentAcceleration - maxAccChange;
        }

        return Math.max(-maxAcceleration, Math.min(maxAcceleration, newAcc));
    }

    /**
     * 计算平滑的高度变化曲线（S型曲线）
     */
    private static double calculateSmoothAltitude(double startAlt, double endAlt,
                                                  double totalTime, double currentTime) {
        double t = currentTime / totalTime;
        if (t <= 0.2) {
            // 加速阶段：t²曲线
            return startAlt + (endAlt - startAlt) * Math.pow(t / 0.2, 2);
        } else if (t >= 0.8) {
            // 减速阶段：(1 - (1-t)²)曲线
            return startAlt + (endAlt - startAlt) * (1 - Math.pow((1 - t) / 0.2, 2));
        } else {
            // 匀速阶段
            return startAlt + (endAlt - startAlt) * (0.25 + (t - 0.2) * 0.75 / 0.6);
        }
    }

    /**
     * 计算两点之间的距离（米）
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
     * 计算两点之间的航向角（度）
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

        // 转换为度并调整为0-360范围
        bearing = Math.toDegrees(bearing);
        return (bearing + 360) % 360;
    }

    /**
     * 将地理坐标转换为平面坐标（以参考点为原点）
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
     * 将平面坐标转换为地理坐标
     */
    private static Waypoint toGeographicCoordinates(Waypoint reference, Point2D.Double planePoint) {
        double distance = Math.sqrt(planePoint.x * planePoint.x + planePoint.y * planePoint.y);
        if (distance < 0.001) {
            return new Waypoint(reference.getLongitude(), reference.getLatitude(), reference.getAltitude());
        }

        double heading = Math.toDegrees(Math.atan2(planePoint.x, planePoint.y));
        heading = (heading + 360) % 360;

        // 将距离和航向转换为经纬度变化
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
