package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;

/**
 * Empeche un distributeur (dispenser) d'equiper automatiquement une statue
 * (armor stand) avec un objet en diamant controle - contournement automatise
 * du blocage manuel gere par ArmorStandBlockListener.
 */
public final class DispenserArmorStandBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispenseArmor(BlockDispenseArmorEvent event) {
        if (DiamondItems.isControlled(event.getItem().getType())) {
            event.setCancelled(true);
        }
    }
}
