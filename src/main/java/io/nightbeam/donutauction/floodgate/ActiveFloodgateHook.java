package io.nightbeam.donutauction.floodgate;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

public final class ActiveFloodgateHook implements FloodgateHook {

    private final FloodgateApi api;

    public ActiveFloodgateHook() {
        this.api = FloodgateApi.getInstance();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isFloodgatePlayer(Player player) {
        return api.isFloodgatePlayer(player.getUniqueId());
    }

    @Override
    public void sendForm(Player player, Form form) {
        FloodgatePlayer fPlayer = api.getPlayer(player.getUniqueId());
        if (fPlayer != null) {
            fPlayer.sendForm(form);
        }
    }
}
