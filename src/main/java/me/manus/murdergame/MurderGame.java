package me.manus.murdergame;

import org.bukkit.plugin.java.JavaPlugin;

public class MurderGame extends JavaPlugin {
    private static MurderGame instance;

    private ArenaManager arenaManager;
    private MapManager mapManager;
    private SetupManager setupManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.mapManager = new MapManager();
        reloadMapsFromConfig();
        this.arenaManager = new ArenaManager(mapManager);
        this.setupManager = new SetupManager();

        getCommand("volg").setExecutor(new GameCommand(arenaManager, setupManager));
        getServer().getPluginManager().registerEvents(new RoomListener(arenaManager, setupManager), this);
        getLogger().info("Violent Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Violent Plugin has been disabled!");
    }

    /** Called on startup and on /volg reload */
    public void reloadMapsFromConfig() {
        reloadConfig();
        if (mapManager != null) mapManager.getAvailableMaps().clear();
        if (!getConfig().contains("maps")) return;
        for (String key : getConfig().getConfigurationSection("maps").getKeys(false)) {
            String path = "maps." + key;
            org.bukkit.Location lobby = parseLocation(getConfig().getString(path + ".lobby"));
            org.bukkit.Location exit  = parseLocation(getConfig().getString(path + ".exit"));
            double size = getConfig().getDouble(path + ".size", 0);

            java.util.List<org.bukkit.Location> spawns = new java.util.ArrayList<>();
            if (getConfig().contains(path + ".spawns")) {
                for (String spawnStr : getConfig().getStringList(path + ".spawns")) {
                    org.bukkit.Location spawn = parseLocation(spawnStr);
                    if (spawn != null) spawns.add(spawn);
                }
            }
            mapManager.addMap(new MapData(key, lobby, spawns, exit, size));
            getLogger().info("Loaded map: " + key + " with size: " + size);
        }
    }

    private org.bukkit.Location parseLocation(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(",");
        if (parts.length < 4) return null;
        try {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new org.bukkit.Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    public static MurderGame getInstance() { return instance; }
    public SetupManager getSetupManager()  { return setupManager; }
}
