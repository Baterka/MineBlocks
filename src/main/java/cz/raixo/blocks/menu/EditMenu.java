package cz.raixo.blocks.menu;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.commands.MainCommand;
import cz.raixo.blocks.menu.effects.EffectsEditMenu;
import cz.raixo.blocks.menu.rewards.RewardSectionsMenu;
import cz.raixo.blocks.menu.utils.ConversationUtil;
import cz.raixo.blocks.menu.utils.LocationPicker;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.NumberUtil;
import eu.d0by.utils.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import pl.socketbyte.opengui.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditMenu extends GUIExtender {

    private final MineBlock mineBlock;

    public EditMenu(MineBlock mineBlock, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Edit mine block | <#2bb9e0>" + mineBlock.getName()), Rows.THREE));
        this.mineBlock = mineBlock;
        getGuiSettings().setUseInPlayerInventory(false);
        getGuiSettings().setPlayerInventoryResponse(new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                ItemStack itemStack = event.getClickedInventory().getItem(event.getSlot());
                if (itemStack == null) return;
                if (!itemStack.getType().isBlock()) return;
                setType(itemStack.getType());
                redraw();
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 100, 100);
                }
            }
        });
        redraw();
        openInventory(player);
    }

    public void redraw() {
        setItem(9, new GUIItemBuilder(Material.APPLE).setName(Common.colorize("<#c21759>&lHealth")).setLore(
                Common.colorize(Arrays.asList("&7Current health: <#c21759>" + this.getHealth(), "&r", "<#c21759>Click to edit!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    MainCommand.message(player, "Please type the number of lives into the chat!");
                    ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {
                        @Override
                        public boolean onMessage(String message) {
                            Optional<Integer> number = NumberUtil.parseInt(message);
                            if (number.isPresent()) {
                                setHealth(number.get());
                                return false;
                            } else {
                                MainCommand.error(player, "Invalid number! If you want to exit this prompt, type 'exit'");
                                return true;
                            }
                        }

                        @Override
                        public void onExit() {
                            redraw();
                            openInventory(player);
                        }
                    });
                }
            }
        });
        setItem(11, new GUIItemBuilder(getType()).setName(Common.colorize("<#239bdb>&lType")).setLore(
                Common.colorize(Arrays.asList("&7Type of the block.", "&7For example: Stone, Diamond block", "&r", "<#239bdb>Click on block in your inventory!"))
        ));
        setItem(13, new GUIItemBuilder(Material.NAME_TAG).setName(Common.colorize("<#18b5a5>&lName")).setLore(
                Common.colorize(Arrays.asList("&7Current name: <#18b5a5>" + getName(), "&r", "<#18b5a5>You can't change this value!"))
        ));
        setItem(15, new GUIItemBuilder(Material.COMPASS).setName(Common.colorize("<#37c477>&lLocation")).setLore(
                Common.colorize(Arrays.asList("&7Current location: <#37c477>" + getLocationAsString(), "&r", "<#37c477>Click to edit!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    event.getWhoClicked().closeInventory();
                    MainCommand.message(player, "Click on block, that you want to select!");
                    LocationPicker.getInstance().pickLocation(player, location -> {
                        setLocation(location);
                        redraw();
                        openInventory(player);
                    });
                }
            }
        });
        setItem(17, new GUIItemBuilder(Material.CLOCK).setName(Common.colorize("<#21b830>&lTimeout")).setLore(
                Common.colorize(Arrays.asList("&7Current timeout: <#21b830>" + this.getTimeout() + "s", "&r", "<#21b830>Click to edit!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    MainCommand.message(player, "Please type the number of seconds into the chat!");
                    ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {
                        @Override
                        public boolean onMessage(String message) {
                            Optional<Integer> number = NumberUtil.parseInt(message);
                            if (number.isPresent()) {
                                setTimeout(number.get());
                                return false;
                            } else {
                                MainCommand.error(player, "Invalid number! If you want to exit this prompt, type 'exit'");
                                return true;
                            }
                        }

                        @Override
                        public void onExit() {
                            redraw();
                            openInventory(player);
                        }
                    });
                }
            }
        });
        List<String> hologramLore = new ArrayList<>();
        hologramLore.add(Common.colorize("&7Lines:"));
        hologramLore.addAll(mineBlock.getHologram().stream().map(line -> Common.colorize("&8 - <#7621b8>") + line).collect(Collectors.toList()));
        hologramLore.add(Common.colorize("&r"));
        hologramLore.add(Common.colorize("<#7621b8>You can edit hologram in config.yml!"));
        setItem(19, new GUIItemBuilder(Material.OAK_SIGN).setName(Common.colorize("<#7621b8>&lHologram")).setLore(hologramLore));
        int effectCount = this.mineBlock.getEffects().size();
        setItem(21, new GUIItemBuilder(Material.DRAGON_BREATH).setName(Common.colorize("<#9a38c7>&lEffects")).setLore(
                Common.colorize(Arrays.asList("&7You currently have <#9a38c7>" + effectCount + (effectCount == 1 ? " effect" : " effects"), "&r", "<#9a38c7>Click to edit!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        new EffectsEditMenu(mineBlock, player);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        int rewardSectionCount = this.mineBlock.getRewards().size();
        setItem(23, new GUIItemBuilder(Material.GOLD_NUGGET).setName(Common.colorize("<#ba1e6f>&lRewards")).setLore(
                Common.colorize(Arrays.asList("&7You currently have <#ba1e6f>" + rewardSectionCount + (rewardSectionCount == 1 ? " reward section" : " reward sections"), "&r", "<#ba1e6f>Click to edit!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                try {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        new RewardSectionsMenu(mineBlock, player);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
        List<String> breakMessageLore = new ArrayList<>();
        breakMessageLore.add("&7Current message:");
        if (!mineBlock.getBreakMessage().equals("")) {
            breakMessageLore.addAll(Arrays.stream(mineBlock.getBreakMessage().split("<nl>")).map(s -> "&8 - <#d44a91>" + s).collect(Collectors.toList()));
        } else {
            breakMessageLore.add("<#d44a91>None message");
        }
        breakMessageLore.add("&r");
        breakMessageLore.add("<#d44a91>Click to edit!");
        setItem(25, new GUIItemBuilder(Material.NETHERITE_PICKAXE).addFlags(ItemFlag.values()).setName(Common.colorize("<#d44a91>&lBreak message")).setLore(Common.colorize(breakMessageLore)), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    MainCommand.message(player, "Please type new break message! Use <#2bb9e0><nl> &rto indicate a newline");
                    ConversationUtil.getInstance().createConversation(player, new ConversationUtil.ConversationListener() {
                        @Override
                        public boolean onMessage(String message) {
                            mineBlock.setBreakMessage(message.equalsIgnoreCase("none") ? "" : message);
                            saveToConfig();
                            return false;
                        }

                        @Override
                        public void onExit() {
                            redraw();
                            openInventory(player);
                        }
                    });
                }
            }
        });
    }

    private String getLocationAsString() {
        Location location = mineBlock.getLocation();
        if (location == null) return "None";
        String s = location.getWorld().getName() + ", ";
        s += location.getBlockX() + ", ";
        s += location.getBlockY() + ", ";
        s += location.getBlockZ();
        return s;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    public String getName() {
        return mineBlock.getName();
    }

    public Material getType() {
        return mineBlock.getBlockType();
    }

    public void setType(Material type) {
        mineBlock.setBlockType(type);
        saveToConfig();
    }

    public int getTimeout() {
        return mineBlock.getBlockSeconds();
    }

    public void setTimeout(int timeout) {
        mineBlock.setBlockSeconds(timeout);
        saveToConfig();
    }

    public long getHealth() {
        return mineBlock.getMaxHealth();
    }

    public void setHealth(long health) {
        mineBlock.setMaxHealth(health);
        saveToConfig();
    }

    public Location getLocation() {
        return mineBlock.getLocation();
    }

    public void setLocation(Location location) {
        mineBlock.setLocation(location);
        saveToConfig();
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
