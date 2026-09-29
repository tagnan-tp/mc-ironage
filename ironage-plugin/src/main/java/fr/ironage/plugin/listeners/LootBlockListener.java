package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Empeche en permanence les objets en diamant controles d'apparaitre dans le loot
 * genere par les loot tables vanilla (coffres de structures, peche, etc.).
 */
public final class LootBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> filtered = new ArrayList<>();
        boolean changed = false;

        for (ItemStack item : event.getLoot()) {
            if (item != null && DiamondItems.isControlled(item.getType())) {
                changed = true;
                continue;
            }
            filtered.add(item);
        }

        if (changed) {
            event.setLoot(filtered);
        }
    }
}
