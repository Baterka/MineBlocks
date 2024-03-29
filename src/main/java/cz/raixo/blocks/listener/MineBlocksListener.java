package cz.raixo.blocks.listener;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.models.MineBlock;
import eu.d0by.utils.Common;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MineBlocksListener implements Listener {

    private final MineBlocksPlugin plugin;
    private final Map<UUID, Long> lastBreak = new HashMap<>();

    public MineBlocksListener(MineBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastBreak.remove(e.getPlayer().getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                for (MineBlock block : plugin.getBlocks()) {
                    block.hideHologram(e.getPlayer());
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.checkVersionNotification(e.getPlayer());
        new BukkitRunnable() {
            @Override
            public void run() {
                for (MineBlock block : plugin.getBlocks()) {
                    block.showHologram(e.getPlayer());
                }
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        MineBlock mineBlock = plugin.getBlock(e.getBlock().getLocation());
        if (mineBlock == null) return;
        e.setCancelled(true);
        if (lastBreak.containsKey(e.getPlayer().getUniqueId()) &&
                lastBreak.get(e.getPlayer().getUniqueId()) + plugin.getBlockConfig().getLong("options.block-break-limit", -1) > System.currentTimeMillis()) {
            return;
        }
        lastBreak.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        if (plugin.getAfkAdapter().isAFK(e.getPlayer()) && plugin.getBlockConfig().getBoolean("options.blockafk", true)) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.valueOf(plugin.getBlockConfig().getString("options.notification-type", "ACTION_BAR")), TextComponent.fromLegacyText(
                    Common.colorize(
                            mineBlock.parseHoloLine(plugin.getBlockConfig().getString("lang.afk", "&cYou are AFK!"))
                    )
            ));
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
            return;
        }
        String permission = mineBlock.getPermission();
        if (permission != null && !permission.isEmpty() && !e.getPlayer().hasPermission(permission)) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.valueOf(plugin.getBlockConfig().getString("options.notification-type", "ACTION_BAR")), TextComponent.fromLegacyText(
                    Common.colorize(
                            mineBlock.parseHoloLine(plugin.getBlockConfig().getString("lang.no-permission", "&cYou don't have permission to break this block!"))
                    )
            ));
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
            return;
        }
        if (!mineBlock.isBlocked()) {
            if (e.getBlock().getType() != mineBlock.getBlockType()) {
                e.getBlock().setType(mineBlock.getBlockType());
            }
            mineBlock.onBreak(e.getPlayer());
        } else {
            e.getPlayer().spigot().sendMessage(ChatMessageType.valueOf(plugin.getBlockConfig().getString("options.notification-type", "ACTION_BAR")), TextComponent.fromLegacyText(
                    Common.colorize(
                            mineBlock.parseHoloLine(MineBlocksPlugin.getInstance().getBlockConfig().getString("lang.timeout", "&cYou can't destroy the block now!"))
                    )
            ));
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
            if (mineBlock.getCooldownBlock() != null && e.getBlock().getType() != mineBlock.getCooldownBlock()) {
                e.getBlock().setType(mineBlock.getCooldownBlock());
            }
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        softReload();
    }

    @EventHandler
    public void onWorldUnLoad(WorldUnloadEvent e) {
        softReload();
    }

    public void softReload() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    MineBlocksPlugin.getInstance().softReload();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }.runTask(MineBlocksPlugin.getInstance());
    }

}
