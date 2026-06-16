package me.manus.murdergame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.UUID;

public class LightSystem {
    private boolean lightsOn = true;
    private GameRoom room;

    public LightSystem(GameRoom room) {
        this.room = room;
    }

    public void sabotageLights() {
        lightsOn = false;
        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && room.getRoleManager().getRole(uuid) != PlayerRole.Role.MURDERER) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
            }
        }
    }

    public void fixLights() {
        lightsOn = true;
        for (UUID uuid : room.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
    }

    public static ItemStack getRepairTool() {
        ItemStack tool = new ItemStack(Material.SHEARS);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName("§eWrench");
        tool.setItemMeta(meta);
        return tool;
    }

    public boolean isLightsOn() { return lightsOn; }
}
