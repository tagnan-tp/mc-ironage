package fr.ironage.plugin.listeners;

import fr.ironage.plugin.IronAgePlugin;
import fr.ironage.plugin.WindBurstBookManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Regles d'enchantement de la masse :
 * - une masse SANS Wind Burst ne peut pas etre enchantee a la table d'enchantement ;
 * - une masse AVEC Wind Burst, posee dans la table, propose 3 choix : les 3 AUTRES niveaux
 *   de Wind Burst (parmi I a IV), du plus bas au plus haut. Choisir remplace le niveau ;
 * - la table ne produit jamais de livre Wind Burst ;
 * - a l'enclume, une masse ne peut recevoir AUCUN enchantement autre que Wind Burst ;
 * - un livre Wind Burst qui ne vient pas de la recette de l'oeuf de dragon (loot, creatif...)
 *   est inutilisable a l'enclume.
 */
public final class MaceRulesListener implements Listener {

    /** Niveaux d'XP requis pour les 3 propositions de la table (modifiable dans config.yml). */
    private static final int[] DEFAULT_TABLE_COSTS = {10, 20, 30};

    private final IronAgePlugin plugin;
    private final WindBurstBookManager books;

    public MaceRulesListener(IronAgePlugin plugin, WindBurstBookManager books) {
        this.plugin = plugin;
        this.books = books;
    }

    /** Les niveaux Wind Burst (1 a MAX) autres que le niveau actuel, par ordre croissant. */
    private List<Integer> otherLevels(int current) {
        List<Integer> levels = new ArrayList<>();
        for (int level = 1; level <= WindBurstBookManager.MAX_LEVEL; level++) {
            if (level != current) {
                levels.add(level);
            }
        }
        return levels;
    }

    private int tableCost(int slot) {
        int safeSlot = Math.max(0, Math.min(slot, DEFAULT_TABLE_COSTS.length - 1));
        List<Integer> configured = plugin.getConfig().getIntegerList("windburst-table-costs");
        if (safeSlot < configured.size() && configured.get(safeSlot) > 0) {
            return configured.get(safeSlot);
        }
        return DEFAULT_TABLE_COSTS[safeSlot];
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() != Material.MACE) {
            return;
        }

        int current = item.getEnchantmentLevel(Enchantment.WIND_BURST);
        if (current <= 0) {
            // Masse sans Wind Burst : jamais enchantable a la table.
            event.setCancelled(true);
            return;
        }

        // Masse Wind Burst : les 3 propositions deviennent les 3 autres niveaux.
        List<Integer> options = otherLevels(current);
        EnchantmentOffer[] offers = event.getOffers();
        for (int slot = 0; slot < offers.length && slot < options.size(); slot++) {
            offers[slot] = new EnchantmentOffer(Enchantment.WIND_BURST, options.get(slot), tableCost(slot));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.MACE) {
            int current = item.getEnchantmentLevel(Enchantment.WIND_BURST);
            List<Integer> options = otherLevels(current);
            int button = event.whichButton();
            if (current <= 0 || button < 0 || button >= options.size()) {
                event.setCancelled(true);
                return;
            }

            int chosen = options.get(button);
            event.getEnchantsToAdd().clear();
            event.getEnchantsToAdd().put(Enchantment.WIND_BURST, chosen);

            // Securite : on verifie 1 tick plus tard que la masse porte bien le niveau choisi,
            // meme si le moteur plafonnait ou ignorait un niveau superieur au maximum vanilla.
            Inventory table = event.getView().getTopInventory();
            Bukkit.getScheduler().runTask(plugin, () -> forceLevel(table, chosen));
            return;
        }

        // Securite : jamais de Wind Burst issu de la table (livres compris).
        event.getEnchantsToAdd().remove(Enchantment.WIND_BURST);
        if (item.getType() == Material.BOOK && event.getEnchantsToAdd().isEmpty()) {
            event.setCancelled(true);
        }
    }

    private void forceLevel(Inventory table, int level) {
        ItemStack mace = table.getItem(0);
        if (mace == null || mace.getType() != Material.MACE) {
            return;
        }
        if (mace.getEnchantmentLevel(Enchantment.WIND_BURST) == level) {
            return;
        }
        mace.addUnsafeEnchantment(Enchantment.WIND_BURST, level);
        table.setItem(0, mace);
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
