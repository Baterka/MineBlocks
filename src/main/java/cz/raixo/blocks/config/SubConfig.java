package cz.raixo.blocks.config;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubConfig implements ConfigurationSection {

    private final ConfigurationSection configuration;

    public SubConfig(ConfigurationSection configuration) {
        this.configuration = configuration;
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
        return new SubConfig(configuration.createSection(s));
    }

    @Override
    public ConfigurationSection createSection(String s, Map<?, ?> map) {
        return new SubConfig(configuration.createSection(s, map));
    }

    @Override
    public String getString(String s) {
        if (configuration.isList(s)) return String.join("<nl>", configuration.getStringList(s));
        return configuration.getString(s);
    }

    @Override
    public String getString(String s, String s1) {
        if (configuration.isList(s)) return String.join("<nl>", configuration.getStringList(s));
        return configuration.getString(s, s1);
    }

    @Override
    public boolean isString(String s) {
        return configuration.isString(s) || configuration.isList(s);
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
        return new SubConfig(configuration.getConfigurationSection(s));
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
