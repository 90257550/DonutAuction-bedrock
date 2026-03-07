package io.nightbeam.donutauction.hook;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface DonutCoreHook {

    boolean isAvailable();

    String resolveDisplayName(Player player);

    default Component menuPrefix() {
        return Component.empty();
    }
}