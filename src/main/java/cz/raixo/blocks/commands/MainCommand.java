package cz.raixo.blocks.commands;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.menu.CreateMenu;
import cz.raixo.blocks.menu.EditMenu;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.NumberUtil;
import eu.d0by.utils.Common;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class MainCommand implements CommandExecutor, TabCompleter {

    public static void error(CommandSender commandSender, String message) {
        commandSender.sendMessage(
                Common.colorize("&f&l! <#e84646>" + message.replace("&r", "<#e84646>"))
        );
    }

    public static void message(CommandSender commandSender, String message) {
        commandSender.sendMessage(
                Common.colorize("&f&l| <#bdb9b9>" + message.replace("&r", "<#bdb9b9>"))
        );
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.hasPermission("mb.admin")) {
            commandSender.sendMessage(Common.colorize("<#e84646>You are not allowed to do this!"));
            return true;
        }
        if (args.length > 0) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload": {
                    try {
                        MineBlocksPlugin.getInstance().reload();
                        message(commandSender, "Reloaded successfully!");
                    } catch (Throwable t) {
                        t.printStackTrace();
                        error(commandSender, "An error has occurred!");
                    }
                    break;
                }
                case "create": {
                    if (args.length > 1) {
                        if (commandSender instanceof Player) {
                            Player player = (Player) commandSender;
                            if (MineBlocksPlugin.getInstance().getBlock(args[1]) != null) {
                                MainCommand.error(player, "This name is taken!");
                            } else {
                                new CreateMenu(args[1], player);
                            }
                        } else {
                            error(commandSender, "Only players can use this command!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb create <name>");
                    }
                    break;
                }
                case "edit": {
                    if (args.length > 1) {
                        String name = args[1];
                        MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                        if (mineBlock != null) {
                            if (commandSender instanceof Player) {
                                Player player = (Player) commandSender;
                                new EditMenu(mineBlock, player);
                            }
                        } else {
                            error(commandSender, "Invalid block name!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb edit <name>");
                    }
                    break;
                }
                case "sethealth": {
                    if (args.length > 2) {
                        String name = args[1];
                        Optional<Integer> health = NumberUtil.parseInt(args[2]);
                        if (health.isPresent()) {
                            MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                            if (mineBlock != null) {
                                mineBlock.setHealth(
                                        Math.max(
                                                Math.min(
                                                        health.get(),
                                                        mineBlock.getMaxHealth()
                                                ),
                                                1
                                        )
                                );
                                message(commandSender, "Set health to <#2bb9e0>" + mineBlock.getHealth() + " &rfor block <#2bb9e0>" + mineBlock.getName() + "&r!");
                            } else {
                                error(commandSender, "Invalid block name!");
                            }
                        } else {
                            error(commandSender, "Invalid health value!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb sethealth <name> <health>");
                    }
                    break;
                }
                case "remove": {
                    if (args.length > 1) {
                        String name = args[1];
                        MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                        if (mineBlock != null) {
                            MineBlocksPlugin.getInstance().removeBlock(mineBlock.getLocation());
                            MineBlocksPlugin.getInstance().saveBlocks();
                            message(commandSender, "Block <#2bb9e0>" + mineBlock.getName() + " &rwas removed!");
                        } else {
                            error(commandSender, "Invalid block name!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb remove <name>");
                    }
                    break;
                }
                case "resettimeout": {
                    if (args.length > 1) {
                        String name = args[1];
                        MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                        if (mineBlock != null) {
                            if (mineBlock.isBlocked()) {
                                mineBlock.setBlockedUntil(new Date(0));
                                message(commandSender, "Timeout for block <#2bb9e0>" + mineBlock.getName() + " &rwas removed!");
                            } else {
                                error(commandSender, "Block <#bdb9b9>" + mineBlock.getName() + " &rdoes not have an active timeout!");
                            }
                        } else {
                            error(commandSender, "Invalid block name!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb remove <name>");
                    }
                    break;
                }
                case "tp": {
                    if (args.length > 1) {
                        String name = args[1];
                        MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                        if (mineBlock != null) {
                            if (commandSender instanceof Player) {
                                Player player = (Player) commandSender;
                                player.teleport(mineBlock.getLocation().clone().add(.5, 2, .5));
                                message(commandSender, "Teleported to <#2bb9e0>" + mineBlock.getName() + " &rmine block!");
                            }
                        } else {
                            error(commandSender, "Invalid block name!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb tp <name>");
                    }
                    break;
                }
                case "reset": {
                    if (args.length > 1) {
                        String name = args[1];
                        MineBlock mineBlock = MineBlocksPlugin.getInstance().getBlock(name);
                        if (mineBlock != null) {
                            mineBlock.reset();
                            message(commandSender, "Mineblock <#2bb9e0>" + mineBlock.getName() + " &rhas been reset!");
                        } else {
                            error(commandSender, "Invalid block name!");
                        }
                    } else {
                        error(commandSender, "Invalid arguments! Please use /mb reset <name>");
                    }
                    break;
                }
                case "list": {
                    StringBuilder blocks = new StringBuilder();
                    for (MineBlock block : MineBlocksPlugin.getInstance().getBlocks()) {
                        blocks.append(" &8- <#42b1b3>").append(block.getName()).append(" <#4d5454>| <#95a1a1>").append(getLocationAsString(block.getLocation())).append("\n");
                    }
                    if (blocks.toString().equals("")) {
                        blocks = new StringBuilder("&7 &7 Nothing to show");
                    }
                    commandSender.sendMessage(
                            Common.colorize("\n <#44d48c>&lMINE BLOCKS</#448ed4> \n&r\n &7Blocks: \n" + blocks + "\n&r")
                    );
                    break;
                }
                case "support": {
                    message(commandSender, "Official discord server:<#2bb9e0> https://discord.gg/gTKvK48Par");
                    break;
                }
                case "version": {
                    message(commandSender, "Version of installed <#2bb9e0>MineBlocks &ris <#2bb9e0>" + MineBlocksPlugin.getInstance().getDescription().getVersion() + "&r!");
                    break;
                }
                default: {
                    error(commandSender, "Unknown subcommand!");
                    break;
                }
            }
        } else {
            helpMenu(commandSender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length < 1) return new ArrayList<>();
        return StringUtil.copyPartialMatches(strings[strings.length - 1], onTabCompleteInternal(commandSender, command, s, strings), new ArrayList<>());
    }

    public List<String> onTabCompleteInternal(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("mb.admin")) return new ArrayList<>();
        if (strings.length > 0) {
            if (strings.length > 1) {
                switch (strings[0].toLowerCase(Locale.ROOT)) {
                    case "sethealth":
                    case "edit":
                    case "remove":
                    case "tp":
                    case "resettimeout":
                    case "reset":
                        if (strings.length > 2) return new ArrayList<>();
                        else {
                            return MineBlocksPlugin.getInstance().getBlocks().stream().map(MineBlock::getName).collect(Collectors.toList());
                        }
                }
            } else
                return List.of("reload", "create", "edit", "remove", "tp", "list", "reset", "sethealth", "resettimeout", "support", "version");
        }
        return new ArrayList<>();
    }

    private void helpMenu(CommandSender commandSender) {
        String s1 = "\n";
        s1 += "<#44d48c> &lMINE BLOCKS</#448ed4>\n&7\n";
        s1 += "&7 Commands:\n";
        s1 += "&8 - <#42b1b3>/mb <#4d5454>| <#95a1a1>Help menu\n";
        s1 += "&8 - <#42b1b3>/mb reload <#4d5454>| <#95a1a1>Reloads configuration file\n";
        s1 += "&8 - <#42b1b3>/mb create <name> <#4d5454>| <#95a1a1>Creates mine block\n";
        s1 += "&8 - <#42b1b3>/mb edit <name> <#4d5454>| <#95a1a1>Edits mine block\n";
        s1 += "&8 - <#42b1b3>/mb remove <name> <#4d5454>| <#95a1a1>Removes mine block\n";
        s1 += "&8 - <#42b1b3>/mb tp <name> <#4d5454>| <#95a1a1>Teleports you to mine block\n";
        s1 += "&8 - <#42b1b3>/mb list <#4d5454>| <#95a1a1>List of mine blocks\n";
        s1 += "&8 - <#42b1b3>/mb sethealth <name> <health> <#4d5454>| <#95a1a1>Edits block's health\n";
        s1 += "&8 - <#42b1b3>/mb resettimeout <name> <#4d5454>| <#95a1a1>Resets block's current timeout\n";
        s1 += "&8 - <#42b1b3>/mb reset <name> <#4d5454>| <#95a1a1>Resets block's current breaks\n";
        s1 += "&8 - <#42b1b3>/mb support <#4d5454>| <#95a1a1>Invite link to an official discord support server\n";
        s1 += "&8 - <#42b1b3>/mb version <#4d5454>| <#95a1a1>Shows the version of the installed plugin\n";
        s1 += "&7\n <#62a6a6>Created by &lRAIXOCZ";
        commandSender.sendMessage(Common.colorize(s1));
    }

    private String getLocationAsString(Location location) {
        if (location == null) return "None";
        String s = location.getWorld().getName() + ", ";
        s += location.getBlockX() + ", ";
        s += location.getBlockY() + ", ";
        s += location.getBlockZ();
        return s;
    }

}
