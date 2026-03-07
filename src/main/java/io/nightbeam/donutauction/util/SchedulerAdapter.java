package io.nightbeam.donutauction.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class SchedulerAdapter {

    private final Plugin plugin;
    private final ExecutorService asyncExecutor;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() / 2), new NamedThreadFactory());
    }

    public Executor asyncExecutor() {
        return asyncExecutor;
    }

    public void runAsync(Runnable runnable) {
        asyncExecutor.execute(runnable);
    }

    public void runGlobal(Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, runnable);
    }

    public void runEntity(Entity entity, Runnable runnable) {
        entity.getScheduler().execute(plugin, runnable, null, 1L);
    }

    public ScheduledTask runGlobalRepeating(Runnable runnable, long initialDelayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), initialDelayTicks, periodTicks);
    }

    public void shutdown() {
        asyncExecutor.shutdownNow();
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "DonutAuctionHouse-Async-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}