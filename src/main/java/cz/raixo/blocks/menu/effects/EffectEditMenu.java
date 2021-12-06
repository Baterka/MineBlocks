package cz.raixo.blocks.menu.effects;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.effects.register.EffectManager;
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
import java.util.List;

public class EffectEditMenu extends GUIExtender {

    private final Effect effect;
    private final EffectManager effectManager;
    private final MineBlock mineBlock;

    public EffectEditMenu(Effect effect, EffectManager effectManager, MineBlock mineBlock, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Effect edit | <#3875c7>" + mineBlock.getName()), Rows.THREE));
        this.effect = effect;
        this.effectManager = effectManager;
        this.mineBlock = mineBlock;
        redraw();
        openInventory(player);
    }

    public void redraw() {
        List<Pair<EffectOption, Object>> effectData = effectManager.getEffectData(effect);
        for (int i = 0; i < effectData.size(); i++) {
            Pair<EffectOption, Object> data = effectData.get(i);
            setItem(i, toItem(data), new GUIExtenderItem() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    if (!(event.getWhoClicked() instanceof Player)) return;
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    EffectOption.chatInput(data.getKey().getType(), player, (str) -> {
                        if (str != null) {
                            effectManager.setEffectData(data.getKey(), effect, str);
                            saveToConfig();
                        }
                        redraw();
                        openInventory(player);
                    });
                }
            });
        }
        setItem(18, new GUIItemBuilder(Material.ARROW).setName(Common.colorize("&7Go back!")), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    new EffectsEditMenu(mineBlock, player);
                }
            }
        });
        setItem(21, EffectsEditMenu.toItem(this.effect, false));
        setItem(23, new GUIItemBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM4YWIxNDU3NDdiNGJkMDljZTAzNTQzNTQ5NDhjZTY5ZmY2ZjQxZDllMDk4YzY4NDhiODBlMTg3ZTkxOSJ9fX0=")
                .setName(Common.colorize("<#e84646>&lRemove"))
                .setLore(Common.colorize(Arrays.asList("&7Deletes this effect", "&r", "<#e84646>Click to remove!"))), new GUIExtenderItem() {
            @Override
            public void onClick(InventoryClickEvent event) {
                mineBlock.getEffects().remove(effect);
                if (event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    new EffectsEditMenu(mineBlock, player);
                }
                saveToConfig();
            }
        });
    }

    private GUIItemBuilder toItem(Pair<EffectOption, Object> pair) {
        return new GUIItemBuilder(pair.getKey().getType().getIcon()).setName(Common.colorize("<#9a38c7>" + pair.getKey().getName())).setLore(Common.colorize(
                Arrays.asList("&7Current value: <#9a38c7>" + toString(pair), "&r", "<#9a38c7>Click to edit!")
        ));
    }

    private String toString(Pair<EffectOption, Object> pair) {
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
