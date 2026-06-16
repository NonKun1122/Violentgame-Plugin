package me.manus.murdergame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapManager {
    private List<MapData> availableMaps;

    public MapManager() {
        this.availableMaps = new ArrayList<>();
    }

    public void addMap(MapData map) {
        availableMaps.add(map);
    }

    public MapData getRandomMap(List<String> activeMapNames) {
        if (availableMaps.isEmpty()) return null;
        
        List<MapData> pool = new ArrayList<>();
        for (MapData map : availableMaps) {
            if (!activeMapNames.contains(map.getMapName())) {
                pool.add(map);
            }
        }
        
        // If all maps are used, just pick any random one
        if (pool.isEmpty()) pool = availableMaps;
        
        return pool.get(new Random().nextInt(pool.size()));
    }

    public List<MapData> getAvailableMaps() {
        return availableMaps;
    }
}
