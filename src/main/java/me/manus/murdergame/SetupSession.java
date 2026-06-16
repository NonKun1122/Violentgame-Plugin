package me.manus.murdergame;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Tracks an admin's active setup session (setlobby / setspawn).
 */
public class SetupSession {
    public enum Mode { SETLOBBY, SETSPAWN }

    private final UUID playerUUID;
    private final String roomName;
    private final Mode mode;
    /** Item given to the admin so they can right-click a block. */
    private org.bukkit.inventory.ItemStack wand;

    public SetupSession(UUID playerUUID, String roomName, Mode mode) {
        this.playerUUID = playerUUID;
        this.roomName = roomName;
        this.mode = mode;
    }

    public UUID getPlayerUUID()  { return playerUUID; }
    public String getRoomName()  { return roomName; }
    public Mode getMode()        { return mode; }
    public org.bukkit.inventory.ItemStack getWand() { return wand; }
    public void setWand(org.bukkit.inventory.ItemStack wand) { this.wand = wand; }
}
