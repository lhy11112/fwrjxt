package org.jeecg.modules.uav.service;

import org.jeecg.modules.uav.entity.Airspace;

import java.util.List;

public class PolygonAirspace extends Airspace {
    // 内部类：表示多边形的顶点
    public static class Point {
        private double longitude;  // 经度
        private double latitude;   // 纬度
        
        public Point(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public double getLatitude() {
            return latitude;
        }
    }
    
    private List<Point> vertices;  // 多边形顶点列表
    
    public PolygonAirspace(String name, List<Point> vertices, 
                          double minAltitude, double maxAltitude) {
        super(name, minAltitude, maxAltitude);
        this.vertices = vertices;
    }
    
    @Override
    protected boolean isPointInArea(double longitude, double latitude) {
        // 使用射线法判断点是否在多边形内
        int n = vertices.size();
        boolean inside = false;
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point vi = vertices.get(i);
            Point vj = vertices.get(j);
            
            // 检查点是否在边的端点上
            if ((vi.getLongitude() == longitude && vi.getLatitude() == latitude) ||
                (vj.getLongitude() == longitude && vj.getLatitude() == latitude)) {
                return true;
            }
            
            // 检查射线是否与边相交
            if (((vi.getLatitude() > latitude) != (vj.getLatitude() > latitude)) &&
                (longitude < (vj.getLongitude() - vi.getLongitude()) * (latitude - vi.getLatitude()) / 
                (vj.getLatitude() - vi.getLatitude()) + vi.getLongitude())) {
                inside = !inside;
            }
        }
        
        return inside;
    }
    
    public List<Point> getVertices() {
        return vertices;
    }
}
