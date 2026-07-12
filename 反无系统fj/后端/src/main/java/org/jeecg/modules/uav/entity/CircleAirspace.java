package org.jeecg.modules.uav.entity;

public class CircleAirspace extends Airspace {
    private double centerLongitude;  // 中心经度
    private double centerLatitude;   // 中心纬度
    private double radius;           // 半径（米）
    
    public CircleAirspace(String name, double centerLongitude, double centerLatitude, 
                         double radius, double minAltitude, double maxAltitude) {
        super(name, minAltitude, maxAltitude);
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.radius = radius;
    }
    
    @Override
    protected boolean isPointInArea(double longitude, double latitude) {
        // 计算两点之间的距离（简化计算，实际应用中应使用更精确的地理距离算法）
        double distance = Math.sqrt(
            Math.pow(longitude - centerLongitude, 2) + 
            Math.pow(latitude - centerLatitude, 2)
        );
        // 如果距离小于等于半径，则在圆形区域内
        return distance <= radius;
    }
}
