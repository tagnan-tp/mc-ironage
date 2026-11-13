package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Retire les objets en diamant controles du loot distribue par les blocs qui
 * "dispensent" du loot : vaults (et vaults ominous) des Trial Chambers. Ce loot
 * ne passe PAS par LootGenerateEvent (gere par LootBlockListener), d'ou ce
 * listener dedie.
 */
public final class DispensedLootBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispenseLoot(BlockDispenseLootEvent event) {
        List<ItemStack> filtered = new ArrayList<>();
        boolean changed = false;

        for (ItemStack item : event.getDispensedLoot()) {
            if (item != null && DiamondItems.isControlled(item.getType())) {
                changed = true;
                continue;
            }
            filtered.add(item);
        }

        if (changed) {
            event.setDispensedLoot(filtered);
        }
    }
}
