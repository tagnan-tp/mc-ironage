package fr.ironage.plugin;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Gere le nombre de "charges" de reroll manuel disponibles sur la houe en
 * diamant unique. Contrairement a un cooldown temporel classique, la charge
 * est stockee directement sur l'objet (persiste a travers les echanges de
 * mains) et affichee dans son nom, en violet.
 * <p>
 * Une charge se consomme via /diamondhoe (hors mode "op", illimite), et se
 * regagne en posant la houe + un bloc de diamant sur une table de craft (voir
 * DiamondHoeCooldownResetListener) - un seul bloc consomme par charge
 * regagnee, et plus rien ne se passe une fois la houe a son maximum.
 */
public final class DiamondHoeChargeManager {

    private static final String CHARGES_KEY = "diamondhoe-charges";
    private static final String BASE_NAME = "\u00A7b\u00A7lHoue en Diamant Unique";

    private final IronAgePlugin plugin;
    private final NamespacedKey chargesKey;

    public DiamondHoeChargeManager(IronAgePlugin plugin) {
        this.plugin = plugin;
        this.chargesKey = new NamespacedKey(plugin, CHARGES_KEY);
    }

    public int getMaxCharges() {
        return Math.max(0, plugin.getConfig().getInt("diamondhoe-max-charges", 2));
    }

    public int getCharges(ItemStack hoe) {
        if (hoe == null || !hoe.hasItemMeta()) {
            return getMaxCharges();
        }
        Integer stored = hoe.getItemMeta().getPersistentDataContainer().get(chargesKey, PersistentDataType.INTEGER);
        return stored != null ? stored : getMaxCharges();
    }

    /** A appeler une seule fois, quand la houe est creee (distribution initiale
     * ou /ironage give) : demarre a charge maximale. */
    public void initialize(ItemStack hoe) {
        setCharges(hoe, getMaxCharges());
    }

    /** Consomme une charge si possible. Retourne false (et ne change rien) si
     * la houe n'a plus aucune charge disponible. */
    public boolean consumeCharge(ItemStack hoe) {
        int current = getCharges(hoe);
        if (current <= 0) {
            return false;
        }
        setCharges(hoe, current - 1);
        return true;
    }

    /** Tente de regagner une charge. Retourne false (et ne change rien) si la
     * houe est deja a sa charge maximale. */
    public boolean addCharge(ItemStack hoe) {
        int max = getMaxCharges();
        int current = getCharges(hoe);
        if (current >= max) {
            return false;
        }
        setCharges(hoe, current + 1);
        return true;
    }

    private void setCharges(ItemStack hoe, int charges) {
        int max = getMaxCharges();
        int clamped = Math.max(0, Math.min(charges, max));
        hoe.editMeta(meta -> {
            meta.getPersistentDataContainer().set(chargesKey, PersistentDataType.INTEGER, clamped);
            meta.setDisplayName(BASE_NAME + " \u00A7d(" + clamped + "/" + max + " charges)");
        });
    }
}
