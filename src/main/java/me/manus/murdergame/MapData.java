package me.manus.murdergame;

import org.bukkit.Location;
import java.util.List;

public class MapData {
    private String mapName;
    private Location lobby;
    private List<Location> spawns;
    private Location exit;
    private double size;

    public MapData(String mapName, Location lobby, List<Location> spawns, Location exit, double size) {
        this.mapName = mapName;
        this.lobby = lobby;
        this.spawns = spawns;
        this.exit = exit;
        this.size = size;
    }

    public String getMapName() { return mapName; }
    public Location getLobby() { return lobby; }
    public List<Location> getSpawns() { return spawns; }
    public Location getExit() { return exit; }
    public double getSize() { return size; }
}
