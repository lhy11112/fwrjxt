package org.jeecg.modules.uav.entity;

public abstract class Airspace {
    private String name;
    private double minAltitude;  // 最小高度（米）
    private double maxAltitude;  // 最大高度（米）
    
    public Airspace(String name, double minAltitude, double maxAltitude) {
        this.name = name;
        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
    }
    
    // 判断无人机是否在空域内（包括高度判断）
    public boolean contains(Drone drone) {
        // 先判断高度是否在范围内
        if (drone.getAltitude() < minAltitude || drone.getAltitude() > maxAltitude) {
            return false;
        }
        // 再判断水平位置是否在空域内
        return isPointInArea(drone.getLongitude(), drone.getLatitude());
    }
    
    // 抽象方法：判断点（经纬度）是否在空域水平范围内
    protected abstract boolean isPointInArea(double longitude, double latitude);
    
    // Getters
    public String getName() {
        return name;
    }
    
    public double getMinAltitude() {
        return minAltitude;
    }
    
    public double getMaxAltitude() {
        return maxAltitude;
    }
}
