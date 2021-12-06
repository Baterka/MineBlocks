package cz.raixo.blocks.menu.effects;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.options.EffectOption;
import cz.raixo.blocks.effects.register.EffectInfo;
import cz.raixo.blocks.effects.register.EffectManager;
import cz.raixo.blocks.effects.register.EffectRegister;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.util.Pair;
import eu.d0by.utils.Common;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import pl.socketbyte.opengui.*;

import java.io.IOException;
import java.util.*;

public class EffectCreateMenu extends GUIExtender {

    private final MineBlock mineBlock;
    private final List<Pair<EffectOption, String>> options = new ArrayList<>();
    private EffectManager selected;

    public EffectCreateMenu(MineBlock mineBlock, Player player) {
        super(new GUI(Common.colorize("&f&l| <#5c5f63>Effect create | <#3875c7>" + mineBlock.getName()), Rows.THREE));
        this.mineBlock = mineBlock;
        redraw();
        openInventory(player);
    }

    public void redraw() {
        getBukkitInventory().clear();
        List<EffectManager> effects = new ArrayList<>(EffectRegister.getEffects());
        for (int i = 0; i < effects.size() && i < 9; i++) {
            EffectManager effectManager = effects.get(i);
            setItem(i, toItem(effectManager), new GUIExtenderItem() {
                @Override
                public void onClick(InventoryClickEvent event) {
                    select(effectManager);
                }
            });
        }
        if (selected != null) {
            for (int i = 0; i < options.size() && i < 9; i++) {
                Pair<EffectOption, String> option = options.get(i);
                String valueInput = option.getValue();
                String value = valueInput == null ? "&cNone" : EffectOption.toReadable(option.getKey().getType(), valueInput);
                List<String> lore = new ArrayList<>();
                lore.add("&7Current value: <#9a38c7>" + value);
                lore.add("&r");
                lore.add("<#9a38c7>Click to edit!");
                setItem(i + 9, new GUIItemBuilder(option.getKey().getType().getIcon()).setName(Common.colorize("<#9a38c7>" + option.getKey().getName())).setLore(Common.colorize(lore)), new GUIExtenderItem() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        if (!(event.getWhoClicked() instanceof Player)) return;
                        Player player = (Player) event.getWhoClicked();
                        player.closeInventory();
                        EffectOption.chatInput(option.getKey().getType(), player, (str) -> {
                            if (str != null) option.setValue(str);
                            redraw();
                            openInventory(player);
                        });
                    }
                });
            }
            if (options.stream().filter(o -> o.getValue() == null).count() < 1) {
                setItem(26, new GUIItemBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIwYzE2NDdkYWU1ZDVmNmJjNWRjYTU0OWYxNjUyNTU2YzdmMWJjMDhhZGVlMzdjY2ZjNDA5MGJjMjBlNjQ3ZSJ9fX0=")
                        .setName(Common.colorize("<#9a38c7>&lCreate effect")).setLore(Common.colorize(
                                Arrays.asList("&7Creates your", "&7new beautiful effect!", "&r", "<#9a38c7>Click to create!")
                        )), new GUIExtenderItem() {
                    @Override
                    public void onClick(InventoryClickEvent event) {
                        try {
                            if (selected == null) return;
                            StringBuilder opt = new StringBuilder(selected.getEffectInfo().getRegisterName().toLowerCase(Locale.ROOT) + ";");
                            String optString;
                            for (Pair<EffectOption, String> option : options) {
                                opt.append(option.getValue()).append(",");
                            }
                            optString = opt.toString();
                            if (optString.endsWith(",")) {
                                optString = optString.substring(0, opt.length() - 1);
                            }
                            Effect effect = null;
                            try {
                                effect = EffectRegister.create(optString);
                            } catch (InvalidEffectArgumentsException e) {
                                e.printStackTrace();
                            }
                            if (effect != null) {
                                mineBlock.addEffectAndRun(effect);
                                saveToConfig();
                            }
                            if (event.getWhoClicked() instanceof Player) {
                                Player player = (Player) event.getWhoClicked();
                                new EffectsEditMenu(mineBlock, player);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
            }
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
    }

    private GUIItemBuilder toItem(EffectManager effectManager) {
        EffectInfo effectInfo = effectManager.getEffectInfo();
        List<String> lore = new LinkedList<>();
        if (effectInfo.getDescription() != null) {
            lore.add("<#5c5f63>" + effectInfo.getDescription());
        }
        lore.add("&r");
        lore.add(effectManager.equals(selected) ? "<#9a38c7>Selected!" : "<#9a38c7>Click to select!");
        GUIItemBuilder guiItemBuilder = new GUIItemBuilder(effectInfo.getIcon()).setName(Common.colorize("<#9a38c7>" + effectInfo.getName())).setLore(Common.colorize(lore));
        if (effectManager.equals(selected)) {
            guiItemBuilder.addFlags(ItemFlag.values());
            guiItemBuilder.setEnchantments(Arrays.asList(new ItemEnchantment(Enchantment.ARROW_DAMAGE, 69)));
        }
        return guiItemBuilder;
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

    public void select(EffectManager effectManager) {
        this.selected = effectManager;
        this.options.clear();
        for (EffectOption option : effectManager.getEffectInfo().getOptions()) {
            this.options.add(new Pair<>(option, null));
        }
        redraw();
    }

}
