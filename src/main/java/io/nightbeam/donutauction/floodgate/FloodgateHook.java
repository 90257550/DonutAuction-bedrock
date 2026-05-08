package io.nightbeam.donutauction.floodgate;

import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.Form;

public interface FloodgateHook {

    boolean isAvailable();

    boolean isFloodgatePlayer(Player player);

    void sendForm(Player player, Form form);
}
