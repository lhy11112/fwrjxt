package org.jeecg.modules.uav.util;

import org.jeecg.modules.uav.vo.DronePositions;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenerateFlightRouteUtils {
    public static List<DronePositions> generateFlightRoute(
            double startLon, double startLat, double startAlt,
            double endLon, double endLat, double endAlt,
            int totalTimeSeconds) {

        List<DronePosition> route = new ArrayList<>();

        // 获取当前的系统时间
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime currentTimeZ = ZonedDateTime.ofInstant(now, zoneId);

        // 计算总距离和速度
        double distance = calculateDistance(startLon, startLat, endLon, endLat);
        double speed = distance / (totalTimeSeconds / 3600.0); // 米/秒

        // 高度变化阶段：爬升、巡航、降落
        double climbAlt = 200; // 爬升高度
        double descendAlt = endAlt; // 降落高度
        int climbTime = 30; // 爬升时间（秒）
        int cruiseTime = totalTimeSeconds - 2 * climbTime; // 巡航时间
        int descendTime = 30; // 降落时间（秒）

        // 时间分割
        int steps = totalTimeSeconds * 10; // 每0.1秒一个点
        double stepTime = 0.1;

        // 当前状态
        double currentTime = 0.0;
        double currentLon = startLon;
        double currentLat = startLat;
        double currentAlt = startAlt;

        // 添加起点
        route.add(new DronePosition(currentLon, currentLat, currentAlt, currentTime));
        route.get(0).zonedDateTime = currentTimeZ;

        for (int i = 1; i <= steps; i++) {
            currentTime += stepTime;

            // 处理高度变化
            if (currentTime <= climbTime) {
                // 爬升阶段
                currentAlt = startAlt + (climbAlt - startAlt) * (currentTime / climbTime);
            } else if (currentTime > climbTime && currentTime <= climbTime + cruiseTime) {
                // 巡航阶段
                currentAlt = climbAlt;
            } else if (currentTime > climbTime + cruiseTime && currentTime <= totalTimeSeconds) {
                // 降落阶段
                currentAlt = climbAlt - (climbAlt - descendAlt) * ((currentTime - (climbTime + cruiseTime)) / descendTime);
            }

            // 计算经纬度
            double progress = currentTime / totalTimeSeconds;
            double newLon = startLon + (endLon - startLon) * progress;
            double newLat = startLat + (endLat - startLat) * progress;

            // 创建新的DronePosition对象，并设置时间
            DronePosition position = new DronePosition(newLon, newLat, currentAlt, currentTime);
            position.zonedDateTime = currentTimeZ.plusSeconds((long) currentTime);

            route.add(position);
        }
        List<DronePositions> dronePositions=new ArrayList<>();
        route.stream().forEach(item->{
            DronePositions dronePositions1=new DronePositions();
            dronePositions1.setAltitude(item.altitude);
            Instant instant = item.zonedDateTime.toInstant();
            dronePositions1.setZonedDateTime(new Date(instant.toEpochMilli()));
            dronePositions1.setLatitude(item.latitude);
            dronePositions1.setLongitude(item.longitude);
            dronePositions1.setTime(item.time);
            BeanUtils.copyProperties(item,dronePositions1);
            dronePositions.add(dronePositions1);
        });
        return dronePositions;
    }

    private static double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        // 使用Haversine公式计算两点间的大圆距离
        double a = Math.toRadians(lat1);
        double b = Math.toRadians(lat2);
        double dlon = Math.toRadians(lon2 - lon1);
        double cosTheta = Math.cos(a) * Math.cos(b) * Math.cos(dlon) + Math.sin(a) * Math.sin(b);
        double distance = 6371000 * Math.acos(cosTheta); // 结果为米
        return distance;
    }
}

class DronePosition {
    double longitude;
    double latitude;
    double altitude;
    double time; // 以秒为单位的时间，从0开始
    ZonedDateTime zonedDateTime; // 详细的时间信息

    DronePosition(double lon, double lat, double alt, double t) {
        this.longitude = lon;
        this.latitude = lat;
        this.altitude = alt;
        this.time = t;
        this.zonedDateTime = null;
    }

    public String getFormattedTime() {
        if (zonedDateTime != null) {
            return zonedDateTime.toString();
        } else {
            return String.format("时间: %.3f秒", time);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "经度: %.4f°, 纬度: %.4f°, 高度: %.2f米, %s",
                longitude, latitude, altitude, getFormattedTime()
        );
    }
}
