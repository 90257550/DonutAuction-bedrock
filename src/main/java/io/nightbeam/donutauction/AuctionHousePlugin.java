package io.nightbeam.donutauction;

import io.nightbeam.donutauction.command.AuctionCommand;
import io.nightbeam.donutauction.economy.VaultEconomyProvider;
import io.nightbeam.donutauction.floodgate.ActiveFloodgateHook;
import io.nightbeam.donutauction.floodgate.FloodgateFormHandler;
import io.nightbeam.donutauction.floodgate.FloodgateHook;
import io.nightbeam.donutauction.floodgate.NoopFloodgateHook;
import io.nightbeam.donutauction.gui.GuiManager;
import io.nightbeam.donutauction.hook.DonutCoreHook;
import io.nightbeam.donutauction.hook.HookManager;
import io.nightbeam.donutauction.listener.AuctionChatListener;
import io.nightbeam.donutauction.listener.AuctionInventoryListener;
import io.nightbeam.donutauction.listener.PlayerQuitListener;
import io.nightbeam.donutauction.service.AuctionManager;
import io.nightbeam.donutauction.service.AuctionService;
import io.nightbeam.donutauction.storage.AuctionRepository;
import io.nightbeam.donutauction.storage.DatabaseManager;
import io.nightbeam.donutauction.storage.SqlAuctionRepository;
import io.nightbeam.donutauction.util.MessageUtil;
import io.nightbeam.donutauction.util.SchedulerAdapter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuctionHousePlugin extends JavaPlugin {

    private SchedulerAdapter schedulerAdapter;
    private VaultEconomyProvider economyProvider;
    private DatabaseManager databaseManager;
    private AuctionRepository auctionRepository;
    private AuctionManager auctionManager;
    private AuctionService auctionService;
    private GuiManager guiManager;
    private DonutCoreHook donutCoreHook;
    private FloodgateHook floodgateHook;
    private FloodgateFormHandler floodgateFormHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        applyConfigDefaults();

        this.schedulerAdapter = new SchedulerAdapter(this);
        this.economyProvider = VaultEconomyProvider.create(this)
                .orElseThrow(() -> new IllegalStateException("Vault economy provider was not found."));
        this.databaseManager = DatabaseManager.fromConfig(this);
        this.databaseManager.start();

        this.auctionRepository = new SqlAuctionRepository(databaseManager);
        this.auctionManager = new AuctionManager(getConfig().getInt("auction.browse-page-size", 45));
        this.donutCoreHook = HookManager.create(this);
        this.auctionService = new AuctionService(this, schedulerAdapter, economyProvider, auctionRepository, auctionManager, donutCoreHook);
        this.guiManager = new GuiManager(this, auctionService, auctionManager, donutCoreHook);

        this.floodgateHook = createFloodgateHook();
        if (floodgateHook.isAvailable()) {
            this.floodgateFormHandler = new FloodgateFormHandler(this, floodgateHook, guiManager, auctionService, donutCoreHook);
            guiManager.setFloodgateFormHandler(floodgateFormHandler);
        }

        this.auctionService.initialize();
        registerCommands();
        registerListeners();

        getLogger().info("DonutAuctionHouse enabled using " + databaseManager.getDatabaseType().name() + " storage.");
    }

    @Override
    public void onDisable() {
        if (auctionService != null) {
            auctionService.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    private void registerCommands() {
        AuctionCommand auctionCommand = new AuctionCommand(this, auctionService, guiManager);
        registerCommand("ah", auctionCommand);
        registerCommand("auction", auctionCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AuctionInventoryListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new AuctionChatListener(guiManager), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(guiManager), this);
    }

    private void registerCommand(String name, AuctionCommand command) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            throw new IllegalStateException("Missing command in plugin.yml: " + name);
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    public SchedulerAdapter schedulerAdapter() {
        return schedulerAdapter;
    }

    public MessageUtil messages() {
        return new MessageUtil(getConfig().getString("messages.prefix", "&6[DonutAuctionHouse]&r "));
    }

    public void applyConfigDefaults() {
        getConfig().addDefault("auction.min-price", 10.0D);
        getConfig().addDefault("messages.price-below-min", "&cMinimum auction price is &6%min_price%&c.");
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private FloodgateHook createFloodgateHook() {
        try {
            if (getServer().getPluginManager().getPlugin("Floodgate") != null) {
                getLogger().info("Floodgate detected, enabling Bedrock form UI.");
                return new ActiveFloodgateHook();
            }
        } catch (Exception e) {
            getLogger().warning("Failed to initialize Floodgate hook: " + e.getMessage());
        }
        return new NoopFloodgateHook();
    }
}