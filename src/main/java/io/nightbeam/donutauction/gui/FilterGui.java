package io.nightbeam.donutauction.gui;

import io.nightbeam.donutauction.model.AuctionFilterCategory;
import io.nightbeam.donutauction.model.PlayerAuctionSession;
import io.nightbeam.donutauction.util.ItemBuilder;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class FilterGui extends BaseGui {

    private static final Component TITLE = Component.text("ᴀᴜᴄᴛɪᴏɴ • ғɪʟᴛᴇʀ", NamedTextColor.GOLD);

    private final GuiManager guiManager;
    private final PlayerAuctionSession session;
    private final Map<Integer, AuctionFilterCategory> categories = new HashMap<>();

    public FilterGui(GuiManager guiManager, PlayerAuctionSession session) {
        this.guiManager = guiManager;
        this.session = session;
    }

    @Override
    public Inventory render(Player player) {
        Inventory inventory = attach(Bukkit.createInventory(this, 27, TITLE));
        categories.clear();

        AuctionFilterCategory[] values = {
                AuctionFilterCategory.BLOCKS,
                AuctionFilterCategory.TOOLS,
                AuctionFilterCategory.FOOD,
                AuctionFilterCategory.COMBAT,
                AuctionFilterCategory.POTIONS,
                AuctionFilterCategory.BOOKS,
                AuctionFilterCategory.INGREDIENTS,
                AuctionFilterCategory.UTILITIES
        };

        for (int index = 0; index < values.length; index++) {
            AuctionFilterCategory category = values[index];
            inventory.setItem(index, ItemBuilder.of(category.icon())
                    .name(Component.text(category.displayName(), NamedTextColor.WHITE))
                    .lore(Component.text("Filter auction listings", NamedTextColor.GRAY))
                    .build());
            categories.put(index, category);
        }

        inventory.setItem(22, ItemBuilder.of(Material.BARRIER)
                .name(Component.text("Clear Filter", NamedTextColor.RED))
                .lore(Component.text("Show all listings", NamedTextColor.GRAY))
                .build());
        return inventory;
    }

    @Override
    public void handleClick(Player player, InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }

        int slot = event.getSlot();
        if (slot == 22) {
            session.request(session.request().withFilter(AuctionFilterCategory.ALL));
            guiManager.openAuctionHouse(player, session);
            return;
        }

        AuctionFilterCategory category = categories.get(slot);
        if (category != null) {
            session.request(session.request().withFilter(category));
            guiManager.openAuctionHouse(player, session);
        }
    }
}