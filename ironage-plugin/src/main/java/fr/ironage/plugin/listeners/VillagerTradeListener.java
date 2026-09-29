package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Empeche les villageois (armurier, forgeron d'armes...) de proposer un objet en diamant
 * controle a l'achat.
 * <p>
 * Quand un equivalent en cuivre existe (c'est le cas pour les 10 objets controles depuis
 * l'ajout des outils/armes/armures en cuivre), le commerce est transforme pour vendre
 * l'equivalent cuivre a la place, avec le meme prix/niveau/experience.
 * Sinon, seul ce commerce precis est retire (les autres commerces du villageois restent
 * intacts).
 */
public final class VillagerTradeListener implements Listener {

    private final Plugin plugin;

    public VillagerTradeListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // Cas normal : interception au moment ou le villageois recoit un nouveau commerce
    // (a l'attribution du metier ou lors d'un restock apres montee de niveau).
    @EventHandler(priority = EventPriority.HIGH)
    public void onAcquireTrade(VillagerAcquireTradeEvent event) {
        TradeFix fix = fixRecipeIfNeeded(event.getRecipe());
        if (fix == null) {
            return;
        }
        if (fix.remove) {
            event.setCancelled(true);
        } else {
            event.setRecipe(fix.replacement);
        }
    }

    // Filet de securite : re-scanne tous les commerces d'un villageois juste apres
    // l'attribution de son metier, au cas ou certains commerces initiaux ne
    // declencheraient pas VillagerAcquireTradeEvent individuellement.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCareerChange(VillagerCareerChangeEvent event) {
        Villager villager = event.getEntity();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!villager.isValid()) {
                    return;
                }
                List<MerchantRecipe> recipes = villager.getRecipes();
                List<MerchantRecipe> updated = new ArrayList<>();
                boolean changed = false;

                for (MerchantRecipe recipe : recipes) {
                    TradeFix fix = fixRecipeIfNeeded(recipe);
                    if (fix == null) {
                        updated.add(recipe);
                    } else if (fix.remove) {
                        changed = true;
                    } else {
                        updated.add(fix.replacement);
                        changed = true;
                    }
                }

                if (changed) {
                    villager.setRecipes(updated);
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    /** Retourne null si rien a changer, sinon l'action a effectuer sur ce commerce. */
    private TradeFix fixRecipeIfNeeded(MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        if (!DiamondItems.isControlled(result.getType())) {
            return null;
        }

        Material copper = DiamondItems.copperEquivalent(result.getType());
        if (copper == null) {
            return TradeFix.remove();
        }

        ItemStack newResult = new ItemStack(copper, result.getAmount());
        MerchantRecipe newRecipe = new MerchantRecipe(newResult, recipe.getMaxUses());
        newRecipe.setUses(recipe.getUses());
        newRecipe.setExperienceReward(recipe.hasExperienceReward());
        newRecipe.setVillagerExperience(recipe.getVillagerExperience());
        newRecipe.setPriceMultiplier(recipe.getPriceMultiplier());
        newRecipe.setDemand(recipe.getDemand());
        newRecipe.setSpecialPrice(recipe.getSpecialPrice());
        newRecipe.setIngredients(recipe.getIngredients());
        return TradeFix.replace(newRecipe);
    }

    private static final class TradeFix {
        final boolean remove;
        final MerchantRecipe replacement;

        private TradeFix(boolean remove, MerchantRecipe replacement) {
            this.remove = remove;
            this.replacement = replacement;
        }

        static TradeFix remove() {
            return new TradeFix(true, null);
        }

        static TradeFix replace(MerchantRecipe recipe) {
            return new TradeFix(false, recipe);
        }
    }
}
