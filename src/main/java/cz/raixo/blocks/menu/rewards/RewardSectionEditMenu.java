package cz.raixo.blocks.menu.rewards;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.commands.MainCommand;
import cz.raixo.blocks.menu.utils.ConversationUtil;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.models.reward.Reward;
import cz.raixo.blocks.models.reward.RewardSection;
import cz.raixo.blocks.util.NumberUtil;
import eu.d0by.utils.Common;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.socketbyte.opengui.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class RewardSectionEditMenu extends GUIExtender {

    private final MineBlock mineBlock;
    private final RewardSection rewardSection;

    public RewardSectionEditMenu(MineBlock mineBlock, RewardSection rewardSection, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Reward section edit | <#2bb9e0>" + mineBlock.getName()), Rows.THREE));
        this.mineBlock = mineBlock;
        this.rewardSection = rewardSection;
        redraw();
        openInventory(player);
    }

    public void redraw() {
        List<Reward> rewards = rewardSection.getRewards();
        getBukkitInventory().clear();
        for (int i = 0; i < rewards.size() && i < getBukkitInventory().getSize() - 9; i++) {
            Reward reward = rewards.get(i);
            setItem(i, toItem(reward), new GUIExtenderItem() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    rewardSection.removeReward(reward);
                    redraw();
                    saveToConfig();
                }
            });
        }
        if (rewards.isEmpty()) {
            setItem(13, new GUIItemBuilder(Material.PAPER).setName(Common.colorize("<#e84646>Nothing!")).setLore(Common.colorize("&7There is nothing to show!")));
        }
        setItem(18, new GUIItemBuilder(Material.ARROW).setName(Common.colorize("&7Go back!")), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    new RewardSectionsMenu(mineBlock, player);
                }
            }
        });
        setItem(21, RewardSectionsMenu.toItem(this.rewardSection, false));
        setItem(23, new GUIItemBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM4YWIxNDU3NDdiNGJkMDljZTAzNTQzNTQ5NDhjZTY5ZmY2ZjQxZDllMDk4YzY4NDhiODBlMTg3ZTkxOSJ9fX0=")
                .setName(Common.colorize("<#e84646>&lRemove"))
                .setLore(Common.colorize(Arrays.asList("&7Deletes this reward section", "&r", "<#e84646>Click to remove!"))), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                mineBlock.getRewards().remove(rewardSection);
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    new RewardSectionsMenu(mineBlock, player);
                }
                saveToConfig();
            }
        });
        setItem(26, new GUIItemBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                .setName(Common.colorize("<#ba1e6f>&lNew reward")).setLore(Common.colorize(
                        Arrays.asList("&7You can use %player%",
                                "&7in reward's command!",
                                "&r", "<#ba1e6f>Click to create!")
                )), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) return;
                Player player = (Player) event.getWhoClicked();
                event.getWhoClicked().closeInventory();
                MainCommand.message(player, "Please type chance of this reward! Higher number means higher chance");
                ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {

                    int chance = -1;
                    String command;

                    @Override
                    public boolean onMessage(String message) {
                        if (chance == -1) {
                            Optional<Integer> optionalInteger = NumberUtil.parseInt(message);
                            if (optionalInteger.isEmpty()) {
                                MainCommand.error(player, "Invalid number!");
                            } else {
                                this.chance = Math.max(optionalInteger.get(), 0);
                                MainCommand.message(player, "<#2bb9e0>Chance &rwas set to <#2bb9e0>" + chance);
                                MainCommand.message(player, "Please type the command of this reward! <#2bb9e0>Don't use / at the beginning of the command!");
                            }
                        } else if (command == null) {
                            command = message;
                            rewardSection.addReward(new Reward(this.chance, this.command));
                            saveToConfig();
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void onExit() {
                        redraw();
                        openInventory(player);
                    }
                });
            }
        });
    }

    public GUIItemBuilder toItem(Reward reward) {
        List<String> lore = new LinkedList<>();
        lore.add("&7Chance: <#ba1e6f>" + reward.getChance());
        lore.add("&7Command: <#ba1e6f>/" + reward.getCommand());
        lore.add("&r");
        lore.add("<#ba1e6f>Click to remove!");
        return new GUIItemBuilder(Material.PAPER)
                .setName(Common.colorize("<#ba1e6f>Reward"))
                .setLore(Common.colorize(lore))
                ;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    public void saveToConfig() {
        MineBlocksPlugin plugin = MineBlocksPlugin.getInstance();
        if (!plugin.isRegistered(mineBlock)) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getBlockConfig().saveBlock(mineBlock);
                try {
                    plugin.getBlockConfig().save(plugin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTask(plugin);
    }

}
