package me.manus.murdergame;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for managing spawn points (player spawns + murderer spawn).
 * Title: "§0SpawnSetup:<roomName>"
 *
 * Layout (27 slots):
 *   Slot 0-9  : Player spawn slots  (max 10)
 *   Slot 13   : Add Player Spawn button
 *   Slot 22   : Murderer Spawn button  (1 slot, slot 18-26 area)
 *   Slot 26   : Done / Close
 */
public class SpawnSetupUI {

    public static final String TITLE_PREFIX = "§0SpawnSetup:";

    public static void open(Player player, GameRoom room) {
        String title = TITLE_PREFIX + room.getName();
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // --- Player spawn slots (index 0-9) ---
        List<Location> spawns = room.getSpawnPoints();
        int playerSpawnCount = 0;
        for (Location loc : spawns) {
            if (loc.hasMetadata("murderer")) continue; // skip murderer slot marker (we use tag below)
            if (playerSpawnCount < 10) {
                inv.setItem(playerSpawnCount, makeSpawnIcon(playerSpawnCount + 1, loc, false));
                playerSpawnCount++;
            }
        }

        // "Add Player Spawn" button — slot 13
        ItemStack addPlayerSpawn = new ItemStack(playerSpawnCount < 10 ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta apm = addPlayerSpawn.getItemMeta();
        apm.setDisplayName(playerSpawnCount < 10 ? "§a§l+ เพิ่มจุดเกิดผู้เล่น §7(" + playerSpawnCount + "/10)" : "§c§lเต็มแล้ว (10/10)");
        List<String> aplore = new ArrayList<>();
        aplore.add("§7ใช้ Spawn Wand คลิกขวาที่บล็อก");
        aplore.add("§7เพื่อเพิ่มจุดเกิดผู้เล่น");
        apm.setLore(aplore);
        addPlayerSpawn.setItemMeta(apm);
        inv.setItem(13, addPlayerSpawn);

        // "Murderer Spawn" button — slot 22
        Location murdererSpawn = room.getMurdererSpawn();
        ItemStack murdBtn = new ItemStack(murdererSpawn != null ? Material.RED_CONCRETE : Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta mm = murdBtn.getItemMeta();
        mm.setDisplayName("§c§l⚔ จุดเกิดฆาตกร");
        List<String> mlore = new ArrayList<>();
        if (murdererSpawn != null) {
            mlore.add("§7ตั้งค่าแล้ว ✔");
            mlore.add(String.format("§8X:%.1f Y:%.1f Z:%.1f", murdererSpawn.getX(), murdererSpawn.getY(), murdererSpawn.getZ()));
            mlore.add("§eคลิกเพื่อตั้งใหม่");
        } else {
            mlore.add("§cยังไม่ได้ตั้งค่า");
            mlore.add("§eคลิกเพื่อเปิด Wand ตั้งจุดเกิดฆาตกร");
        }
        mm.setLore(mlore);
        murdBtn.setItemMeta(mm);
        inv.setItem(22, murdBtn);

        // "Done" button — slot 26
        ItemStack done = new ItemStack(Material.NETHER_STAR);
        ItemMeta dm = done.getItemMeta();
        dm.setDisplayName("§6§l✔ Done");
        dm.setLore(List.of("§7ปิด GUI นี้"));
        done.setItemMeta(dm);
        inv.setItem(26, done);

        player.openInventory(inv);
    }

    // ---------------------------------------------------------------

    private static ItemStack makeSpawnIcon(int number, Location loc, boolean isMurderer) {
        ItemStack icon = new ItemStack(isMurderer ? Material.RED_WOOL : Material.LIME_WOOL);
        ItemMeta meta = icon.getItemMeta();
        meta.setDisplayName((isMurderer ? "§c⚔ Murderer Spawn" : "§aPlayer Spawn #" + number));
        List<String> lore = new ArrayList<>();
        lore.add(String.format("§8X:%.1f Y:%.1f Z:%.1f", loc.getX(), loc.getY(), loc.getZ()));
        lore.add("§cคลิกเพื่อลบจุดนี้");
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }
}
