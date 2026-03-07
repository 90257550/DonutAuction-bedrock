package io.nightbeam.donutauction.listener;

import io.nightbeam.donutauction.gui.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerQuitListener implements Listener {

    private final GuiManager guiManager;

    public PlayerQuitListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        guiManager.clear(event.getPlayer());
    }
}