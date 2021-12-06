package cz.raixo.blocks.menu;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.commands.MainCommand;
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
import org.bukkit.inventory.ItemStack;
import pl.socketbyte.opengui.*;

import java.util.Arrays;
import java.util.Optional;

public class CreateMenu extends GUIExtender {

    private final Player player;
    private String name;
    private Material type = Material.STONE;
    private int timeout = 0;
    private long health = 100;
    private Location location;

    public CreateMenu(String name, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Create new mine block"), Rows.THREE));
        this.name = name;
        this.player = player;
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
                Common.colorize(Arrays.asList("&7Current health: <#c21759>" + this.health, "&r", "<#c21759>Click to edit!"))
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
        setItem(11, new GUIItemBuilder(type).setName(Common.colorize("<#239bdb>&lType")).setLore(
                Common.colorize(Arrays.asList("&7Type of the block.", "&7For example: Stone, Diamond block", "&r", "<#239bdb>Click on block in your inventory!"))
        ));
        setItem(13, new GUIItemBuilder(Material.EMERALD).setName(Common.colorize("<#18b5a5>&lCreate")).setLore(
                Common.colorize(Arrays.asList("&7Name: <#18b5a5>" + this.name, "&r", "<#18b5a5>Create new mine block!"))
        ), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (MineBlocksPlugin.getInstance().getBlock(name) != null) {
                    MainCommand.error(event.getWhoClicked(), "This name is taken!");
                    return;
                }
                if (location == null) {
                    MainCommand.error(event.getWhoClicked(), "Location is required");
                    return;
                }
                try {
                    if (MineBlocksPlugin.getInstance().getBlock(location) != null) {
                        MainCommand.error(player, "This location is taken!");
                        return;
                    }
                    event.getWhoClicked().closeInventory();
                    MineBlock mineBlock = new MineBlock();
                    mineBlock.setName(name);
                    mineBlock.setBlockType(type);
                    mineBlock.setBlockSeconds(timeout);
                    mineBlock.setHologram(Arrays.asList("#ICON: %type%", "<#44d48c>&lMINE BLOCKS</#448ed4>", "<#41a6d9>%health%/%max_health%", "<#8d9599>Break to get reward", "&c%timeout%"));
                    mineBlock.setBreakMessage("");
                    mineBlock.setMaxHealth(health);
                    mineBlock.setLocation(location);
                    MineBlocksPlugin.getInstance().createBlock(mineBlock);
                    MainCommand.message(event.getWhoClicked(), "Block <#2bb9e0>" + mineBlock.getName() + " &rsuccessfully created!");
                    MainCommand.message(event.getWhoClicked(), "You can edit rewards, effects, hologram and break message through /mb edit <#2bb9e0>" + mineBlock.getName() + "&r!");
                    MineBlocksPlugin.getInstance().saveBlocks();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
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
                Common.colorize(Arrays.asList("&7Current timeout: <#21b830>" + this.timeout + "s", "&r", "<#21b830>Click to edit!"))
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
    }

    private String getLocationAsString() {
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Material getType() {
        return type;
    }

    public void setType(Material type) {
        this.type = type;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getHealth() {
        return health;
    }

    public void setHealth(long health) {
        this.health = health;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
