package cz.raixo.blocks.menu.rewards;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.commands.MainCommand;
import cz.raixo.blocks.menu.EditMenu;
import cz.raixo.blocks.menu.utils.ConversationUtil;
import cz.raixo.blocks.models.MineBlock;
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
import java.util.*;

public class RewardSectionsMenu extends GUIExtender {

    private final MineBlock mineBlock;

    public RewardSectionsMenu(MineBlock mineBlock, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Reward sections edit | <#2bb9e0>" + mineBlock.getName()), Rows.THREE));
        this.mineBlock = mineBlock;
        redraw();
        openInventory(player);
    }

    public static GUIItemBuilder toItem(RewardSection rewardSection) {
        return toItem(rewardSection, true);
    }

    public static GUIItemBuilder toItem(RewardSection rewardSection, boolean edit) {
        List<String> lore = new LinkedList<>();
        lore.add("&7From: <#ba1e6f>" + rewardSection.getFrom() + " breaks");
        lore.add("&7To: <#ba1e6f>" + rewardSection.getTo() + " breaks");
        if (edit) {
            lore.add("&r");
            lore.add("<#ba1e6f>Click to edit!");
        }
        return new GUIItemBuilder(Material.CHEST)
                .setName(Common.colorize("<#ba1e6f>") + (rewardSection.getName().equals("") ? "Reward section" : rewardSection.getName()))
                .setLore(Common.colorize(lore))
                ;
    }

    public void redraw() {
        List<RewardSection> rewardSections = new ArrayList<>(mineBlock.getRewards());
        getBukkitInventory().clear();
        for (int i = 0; i < rewardSections.size() && i < getBukkitInventory().getSize() - 9; i++) {
            RewardSection rewardSection = rewardSections.get(i);
            setItem(i, toItem(rewardSection), new GUIExtenderItem() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (!(event.getWhoClicked() instanceof Player)) return;
                    Player player = (Player) event.getWhoClicked();
                    new RewardSectionEditMenu(mineBlock, rewardSection, player);
                }
            });
        }
        if (rewardSections.isEmpty()) {
            setItem(13, new GUIItemBuilder(Material.PAPER).setName(Common.colorize("<#e84646>Nothing!")).setLore(Common.colorize("&7There is nothing to show!")));
        }
        setItem(18, new GUIItemBuilder(Material.ARROW).setName(Common.colorize("&7Go back!")), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    new EditMenu(mineBlock, player);
                }
            }
        });
        setItem(26, new GUIItemBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWEyZDg5MWM2YWU5ZjZiYWEwNDBkNzM2YWI4NGQ0ODM0NGJiNmI3MGQ3ZjFhMjgwZGQxMmNiYWM0ZDc3NyJ9fX0=")
                        .setName(Common.colorize("<#ba1e6f>&lNew reward section")).setLore(Common.colorize(
                                Arrays.asList("&7The reward section contains",
                                        "&7rewards, players will receive",
                                        "&7one of them if they",
                                        "&7meet its range of breaks",
                                        "&r", "<#ba1e6f>Click to create!")
                        )),
                new GUIExtenderItem() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (!(event.getWhoClicked() instanceof Player)) return;
                        Player player = (Player) event.getWhoClicked();
                        event.getWhoClicked().closeInventory();
                        MainCommand.message(player, "Please type the range of this reward section! (from)");
                        ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {

                            private int from = -1;
                            private int to = -1;
                            private String name;

                            @Override
                            public boolean onMessage(String message) {
                                if (from == -1) {
                                    Optional<Integer> optionalInteger = NumberUtil.parseInt(message);
                                    if (optionalInteger.isEmpty()) {
                                        MainCommand.error(player, "Invalid number!");
                                    } else {
                                        from = Math.max(optionalInteger.get(), 0);
                                        MainCommand.message(player, "Range <#2bb9e0>from &rwas set to <#2bb9e0>" + from);
                                        MainCommand.message(player, "Please type the range of this reward section! (to)");
                                    }
                                } else if (to == -1) {
                                    Optional<Integer> optionalInteger = NumberUtil.parseInt(message);
                                    if (optionalInteger.isEmpty()) {
                                        MainCommand.error(player, "Invalid number!");
                                    } else {
                                        to = Math.max(optionalInteger.get(), 0);
                                        MainCommand.message(player, "Range <#2bb9e0>to &rwas set to <#2bb9e0>" + to);
                                        MainCommand.message(player, "Please type the name of this reward section! Type <#2bb9e0>none &rif you don't want to set the name!");
                                    }
                                } else if (name == null) {
                                    if (message.equalsIgnoreCase("none")) {
                                        name = "";
                                    } else {
                                        name = message;
                                    }
                                    MainCommand.message(player, "Name was set to <#2bb9e0>" + (name.equals("") ? "None" : name));
                                    RewardSection rewardSection = new RewardSection(Math.min(from, to), Math.max(from, to), name);
                                    long count = mineBlock.getRewards().stream().filter(rs -> rs.getFrom() == from && rs.getTo() == to && rs.getName().equalsIgnoreCase(name)).count();
                                    if (count > 0) {
                                        MainCommand.error(player, "This section already exists!");
                                        return false;
                                    }
                                    mineBlock.getRewards().add(rewardSection);
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
                }
        );
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
