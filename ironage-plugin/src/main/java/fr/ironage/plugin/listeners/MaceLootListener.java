package fr.ironage.plugin.listeners;

import fr.ironage.plugin.WindBurstBookManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseLootEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Retire des loots generes (coffres de structures ET vaults des Trial Chambers) les livres
 * Wind Burst, et retire de toute masse trouvee en loot les enchantements autres que Wind Burst.
 */
public final class MaceLootListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLoot(LootGenerateEvent event) {
        List<ItemStack> filtered = filter(event.getLoot());
        if (filtered != null) {
            event.setLoot(filtered);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDispense(BlockDispenseLootEvent event) {
        List<ItemStack> filtered = filter(event.getDispensedLoot());
        if (filtered != null) {
            event.setDispensedLoot(filtered);
        }
    }

    /** Retourne la liste filtree, ou null si rien n'a change. */
    private List<ItemStack> filter(List<ItemStack> loot) {
        List<ItemStack> result = new ArrayList<>();
        boolean changed = false;

        for (ItemStack item : loot) {
            if (item == null) {
                result.add(item);
                continue;
            }
            if (WindBurstBookManager.hasWindBurstBook(item)) {
                changed = true;
                continue;
            }
            if (item.getType() == Material.MACE) {
                ItemStack copy = item.clone();
                boolean stripped = false;
                for (Enchantment enchantment : new ArrayList<>(copy.getEnchantments().keySet())) {
                    if (!enchantment.equals(Enchantment.WIND_BURST)) {
                        copy.removeEnchantment(enchantment);
                        stripped = true;
                    }
                }
                if (stripped) {
                    result.add(copy);
                    changed = true;
                    continue;
                }
            }
            result.add(item);
        }
        return changed ? result : null;
    }
}
