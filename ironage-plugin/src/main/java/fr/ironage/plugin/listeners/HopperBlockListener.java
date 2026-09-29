package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche en permanence :
 * - un entonnoir (ou distributeur/dropper) de transferer automatiquement un objet
 *   en diamant controle entre deux inventaires,
 * - un entonnoir de ramasser un objet en diamant controle pose au sol.
 */
public final class HopperBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (!DiamondItems.isControlled(item.getType())) {
            return;
        }

        InventoryType sourceType = event.getSource().getType();
        InventoryType destType = event.getDestination().getType();

        boolean involvesAutomatedContainer =
                sourceType == InventoryType.HOPPER || destType == InventoryType.HOPPER
                        || sourceType == InventoryType.DISPENSER || destType == InventoryType.DISPENSER
                        || sourceType == InventoryType.DROPPER || destType == InventoryType.DROPPER;

        if (involvesAutomatedContainer) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent event) {
        if (event.getInventory().getType() != InventoryType.HOPPER) {
            return;
        }

        Item entity = event.getItem();
        ItemStack stack = entity.getItemStack();
        if (DiamondItems.isControlled(stack.getType())) {
            event.setCancelled(true);
        }
    }
}
