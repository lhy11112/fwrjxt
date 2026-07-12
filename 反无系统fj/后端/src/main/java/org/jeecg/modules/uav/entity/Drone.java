package org.jeecg.modules.uav.entity;

public class Drone {
    private String id;
    private double longitude;  // 经度
    private double latitude;   // 纬度
    private double altitude;   // 高度（米）
    
    public Drone(String id, double longitude, double latitude) {
        this(id, longitude, latitude, 0);
    }
    
    public Drone(String id, double longitude, double latitude, double altitude) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getAltitude() {
        return altitude;
    }
    
    @Override
    public String toString() {
        return String.format("Drone{id='%s', 经度=%.1f, 纬度=%.1f, 高度=%.1f米}", 
                            id, longitude, latitude, altitude);
    }
}
