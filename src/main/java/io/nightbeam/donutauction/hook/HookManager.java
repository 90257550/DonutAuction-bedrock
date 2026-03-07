package io.nightbeam.donutauction.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class HookManager {

    private HookManager() {
    }

    public static DonutCoreHook create(JavaPlugin plugin) {
        Plugin donutCore = Bukkit.getPluginManager().getPlugin("DonutCore");
        if (donutCore == null || !donutCore.isEnabled()) {
            return new NoopDonutCoreHook();
        }
        plugin.getLogger().info("DonutCore detected, enabling optional integration.");
        return new ReflectiveDonutCoreHook(donutCore);
    }
}