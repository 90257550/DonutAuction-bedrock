package io.nightbeam.donutauction.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class BaseGui implements InventoryHolder {

    private Inventory inventory;

    public abstract Inventory render(Player player);

    public abstract void handleClick(Player player, InventoryClickEvent event);

    @Override
    public final Inventory getInventory() {
        return inventory;
    }

    protected final Inventory attach(Inventory inventory) {
        this.inventory = inventory;
        return inventory;
    }
}