package fr.ironage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Le SEUL moyen d'obtenir un livre Wind Burst : poser un OEUF DE DRAGON dans une
 * tailleuse de pierre (stonecutter), qui propose alors au choix Wind Burst I, II, III ou IV.
 * Les livres ainsi obtenus portent un tag invisible ("legitime") ; tout autre livre
 * Wind Burst (loot, creatif...) est refuse a l'enclume (voir MaceRulesListener).
 */
public final class WindBurstBookManager {

    private static final String LEGIT_KEY = "windburst-book";
    private static final int MAX_LEVEL = 4;

    private final IronAgePlugin plugin;
    private final NamespacedKey legitKey;

    public WindBurstBookManager(IronAgePlugin plugin) {
        this.plugin = plugin;
        this.legitKey = new NamespacedKey(plugin, LEGIT_KEY);
    }

    /** Livre enchante Wind Burst du niveau donne (le niveau peut depasser le max vanilla de 3). */
    public ItemStack createBook(int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.editMeta(meta -> {
            if (meta instanceof EnchantmentStorageMeta storage) {
                storage.addStoredEnchant(Enchantment.WIND_BURST, level, true);
            }
            meta.getPersistentDataContainer().set(legitKey, PersistentDataType.BYTE, (byte) 1);
        });
        return book;
    }

    /** Enregistre 4 recettes de tailleuse de pierre : oeuf de dragon -> Wind Burst I a IV. */
    public void registerRecipes() {
        unregisterRecipes();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            StonecuttingRecipe recipe = new StonecuttingRecipe(
                    recipeKey(level), createBook(level), new RecipeChoice.MaterialChoice(Material.DRAGON_EGG));
            Bukkit.addRecipe(recipe);
        }
    }

    public void unregisterRecipes() {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            Bukkit.removeRecipe(recipeKey(level));
        }
    }

    private NamespacedKey recipeKey(int level) {
        return new NamespacedKey(plugin, "windburst_book_" + level);
    }

    /** Livre obtenu par la recette de l'oeuf de dragon (et donc autorise). */
    public boolean isLegitBook(ItemStack stack) {
        return stack != null && stack.getType() == Material.ENCHANTED_BOOK && stack.hasItemMeta()
                && stack.getItemMeta().getPersistentDataContainer().has(legitKey, PersistentDataType.BYTE);
    }

    /** Vrai pour tout livre enchante contenant Wind Burst, legitime ou non. */
    public static boolean hasWindBurstBook(ItemStack stack) {
        return stack != null && stack.getType() == Material.ENCHANTED_BOOK
                && stack.getItemMeta() instanceof EnchantmentStorageMeta storage
                && storage.hasStoredEnchant(Enchantment.WIND_BURST);
    }
}
