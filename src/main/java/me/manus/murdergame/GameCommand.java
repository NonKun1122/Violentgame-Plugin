package me.manus.murdergame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommand implements CommandExecutor {
    private final ArenaManager arenaManager;
    private final SetupManager setupManager;

    public GameCommand(ArenaManager arenaManager, SetupManager setupManager) {
        this.arenaManager = arenaManager;
        this.setupManager = setupManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // --- Console fallback ---
        if (!(sender instanceof Player)) {
            sender.sendMessage("[Violent] Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        // /volg  (no args) -> open room GUI
        if (args.length == 0) {
            new RoomUI(arenaManager).openRoomSelector(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /volg help
        if (sub.equals("help")) {
            sendHelp(player);
            return true;
        }

        // /volg reload
        if (sub.equals("reload")) {
            if (!player.hasPermission("violent.admin")) {
                player.sendMessage("§cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
                return true;
            }
            MurderGame.getInstance().reloadConfig();
            MurderGame.getInstance().reloadMapsFromConfig();
            player.sendMessage("§a[Violent] รีโหลด config เรียบร้อยแล้ว!");
            return true;
        }

        // /volg create <roomName>
        if (sub.equals("create") && args.length > 1) {
            GameRoom room = arenaManager.createRoom(args[1]);
            if (room != null) {
                player.sendMessage("§aสร้างห้องเรียบร้อย: " + args[1]);
            } else {
                player.sendMessage("§cไม่สามารถสร้างห้องได้ (อาจจะไม่มีแผนที่ว่าง)");
            }
            return true;
        }

        // /volg join [roomName]
        if (sub.equals("join")) {
            if (args.length > 1) {
                GameRoom room = arenaManager.getRoom(args[1]);
                if (room != null) {
                    if (room.isFull()) {
                        player.sendMessage("§cห้องนี้เต็มแล้ว!");
                    } else {
                        room.addPlayer(player.getUniqueId());
                        player.sendMessage("§aเข้าร่วมห้อง: " + args[1]);
                    }
                } else {
                    player.sendMessage("§cไม่พบห้องที่ระบุ");
                }
            } else {
                new RoomUI(arenaManager).openRoomSelector(player);
            }
            return true;
        }

        // /volg start
        if (sub.equals("start")) {
            GameRoom room = arenaManager.getPlayerRoom(player);
            if (room != null) {
                room.startGame();
            } else {
                player.sendMessage("§cคุณไม่ได้อยู่ในห้องเกม!");
            }
            return true;
        }

        // /volg setlobby <roomName>
        if (sub.equals("setlobby")) {
            if (!player.hasPermission("violent.admin")) {
                player.sendMessage("§cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cการใช้งาน: /volg setlobby <ชื่อห้อง>");
                return true;
            }
            String roomName = args[1];
            GameRoom room = arenaManager.getRoom(roomName);
            if (room == null) {
                player.sendMessage("§cไม่พบห้อง: " + roomName);
                return true;
            }
            setupManager.startLobbySession(player, roomName);
            return true;
        }

        // /volg setspawn <roomName>
        if (sub.equals("setspawn")) {
            if (!player.hasPermission("violent.admin")) {
                player.sendMessage("§cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cการใช้งาน: /volg setspawn <ชื่อห้อง>");
                return true;
            }
            String roomName = args[1];
            GameRoom room = arenaManager.getRoom(roomName);
            if (room == null) {
                player.sendMessage("§cไม่พบห้อง: " + roomName);
                return true;
            }
            // Open the spawn-setup GUI first, then admin can use buttons to get wands
            SpawnSetupUI.open(player, room);
            return true;
        }

        // /volg setexit <1|2>
        if (sub.equals("setexit")) {
            GameRoom room = arenaManager.getPlayerRoom(player);
            if (room != null) {
                if (args.length > 1) {
                    if (args[1].equals("1")) {
                        room.setExit1(player.getLocation());
                        player.sendMessage("§aตั้งจุดทางออกที่ 1 เรียบร้อยแล้ว!");
                    } else if (args[1].equals("2")) {
                        room.setExit2(player.getLocation());
                        player.sendMessage("§aตั้งจุดทางออกที่ 2 เรียบร้อยแล้ว!");
                    } else {
                        player.sendMessage("§cกรุณาระบุจุดทางออก (1 หรือ 2) เช่น /volg setexit 1");
                    }
                } else {
                    player.sendMessage("§cกรุณาระบุจุดทางออก (1 หรือ 2) เช่น /volg setexit 1");
                }
            } else {
                player.sendMessage("§cคุณไม่ได้อยู่ในห้องเกม!");
            }
            return true;
        }

        // /volg setsize <size>
        if (sub.equals("setsize")) {
            GameRoom room = arenaManager.getPlayerRoom(player);
            if (room != null) {
                if (args.length > 1) {
                    try {
                        double size = Double.parseDouble(args[1]);
                        room.setMapSize(size);
                        player.sendMessage("§aตั้งขนาดแผนที่ (WorldBorder) เป็น: " + size + " บล็อก");
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cกรุณาระบุตัวเลขขนาดแผนที่ เช่น /volg setsize 50");
                    }
                } else {
                    player.sendMessage("§cกรุณาระบุขนาดแผนที่ เช่น /volg setsize 50");
                }
            } else {
                player.sendMessage("§cคุณต้องอยู่ในห้องเพื่อตั้งค่าขนาดแผนที่!");
            }
            return true;
        }

        // /volg give wrench
        if (sub.equals("give") && args.length > 1 && args[1].equalsIgnoreCase("wrench")) {
            if (player.hasPermission("violent.admin")) {
                player.getInventory().addItem(getWrenchChest());
                player.sendMessage("§aได้รับกล่องอุปกรณ์ซ่อมไฟ (Wrench Chest) เรียบร้อยแล้ว!");
            } else {
                player.sendMessage("§cคุณไม่มีสิทธิ์ใช้คำสั่งนี้!");
            }
            return true;
        }

        // Default
        sendHelp(player);
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§l=== Violent Help ===");
        player.sendMessage("§e/volg §7- เปิดเมนูเลือกห้อง");
        player.sendMessage("§e/volg help §7- แสดงคำสั่งทั้งหมด");
        player.sendMessage("§e/volg reload §7- รีโหลด config");
        player.sendMessage("§e/volg create <ชื่อห้อง> §7- สร้างห้องใหม่");
        player.sendMessage("§e/volg join <ชื่อห้อง> §7- เข้าร่วมห้อง");
        player.sendMessage("§e/volg start §7- เริ่มเกม (สำหรับเจ้าของห้อง/แอดมิน)");
        player.sendMessage("§e/volg setlobby <ชื่อห้อง> §7- ตั้งจุดเข้า Lobby (คลิกบล็อก)");
        player.sendMessage("§e/volg setspawn <ชื่อห้อง> §7- เปิด GUI ตั้งจุดเกิด");
        player.sendMessage("§e/volg setexit <1|2> §7- ตั้งจุดทางออก");
        player.sendMessage("§e/volg setsize <ขนาด> §7- ตั้งขนาดแผนที่ (WorldBorder)");
        player.sendMessage("§e/volg give wrench §7- รับอุปกรณ์ซ่อมไฟ");
        player.sendMessage("§6§l=======================");
    }

    private org.bukkit.inventory.ItemStack getWrenchChest() {
        org.bukkit.inventory.ItemStack chest = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CHEST);
        org.bukkit.inventory.meta.BlockStateMeta meta = (org.bukkit.inventory.meta.BlockStateMeta) chest.getItemMeta();
        org.bukkit.block.Chest chestState = (org.bukkit.block.Chest) meta.getBlockState();

        org.bukkit.inventory.ItemStack wrench = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SHEARS);
        org.bukkit.inventory.meta.ItemMeta wrenchMeta = wrench.getItemMeta();
        wrenchMeta.setDisplayName("§eWrench");
        wrench.setItemMeta(wrenchMeta);

        chestState.getInventory().addItem(wrench);
        meta.setBlockState(chestState);
        meta.setDisplayName("§6Wrench Kit");
        chest.setItemMeta(meta);
        return chest;
    }
}
