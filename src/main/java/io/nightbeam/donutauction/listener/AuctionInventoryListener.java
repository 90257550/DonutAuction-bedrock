package io.nightbeam.donutauction.listener;

import io.nightbeam.donutauction.gui.BaseGui;
import io.nightbeam.donutauction.gui.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class AuctionInventoryListener implements Listener {

    private final GuiManager guiManager;

    public AuctionInventoryListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        guiManager.handleInventoryClick(player, event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder(false) instanceof io.nightbeam.donutauction.gui.BaseGui) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder(false) instanceof BaseGui)) {
            return;
        }

        guiManager.plugin().schedulerAdapter().runEntity(player, () -> {
            if (guiManager.isAwaitingSearch(player.getUniqueId())) {
                return;
            }
            if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof BaseGui) {
                return;
            }
            guiManager.resetSearchFilter(player);
        });
    }
}