package io.nightbeam.donutauction.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemBuilder {

    private final ItemStack itemStack;

    private ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder name(Component component) {
        editMeta(meta -> meta.displayName(component));
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        editMeta(meta -> meta.lore(lore));
        return this;
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        editMeta(meta -> meta.addItemFlags(flags));
        return this;
    }

    public ItemBuilder hideAll() {
        return addFlags(ItemFlag.values());
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = itemStack.getItemMeta();
        consumer.accept(meta);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(Component... loreLines) {
        List<Component> lore = new ArrayList<>();
        for (Component loreLine : loreLines) {
            lore.add(loreLine);
        }
        return lore(lore);
    }

    public ItemStack build() {
        return itemStack;
    }
}