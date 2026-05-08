package io.nightbeam.donutauction.floodgate;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;

public final class NoopFloodgateHook implements FloodgateHook {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean isFloodgatePlayer(Player player) {
        return false;
    }

    @Override
    public void sendForm(Player player, Form form) {
    }
}
