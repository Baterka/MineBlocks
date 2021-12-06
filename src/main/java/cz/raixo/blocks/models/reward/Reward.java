package cz.raixo.blocks.models.reward;

import org.bukkit.Bukkit;

public class Reward {

    private int chance;
    private String command;

    public Reward(int chance, String command) {
        this.chance = chance;
        this.command = command;
    }

    public void executeFor(String player) {
        String cmd = command.replace("%player%", player);
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
