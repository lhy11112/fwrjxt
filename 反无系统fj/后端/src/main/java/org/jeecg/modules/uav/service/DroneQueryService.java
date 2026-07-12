package org.jeecg.modules.uav.service;

import org.jeecg.modules.uav.entity.Airspace;
import org.jeecg.modules.uav.entity.Drone;

import java.util.ArrayList;
import java.util.List;

public class DroneQueryService {
    private List<Drone> drones;
    
    public DroneQueryService(List<Drone> drones) {
        this.drones = drones;
    }
    
    // 查询指定空域内的所有无人机
    public List<Drone> queryByAirspace(Airspace airspace) {
        List<Drone> result = new ArrayList<>();
        
        for (Drone drone : drones) {
            if (airspace.contains(drone)) {
                result.add(drone);
            }
        }
        
        return result;
    }
}
