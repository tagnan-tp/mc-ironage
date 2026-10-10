package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import fr.ironage.plugin.IronAgePlugin;
import fr.ironage.plugin.SpecialChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
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
 * Empeche en permanence de stocker/stacker un objet en diamant controle dans un conteneur :
 * coffre, coffre double, baril, shulker box, four/fumoir/fourneau, chaudron a potions,
 * distributeur, dropper, entonnoir, enderchest, ou toute entite de stockage (wagon-coffre,
 * bateau-coffre, cheval/ane/mule/lama charge, et toute future entite de stockage).
 * <p>
 * Exception explicite : enclume et table d'enchantement.
 * <p>
 * Le message envoye au joueur est configurable dans config.yml (cle "deny-storage-message"),
 * avec le placeholder %item% remplace par le nom lisible de l'objet concerne.
 */
public final class StorageBlockListener implements Listener {

    private static final String DEFAULT_MESSAGE =
            "&cCet objet en diamant (%item%) est un artefact unique : il ne peut pas etre stocke dans un conteneur.";

    private final IronAgePlugin plugin;
    private final SpecialChest specialChest;

    public StorageBlockListener(IronAgePlugin plugin) {
        this.plugin = plugin;
        this.specialChest = new SpecialChest(plugin);
    }

    private boolean isExemptInventory(InventoryType type) {
        return type == InventoryType.ANVIL || type == InventoryType.ENCHANTING;
    }

    // Couvre les coffres/barils/fours/etc (BlockState), les coffres doubles,
    // et l'enderchest (dont l'inventaire appartient au JOUEUR et pas au bloc,
    // d'ou la verification separee sur le type d'inventaire).
    // Regle generique : TOUTE entite possedant un inventaire de stockage est
    // aussi bloquee (wagon-coffre, bateau-coffre, chevaux/anes/mules/lamas
    // charges, et toute future entite de stockage ajoutee par Mojang) - sauf
    // le joueur lui-meme, dont l'inventaire personnel doit rester utilisable.
    // Exception : le coffre special donne par /ironage chestgive (tague), qui
    // a explicitement le droit de stocker ces objets.
    private boolean isBlockEntityInventory(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        if (isExemptInventory(inventory.getType())) {
            return false;
        }
        if (inventory.getType() == InventoryType.ENDER_CHEST) {
            return true;
        }

        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof DoubleChest doubleChest) {
            InventoryHolder leftHolder = doubleChest.getLeftSide();
            InventoryHolder rightHolder = doubleChest.getRightSide();
            boolean leftSpecial = leftHolder instanceof BlockState leftState
                    && specialChest.isSpecialChestBlock(leftState);
            boolean rightSpecial = rightHolder instanceof BlockState rightState
                    && specialChest.isSpecialChestBlock(rightState);
            // Les deux cotes doivent etre speciaux pour que le double-coffre soit exempte,
            // sinon on reste sur le comportement bloquant par defaut.
            return !(leftSpecial && rightSpecial);
        }

        if (holder instanceof BlockState state && specialChest.isSpecialChestBlock(state)) {
            return false;
        }

        if (holder instanceof BlockState) {
            return true;
        }

        // Toute entite (wagon-coffre, bateau-coffre, animal charge, etc.) qui n'est
        // pas un joueur est traitee comme un conteneur a bloquer.
        return holder instanceof Entity && !(holder instanceof Player);
    }

    private String buildDenyMessage(ItemStack item) {
        String raw = plugin.getConfig().getString("deny-storage-message", DEFAULT_MESSAGE);
        String withPlaceholder = raw.replace("%item%", prettyName(item.getType()));
        return ChatColor.translateAlternateColorCodes('&', withPlaceholder);
    }

    private String prettyName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
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
                ItemStack relevant = movingControlledIntoContainer ? cursor : current;
                player.sendMessage(buildDenyMessage(relevant));
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
                    player.sendMessage(buildDenyMessage(oldCursor));
                }
                return;
            }
        }
    }
}
