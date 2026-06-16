package me.manus.murdergame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class RoomUI {
    private ArenaManager arenaManager;

    public RoomUI(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    public void openRoomSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§0Select a Game Room");

        // Create Room Button
        ItemStack createItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta createMeta = createItem.getItemMeta();
        createMeta.setDisplayName("§b§l[ Create Custom Room ]");
        List<String> createLore = new ArrayList<>();
        createLore.add("§7Click to create your own room");
        createLore.add("§7with a random map!");
        createMeta.setLore(createLore);
        createItem.setItemMeta(createMeta);
        inv.setItem(26, createItem);

        for (GameRoom room : arenaManager.getRooms()) {
            ItemStack item = new ItemStack(room.isFull() ? Material.RED_CONCRETE : Material.GREEN_CONCRETE);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§eRoom: " + room.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Players: §f" + room.getPlayers().size() + "/6");
            lore.add("§7Status: §f" + room.getState().toString());
            if (room.isFull()) {
                lore.add("§cRoom is Full!");
            } else {
                lore.add("§aClick to Join!");
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        player.openInventory(inv);
    }
}
