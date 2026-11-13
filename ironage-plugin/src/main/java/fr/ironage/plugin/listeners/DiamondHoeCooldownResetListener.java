package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondHoeChargeManager;
import fr.ironage.plugin.HolderScanner;
import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Astuce cachee : placer la houe en diamant unique ET un bloc de diamant dans
 * une table de craft (ou la grille 2x2) lui rend UNE charge (voir
 * DiamondHoeChargeManager), en consommant UN bloc de diamant a la fois.
 * <p>
 * Si la houe est deja a sa charge maximale, rien n'est consomme et rien ne se
 * reproduit : poser toute une pile de blocs de diamant ne gaspille donc que le
 * strict necessaire pour remonter au maximum, jamais plus.
 */
public final class DiamondHoeCooldownResetListener implements Listener {

    private final IronAgePlugin plugin;
    private final HolderScanner holderScanner;
    private final DiamondHoeChargeManager chargeManager;

    public DiamondHoeCooldownResetListener(IronAgePlugin plugin, HolderScanner holderScanner,
                                            DiamondHoeChargeManager chargeManager) {
        this.plugin = plugin;
        this.holderScanner = holderScanner;
        this.chargeManager = chargeManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        int hoeSlot = -1;
        int diamondBlockSlot = -1;

        for (int i = 0; i < matrix.length; i++) {
            ItemStack stack = matrix[i];
            if (stack == null) {
                continue;
            }
            if (hoeSlot == -1 && stack.getType() == Material.DIAMOND_HOE && holderScanner.isTaggedUniqueItem(stack)) {
                hoeSlot = i;
            } else if (diamondBlockSlot == -1 && stack.getType() == Material.DIAMOND_BLOCK) {
                diamondBlockSlot = i;
            }
        }

        if (hoeSlot == -1 || diamondBlockSlot == -1) {
            return;
        }

        // Cette combinaison ne correspond a aucune vraie recette : jamais de resultat accidentel.
        inventory.setResult(null);

        HumanEntity human = event.getView().getPlayer();
        int finalHoeSlot = hoeSlot;
        int finalDiamondBlockSlot = diamondBlockSlot;

        // On modifie la grille au tick suivant plutot que dans l'evenement lui-meme,
        // pour eviter tout effet de bord avec l'ouverture en cours de PrepareItemCraftEvent.
        Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack[] currentMatrix = inventory.getMatrix();
            ItemStack hoeStack = currentMatrix[finalHoeSlot];
            ItemStack diamondBlockStack = currentMatrix[finalDiamondBlockSlot];

            if (hoeStack == null || hoeStack.getType() != Material.DIAMOND_HOE
                    || diamondBlockStack == null || diamondBlockStack.getType() != Material.DIAMOND_BLOCK) {
                return; // la configuration a change entre-temps, on annule proprement
            }

            if (!chargeManager.addCharge(hoeStack)) {
                human.sendMessage("\u00A77La houe en diamant est deja chargee au maximum.");
                return;
            }

            diamondBlockStack.setAmount(diamondBlockStack.getAmount() - 1);
            currentMatrix[finalDiamondBlockSlot] = diamondBlockStack.getAmount() <= 0 ? null : diamondBlockStack;
            currentMatrix[finalHoeSlot] = hoeStack;
            inventory.setMatrix(currentMatrix);

            int charges = chargeManager.getCharges(hoeStack);
            int max = chargeManager.getMaxCharges();
            human.sendMessage("\u00A7bLa houe en diamant a regagne une charge (" + charges + "/" + max + ").");
        });
    }
}
