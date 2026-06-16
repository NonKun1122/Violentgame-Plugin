package me.manus.murdergame;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages active admin setup sessions (setlobby / setspawn wand).
 */
public class SetupManager {

    private final Map<UUID, SetupSession> sessions = new HashMap<>();

    public static final String LOBBY_WAND_NAME  = "§b§lLobby Wand §7(Right-click block)";
    public static final String SPAWN_WAND_NAME  = "§a§lSpawn Wand §7(Right-click block)";
    public static final String MURD_WAND_NAME   = "§c§l⚔ Murderer Spawn Wand §7(Right-click block)";

    // Expose the map so RoomListener can manually insert murderer sessions
    public Map<UUID, SetupSession> getActiveSessions() { return sessions; }

    // ---------------------------------------------------------------

    public void startLobbySession(Player player, String roomName) {
        SetupSession session = new SetupSession(player.getUniqueId(), roomName, SetupSession.Mode.SETLOBBY);
        ItemStack wand = createWand(Material.BLAZE_ROD, LOBBY_WAND_NAME, roomName,
                "§7Right-click a block to set the",
                "§7Lobby entrance for room §e" + roomName);
        session.setWand(wand);
        sessions.put(player.getUniqueId(), session);
        giveWand(player, wand);
        player.sendMessage("§b[Setup] §fได้รับ §bLobby Wand §fแล้ว! คลิกขวาที่บล็อกที่ต้องการให้เป็นทางเข้า Lobby ของห้อง §e" + roomName);
    }

    public void startSpawnSession(Player player, String roomName) {
        SetupSession session = new SetupSession(player.getUniqueId(), roomName, SetupSession.Mode.SETSPAWN);
        ItemStack wand = createWand(Material.BLAZE_ROD, SPAWN_WAND_NAME, roomName,
                "§7Right-click a block to add",
                "§7a spawn point for room §e" + roomName);
        session.setWand(wand);
        sessions.put(player.getUniqueId(), session);
        giveWand(player, wand);
        player.sendMessage("§a[Setup] §fได้รับ §aSpawn Wand §fแล้ว! คลิกขวาที่บล็อกเพื่อเพิ่มจุดเกิดสำหรับห้อง §e" + roomName);
    }

    public SetupSession getSession(UUID uuid) { return sessions.get(uuid); }
    public boolean hasSession(UUID uuid)       { return sessions.containsKey(uuid); }
    public void endSession(UUID uuid)          { sessions.remove(uuid); }

    // ---------------------------------------------------------------

    private ItemStack createWand(Material mat, String displayName, String roomName, String... loreLines) {
        ItemStack wand = new ItemStack(mat);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();
        lore.add("§7Room: §e" + roomName);
        lore.addAll(Arrays.asList(loreLines));
        meta.setLore(lore);
        wand.setItemMeta(meta);
        return wand;
    }

    public void giveWand(Player player, ItemStack wand) {
        if (player.getInventory().getItem(8) == null) {
            player.getInventory().setItem(8, wand);
            player.getInventory().setHeldItemSlot(8);
        } else {
            player.getInventory().addItem(wand);
        }
    }

    public void removeWand(Player player, SetupSession session) {
        if (session.getWand() == null) return;
        player.getInventory().remove(session.getWand());
    }
}
