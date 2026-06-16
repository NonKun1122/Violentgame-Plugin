package me.manus.murdergame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameRoom {
    private String name;
    private String mapName;
    private List<UUID> players;
    private GameState state;
    private Location lobbyLocation;
    private Location lobbyBlock;          // block the admin clicked for lobby entrance
    private List<Location> spawnPoints;   // player spawn points (max 10)
    private Location murdererSpawn;       // 1 murderer spawn point
    private PlayerRole roleManager;
    private LightSystem lightSystem;
    private Location exit1;
    private Location exit2;
    private boolean exitOpen = false;
    private double mapSize;
    private int timeLeft;
    private org.bukkit.scheduler.BukkitTask gameTask;

    public enum GameState {
        WAITING, STARTING, IN_GAME, ENDING
    }

    public GameRoom(String name) {
        this.name = name;
        this.players = new ArrayList<>();
        this.state = GameState.WAITING;
        this.spawnPoints = new ArrayList<>();
        this.roleManager = new PlayerRole();
        this.lightSystem = new LightSystem(this);
    }

    // --- Lobby block (where players click to enter lobby) ---
    public Location getLobbyBlock() { return lobbyBlock; }
    public void setLobbyBlock(Location loc) { this.lobbyBlock = loc; }

    // --- Murderer spawn ---
    public Location getMurdererSpawn() { return murdererSpawn; }
    public void setMurdererSpawn(Location loc) { this.murdererSpawn = loc; }

    // --- Exits ---
    public LightSystem getLightSystem() { return lightSystem; }
    public Location getExit1() { return exit1; }
    public void setExit1(Location loc) { this.exit1 = loc; }
    public Location getExit2() { return exit2; }
    public void setExit2(Location loc) { this.exit2 = loc; }
    public boolean isExitOpen() { return exitOpen; }
    public void setExitOpen(boolean open) { this.exitOpen = open; }

    public void setMapSize(double size) { this.mapSize = size; }
    public double getMapSize() { return mapSize; }

    public void startGame() {
        if (players.size() < 2) {
            Bukkit.broadcastMessage("§cต้องการผู้เล่นอย่างน้อย 2 คนเพื่อเริ่มเกม!");
            return;
        }
        if (exit1 == null || exit2 == null) {
            Bukkit.broadcastMessage("§cไม่สามารถเริ่มเกมได้ เนื่องจากยังไม่ได้ตั้งจุดทางออกครบ 2 จุด!");
            return;
        }

        if (mapSize > 0 && !spawnPoints.isEmpty()) {
            Location center = spawnPoints.get(0);
            org.bukkit.WorldBorder border = center.getWorld().getWorldBorder();
            border.setCenter(center);
            border.setSize(mapSize);
            border.setDamageAmount(0.2);
            border.setWarningDistance(5);
        }

        setState(GameState.IN_GAME);
        assignRoles();
        teleportPlayersToSpawn();
        this.timeLeft = 300;

        gameTask = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    endGame("§cหมดเวลา! ฆาตกรชนะ!");
                    this.cancel();
                    return;
                }
                for (UUID uuid : players) {
                    org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        GameScoreboard.updateScoreboard(p, GameRoom.this, timeLeft);
                    }
                }
                timeLeft--;
                checkWinCondition();
            }
        }.runTaskTimer(MurderGame.getInstance(), 0L, 20L);

        Bukkit.broadcastMessage("§aเกมเริ่มแล้ว! หาตัวฆาตกรและซ่อมไฟเพื่อหาทางออก!");
    }

    public void checkWinCondition() {
        int survivors = 0;
        int murderers = 0;
        for (UUID uuid : players) {
            if (roleManager.getRole(uuid) == PlayerRole.Role.MURDERER) murderers++;
            else survivors++;
        }
        if (murderers == 0) endGame("§aผู้รอดชีวิตชนะ!");
        else if (survivors == 0) endGame("§cฆาตกรชนะ!");
    }

    public void endGame(String message) {
        if (gameTask != null) gameTask.cancel();
        setState(GameState.ENDING);
        Bukkit.broadcastMessage(message);

        if (!spawnPoints.isEmpty()) {
            org.bukkit.WorldBorder border = spawnPoints.get(0).getWorld().getWorldBorder();
            border.reset();
        }
        for (UUID uuid : players) {
            org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    public void assignRoles() {
        if (players.isEmpty()) return;
        java.util.Collections.shuffle(players);
        UUID murderer = players.get(0);
        roleManager.setRole(murderer, PlayerRole.Role.MURDERER);
        for (int i = 1; i < players.size(); i++) {
            if (i == 1) roleManager.setRole(players.get(i), PlayerRole.Role.ENGINEER);
            else roleManager.setRole(players.get(i), PlayerRole.Role.SURVIVOR);
        }
    }

    public PlayerRole getRoleManager() { return roleManager; }

    public void addPlayer(UUID uuid) {
        if (players.size() < 6) players.add(uuid);
    }

    public boolean isFull() { return players.size() >= 6; }
    public void removePlayer(UUID uuid) { players.remove(uuid); }

    public String getName() { return name; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public List<UUID> getPlayers() { return players; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Location getLobbyLocation() { return lobbyLocation; }
    public void setLobbyLocation(Location loc) { this.lobbyLocation = loc; }
    public List<Location> getSpawnPoints() { return spawnPoints; }

    public void teleportPlayersToSpawn() {
        if (spawnPoints.isEmpty()) return;
        java.util.Random rand = new java.util.Random();
        for (UUID uuid : players) {
            org.bukkit.entity.Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            // Murderer gets their own spawn if set
            if (roleManager.getRole(uuid) == PlayerRole.Role.MURDERER && murdererSpawn != null) {
                p.teleport(murdererSpawn);
            } else {
                Location loc = spawnPoints.get(rand.nextInt(spawnPoints.size()));
                p.teleport(loc);
            }
        }
    }
}
