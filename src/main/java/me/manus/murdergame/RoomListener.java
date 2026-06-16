package me.manus.murdergame;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RoomListener implements Listener {
    private final ArenaManager arenaManager;
    private final SetupManager setupManager;

    public RoomListener(ArenaManager arenaManager, SetupManager setupManager) {
        this.arenaManager = arenaManager;
        this.setupManager = setupManager;
    }

    // ---------------------------------------------------------------
    // Wand right-click: setlobby / setspawn / murderer spawn
    // ---------------------------------------------------------------
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!setupManager.hasSession(player.getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || !held.hasItemMeta()) return;
        String heldName = held.getItemMeta().getDisplayName();

        boolean isLobbyWand  = heldName.equals(SetupManager.LOBBY_WAND_NAME);
        boolean isSpawnWand  = heldName.equals(SetupManager.SPAWN_WAND_NAME);
        boolean isMurdWand   = heldName.equals(SetupManager.MURD_WAND_NAME);
        if (!isLobbyWand && !isSpawnWand && !isMurdWand) return;

        event.setCancelled(true);

        SetupSession session = setupManager.getSession(player.getUniqueId());
        GameRoom room = arenaManager.getRoom(session.getRoomName());
        if (room == null) {
            player.sendMessage("§cห้องไม่พบแล้ว! ยกเลิก setup session");
            setupManager.endSession(player.getUniqueId());
            return;
        }

        Location clickedLoc = event.getClickedBlock().getLocation().clone().add(0.5, 1, 0.5);

        if (isLobbyWand) {
            room.setLobbyBlock(event.getClickedBlock().getLocation());
            room.setLobbyLocation(clickedLoc);
            player.sendMessage("§b[Setup] §fตั้งจุดเข้า Lobby สำหรับห้อง §e" + session.getRoomName() + " §fเรียบร้อยแล้ว!");
            setupManager.removeWand(player, session);
            setupManager.endSession(player.getUniqueId());

        } else if (isSpawnWand) {
            if (room.getSpawnPoints().size() >= 10) {
                player.sendMessage("§cจุดเกิดผู้เล่นเต็มแล้ว! (10/10)");
                return;
            }
            room.getSpawnPoints().add(clickedLoc);
            player.sendMessage("§a[Setup] §fเพิ่มจุดเกิดผู้เล่น #" + room.getSpawnPoints().size()
                    + " สำหรับห้อง §e" + session.getRoomName());
            setupManager.removeWand(player, session);
            setupManager.endSession(player.getUniqueId());
            SpawnSetupUI.open(player, room);

        } else { // isMurdWand
            room.setMurdererSpawn(clickedLoc);
            player.sendMessage("§c[Setup] §fตั้งจุดเกิดฆาตกรสำหรับห้อง §e" + session.getRoomName() + " §fเรียบร้อยแล้ว!");
            setupManager.removeWand(player, session);
            setupManager.endSession(player.getUniqueId());
            SpawnSetupUI.open(player, room);
        }
    }

    // ---------------------------------------------------------------
    // SpawnSetup GUI + Room Selector GUI
    // ---------------------------------------------------------------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // --- SpawnSetup GUI ---
        if (title.startsWith(SpawnSetupUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            String roomName = title.substring(SpawnSetupUI.TITLE_PREFIX.length());
            GameRoom room = arenaManager.getRoom(roomName);
            if (room == null) return;

            int slot = event.getRawSlot();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            // Slot 13 = Add Player Spawn
            if (slot == 13) {
                if (room.getSpawnPoints().size() >= 10) {
                    player.sendMessage("§cจุดเกิดผู้เล่นเต็มแล้ว (10/10)!");
                    return;
                }
                player.closeInventory();
                setupManager.startSpawnSession(player, roomName);
                return;
            }

            // Slot 22 = Murderer Spawn
            if (slot == 22) {
                player.closeInventory();
                // Give murderer wand directly
                ItemStack wand = makeMurdererWand(roomName);
                SetupSession sess = new SetupSession(player.getUniqueId(), roomName, SetupSession.Mode.SETSPAWN);
                sess.setWand(wand);
                setupManager.getActiveSessions().put(player.getUniqueId(), sess);
                setupManager.giveWand(player, wand);
                player.sendMessage("§c[Setup] §fได้รับ §c⚔ Murderer Spawn Wand §fแล้ว! คลิกขวาที่บล็อกเพื่อตั้งจุดเกิดฆาตกรสำหรับห้อง §e" + roomName);
                return;
            }

            // Slot 26 = Done
            if (slot == 26) {
                player.closeInventory();
                return;
            }

            // Slots 0-9 = player spawn icons -> remove on click
            if (slot >= 0 && slot < 10 && slot < room.getSpawnPoints().size()) {
                room.getSpawnPoints().remove(slot);
                player.sendMessage("§c[Setup] ลบจุดเกิดผู้เล่น #" + (slot + 1) + " แล้ว");
                SpawnSetupUI.open(player, room);
            }
            return;
        }

        // --- Room Selector GUI ---
        if (title.equals("§0Select a Game Room")) {
            event.setCancelled(true);
            ItemStack item = event.getCurrentItem();
            if (item == null || !item.hasItemMeta()) return;

            String displayName = item.getItemMeta().getDisplayName();
            Player player = (Player) event.getWhoClicked();

            if (displayName.equals("§b§l[ Create Custom Room ]")) {
                String randomName = "Room-" + (arenaManager.getRooms().size() + 1);
                GameRoom newRoom = arenaManager.createRoom(randomName);
                if (newRoom != null) {
                    newRoom.addPlayer(player.getUniqueId());
                    player.sendMessage("§aสร้างและเข้าร่วมห้องเรียบร้อย: " + randomName);
                    player.closeInventory();
                } else {
                    player.sendMessage("§cไม่สามารถสร้างห้องได้ (อาจจะไม่มีแผนที่ว่าง)");
                }
                return;
            }

            String roomName = displayName.replace("§eRoom: ", "");
            GameRoom room = arenaManager.getRoom(roomName);
            if (room != null) {
                if (room.isFull()) {
                    player.sendMessage("§cห้องนี้เต็มแล้ว!");
                } else {
                    room.addPlayer(player.getUniqueId());
                    player.sendMessage("§aคุณได้เข้าร่วมห้อง " + roomName + " แล้ว!");
                    player.closeInventory();
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Step on lobby block -> join room and teleport
    // ---------------------------------------------------------------
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only trigger when the block changes (not every micro-movement)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        Location standing = event.getTo().clone().subtract(0, 1, 0);

        for (GameRoom room : arenaManager.getRooms()) {
            Location lb = room.getLobbyBlock();
            if (lb == null) continue;
            if (lb.getBlockX() == standing.getBlockX()
                    && lb.getBlockY() == standing.getBlockY()
                    && lb.getBlockZ() == standing.getBlockZ()
                    && lb.getWorld().equals(standing.getWorld())) {

                if (room.getPlayers().contains(player.getUniqueId())) return;
                if (room.isFull()) {
                    player.sendMessage("§cห้องนี้เต็มแล้ว!");
                    return;
                }
                room.addPlayer(player.getUniqueId());
                if (room.getLobbyLocation() != null) {
                    player.teleport(room.getLobbyLocation());
                }
                player.sendMessage("§aคุณเข้าร่วมห้อง §e" + room.getName() + " §fผ่านทาง Lobby แล้ว!");
                break;
            }
        }
    }

    // ---------------------------------------------------------------
    private ItemStack makeMurdererWand(String roomName) {
        org.bukkit.inventory.ItemStack wand = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_ROD);
        org.bukkit.inventory.meta.ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(SetupManager.MURD_WAND_NAME);
        meta.setLore(List.of("§7Room: §e" + roomName, "§7คลิกขวาที่บล็อกเพื่อตั้งจุดเกิดฆาตกร"));
        wand.setItemMeta(meta);
        return wand;
    }
}
