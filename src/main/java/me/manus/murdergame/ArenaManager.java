package me.manus.murdergame;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaManager {
    private List<GameRoom> rooms;
    private MapManager mapManager;

    public ArenaManager(MapManager mapManager) {
        this.rooms = new ArrayList<>();
        this.mapManager = mapManager;
    }

    public GameRoom createRoom(String name) {
        List<String> activeMaps = rooms.stream()
                .map(r -> r.getMapName())
                .filter(n -> n != null)
                .toList();
                
        MapData map = mapManager.getRandomMap(activeMaps);
        if (map == null) return null;
        
        GameRoom room = new GameRoom(name);
        room.setMapName(map.getMapName());
        room.setLobbyLocation(map.getLobby());
        room.getSpawnPoints().addAll(map.getSpawns());
        room.setExit1(map.getExit());
        room.setExit2(map.getExit());
        room.setMapSize(map.getSize());
        
        rooms.add(room);
        return room;
    }

    public MapManager getMapManager() { return mapManager; }

    public GameRoom getRoom(String name) {
        return rooms.stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public GameRoom getPlayerRoom(Player player) {
        return rooms.stream().filter(r -> r.getPlayers().contains(player.getUniqueId())).findFirst().orElse(null);
    }

    public List<GameRoom> getRooms() {
        return rooms;
    }
}
