package fr.ironage.plugin.listeners;

import fr.ironage.plugin.WindBurstBookManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Regles d'enchantement de la masse :
 * - la table d'enchantement ne peut plus enchanter une masse, et ne produit jamais de
 *   livre Wind Burst ;
 * - a l'enclume, une masse ne peut recevoir AUCUN enchantement autre que Wind Burst ;
 * - un livre Wind Burst qui ne vient pas de la recette de l'oeuf de dragon (loot, creatif...)
 *   est inutilisable a l'enclume.
 */
public final class MaceRulesListener implements Listener {

    private final WindBurstBookManager books;

    public MaceRulesListener(WindBurstBookManager books) {
        this.books = books;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        if (event.getItem().getType() == Material.MACE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.MACE) {
            event.setCancelled(true);
            return;
        }
        // Securite : jamais de Wind Burst issu de la table (livres compris).
        event.getEnchantsToAdd().remove(Enchantment.WIND_BURST);
        if (item.getType() == Material.BOOK && event.getEnchantsToAdd().isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        // Livre Wind Burst non legitime : inutilisable.
        if ((WindBurstBookManager.hasWindBurstBook(first) && !books.isLegitBook(first))
                || (WindBurstBookManager.hasWindBurstBook(second) && !books.isLegitBook(second))) {
            event.setResult(null);
            return;
        }

        // Une masse ne peut porter que Wind Burst.
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.MACE) {
            for (Enchantment enchantment : result.getEnchantments().keySet()) {
                if (!enchantment.equals(Enchantment.WIND_BURST)) {
                    event.setResult(null);
                    return;
                }
            }
        }
    }
}
