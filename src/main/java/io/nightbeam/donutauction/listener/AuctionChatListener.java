package io.nightbeam.donutauction.listener;

import io.nightbeam.donutauction.gui.GuiManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class AuctionChatListener implements Listener {

    private final GuiManager guiManager;

    public AuctionChatListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!guiManager.isAwaitingSearch(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        String query = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.message());
        guiManager.plugin().schedulerAdapter().runEntity(player, () -> guiManager.handleSearchInput(player, query));
    }
}