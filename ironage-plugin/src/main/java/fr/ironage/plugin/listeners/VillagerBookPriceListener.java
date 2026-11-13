package fr.ironage.plugin.listeners;

import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Applique le multiplicateur de prix (/villagermultiplier) aux livres enchantes vendus par
 * les villageois.
 * <p>
 * Technique : a l'ouverture du menu de commerce, on ajuste le "prix special" (le meme
 * mecanisme que les remises de reputation) de chaque offre dont le resultat est un livre
 * enchante, pour que le prix de base en emeraudes soit multiplie. Minecraft remet ces
 * prix speciaux a zero a la fermeture du menu : le multiplicateur peut donc etre change
 * a tout moment, sans jamais se cumuler et sans toucher aux commerces enregistres.
 */
public final class VillagerBookPriceListener implements Listener {

    private static final int MAX_PRICE = 64;

    private final IronAgePlugin plugin;

    public VillagerBookPriceListener(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getInventory() instanceof MerchantInventory merchantInventory)) {
            return;
        }
        Merchant merchant = merchantInventory.getMerchant();
        if (!(merchant instanceof Villager villager)) {
            return;
        }

        double multiplier = plugin.getConfig().getDouble("villager-book-price-multiplier", 1.0);
        if (multiplier == 1.0 || multiplier <= 0) {
            return;
        }

        List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
        boolean changed = false;

        for (MerchantRecipe recipe : recipes) {
            if (recipe.getResult().getType() != Material.ENCHANTED_BOOK) {
                continue;
            }
            List<ItemStack> ingredients = recipe.getIngredients();
            if (ingredients.isEmpty()) {
                continue;
            }

            int base = ingredients.get(0).getAmount();
            long scaled = Math.round(base * multiplier);
            int desired = (int) Math.max(1, Math.min(MAX_PRICE, scaled));
            int delta = desired - base;

            if (delta != 0) {
                recipe.setSpecialPrice(recipe.getSpecialPrice() + delta);
                changed = true;
            }
        }

        if (changed) {
            villager.setRecipes(recipes);
        }
    }
}
