package io.nightbeam.donutauction.economy;

import java.util.Optional;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultEconomyProvider {

    private final Economy economy;

    private VaultEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    public static Optional<VaultEconomyProvider> create(JavaPlugin plugin) {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider == null || provider.getProvider() == null) {
            plugin.getLogger().severe("Vault is installed but no economy provider is registered.");
            return Optional.empty();
        }
        return Optional.of(new VaultEconomyProvider(provider.getProvider()));
    }

    public boolean has(OfflinePlayer player, double amount) {
        return economy.has(player, amount);
    }

    public EconomyResponse withdraw(OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, amount);
    }

    public EconomyResponse deposit(OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount);
    }

    public String format(double amount) {
        return economy.format(amount);
    }
}