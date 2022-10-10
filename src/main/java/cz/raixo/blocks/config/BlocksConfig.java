package cz.raixo.blocks.config;

import cz.raixo.blocks.effects.Effect;
import cz.raixo.blocks.effects.exceptions.InvalidEffectArgumentsException;
import cz.raixo.blocks.effects.register.EffectRegister;
import cz.raixo.blocks.models.MineBlock;
import cz.raixo.blocks.models.reward.Reward;
import cz.raixo.blocks.models.reward.RewardSection;
import cz.raixo.blocks.util.NumberUtil;
import eu.d0by.utils.Common;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BlocksConfig implements ConfigurationSection {

    private static final String CONFIG_NAME = "config.yml";
    private final Plugin plugin;
    private YamlConfiguration configuration;
    private boolean warnInConfig = false;

    public BlocksConfig(Plugin plugin) throws IOException {
        this.plugin = plugin;
        reload(plugin);
    }

    private void configError(String s) {
        warnInConfig = true;
        plugin.getLogger().log(Level.WARNING, "Configuration error: " + s);
    }

    public void reload(Plugin plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), CONFIG_NAME);
        if (!configFile.exists()) {
            configFile.createNewFile();
            Files.copy(Objects.requireNonNull(plugin.getResource("config.yml")), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        this.configuration = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save(Plugin plugin) throws IOException {
        File configFile = new File(plugin.getDataFolder(), CONFIG_NAME);
        if (!configFile.exists()) configFile.createNewFile();
        configuration.save(configFile);
    }

    public String getColoredString(String path) {
        if (!isString(path)) return null;
        return Common.colorize(Objects.requireNonNull(getString(path)));
    }

    public String getColoredString(String path, String s) {
        String cs = getColoredString(path);
        return cs == null ? Common.colorize(s) : cs;
    }

    public List<MineBlock> getBlocks() {
        this.warnInConfig = false;
        ConfigurationSection blocksSection = getConfigurationSection("blocks");
        if (blocksSection == null) return new LinkedList<>();
        List<MineBlock> blocks = new LinkedList<>();
        for (String key : blocksSection.getKeys(false)) {
            blocks.add(getBlock(key));
        }
        if (this.warnInConfig) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("mb.admin") || onlinePlayer.isOp()) {
                    onlinePlayer.sendMessage(Common.colorize("<#e84646>You have an error in your mine block configuration!\nCheck console for more details!"));
                }
            }
        }
        return blocks;
    }

    public MineBlock getBlock(String name) {
        MineBlock mineBlock = new MineBlock();
        ConfigurationSection blockConfig = getConfigurationSection("blocks." + name);
        if (blockConfig == null) return null;
        mineBlock.setName(name);
        try {
            Material mat = Material.valueOf(blockConfig.getString("type"));
            if (!mat.isBlock()) throw new IllegalArgumentException("Type must be a block");
            mineBlock.setBlockType(mat);
        } catch (Throwable t) {
            configError("Block '" + name + "' has invalid type");
            return null;
        }
        try {
            String cooldownTypeString = blockConfig.getString("cooldownType");
            if (cooldownTypeString != null) {
                Material cooldownType = Material.valueOf(cooldownTypeString);
                if (!cooldownType.isBlock()) throw new IllegalArgumentException("Cooldown type must be a block");
                mineBlock.setCooldownBlock(cooldownType);
            }
        } catch (Throwable t) {
            configError("Block '" + name + "' has invalid cooldown type");
            return null;
        }
        if (blockConfig.isInt("timeout")) {
            mineBlock.setBlockSeconds(blockConfig.getInt("timeout"));
        }
        if (!blockConfig.isList("hologram")) {
            configError("Block '" + name + "' has invalid hologram");
            return null;
        }
        mineBlock.setHologram(blockConfig.getStringList("hologram"));
        if (!blockConfig.isString("breakMessage")) {
            configError("Block '" + name + "' has invalid breakMessage");
            return null;
        }
        mineBlock.setBreakMessage(Common.colorize(blockConfig.getString("breakMessage", "")));
        if (!blockConfig.isString("respawnMessage")) {
            configError("Block '" + name + "' has invalid respawnMessage");
            return null;
        }
        mineBlock.setRespawnMessage(Common.colorize(blockConfig.getString("respawnMessage", "")));
        if (!blockConfig.isInt("health")) {
            configError("Block '" + name + "' has invalid health");
            return null;
        }
        mineBlock.setMaxHealth(blockConfig.getInt("health"));
        List<RewardSection> rewards = getRewards(name);
        if (rewards != null) {
            mineBlock.setRewards(rewards);
        }
        mineBlock.setTopRewards(getTopRewards(name));
        mineBlock.setLastBreakRewards(getLastBreakRewards(name));
        mineBlock.setBreakRewards(getBreakRewards(name));
        try {
            ConfigurationSection locationSection = blockConfig.getConfigurationSection("location");
            assert locationSection != null;
            World world = Bukkit.getWorld(locationSection.getString("world", ""));
            if (world == null) {
                configError("Block '" + name + "' has invalid location (world)");
                return null;
            }
            Location location = new Location(
                    world,
                    locationSection.getInt("x"),
                    locationSection.getInt("y"),
                    locationSection.getInt("z")
            );
            mineBlock.setLocation(location);
        } catch (Throwable t) {
            t.printStackTrace();
            configError("Block '" + name + "' has invalid location");
            return null;
        }
        if (blockConfig.isList("effects")) {
            List<String> effectsConfig = blockConfig.getStringList("effects");
            for (String s : effectsConfig) {
                try {
                    Effect effect = EffectRegister.create(s);
                    if (effect == null) {
                        configError("Block '" + name + "' has invalid effect '" + s + "'");
                    } else mineBlock.addEffect(effect);
                } catch (InvalidEffectArgumentsException ex) {
                    configError("Block '" + name + "' has invalid effect arguments '" + s + "'");
                } catch (Throwable t) {
                    configError("Block '" + name + "' has invalid effect '" + s + "'");
                }
            }
        }
        return mineBlock;
    }

    private List<RewardSection> getRewards(String name) {
        try {
            ConfigurationSection blockConfig = getConfigurationSection("blocks." + name + ".rewards");
            if (blockConfig == null) return new LinkedList<>();
            List<RewardSection> rewards = new LinkedList<>();
            for (String key : blockConfig.getKeys(false)) {
                if (blockConfig.isList(key)) {
                    String[] fromTo = key.split("-", 3);
                    if (fromTo.length < 2) {
                        configError("Block '" + name + "' has invalid reward section name '" + key + "'");
                        continue;
                    }
                    Optional<Integer> from = NumberUtil.parseInt(fromTo[0]);
                    Optional<Integer> to = NumberUtil.parseInt(fromTo[1]);
                    if (from.isEmpty() || to.isEmpty()) {
                        configError("Block '" + name + "' has invalid reward section name '" + key + "'");
                        continue;
                    }
                    String sectionName = "";
                    if (fromTo.length > 2) sectionName = fromTo[2];
                    RewardSection rewardSection = new RewardSection(from.get(), to.get(), sectionName);
                    for (String s : blockConfig.getStringList(key)) {
                        String[] data = s.split(";", 2);
                        if (data.length < 2) {
                            configError("Block '" + name + "' has invalid reward '" + s + "' in reward section '" + key + "'");
                            continue;
                        }
                        Optional<Integer> chance = NumberUtil.parseInt(data[0]);
                        if (chance.isEmpty()) {
                            configError("Block '" + name + "' has invalid chance '" + data[0] + "' in reward '" + s + "' in reward section '" + key + "'");
                            continue;
                        }
                        rewardSection.addReward(
                                new Reward(chance.get(), data[1])
                        );
                    }
                    rewards.add(rewardSection);
                }
            }
            return rewards;
        } catch (Throwable t) {
            configError("Block '" + name + "' has invalid rewards");
            return null;
        }
    }

    private Map<Integer, List<Reward>> getTopRewards(String name) {
        ConfigurationSection rewardsConfig = getConfigurationSection("blocks." + name + ".topRewards");
        if (rewardsConfig == null) return Collections.emptyMap();
        Map<Integer, List<Reward>> rewardsMap = new HashMap<>();
        for (String key : rewardsConfig.getKeys(false)) {
            NumberUtil.parseInt(key).ifPresent(pos -> rewardsMap.put(pos, rewardsConfig.getStringList(key).stream().map(s -> {
                String[] data = s.split(";", 2);
                if (data.length < 2) {
                    configError("Block '" + name + "' has invalid reward '" + s + "' in top reward '" + key + "'");
                    return null;
                }
                Optional<Integer> chance = NumberUtil.parseInt(data[0]);
                if (chance.isEmpty()) {
                    configError("Block '" + name + "' has invalid chance '" + data[0] + "' in reward '" + s + "' in top reward '" + key + "'");
                    return null;
                }
                return new Reward(chance.get(), data[1]);
            }).filter(Objects::nonNull).collect(Collectors.toList())));
        }
        return rewardsMap;
    }

    private List<Reward> getLastBreakRewards(String name) {
        if (!isList("blocks." + name + ".lastBreakReward")) return Collections.emptyList();
        return getStringList("blocks." + name + ".lastBreakReward").stream().map(s -> {
            String[] data = s.split(";", 2);
            if (data.length < 2) {
                configError("Block '" + name + "' has invalid reward '" + s + "' in last break reward");
                return null;
            }
            Optional<Integer> chance = NumberUtil.parseInt(data[0]);
            if (chance.isEmpty()) {
                configError("Block '" + name + "' has invalid chance '" + data[0] + "' in reward '" + s + "' in last break reward");
                return null;
            }
            return new Reward(chance.get(), data[1]);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Reward> getBreakRewards(String name) {
        if (!isList("blocks." + name + ".breakReward")) return Collections.emptyList();
        return getStringList("blocks." + name + ".breakReward").stream().map(s -> {
            String[] data = s.split(";", 2);
            if (data.length < 2) {
                configError("Block '" + name + "' has invalid reward '" + s + "' in break reward");
                return null;
            }
            Optional<Integer> chance = NumberUtil.parseInt(data[0]);
            if (chance.isEmpty()) {
                configError("Block '" + name + "' has invalid chance '" + data[0] + "' in reward '" + s + "' in break reward");
                return null;
            }
            return new Reward(chance.get(), data[1]);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void saveTopRewards(String name, Map<Integer, List<Reward>> rewards) {
        ConfigurationSection rewardsConfig = createSection("blocks." + name + ".topRewards");
        for (Map.Entry<Integer, List<Reward>> entry : rewards.entrySet()) {
            rewardsConfig.set(entry.getKey().toString(),
                    entry.getValue().stream().map(reward -> reward.getChance() + ";" + reward.getCommand())
                            .collect(Collectors.toList())
            );
        }
    }

    private void saveLastBreakRewards(String name, List<Reward> rewards) {
        set("blocks." + name + ".lastBreakReward",
                rewards.stream().map(reward -> reward.getChance() + ";" + reward.getCommand())
                        .collect(Collectors.toList())
        );
    }

    private void saveBreakRewards(String name, List<Reward> rewards) {
        set("blocks." + name + ".breakReward",
                rewards.stream().map(reward -> reward.getChance() + ";" + reward.getCommand())
                        .collect(Collectors.toList())
        );
    }

    public void clearBlocks() {
        createSection("blocks");
    }

    public void saveBlock(MineBlock mineBlock) {
        ConfigurationSection blockConfig = createSection("blocks." + mineBlock.getName());
        ConfigurationSection locationSection = blockConfig.createSection("location");
        Location location = mineBlock.getLocation();
        locationSection.set("world", Objects.requireNonNull(location.getWorld()).getName());
        locationSection.set("x", location.getBlockX());
        locationSection.set("y", location.getBlockY());
        locationSection.set("z", location.getBlockZ());
        blockConfig.set("type", mineBlock.getBlockType().name());
        blockConfig.set("cooldownType", Optional.ofNullable(mineBlock.getCooldownBlock()).map(Material::name).orElse(null));
        blockConfig.set("hologram", mineBlock.getHologram());
        blockConfig.set("breakMessage", mineBlock.getBreakMessage());
        blockConfig.set("respawnMessage", mineBlock.getRespawnMessage());
        if (mineBlock.getBlockSeconds() > 0) {
            blockConfig.set("timeout", mineBlock.getBlockSeconds());
        }
        blockConfig.set("health", mineBlock.getMaxHealth());
        List<String> effects = mineBlock.getEffects().stream().map(EffectRegister::save).filter(Objects::nonNull).collect(Collectors.toList());
        if (!effects.isEmpty()) blockConfig.set("effects", effects);
        saveRewards(mineBlock.getName(), mineBlock.getRewards());
        saveTopRewards(mineBlock.getName(), mineBlock.getTopRewards());
        saveLastBreakRewards(mineBlock.getName(), mineBlock.getLastBreakRewards());
        saveBreakRewards(mineBlock.getName(), mineBlock.getBreakRewards());
    }

    private void saveRewards(String name, List<RewardSection> rewardSections) {
        ConfigurationSection rewardsSection = createSection("blocks." + name + ".rewards");
        for (RewardSection rewardSection : rewardSections) {
            rewardsSection.set(rewardSection.getFrom() + "-" + rewardSection.getTo() + (rewardSection.getName() == null || rewardSection.getName().equals("") ? "" : "-" + rewardSection.getName()), rewardSection.toStringList());
        }
    }

    @Override
    public Set<String> getKeys(boolean b) {
        return configuration.getKeys(b);
    }

    @Override
    public Map<String, Object> getValues(boolean b) {
        return configuration.getValues(b);
    }

    @Override
    public boolean contains(String s) {
        return configuration.contains(s);
    }

    @Override
    public boolean contains(String s, boolean b) {
        return configuration.contains(s, b);
    }

    @Override
    public boolean isSet(String s) {
        return configuration.isSet(s);
    }

    @Override
    public String getCurrentPath() {
        return configuration.getCurrentPath();
    }

    @Override
    public @NotNull String getName() {
        return configuration.getName();
    }

    @Override
    public Configuration getRoot() {
        return configuration.getRoot();
    }

    @Override
    public ConfigurationSection getParent() {
        return configuration.getParent();
    }

    @Override
    public Object get(String s) {
        return configuration.get(s);
    }

    @Override
    public Object get(String s, Object o) {
        return configuration.get(s, o);
    }

    @Override
    public void set(String s, Object o) {
        configuration.set(s, o);
    }

    @Override
    public ConfigurationSection createSection(String s) {
        return configuration.createSection(s);
    }

    @Override
    public ConfigurationSection createSection(String s, Map<?, ?> map) {
        return configuration.createSection(s, map);
    }

    @Override
    public String getString(String s) {
        return configuration.getString(s);
    }

    @Override
    public String getString(String s, String s1) {
        return configuration.getString(s, s1);
    }

    @Override
    public boolean isString(String s) {
        return configuration.isString(s);
    }

    @Override
    public int getInt(String s) {
        return configuration.getInt(s);
    }

    @Override
    public int getInt(String s, int i) {
        return configuration.getInt(s, i);
    }

    @Override
    public boolean isInt(String s) {
        return configuration.isInt(s);
    }

    @Override
    public boolean getBoolean(String s) {
        return configuration.getBoolean(s);
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return configuration.getBoolean(s, b);
    }

    @Override
    public boolean isBoolean(String s) {
        return configuration.isBoolean(s);
    }

    @Override
    public double getDouble(String s) {
        return configuration.getDouble(s);
    }

    @Override
    public double getDouble(String s, double v) {
        return configuration.getDouble(s, v);
    }

    @Override
    public boolean isDouble(String s) {
        return configuration.isDouble(s);
    }

    @Override
    public long getLong(String s) {
        return configuration.getLong(s);
    }

    @Override
    public long getLong(String s, long l) {
        return configuration.getLong(s, l);
    }

    @Override
    public boolean isLong(String s) {
        return configuration.isLong(s);
    }

    @Override
    public List<?> getList(String s) {
        return configuration.getList(s);
    }

    @Override
    public List<?> getList(String s, List<?> list) {
        return configuration.getList(s, list);
    }

    @Override
    public boolean isList(String s) {
        return configuration.isList(s);
    }

    @Override
    public List<String> getStringList(String s) {
        return configuration.getStringList(s);
    }

    @Override
    public List<Integer> getIntegerList(String s) {
        return configuration.getIntegerList(s);
    }

    @Override
    public List<Boolean> getBooleanList(String s) {
        return configuration.getBooleanList(s);
    }

    @Override
    public List<Double> getDoubleList(String s) {
        return configuration.getDoubleList(s);
    }

    @Override
    public List<Float> getFloatList(String s) {
        return configuration.getFloatList(s);
    }

    @Override
    public List<Long> getLongList(String s) {
        return configuration.getLongList(s);
    }

    @Override
    public List<Byte> getByteList(String s) {
        return configuration.getByteList(s);
    }

    @Override
    public List<Character> getCharacterList(String s) {
        return configuration.getCharacterList(s);
    }

    @Override
    public List<Short> getShortList(String s) {
        return configuration.getShortList(s);
    }

    @Override
    public List<Map<?, ?>> getMapList(String s) {
        return configuration.getMapList(s);
    }

    @Override
    public <T> T getObject(String s, Class<T> aClass) {
        return configuration.getObject(s, aClass);
    }

    @Override
    public <T> T getObject(String s, Class<T> aClass, T t) {
        return configuration.getObject(s, aClass, t);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String s, Class<T> aClass) {
        return configuration.getSerializable(s, aClass);
    }

    @Override
    public <T extends ConfigurationSerializable> T getSerializable(String s, Class<T> aClass, T t) {
        return configuration.getSerializable(s, aClass, t);
    }

    @Override
    public Vector getVector(String s) {
        return configuration.getVector(s);
    }

    @Override
    public Vector getVector(String s, Vector vector) {
        return configuration.getVector(s, vector);
    }

    @Override
    public boolean isVector(String s) {
        return configuration.isVector(s);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String s) {
        return configuration.getOfflinePlayer(s);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String s, OfflinePlayer offlinePlayer) {
        return configuration.getOfflinePlayer(s, offlinePlayer);
    }

    @Override
    public boolean isOfflinePlayer(String s) {
        return configuration.isOfflinePlayer(s);
    }

    @Override
    public ItemStack getItemStack(String s) {
        return configuration.getItemStack(s);
    }

    @Override
    public ItemStack getItemStack(String s, ItemStack itemStack) {
        return configuration.getItemStack(s, itemStack);
    }

    @Override
    public boolean isItemStack(String s) {
        return configuration.isItemStack(s);
    }

    @Override
    public Color getColor(String s) {
        return configuration.getColor(s);
    }

    @Override
    public Color getColor(String s, Color color) {
        return configuration.getColor(s, color);
    }

    @Override
    public boolean isColor(String s) {
        return configuration.isColor(s);
    }

    @Override
    public Location getLocation(String s) {
        return configuration.getLocation(s);
    }

    @Override
    public Location getLocation(String s, Location location) {
        return configuration.getLocation(s, location);
    }

    @Override
    public boolean isLocation(String s) {
        return configuration.isLocation(s);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String s) {
        return configuration.getConfigurationSection(s);
    }

    @Override
    public boolean isConfigurationSection(String s) {
        return configuration.isConfigurationSection(s);
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return configuration.getDefaultSection();
    }

    @Override
    public void addDefault(String s, Object o) {
        configuration.addDefault(s, o);
    }
}
