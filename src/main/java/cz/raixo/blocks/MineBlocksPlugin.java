package cz.raixo.blocks;

import cz.raixo.blocks.afk.AfkAdapter;
import cz.raixo.blocks.commands.MainCommand;
import cz.raixo.blocks.config.BlocksConfig;
import cz.raixo.blocks.effects.executor.ParticleExecutor;
import cz.raixo.blocks.hologram.manager.HologramManager;
import cz.raixo.blocks.listener.MineBlocksListener;
import cz.raixo.blocks.menu.utils.ConversationUtil;
import cz.raixo.blocks.menu.utils.LocationPicker;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.storage.StorageData;
import cz.raixo.blocks.storage.StorageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.socketbyte.opengui.OpenGUI;

import java.io.IOException;
import java.util.*;

public class MineBlocksPlugin extends JavaPlugin {

    private static MineBlocksPlugin instance;
    private boolean running = false;
    private Metrics metrics;
    private ParticleExecutor particleExecutor;
    private BlocksConfig config;
    private StorageManager storageManager;
    private final List<Location> ignoredBlocks = new ArrayList<>();
    private AfkAdapter afkAdapter;
    private HologramManager hologramManager;
    private final Map<Location, MineBlock> blocks = new HashMap<>() {

        @Override
        public MineBlock put(Location l, MineBlock block) {
            if (!ignoredBlocks.contains(l)) {
                block.setBlock();
                block.createHologram();
                block.setUnloaded(false);
            }
            return super.put(l, block);
        }

        @Override
        public MineBlock remove(Object key) {
            if (containsKey(key) && !ignoredBlocks.contains(key)) {
                MineBlock mineBlock = get(key);
                mineBlock.removeBlock();
                mineBlock.removeHologram();
                mineBlock.setBlockedUntil(new Date(0));
                mineBlock.setUnloaded(true);
            }
            return super.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            if (containsKey(key) && !ignoredBlocks.contains(key)) {
                MineBlock mineBlock = get(key);
                mineBlock.removeBlock();
                mineBlock.removeHologram();
                mineBlock.setBlockedUntil(new Date(0));
                mineBlock.setUnloaded(true);
            }
            return super.remove(key, value);
        }

    };

    public static MineBlocksPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.hologramManager = new HologramManager(this);
        } catch (HologramManager.NoHologramPluginException e) {
            e.printStackTrace();
            return;
        }
        getLogger().info("Using " + hologramManager.getHologramPlugin().getPluginName() + " as hologram plugin");
        this.afkAdapter = new AfkAdapter(this);
        OpenGUI.INSTANCE.register(this);
        this.metrics = new Metrics(this, 13178);
        MainCommand mainCommand = new MainCommand();
        PluginCommand pluginCommand = getCommand("mineblocks");
        pluginCommand.setExecutor(mainCommand);
        pluginCommand.setTabCompleter(mainCommand);
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdirs();
        try {
            this.config = new BlocksConfig(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.storageManager = new StorageManager(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (MineBlock block : this.config.getBlocks()) {
            if (block != null) {
                createBlock(block);
                StorageData storageData = this.storageManager.load(block.getName());
                if (storageData != null) {
                    storageData.cloneTo(block);
                }
            }
        }
        Bukkit.getPluginManager().registerEvents(new MineBlocksListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LocationPicker(), this);
        Bukkit.getPluginManager().registerEvents(new ConversationUtil(), this);
        this.particleExecutor = new ParticleExecutor(this);
        metrics.addCustomChart(new Metrics.SimplePie("blocks", () -> String.valueOf(MineBlocksPlugin.this.blocks.size())));
        metrics.addCustomChart(new Metrics.SimplePie("hologram_plugin", () -> hologramManager.getHologramPlugin().getPluginName()));
        metrics.addCustomChart(new Metrics.AdvancedPie("afk_plugins", () -> {
            Map<String, Integer> data = new HashMap<>();
            for (String afkPlugin : afkAdapter.getAfkPlugins()) {
                data.put(afkPlugin, 1);
            }
            return data;
        }));
        running = true;
        getLogger().info("Enabled successfully!");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) checkVersionNotification(onlinePlayer);
        if (!config.contains("options.hologram-auto-refresh")) {
            config.set("options.hologram-auto-refresh", true);
            try {
                config.save(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (config.getBoolean("options.hologram-auto-refresh", true)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (MineBlock value : blocks.values()) {
                        value.getHologramInstance().refreshAllLines();
                    }
                }
            }.runTaskTimer(this, 0, 50);
            getLogger().info("Auto hologram refresh enabled!");
        }
    }

    public MineBlock getBlock(String s) {
        for (MineBlock value : blocks.values()) {
            if (s.equals(value.getName())) return value;
        }
        return null;
    }

    @Override
    public void onDisable() {
        if (!running) return;
        running = false;
        for (MineBlock block : new ArrayList<>(this.getBlocks())) {
            this.removeBlock(block.getLocation());
            this.storageManager.save(block.getName(), new StorageData(block));
        }
        try {
            this.storageManager.saveStorage(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        particleExecutor.disable();
        getLogger().info("Disabled successfully!");
    }

    public MineBlock getBlock(Location location) {
        return blocks.get(location);
    }

    public boolean createBlock(MineBlock mineBlock) {
        if (mineBlock == null) return false;
        if (blocks.containsKey(mineBlock.getLocation())) return false;
        blocks.put(mineBlock.getLocation(), mineBlock);
        return true;
    }

    public boolean isRegistered(MineBlock mineBlock) {
        return mineBlock.equals(getBlock(mineBlock.getLocation()));
    }

    public void removeBlock(Location location) {
        blocks.remove(location);
    }

    public void removeBlock(String name) {
        removeBlock(getBlock(name).getLocation());
    }

    public Collection<MineBlock> getBlocks() {
        return blocks.values();
    }

    public BlocksConfig getBlockConfig() {
        return config;
    }

    public void reload() throws IOException {
        for (MineBlock block : new ArrayList<>(this.getBlocks())) {
            removeBlock(block.getLocation());
            this.storageManager.save(block.getName(), new StorageData(block));
            this.storageManager.saveStorage(this);
        }
        this.particleExecutor.disable();
        this.config.reload(this);
        for (MineBlock block : this.config.getBlocks()) {
            if (block == null) continue;
            createBlock(block);
            StorageData storageData = this.storageManager.load(block.getName());
            if (storageData != null) {
                storageData.cloneTo(block);
            }
        }
        this.particleExecutor = new ParticleExecutor(this);
    }

    public void softReload() throws IOException {
        for (MineBlock block : new ArrayList<>(this.getBlocks())) {
            removeBlock(block.getLocation());
            this.storageManager.save(block.getName(), new StorageData(block));
            this.storageManager.saveStorage(this);
        }
        this.particleExecutor.disable();
        for (MineBlock block : this.config.getBlocks()) {
            if (block == null) continue;
            createBlock(block);
            StorageData storageData = this.storageManager.load(block.getName());
            if (storageData != null) {
                storageData.cloneTo(block);
            }
        }
        this.particleExecutor = new ParticleExecutor(this);
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public void saveBlocks() {
        config.clearBlocks();
        for (MineBlock value : this.blocks.values()) {
            config.saveBlock(value);
        }
        try {
            config.save(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeLocation(MineBlock mineBlock, Location location) {
        Location old = mineBlock.getLocation().clone();
        this.ignoredBlocks.add(old);
        this.blocks.remove(mineBlock.getLocation());
        this.ignoredBlocks.remove(old);
        this.ignoredBlocks.add(location);
        this.blocks.put(location, mineBlock);
        this.ignoredBlocks.remove(location);
    }

    public ParticleExecutor getParticleExecutor() {
        return particleExecutor;
    }

    public AfkAdapter getAfkAdapter() {
        return afkAdapter;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public void checkVersionNotification(Player player) {
        if (player.hasPermission("mb.admin")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String s = MineBlocksPlugin.this.getDescription().getVersion().toLowerCase(Locale.ROOT);
                    boolean b = false;
                    boolean sound = false;
                    if (getHologramManager().getHologramPlugin() == HologramManager.HologramPluginType.CMI) {
                        MainCommand.message(player, "You are using <#2bb9e0>CMI &ras your hologram plugin in <#2bb9e0>MineBlocks&r!");
                        MainCommand.error(player, "Please note that you are using CMI holograms at your own risk! Please use DecentHolograms for the best experience!");
                        sound = true;
                    }
                    if (s.contains("dev")) {
                        MainCommand.message(player, "You are using <#2bb9e0>development &rversion of <#2bb9e0>MineBlocks&r!");
                        b = true;
                    } else if (s.contains("beta")) {
                        MainCommand.message(player, "You are using <#2bb9e0>beta &rversion of <#2bb9e0>MineBlocks&r!");
                        b = true;
                    }
                    if (b) {
                        MainCommand.error(player, "You should not use this version on a production server!");
                    }
                    if (b || sound) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 5000, .5f);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 5000, 1.5f);
                    }
                }
            }.runTaskLater(this, 20);
        }
    }

}
