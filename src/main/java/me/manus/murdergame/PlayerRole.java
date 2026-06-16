package me.manus.murdergame;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerRole {
    public enum Role {
        MURDERER, SURVIVOR, ENGINEER, MEDIC
    }

    private Map<UUID, Role> playerRoles = new HashMap<>();

    public void setRole(UUID uuid, Role role) {
        playerRoles.put(uuid, role);
    }

    public Role getRole(UUID uuid) {
        return playerRoles.getOrDefault(uuid, Role.SURVIVOR);
    }

    public void clearRoles() {
        playerRoles.clear();
    }
}
