package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche de ranger un objet en diamant controle a l'interieur d'un sac (bundle),
 * ce qui le rendrait invisible aux autres controles de stockage (le sac pourrait
 * ensuite etre pose tranquillement dans un coffre).
 */
public final class BundleBlockListener implements Listener {

    private static final String DENY_MESSAGE =
            "\u00A7cCet objet en diamant est un artefact unique : il ne peut pas etre range dans un sac.";

    private boolean isBundle(Material material) {
        return material != null && material.name().endsWith("BUNDLE");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Cas 1 : le joueur clique sur un sac (dans un inventaire) en tenant l'objet
        // diamant sur le curseur -> insertion dans le sac.
        boolean insertingIntoBundle =
                isBundle(current != null ? current.getType() : null)
                        && DiamondItems.isControlled(cursor != null ? cursor.getType() : null);

        // Cas 2 : le joueur tient un sac sur le curseur et clique sur une pile de
        // l'objet diamant -> insertion dans le sac egalement.
        boolean insertingFromCursorBundle =
                isBundle(cursor != null ? cursor.getType() : null)
                        && DiamondItems.isControlled(current != null ? current.getType() : null);

        if (insertingIntoBundle || insertingFromCursorBundle) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(DENY_MESSAGE);
            }
        }
    }
}
