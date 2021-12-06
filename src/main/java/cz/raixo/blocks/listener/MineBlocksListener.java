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

public class MineBlocksListener implements Listener {

    private final MineBlocksPlugin plugin;

    public MineBlocksListener(MineBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
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
        if (plugin.getAfkAdapter().isAFK(e.getPlayer()) && plugin.getBlockConfig().getBoolean("options.blockafk", true)) {
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    Common.colorize(
                            plugin.getBlockConfig().getString("lang.afk", "&cYou are AFK!")
                    )
            ));
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
            return;
        }
        if (!mineBlock.isBlocked()) {
            mineBlock.onBreak(e.getPlayer());
        } else {
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    Common.colorize(
                            MineBlocksPlugin.getInstance().getBlockConfig().getString("lang.timeout", "&cYou can't destroy the block now!")
                    )
            ));
            e.getPlayer().playSound(e.getBlock().getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 100);
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
