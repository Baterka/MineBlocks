package cz.raixo.blocks.models;

import cz.raixo.blocks.MineBlocksPlugin;
import cz.raixo.blocks.config.BlocksConfig;
import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.hologram.Hologram;
import cz.raixo.blocks.models.player.PlayerData;
import cz.raixo.blocks.models.reward.PlayerRewardData;
import cz.raixo.blocks.models.reward.Reward;
import cz.raixo.blocks.models.reward.RewardSection;
import cz.raixo.blocks.storage.StorageData;
import cz.raixo.blocks.util.Cooldown;
import cz.raixo.blocks.util.Placeholder;
import cz.raixo.blocks.util.SimpleRandom;
import eu.d0by.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MineBlock {

    private final Map<UUID, PlayerRewardData> rewardData = new HashMap<>();
    private final List<Effect> effects = new LinkedList<>();
    private final Cooldown topPlayersUpdateCooldown = new Cooldown(TimeUnit.SECONDS, 1);
    private String name;
    private Location location;
    private Material blockType = Material.AIR;
    private Material cooldownBlock = null;
    private String permission;
    private List<String> hologram = new LinkedList<>();
    private Hologram hologramInstance;
    private String breakMessage = "";
    private String respawnMessage = "";
    private long maxHealth = 0;
    private long health = maxHealth;
    private List<RewardSection> rewards = new LinkedList<>();
    private Map<Integer, List<Reward>> topRewards = new HashMap<>();
    private List<Reward> lastBreakRewards = new LinkedList<>();
    private List<Reward> breakRewards = new LinkedList<>();
    private List<PlayerRewardData> topPlayers = new ArrayList<>();
    private int blockMinutes = 0;
    private Date blockedUntil;
    private BukkitTask respawnTask;
    private boolean unloaded = false;

    public void onBreak(Player player) {
        if (isUnloaded()) return;
        UUID uuid = player.getUniqueId();
        rewardData.computeIfAbsent(uuid, k -> new PlayerRewardData(player));
        rewardData.get(uuid).addBreak();
        health--;
        giveBreakRewards(player.getName());
        if (health <= 0) {
            giveLastBreakRewards(player.getName());
            onBreak();
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTopPlayers();
                refreshHologram();
            }
        }.runTaskAsynchronously(MineBlocksPlugin.getInstance());
    }

    public void onBreak() {
        if (isUnloaded()) return;
        saveBreakLog();
        health = maxHealth;
        if (getBlockSeconds() > 0) {
            setBlockedUntil(new Date(System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(this.blockMinutes, TimeUnit.SECONDS)));
        }
        new BukkitRunnable() {

            private final List<PlayerRewardData> topPlayers = new ArrayList<>(MineBlock.this.topPlayers);
            private final Map<UUID, PlayerRewardData> rewardData = new LinkedHashMap<>(MineBlock.this.rewardData);

            @Override
            public void run() {
                if (!MineBlock.this.breakMessage.equals("")) {
                    String message = MineBlock.this.breakMessage.replace("<nl>", "\n");
                    message = message
                            .replace("%max_health%", String.valueOf(MineBlock.this.maxHealth))
                            .replace("%players%", String.valueOf(this.rewardData.size()))
                    ;
                    message = parseTopPlayers(message, topPlayers);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        String playerMessage = message
                                .replace("%breaks%", String.valueOf(rewardData.getOrDefault(onlinePlayer.getUniqueId(), new PlayerRewardData(onlinePlayer)).getBreaks()))
                                .replace("%player%", onlinePlayer.getName());
                        onlinePlayer.sendMessage(playerMessage);
                    }
                }
                for (PlayerRewardData value : new LinkedList<>(rewardData.values())) {
                    value.reward(MineBlock.this);
                }
                for (int i = 0; i < topPlayers.size(); i++) {
                    if (topRewards.containsKey(i + 1)) {
                        List<Reward> topRewardList = topRewards.get(i + 1);
                        if (topRewardList.isEmpty()) continue;
                        PlayerRewardData playerData = topPlayers.get(i);
                        SimpleRandom<Reward> rewardRandom = new SimpleRandom<>();
                        for (Reward reward : topRewardList) {
                            rewardRandom.add(reward.getChance(), reward);
                        }
                        rewardRandom.next().executeFor(playerData.getPlayerData().getName());
                    }
                }
            }
        }.runTask(MineBlocksPlugin.getInstance());
        rewardData.clear();
        if (!isBlocked())
            topPlayers.clear();
    }

    public void updateTopPlayers() {
        updateTopPlayers(false);
    }

    public void updateTopPlayers(boolean force) {
        if (topPlayersUpdateCooldown.canUse() || force) {
            topPlayersUpdateCooldown.use();
            this.topPlayers = this.rewardData.entrySet().stream().sorted(
                    Comparator.comparingInt(value -> {
                        if (value != null) {
                            Map.Entry entry = (Map.Entry) value;
                            if (entry.getValue() instanceof PlayerRewardData) {
                                return ((PlayerRewardData) entry.getValue()).getBreaks();
                            }
                        }
                        return 0;
                    }).reversed()
            ).limit(10).filter(Objects::nonNull).map(Map.Entry::getValue).collect(Collectors.toList());
        }
    }

    public void setBlock() {
        if (isUnloaded()) return;
        if (location != null && blockType != null)
            this.location.getBlock().setType(this.blockType);
    }

    public void createHologram() {
        if (isUnloaded()) return;
        removeHologram();
        if (location == null) return;
        Hologram hologram = MineBlocksPlugin.getInstance().getHologramManager().createHologram(this.location.clone().add(.5, 1, .5));
        for (String s : this.hologram) {
            hologram.addLine(parseHoloLine(s));
        }
        hologram.setLocation(hologram.getLocation().add(0, hologram.getHeight() + .5, 0));
        hologram.realignLines();
        this.hologramInstance = hologram;
        hologram.show(Bukkit.getOnlinePlayers().toArray(Player[]::new));
    }

    private String parseHoloLine(String line) {
        return parseTopPlayers(
                line
                        .replace("%health%", String.valueOf(this.health))
                        .replace("%max_health%", String.valueOf(this.maxHealth))
                        .replace("%name%", this.name)
                        .replace("%type%", this.blockType == null ? "null" : this.blockType.name())
                        .replace("%timeout%", getBlockedUntilString())
        );
    }

    private String parseTopPlayers(String message) {
        return parseTopPlayers(message, getTopPlayers());
    }

    private String parseTopPlayers(String message, List<PlayerRewardData> players) {
        Map<String, String> data = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            if (i < players.size()) {
                data.put("player_" + (i + 1), players.get(i).getPlayerData().getDisplayName());
                data.put("player_" + (i + 1) + "_breaks", String.valueOf(players.get(i).getBreaks()));
            } else {
                data.put("player_" + (i + 1), Common.colorize(MineBlocksPlugin.getInstance().getBlockConfig().getString("lang.top.nobody", "&cNobody")));
                data.put("player_" + (i + 1) + "_breaks", MineBlocksPlugin.getInstance().getBlockConfig().getString("lang.top.nobody-breaks", "0"));
            }
        }
        return Placeholder.translate(message, data);
    }

    public void removeBlock() {
        if (this.location != null)
            this.location.getBlock().setType(Material.AIR);
    }

    public void removeHologram() {
        if (this.hologramInstance != null) {
            this.hologramInstance.delete();
            this.hologramInstance = null;
        }
    }

    public void refreshHologram() {
        if (isUnloaded()) return;
        if (this.hologramInstance == null) {
            createHologram();
            return;
        }
        for (int i = 0; i < this.hologram.size(); i++) {
            String newLine = parseHoloLine(this.hologram.get(i));
            if (!this.hologramInstance.getLine(i).equalsIgnoreCase(newLine))
                this.hologramInstance.setLine(i, newLine);
        }
    }

    public void showHologram(Player... players) {
        if (hologramInstance != null) {
            hologramInstance.show(players);
        }
    }

    public void hideHologram(Player... players) {
        if (hologramInstance != null) {
            hologramInstance.hide(players);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        if (!MineBlocksPlugin.getInstance().isRegistered(this)) {
            this.location = null;
        }
        if (MineBlocksPlugin.getInstance().getBlock(location) != null) return;
        if (this.location != null) {
            removeBlock();
            removeHologram();
            MineBlocksPlugin.getInstance().changeLocation(this, location.clone());
        }
        this.location = location.clone();
        setBlock();
        createHologram();
    }

    public Material getBlockType() {
        return blockType;
    }

    public void setBlockType(Material blockType) {
        this.blockType = blockType;
        refreshHologram();
        setBlock();
    }

    public Material getCooldownBlock() {
        return cooldownBlock;
    }

    public void setCooldownBlock(Material cooldownBlock) {
        this.cooldownBlock = cooldownBlock;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getHologram() {
        return hologram;
    }

    public void setHologram(List<String> hologram) {
        this.hologram = hologram;
        refreshHologram();
    }

    public String getBreakMessage() {
        return breakMessage;
    }

    public void setBreakMessage(String breakMessage) {
        this.breakMessage = breakMessage;
    }

    public String getRespawnMessage() {
        return respawnMessage;
    }

    public void setRespawnMessage(String respawnMessage) {
        this.respawnMessage = respawnMessage;
    }

    public long getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(long maxHealth) {
        this.maxHealth = maxHealth;
        setHealth(maxHealth);
    }

    public long getHealth() {
        return health;
    }

    public void setHealth(long health) {
        this.health = health;
        refreshHologram();
    }

    public Map<UUID, PlayerRewardData> getRewardData() {
        return rewardData;
    }

    public List<RewardSection> getRewards() {
        return rewards;
    }

    public void setRewards(List<RewardSection> rewards) {
        this.rewards = rewards;
    }

    public Map<Integer, List<Reward>> getTopRewards() {
        return topRewards;
    }

    public void setTopRewards(Map<Integer, List<Reward>> topRewards) {
        this.topRewards = topRewards;
    }

    public List<Reward> getLastBreakRewards() {
        return lastBreakRewards;
    }

    public void setLastBreakRewards(List<Reward> lastBreakRewards) {
        this.lastBreakRewards = lastBreakRewards;
    }

    public List<Reward> getBreakRewards() {
        return breakRewards;
    }

    public void setBreakRewards(List<Reward> breakRewards) {
        this.breakRewards = breakRewards;
    }

    public List<PlayerRewardData> getTopPlayers() {
        return new ArrayList<>(topPlayers);
    }

    public Hologram getHologramInstance() {
        return hologramInstance;
    }

    public boolean isBlocked() {
        Date blocked = getBlockedUntil();
        if (blocked == null) return false;
        return getBlockedUntil().getTime() - System.currentTimeMillis() >= 1000;
    }

    public Date getBlockedUntil() {
        return blockedUntil;
    }

    public void setBlockedUntil(Date blockedUntil) {
        this.blockedUntil = blockedUntil;
        if (respawnTask != null) {
            respawnTask.cancel();
            respawnTask = null;
        }
        if (isBlocked()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!MineBlock.this.isBlocked()) {
                        topPlayers.clear();
                        cancel();
                    }
                    refreshHologram();
                }
            }.runTaskTimer(MineBlocksPlugin.getInstance(), 0, 20);

            Material cooldownBlock = getCooldownBlock();
            if (cooldownBlock != null) getLocation().getBlock().setType(cooldownBlock);
            long time = getBlockedUntil().getTime() - System.currentTimeMillis();
            time /= 50;
            if (time > 0) {
                respawnTask = Bukkit.getScheduler().runTaskLater(MineBlocksPlugin.getInstance(), () -> {
                    if (!MineBlock.this.respawnMessage.equals("")) {
                        Bukkit.broadcastMessage(Common.colorize(MineBlock.this.respawnMessage.replace("<nl>", "\n")));
                    }
                    getLocation().getBlock().setType(blockType);
                }, time);
            }
        }
    }

    protected void giveLastBreakRewards(String name) {
        if (lastBreakRewards.isEmpty()) return;
        SimpleRandom<Reward> rewardRandom = new SimpleRandom<>();
        for (Reward reward : lastBreakRewards) {
            rewardRandom.add(reward.getChance(), reward);
        }
        rewardRandom.next().executeFor(name);
    }

    protected void giveBreakRewards(String name) {
        if (breakRewards.isEmpty()) return;
        SimpleRandom<Reward> rewardRandom = new SimpleRandom<>();
        for (Reward reward : breakRewards) {
            rewardRandom.add(reward.getChance(), reward);
        }
        rewardRandom.next().executeFor(name);
    }

    private static final long HOUR_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);
    private static final long MINUTE_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final long SECOND_MS = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);

    public String getBlockedUntilString() {
        if (getBlockedUntil() == null) return "";
        Date relative = new Date(getBlockedUntil().getTime() - System.currentTimeMillis());
        if (relative.getTime() < 0) return "";
        long a = relative.getTime();
        BlocksConfig config = MineBlocksPlugin.getInstance().getBlockConfig();
        String n = config.getString("lang.timeout-prefix", "");
        long hours = a / HOUR_MS;
        a = a % HOUR_MS;
        int minutes = (int) (a / MINUTE_MS);
        a = a % MINUTE_MS;
        int seconds = (int) (a / SECOND_MS);
        if (hours > 0) n += hours + " " + (hours == 1 ? config.getString("lang.timeout-units.hour", "hour") : config.getString("lang.timeout-units.hours", "hours")) + " ";
        if (minutes > 0) n += minutes + " " + (minutes == 1 ? config.getString("lang.timeout-units.minute", "minute") : config.getString("lang.timeout-units.minutes", "minutes")) + " ";
        if (seconds > 0) n += seconds + " " + (seconds == 1 ? config.getString("lang.timeout-units.second", "second") : config.getString("lang.timeout-units.seconds", "seconds")) + " ";
        if (n.endsWith(" ")) n = n.substring(0, n.length() - 1);
        return n;
    }

    public int getBlockSeconds() {
        return blockMinutes;
    }

    public void setBlockSeconds(int blockMinutes) {
        this.blockMinutes = blockMinutes;
    }

    public boolean isUnloaded() {
        return unloaded;
    }

    public void setUnloaded(boolean unloaded) {
        this.unloaded = unloaded;
    }

    public void setRewardData(UUID player, PlayerRewardData rewardData) {
        this.rewardData.put(player, rewardData);
    }

    public void addEffect(Effect effect) {
        if (effect == null) return;
        effects.add(effect);
    }

    public void addEffectAndRun(Effect effect) {
        if (effect == null) return;
        addEffect(effect);
        MineBlocksPlugin.getInstance().getParticleExecutor().runEffect(effect, this);
    }

    public List<Effect> getEffects() {
        return effects;
    }

    public void reset() {
        setBlockedUntil(new Date(0));
        health = maxHealth;
        rewardData.clear();
        topPlayers.clear();
        refreshHologram();
    }

    public JSONArray getRewardDataAsJson() {
        return new StorageData(this).toJson().getJSONArray("rewardData");
    }

    public void saveBreakLog() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk-mm");
        String name = simpleDateFormat.format(date).replace(" ", "_") + "_" + this.getName();
        File logStorage = new File(MineBlocksPlugin.getInstance().getDataFolder(), "logs");
        if (!logStorage.exists()) logStorage.mkdirs();
        File logFile = new File(logStorage, name + ".json");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.writeString(logFile.toPath(), getRewardDataAsJson().toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
