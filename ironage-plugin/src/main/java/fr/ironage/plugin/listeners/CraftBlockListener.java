package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche en permanence le craft de tout objet en diamant controle,
 * que ce soit a l'etabli ou dans la grille 2x2 de l'inventaire joueur.
 */
public final class CraftBlockListener implements Listener {

    // Retire le resultat avant meme que le joueur puisse le recuperer.
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result != null && DiamondItems.isControlled(result.getType())) {
            event.getInventory().setResult(null);
        }
    }

    // Filet de securite si un plugin tiers ou un shift-click contourne le premier evenement.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (DiamondItems.isControlled(result.getType())) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof HumanEntity human) {
                human.closeInventory();
            }
        }
    }
}
