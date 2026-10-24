package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondHoeService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class DiamondHoeTargetLifecycleListener implements Listener {

    private final DiamondHoeService hoeService;

    public DiamondHoeTargetLifecycleListener(DiamondHoeService hoeService) {
        this.hoeService = hoeService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        hoeService.handleTargetUnavailable(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        hoeService.handleTargetUnavailable(event.getEntity().getUniqueId());
    }
}
