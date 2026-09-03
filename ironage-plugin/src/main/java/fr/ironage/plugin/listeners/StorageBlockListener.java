package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche en permanence de stocker/stacker un objet en diamant controle dans un "block entity"
 * (coffre, coffre double, baril, shulker box, four/fumoir/fourneau, chaudron a potions,
 * distributeur, dropper, entonnoir...).
 * <p>
 * Exception explicite : enclume et table d'enchantement (ce ne sont pas des inventaires
 * de stockage persistant, l'objet y transite seulement le temps de l'operation).
 */
public final class StorageBlockListener implements Listener {

    private static final String DENY_MESSAGE =
            "\u00A7cCet objet en diamant est un artefact unique : il ne peut pas etre stocke dans un conteneur.";

    private boolean isExemptInventory(InventoryType type) {
        return type == InventoryType.ANVIL || type == InventoryType.ENCHANTING;
    }

    private boolean isBlockEntityInventory(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        if (isExemptInventory(inventory.getType())) {
            return false;
        }

        InventoryHolder holder = inventory.getHolder();
        // Chest, Barrel, ShulkerBox, Furnace/BlastFurnace/Smoker, BrewingStand, Dispenser,
        // Dropper, Hopper implementent tous BlockState.
        // Le double-coffre a un holder DoubleChest a part.
        return holder instanceof BlockState || holder instanceof DoubleChest;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!isBlockEntityInventory(topInventory)) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        boolean movingControlledIntoContainer =
                event.getClickedInventory() != null
                        && event.getClickedInventory().equals(topInventory)
                        && DiamondItems.isControlled(cursor != null ? cursor.getType() : null);

        boolean shiftClickFromPlayerToContainer =
                event.isShiftClick()
                        && event.getClickedInventory() != null
                        && !event.getClickedInventory().equals(topInventory)
                        && DiamondItems.isControlled(current != null ? current.getType() : null);

        if (movingControlledIntoContainer || shiftClickFromPlayerToContainer) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(DENY_MESSAGE);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!isBlockEntityInventory(topInventory)) {
            return;
        }

        ItemStack oldCursor = event.getOldCursor();
        if (!DiamondItems.isControlled(oldCursor != null ? oldCursor.getType() : null)) {
            return;
        }

        int topSize = topInventory.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    player.sendMessage(DENY_MESSAGE);
                }
                return;
            }
        }
    }
}
