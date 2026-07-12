package org.jeecg.modules.uav.util;
import cn.hutool.core.date.DateUnit;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.geom.Point2D;

public class tysjsc {

    public static class Waypoint {
        private final double longitude;
        private final double latitude;
        private final double altitude;
        private final long stayTime;

        public Waypoint(double longitude, double latitude, double altitude) {
            this(longitude, latitude, altitude, 0);
        }

        public Waypoint(double longitude, double latitude, double altitude, long stayTime) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.altitude = altitude;
            this.stayTime = stayTime;
        }

        public double getLongitude() { return longitude; }
        public double getLatitude() { return latitude; }
        public double getAltitude() { return altitude; }
        public long getStayTime() { return stayTime; }
    }

    public static class TrajectoryPoint {
        public double longitude;
        public double latitude;
        public double altitude;
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
        public Date time;
        public double speed;
        public double heading;

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

    public static class DroneParameters {
        private final double maxAcceleration;
        private final double turnRadius;

        public DroneParameters(double maxAcceleration, double turnRadius) {
            this.maxAcceleration = maxAcceleration;
            this.turnRadius = turnRadius;
        }

        public double getMaxAcceleration() { return maxAcceleration; }
        public double getTurnRadius() { return turnRadius; }
    }

    public static class MultiSegmentSpeedConfig {
        private final List<Double> segmentMaxSpeeds;

        public MultiSegmentSpeedConfig(List<Double> segmentMaxSpeeds) {
            if (segmentMaxSpeeds == null || segmentMaxSpeeds.isEmpty()) {
                throw new IllegalArgumentException("航段速度列表不能为空");
            }
            for (double speed : segmentMaxSpeeds) {
                if (speed <= 0) {
                    throw new IllegalArgumentException("所有航段的最大速度必须大于0，当前存在无效速度：" + speed);
                }
            }
            this.segmentMaxSpeeds = new ArrayList<>(segmentMaxSpeeds);
        }

        public double getSegmentSpeed(int segmentIndex) {
            if (segmentIndex < 0 || segmentIndex >= segmentMaxSpeeds.size()) {
                throw new IndexOutOfBoundsException("航段索引越界，当前航段数：" + segmentMaxSpeeds.size() + "，请求索引：" + segmentIndex);
            }
            return segmentMaxSpeeds.get(segmentIndex);
        }

        public int getSegmentCount() {
            return segmentMaxSpeeds.size();
        }
    }

    private static final double MAX_JERK = 0.5;
    private static final double EARTH_RADIUS = 6371000;

    public static List<DroneTrajectoryGenerators.TrajectoryPoint> generateTrajectory(
            DroneTrajectoryGenerators.Waypoint startPoint, List<DroneTrajectoryGenerators.Waypoint> waypoints, DroneTrajectoryGenerators.Waypoint endPoint,
            Date startTime, DroneTrajectoryGenerators.MultiSegmentSpeedConfig speedConfig, DroneTrajectoryGenerators.DroneParameters droneParams) {

        List<DroneTrajectoryGenerators.Waypoint> fullPath = new ArrayList<>();
        fullPath.add(startPoint);
        if (waypoints != null && !waypoints.isEmpty()) {
            fullPath.addAll(waypoints);
        }
        fullPath.add(endPoint);

        int totalSegment = fullPath.size() - 1;
        if (speedConfig.getSegmentCount() != totalSegment) {
            throw new IllegalArgumentException(
                    "速度配置航段数与实际航段数不匹配！" +
                            "实际航段数（起点→途N→终点）：" + totalSegment +
                            "，配置速度航段数：" + speedConfig.getSegmentCount()
            );
        }

        double[] segmentDistances = new double[totalSegment];
        long[] segmentStayTimes = new long[totalSegment];
        long totalStayTime = 0;
        StringBuilder segmentInfo = new StringBuilder("===== 多航段基础参数汇总 =====\n");

        for (int i = 0; i < totalSegment; i++) {
            DroneTrajectoryGenerators.Waypoint from = fullPath.get(i);
            DroneTrajectoryGenerators.Waypoint to = fullPath.get(i + 1);
            segmentDistances[i] = calculateDistance(from, to);
            boolean isWaypoint = i > 0 && i < fullPath.size() - 1;
            segmentStayTimes[i] = isWaypoint ? from.getStayTime() : 0;
            if (isWaypoint) {
                totalStayTime += segmentStayTimes[i];
            }
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

        long[] segmentFlightTimes = new long[totalSegment];
        long totalFlightTime = 0;
        StringBuilder timeCalcInfo = new StringBuilder("===== 多航段时间计算汇总 =====\n");

        // 存储每个航段实际使用的最大速度（可能因转弯被限制）
        double[] actualSegmentMaxSpeeds = new double[totalSegment];

        for (int i = 0; i < totalSegment; i++) {
            double distance = segmentDistances[i];
            double configuredSpeed = speedConfig.getSegmentSpeed(i);
            double segMaxSpeed = configuredSpeed;

            // === 新增：转弯限速逻辑 ===
            if (i > 0) {
                DroneTrajectoryGenerators.Waypoint prevFromPoint = fullPath.get(i - 1);
                DroneTrajectoryGenerators.Waypoint fromPoint = fullPath.get(i);
                DroneTrajectoryGenerators.Waypoint toPoint = fullPath.get(i + 1);
                double previousHeading = calculateHeading(prevFromPoint, fromPoint);
                double currentHeading = calculateHeading(fromPoint, toPoint);
                double headingDiff = Math.abs(previousHeading - currentHeading);
                headingDiff = Math.min(headingDiff, 360 - headingDiff);

                if (headingDiff > 5.0) {
                    double maxLateralAcc = 2.5;
                    double turnLimitedSpeed = Math.sqrt(maxLateralAcc * droneParams.getTurnRadius());
                    segMaxSpeed = Math.min(configuredSpeed, turnLimitedSpeed);
                    System.out.printf("航段%d检测到转弯%.1f°，速度从%.1f限制为%.1f m/s%n",
                            i + 1, headingDiff, configuredSpeed, segMaxSpeed);
                }
            }
            actualSegmentMaxSpeeds[i] = segMaxSpeed;

            segmentFlightTimes[i] = (long) Math.ceil(distance / segMaxSpeed);
            totalFlightTime += segmentFlightTimes[i];
            timeCalcInfo.append(String.format(
                    "航段%d飞行时间：%d秒（计算公式：%.2f米 ÷ %.1fm/s = %.2f秒 → 向上取整）%n",
                    i+1, segmentFlightTimes[i], distance, segMaxSpeed, distance/segMaxSpeed
            ));
        }

        long totalTimeSec = totalFlightTime + totalStayTime;
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

        List<DroneTrajectoryGenerators.TrajectoryPoint> trajectory = new ArrayList<>();
        Date currentTime = new Date(startTime.getTime());
        double currentSpeed = 0;
        double currentAcceleration = 0;

        double startHeading = calculateHeading(startPoint, fullPath.get(1));
        trajectory.add(new DroneTrajectoryGenerators.TrajectoryPoint(
                startPoint.getLongitude(),
                startPoint.getLatitude(),
                startPoint.getAltitude(),
                new Date(currentTime.getTime()),
                currentSpeed,
                startHeading
        ));

        for (int segIndex = 0; segIndex < totalSegment; segIndex++) {
            DroneTrajectoryGenerators.Waypoint fromPoint = fullPath.get(segIndex);
            DroneTrajectoryGenerators.Waypoint toPoint = fullPath.get(segIndex + 1);
            double segMaxSpeed = actualSegmentMaxSpeeds[segIndex];
            long segFlightTime = segmentFlightTimes[segIndex];
            long segStayTime = segmentStayTimes[segIndex];
            double segDistance = segmentDistances[segIndex];
            double segHeading = calculateHeading(fromPoint, toPoint);
            double segAcceleration = Math.min(droneParams.getMaxAcceleration(), segMaxSpeed / 2);

            if (segStayTime > 0) {
                System.out.printf("===== 开始处理【%s】停留，时长：%d秒 =====%n", getPointDesc(segIndex, fromPoint), segStayTime);
                double stayHeading = calculateHeading(fromPoint, toPoint);
                for (long s = 1; s <= segStayTime; s++) {
                    currentTime.setTime(currentTime.getTime() + 1000);
                    trajectory.add(new DroneTrajectoryGenerators.TrajectoryPoint(
                            fromPoint.getLongitude(),
                            fromPoint.getLatitude(),
                            fromPoint.getAltitude(),
                            new Date(currentTime.getTime()),
                            0,
                            stayHeading
                    ));
                }
                currentSpeed = 0;
                currentAcceleration = 0;
            }

            // 生成转弯点（仅用于可视化或复杂路径，但位置仍以大圆为主）
            List<DroneTrajectoryGenerators.Waypoint> segmentPoints = new ArrayList<>();
            if (segIndex > 0) {
                DroneTrajectoryGenerators.Waypoint prevFromPoint = fullPath.get(segIndex - 1);
                double previousHeading = calculateHeading(prevFromPoint, fromPoint);
                Point2D.Double startPlane = new Point2D.Double(0, 0);
                Point2D.Double endPlane = toPlaneCoordinates(fromPoint, toPoint);
                List<Point2D.Double> turnPoints = generateTurnPoints(
                        startPlane, endPlane, previousHeading, segHeading, droneParams.getTurnRadius());
                for (Point2D.Double turnPoint : turnPoints) {
                    segmentPoints.add(toGeographicCoordinates(fromPoint, turnPoint));
                }
            }
            segmentPoints.add(toPoint);

            // 逐秒生成轨迹
            for (long t = 1; t <= segFlightTime; t++) {
                currentTime.setTime(currentTime.getTime() + 1000);
                double distanceCovered = 0.0;
                boolean canReachMaxSpeed = segFlightTime > 2 * (segMaxSpeed / segAcceleration);

                if (canReachMaxSpeed) {
                    double accTime = segMaxSpeed / segAcceleration;
                    if (t <= accTime) {
                        distanceCovered = 0.5 * segAcceleration * t * t;
                    } else if (t <= segFlightTime - accTime) {
                        distanceCovered = 0.5 * segAcceleration * accTime * accTime
                                + segMaxSpeed * (t - accTime);
                    } else {
                        double decTime = t - (segFlightTime - accTime);
                        distanceCovered = 0.5 * segAcceleration * accTime * accTime
                                + segMaxSpeed * (segFlightTime - 2 * accTime)
                                + segMaxSpeed * decTime - 0.5 * segAcceleration * decTime * decTime;
                    }
                } else {
                    double midTime = segFlightTime / 2.0;
                    if (t <= midTime) {
                        distanceCovered = 0.5 * segAcceleration * t * t;
                    } else {
                        double firstHalfDist = 0.5 * segAcceleration * midTime * midTime;
                        double decTime = t - midTime;
                        double decSpeed = segAcceleration * midTime;
                        distanceCovered = firstHalfDist + decSpeed * decTime - 0.5 * segAcceleration * decTime * decTime;
                    }
                }
                distanceCovered = Math.min(distanceCovered, segDistance);
                double actualRatio = distanceCovered / segDistance;

                // 平滑加减速目标速度
                double targetSpeed;
                if (canReachMaxSpeed) {
                    double accTime = segMaxSpeed / segAcceleration;
                    if (t <= accTime) {
                        targetSpeed = segAcceleration * t;
                    } else if (t > segFlightTime - accTime) {
                        targetSpeed = segMaxSpeed - segAcceleration * (t - (segFlightTime - accTime));
                    } else {
                        targetSpeed = segMaxSpeed;
                    }
                } else {
                    double midTime = segFlightTime / 2.0;
                    if (t <= midTime) {
                        targetSpeed = segAcceleration * t;
                    } else {
                        targetSpeed = segAcceleration * (segFlightTime - t);
                    }
                }
                targetSpeed = Math.min(targetSpeed, segMaxSpeed);
                targetSpeed = Math.max(0, targetSpeed);

                currentAcceleration = calculateSmoothAcceleration(
                        currentSpeed, targetSpeed, segAcceleration, 1, currentAcceleration);
                currentSpeed += currentAcceleration * 1;
                currentSpeed = Math.max(0, Math.min(currentSpeed, segMaxSpeed));

                // === 使用大圆插值得到当前位置 ===
                DroneTrajectoryGenerators.Waypoint currentWaypoint = interpolateAlongGreatCircle(fromPoint, toPoint, actualRatio);
                double smoothAlt = calculateSmoothAltitudeByRatio(
                        fromPoint.getAltitude(), toPoint.getAltitude(), actualRatio);
                currentWaypoint = new DroneTrajectoryGenerators.Waypoint(
                        currentWaypoint.getLongitude(),
                        currentWaypoint.getLatitude(),
                        smoothAlt
                );

                // === 航向：用下一秒预估位置计算 ===
                double nextLon, nextLat;
                if (t < segFlightTime) {
                    double nextDist = currentSpeed; // 1秒走 currentSpeed 米
                    double nextRatio = Math.min(1.0, (distanceCovered + nextDist) / segDistance);
                    DroneTrajectoryGenerators.Waypoint nextWP = interpolateAlongGreatCircle(fromPoint, toPoint, nextRatio);
                    nextLon = nextWP.getLongitude();
                    nextLat = nextWP.getLatitude();
                } else {
                    nextLon = toPoint.getLongitude();
                    nextLat = toPoint.getLatitude();
                }
                double currentHeading = calculateHeading(
                        new DroneTrajectoryGenerators.Waypoint(currentWaypoint.getLongitude(), currentWaypoint.getLatitude(), 0),
                        new DroneTrajectoryGenerators.Waypoint(nextLon, nextLat, 0)
                );

                trajectory.add(new DroneTrajectoryGenerators.TrajectoryPoint(
                        currentWaypoint.getLongitude(),
                        currentWaypoint.getLatitude(),
                        currentWaypoint.getAltitude(),
                        new Date(currentTime.getTime()),
                        currentSpeed,
                        currentHeading
                ));
            }
            System.out.printf("===== 航段%d（%s→%s）轨迹生成完成，共生成%d个轨迹点 =====%n",
                    segIndex+1, getPointDesc(segIndex, fromPoint), getPointDesc(segIndex+1, toPoint), segFlightTime);
        }

        System.out.printf("===== 多航段轨迹生成完成 =====%n");
        System.out.printf("总轨迹点数量：%d个%n", trajectory.size());
        System.out.printf("实际轨迹开始时间：%s%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trajectory.get(0).time));
        System.out.printf("实际轨迹结束时间：%s%n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(trajectory.get(trajectory.size()-1).time));

        return trajectory;
    }

    private static String getPointDesc(int index, DroneTrajectoryGenerators.Waypoint point) {
        if (index == 0) {
            return "起点";
        } else if (index == Integer.MAX_VALUE) {
            return "终点";
        } else {
            return "途经点" + index;
        }
    }

    /**
     * 沿大圆路径插值（球面线性插值）
     */
    private static DroneTrajectoryGenerators.Waypoint interpolateAlongGreatCircle(DroneTrajectoryGenerators.Waypoint from, DroneTrajectoryGenerators.Waypoint to, double ratio) {
        if (ratio <= 0) return from;
        if (ratio >= 1) return to;

        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double d = calculateDistance(from, to) / EARTH_RADIUS;
        if (d < 1e-9) return from;

        double A = Math.sin((1 - ratio) * d) / Math.sin(d);
        double B = Math.sin(ratio * d) / Math.sin(d);

        double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
        double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
        double z = A * Math.sin(lat1) + B * Math.sin(lat2);

        double lat = Math.atan2(z, Math.sqrt(x * x + y * y));
        double lon = Math.atan2(y, x);

        double alt = from.getAltitude() + ratio * (to.getAltitude() - from.getAltitude());
        return new DroneTrajectoryGenerators.Waypoint(Math.toDegrees(lon), Math.toDegrees(lat), alt);
    }

    private static double calculateSmoothAltitudeByRatio(double startAlt, double endAlt, double ratio) {
        if (ratio <= 0.2) {
            return startAlt + (endAlt - startAlt) * Math.pow(ratio / 0.2, 2);
        } else if (ratio >= 0.8) {
            return startAlt + (endAlt - startAlt) * (1 - Math.pow((1 - ratio) / 0.2, 2));
        } else {
            return startAlt + (endAlt - startAlt) * (0.25 + (ratio - 0.2) * 0.75 / 0.6);
        }
    }

    private static List<Point2D.Double> generateTurnPoints(Point2D.Double start, Point2D.Double end,
                                                           double startHeading, double endHeading,
                                                           double turnRadius) {
        List<Point2D.Double> turnPoints = new ArrayList<>();

        double headingDiff = endHeading - startHeading;
        headingDiff = (headingDiff + 360) % 360;
        boolean isLeftTurn = headingDiff > 180;
        if (isLeftTurn) headingDiff -= 360;

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

    private static double calculateSmoothAcceleration(double currentSpeed, double targetSpeed,
                                                      double maxAcceleration, double deltaTime,
                                                      double currentAcceleration) {
        double desiredAcc = (targetSpeed - currentSpeed) / deltaTime;
        double maxAccChange = MAX_JERK * deltaTime;

        double newAcc = desiredAcc;
        if (desiredAcc > currentAcceleration + maxAccChange) {
            newAcc = currentAcceleration + maxAccChange;
        } else if (desiredAcc < currentAcceleration - maxAccChange) {
            newAcc = currentAcceleration - maxAccChange;
        }

        return Math.max(-maxAcceleration, Math.min(maxAcceleration, newAcc));
    }

    private static double calculateDistance(DroneTrajectoryGenerators.Waypoint a, DroneTrajectoryGenerators.Waypoint b) {
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

    private static double calculateHeading(DroneTrajectoryGenerators.Waypoint from, DroneTrajectoryGenerators.Waypoint to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double bearing = Math.atan2(y, x);

        bearing = Math.toDegrees(bearing);
        return (bearing + 360) % 360;
    }

    private static Point2D.Double toPlaneCoordinates(DroneTrajectoryGenerators.Waypoint reference, DroneTrajectoryGenerators.Waypoint point) {
        double distance = calculateDistance(reference, point);
        double heading = calculateHeading(reference, point);

        double radians = Math.toRadians(heading);
        double x = distance * Math.sin(radians);
        double y = distance * Math.cos(radians);

        return new Point2D.Double(x, y);
    }

    private static DroneTrajectoryGenerators.Waypoint toGeographicCoordinates(DroneTrajectoryGenerators.Waypoint reference, Point2D.Double planePoint) {
        double distance = Math.sqrt(planePoint.x * planePoint.x + planePoint.y * planePoint.y);
        if (distance < 0.001) {
            return new DroneTrajectoryGenerators.Waypoint(reference.getLongitude(), reference.getLatitude(), reference.getAltitude());
        }

        double heading = Math.toDegrees(Math.atan2(planePoint.x, planePoint.y));
        heading = (heading + 360) % 360;

        double lat1 = Math.toRadians(reference.getLatitude());
        double lon1 = Math.toRadians(reference.getLongitude());
        double angularDistance = distance / EARTH_RADIUS;
        double headingRad = Math.toRadians(heading);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(headingRad));

        double lon2 = lon1 + Math.atan2(Math.sin(headingRad) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

        return new DroneTrajectoryGenerators.Waypoint(Math.toDegrees(lon2), Math.toDegrees(lat2), reference.getAltitude());
    }

}
