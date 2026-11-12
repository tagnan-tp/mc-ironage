package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

/**
 * Empeche d'equiper une statue (armor stand) avec une armure en diamant controlee.
 * Sans ce blocage, l'objet resterait "possede" par personne aux yeux du scan de
 * detenteur actuel (qui ne regarde que les inventaires des joueurs connectes).
 */
public final class ArmorStandBlockListener implements Listener {

    private static final String DENY_MESSAGE =
            "\u00A7cCet objet en diamant est un artefact unique : il ne peut pas etre equipe sur une statue.";

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onManipulate(PlayerArmorStandManipulateEvent event) {
        if (!DiamondItems.isControlled(event.getPlayerItem().getType())) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        player.sendMessage(DENY_MESSAGE);
    }
}
