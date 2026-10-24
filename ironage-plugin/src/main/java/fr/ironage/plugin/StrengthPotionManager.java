package fr.ironage.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

/**
 * Regles sur les potions de Force : la Force II est interdite pour les joueurs (voir
 * StrengthRuleListener), sauf la splash potion speciale donnee par /force give, reconnaissable
 * a un tag invisible (PersistentDataContainer) et totalement fonctionnelle.
 */
public final class StrengthPotionManager {

    private static final String ADMIN_KEY = "admin-strength-potion";

    private final NamespacedKey adminKey;

    public StrengthPotionManager(IronAgePlugin plugin) {
        this.adminKey = new NamespacedKey(plugin, ADMIN_KEY);
    }

    /** Une splash potion de Force II (taguee, donc exemptee des restrictions). */
    public ItemStack createAdminSplashPotion() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION);
        item.editMeta(meta -> {
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setBasePotionType(PotionType.STRONG_STRENGTH);
            }
            meta.getPersistentDataContainer().set(adminKey, PersistentDataType.BYTE, (byte) 1);
        });
        return item;
    }

    public boolean isAdminPotion(ItemStack stack) {
        return stack != null && stack.hasItemMeta()
                && stack.getItemMeta().getPersistentDataContainer().has(adminKey, PersistentDataType.BYTE);
    }

    /** Vrai pour une potion / splash / lingering / fleche a effet de type Force II. */
    public boolean isStrongStrength(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        Material type = stack.getType();
        if (type != Material.POTION && type != Material.SPLASH_POTION
                && type != Material.LINGERING_POTION && type != Material.TIPPED_ARROW) {
            return false;
        }
        return stack.getItemMeta() instanceof PotionMeta potionMeta
                && potionMeta.getBasePotionType() == PotionType.STRONG_STRENGTH;
    }

    /** Copie de l'objet ramenee a Force I (meme type d'objet : potion, splash, lingering...). */
    public ItemStack downgrade(ItemStack stack) {
        ItemStack copy = stack.clone();
        copy.editMeta(meta -> {
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setBasePotionType(PotionType.STRENGTH);
            }
        });
        return copy;
    }
}
