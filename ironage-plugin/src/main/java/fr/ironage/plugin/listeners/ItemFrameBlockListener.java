package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche de placer un objet en diamant controle dans un cadre-objet (item frame
 * ou glow item frame, GlowItemFrame etendant ItemFrame). Sans ca, l'objet
 * echapperait au suivi du detenteur (ni inventaire joueur, ni conteneur classique).
 */
public final class ItemFrameBlockListener implements Listener {

    private static final String DENY_MESSAGE =
            "\u00A7cCet objet en diamant est un artefact unique : il ne peut pas etre place dans un cadre.";

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            // Evite de traiter deux fois le meme clic (main principale + main secondaire).
            return;
        }

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (DiamondItems.isControlled(held.getType())) {
            event.setCancelled(true);
            player.sendMessage(DENY_MESSAGE);
        }
    }
}
