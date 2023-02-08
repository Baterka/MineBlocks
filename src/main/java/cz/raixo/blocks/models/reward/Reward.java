package cz.raixo.blocks.models.reward;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;

public class Reward {

    private int chance;
    private String command;

    public Reward(int chance, String command) {
        this.chance = chance;
        this.command = command;
    }

    public void executeFor(PlayerRewardData data) {
        String cmd = command
                .replace("%player%", data.getPlayerData().getName())
                .replace("%breaks%", String.valueOf(data.getBreaks()));
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            cmd = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(data.getPlayerData().getUuid()), cmd);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return chance + ";" + command;
    }
}
