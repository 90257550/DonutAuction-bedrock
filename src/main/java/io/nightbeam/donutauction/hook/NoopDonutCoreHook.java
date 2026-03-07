package io.nightbeam.donutauction.hook;

import org.bukkit.entity.Player;

public final class NoopDonutCoreHook implements DonutCoreHook {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String resolveDisplayName(Player player) {
        return player.getName();
    }
}