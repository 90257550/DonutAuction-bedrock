package io.nightbeam.donutauction.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AuctionFilterCategory {
    ALL("All", Material.COMPASS),
    BLOCKS("Blocks", Material.GRASS_BLOCK),
    TOOLS("Tools", Material.DIAMOND_PICKAXE),
    FOOD("Food", Material.GOLDEN_CARROT),
    COMBAT("Combat", Material.DIAMOND_SWORD),
    POTIONS("Potions", Material.POTION),
    BOOKS("Books", Material.ENCHANTED_BOOK),
    INGREDIENTS("Ingredients", Material.BLAZE_POWDER),
    UTILITIES("Utilities", Material.ENDER_CHEST);

    private final String displayName;
    private final Material icon;

    AuctionFilterCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public boolean matches(ItemStack itemStack) {
        Material material = itemStack.getType();
        return switch (this) {
            case ALL -> true;
            case BLOCKS -> material.isBlock();
            case TOOLS -> material.name().endsWith("_AXE") || material.name().endsWith("_PICKAXE")
                    || material.name().endsWith("_SHOVEL") || material.name().endsWith("_HOE")
                    || material == Material.SHEARS || material == Material.FLINT_AND_STEEL || material == Material.FISHING_ROD;
            case FOOD -> material.isEdible();
            case COMBAT -> material.name().endsWith("_SWORD") || material.name().endsWith("_AXE")
                    || material.name().endsWith("_HELMET") || material.name().endsWith("_CHESTPLATE")
                    || material.name().endsWith("_LEGGINGS") || material.name().endsWith("_BOOTS")
                    || material.name().endsWith("_BOW") || material == Material.CROSSBOW
                    || material == Material.TRIDENT || material == Material.SHIELD;
            case POTIONS -> material == Material.POTION || material == Material.SPLASH_POTION
                    || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW;
            case BOOKS -> material == Material.BOOK || material == Material.WRITABLE_BOOK
                    || material == Material.WRITTEN_BOOK || material == Material.ENCHANTED_BOOK || material == Material.KNOWLEDGE_BOOK;
            case INGREDIENTS -> material == Material.BLAZE_POWDER || material == Material.BLAZE_ROD
                    || material == Material.GUNPOWDER || material == Material.STRING || material == Material.SPIDER_EYE
                    || material == Material.FERMENTED_SPIDER_EYE || material == Material.GLISTERING_MELON_SLICE
                    || material == Material.GHAST_TEAR || material == Material.MAGMA_CREAM || material == Material.RABBIT_FOOT
                    || material == Material.PHANTOM_MEMBRANE || material == Material.SUGAR || material == Material.REDSTONE
                    || material == Material.GLOWSTONE_DUST || material == Material.NETHER_WART;
            case UTILITIES -> material == Material.ENDER_CHEST || material == Material.CHEST || material == Material.BARREL
                    || material == Material.SHULKER_BOX || material.name().endsWith("_SHULKER_BOX")
                    || material == Material.ELYTRA || material == Material.LEAD || material == Material.NAME_TAG
                    || material == Material.COMPASS || material == Material.RECOVERY_COMPASS || material == Material.CLOCK;
        };
    }

    public static Optional<AuctionFilterCategory> fromInput(String input) {
        String normalized = input.toUpperCase(Locale.ENGLISH).replace(' ', '_');
        return Arrays.stream(values()).filter(category -> category.name().equals(normalized)).findFirst();
    }
}