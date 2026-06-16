package me.manus.murdergame;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GameScoreboard {
    public static void updateScoreboard(Player player, GameRoom room, int timeLeft) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("murdergame", "dummy", "§6§lMURDER GAME");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score s1 = obj.getScore("§7----------------");
        s1.setScore(7);

        Score s2 = obj.getScore("§fMap: §a" + (room.getMapName() != null ? room.getMapName() : "Random"));
        s2.setScore(6);

        Score s3 = obj.getScore("§fPlayers: §a" + room.getPlayers().size() + "/6");
        s3.setScore(5);

        Score s4 = obj.getScore("§fTime: §e" + formatTime(timeLeft));
        s4.setScore(4);

        PlayerRole.Role role = room.getRoleManager().getRole(player.getUniqueId());
        Score s5 = obj.getScore("§fRole: " + (room.getState() == GameRoom.GameState.IN_GAME ? getRoleColor(role) + role.name() : "§7Waiting..."));
        s5.setScore(3);

        Score s6 = obj.getScore("§7---------------- ");
        s6.setScore(2);

        Score s7 = obj.getScore("§eCreator: nonkungch");
        s7.setScore(1);

        player.setScoreboard(board);
    }

    private static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private static String getRoleColor(PlayerRole.Role role) {
        switch (role) {
            case MURDERER: return "§c";
            case ENGINEER: return "§b";
            case MEDIC: return "§d";
            default: return "§a";
        }
    }
}
