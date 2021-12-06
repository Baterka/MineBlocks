package cz.raixo.blocks.menu.effects;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.effects.register.EffectInfo;
import cz.raixo.blocks.effects.register.EffectManager;
import cz.raixo.blocks.effects.register.EffectRegister;
import cz.raixo.blocks.menu.EditMenu;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.Pair;
import eu.d0by.utils.Common;
import org.bukkit.Color;
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

public class EffectsEditMenu extends GUIExtender {

    private final MineBlock mineBlock;

    public EffectsEditMenu(MineBlock mineBlock, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Effects edit | <#3875c7>" + mineBlock.getName()), Rows.THREE));
        this.mineBlock = mineBlock;
        redraw();
        openInventory(player);
    }

    public static GUIItemBuilder toItem(Effect effect) {
        return toItem(effect, true);
    }

    public static GUIItemBuilder toItem(Effect effect, boolean edit) {
        EffectManager effectManager = EffectRegister.getManager(effect);
        if (effectManager == null) {
            return new GUIItemBuilder(Material.BARRIER).setName(Common.colorize("<#e84646>Unknown effect")).setLore(Common.colorize(
                    Arrays.asList("&7This is weird!", "&7This effect was not found in our database!", "&r", "<#e84646>You can't edit this effect!")
            ));
        }
        EffectInfo effectInfo = effectManager.getEffectInfo();
        List<String> lore = new LinkedList<>();
        if (effectInfo.getDescription() != null) {
            lore.add("<#5c5f63>" + effectInfo.getDescription());
            lore.add("&r");
        }
        List<Pair<EffectOption, Object>> effectData = effectManager.getEffectData(effect);
        for (Pair<EffectOption, Object> data : effectData) {
            String value = toString(data);
            lore.add("&7" + data.getKey().getName() + ": " + (value == null ? "<#e84646>Unknown value" : "<#9a38c7>" + value));
        }
        if (edit) {
            lore.add("&r");
            lore.add("<#9a38c7>Click to edit!");
        }
        return new GUIItemBuilder(effectManager.getEffectInfo().getIcon())
                .setName(Common.colorize("<#9a38c7>") + effectInfo.getName())
                .setLore(Common.colorize(lore));
    }

    public static String toString(Pair<EffectOption, Object> pair) {
        switch (pair.getKey().getType()) {
            case INTEGER:
            case FLOAT: {
                return String.valueOf(pair.getValue());
            }
            case COLOR: {
                if (!(pair.getValue() instanceof Color)) return null;
                Color color = (Color) pair.getValue();
                return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + String.format(" <#%02x%02x%02x>\u2588", color.getRed(), color.getGreen(), color.getBlue());
            }
            default: {
                return null;
            }
        }
    }

    public void redraw() {
        getBukkitInventory().clear();
        List<Effect> effects = new LinkedList<>(mineBlock.getEffects());
        for (int i = 0; i < effects.size() && i < getBukkitInventory().getSize() - 9; i++) {
            Effect effect = effects.get(i);
            setItem(i, toItem(effect), new GUIExtenderItem() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (!(event.getWhoClicked() instanceof Player)) return;
                    Player player = (Player) event.getWhoClicked();
                    EffectManager effectManager = EffectRegister.getManager(effect);
                    if (effectManager == null) return;
                    new EffectEditMenu(effect, effectManager, mineBlock, player);
                }
            });
        }
        if (effects.isEmpty()) {
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
                .setName(Common.colorize("<#9a38c7>&lNew effect")).setLore(Common.colorize(
                        Arrays.asList("&7Make your mine block",
                                "&7stand out!",
                                "&r", "<#9a38c7>Click to create!")
                )), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (!(event.getWhoClicked() instanceof Player)) return;
                Player player = (Player) event.getWhoClicked();
                new EffectCreateMenu(mineBlock, player);
            }
        });
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
