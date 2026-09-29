package fr.ironage.plugin;

import org.bukkit.Material;

import java.util.List;
import java.util.Set;

/**
 * Liste centralisee des 10 items en diamant concernes par le plugin.
 * Toute la logique (craft, stockage, loot, hopper, /ironage) se base sur cette liste.
 */
public final class DiamondItems {

    private DiamondItems() {
    }

    // L'ordre definit aussi l'ordre de distribution de /ironage.
    public static final List<Material> CONTROLLED_MATERIALS = List.of(
            Material.DIAMOND_SWORD,
            Material.DIAMOND_AXE,
            Material.DIAMOND_SPEAR,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_HOE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS
    );

    public static final Set<Material> CONTROLLED_SET = Set.copyOf(CONTROLLED_MATERIALS);

    public static boolean isControlled(Material material) {
        return material != null && CONTROLLED_SET.contains(material);
    }

    /**
     * Retourne l'equivalent en cuivre d'un objet en diamant controle (ex: DIAMOND_SWORD -&gt;
     * COPPER_SWORD), ou null si l'objet n'est pas controle ou si aucun equivalent n'existe.
     */
    public static Material copperEquivalent(Material diamondMaterial) {
        if (!isControlled(diamondMaterial)) {
            return null;
        }
        String copperName = diamondMaterial.name().replace("DIAMOND_", "COPPER_");
        try {
            return Material.valueOf(copperName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
