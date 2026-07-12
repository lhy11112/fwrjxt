package org.jeecg.modules.uav.service;

import org.jeecg.modules.uav.entity.Airspace;

public class RectangleAirspace extends Airspace {
    private double centerLongitude;  // 中心经度
    private double centerLatitude;   // 中心纬度
    private double width;            // 宽度（米）
    private double length;           // 长度（米）
    
    // 计算得到的边界
    private double minLongitude;
    private double maxLongitude;
    private double minLatitude;
    private double maxLatitude;
    
    public RectangleAirspace(String name, double centerLongitude, double centerLatitude, 
                            double width, double length, double minAltitude, double maxAltitude) {
        super(name, minAltitude, maxAltitude);
        this.centerLongitude = centerLongitude;
        this.centerLatitude = centerLatitude;
        this.width = width;
        this.length = length;
        
        // 计算边界
        this.minLongitude = centerLongitude - width / 2;
        this.maxLongitude = centerLongitude + width / 2;
        this.minLatitude = centerLatitude - length / 2;
        this.maxLatitude = centerLatitude + length / 2;
    }
    
    @Override
    protected boolean isPointInArea(double longitude, double latitude) {
        // 检查点是否在矩形边界内
        return longitude >= minLongitude && longitude <= maxLongitude &&
               latitude >= minLatitude && latitude <= maxLatitude;
    }
    
    // Getters
    public double getCenterLongitude() {
        return centerLongitude;
    }
    
    public double getCenterLatitude() {
        return centerLatitude;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getLength() {
        return length;
    }
}
