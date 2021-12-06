package pl.socketbyte.opengui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUIItemBuilder {

    public static ItemStack getHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull,
                "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}"
        );
    }

    private ItemStack item;
    private ItemMeta meta;

    public GUIItemBuilder(String headValue) {
        this(getHead(headValue));
    }

    public GUIItemBuilder(Material material, int amount) {
        setItem(material, amount);
    }

    public GUIItemBuilder(Material material) {
        setItem(material, 1);
    }

    public GUIItemBuilder() {
        setItem(Material.DIRT, 1);
    }

    public GUIItemBuilder(ItemStack itemStack) {
        setItem(itemStack);
    }

    public GUIItemBuilder(Material material, int amount, int data) {
        setItem(material, amount, (short)data);
    }

    public GUIItemBuilder(Material material, int amount, short data) {
        setItem(material, amount, data);
    }

    public GUIItemBuilder setItem(ItemStack itemStack) {
        item = itemStack;
        meta = item.getItemMeta();
        return this;
    }

    public GUIItemBuilder setItem(Material material, int amount, short data) {
        item = new ItemStack(material, amount, data);
        meta = item.getItemMeta();
        return this;
    }

    public GUIItemBuilder setItem(Material material, int amount) {
        item = new ItemStack(material, amount);
        meta = item.getItemMeta();
        return this;
    }

    public GUIItemBuilder setName(String name) {
        meta.setDisplayName(ColorUtil.fixColor(name));
        update();
        return this;
    }

    public GUIItemBuilder setLore(List<String> lore) {
        meta.setLore(ColorUtil.fixColor(lore));
        update();
        return this;
    }

    public GUIItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(ColorUtil.fixColor(lore)));
        update();
        return this;
    }

    public GUIItemBuilder setEnchantments(List<ItemEnchantment> enchantments) {
        for (ItemEnchantment enchantment : enchantments)
            meta.addEnchant(enchantment.getEnchantment(), enchantment.getLevel(), enchantment.isUnsafe());
        update();
        return this;
    }

    public GUIItemBuilder addFlags(ItemFlag... itemFlags) {
        meta.addItemFlags(itemFlags);
        update();
        return this;
    }

    public GUIItemBuilder setEnchantments(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet())
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        update();
        return this;
    }

    public GUIItemBuilder update() {
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemMeta getMeta() {
        return meta;
    }

}
