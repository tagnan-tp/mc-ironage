package fr.ironage.plugin.listeners;

import fr.ironage.plugin.HolderScanner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Relance un scan des detenteurs a chaque connexion/deconnexion, en plus du scan
 * periodique, pour que detenteur-actuel.yml reste a jour rapidement.
 */
public final class HolderTrackingListener implements Listener {

    private final HolderScanner scanner;

    public HolderTrackingListener(HolderScanner scanner) {
        this.scanner = scanner;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        scanner.scanAndUpdate();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        scanner.scanAndUpdate();
    }
}
