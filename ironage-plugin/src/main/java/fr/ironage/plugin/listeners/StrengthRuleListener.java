package fr.ironage.plugin.listeners;

import fr.ironage.plugin.StrengthPotionManager;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Interdit la Force II pour les joueurs, tout en laissant la Force I intacte :
 * - a la fabrication (alambic) : un resultat Force II est ramene a Force I ;
 * - a l'usage (boire / lancer) : filet de securite contre toute autre source de Force II.
 * La splash potion donnee par /force give (taguee) n'est JAMAIS modifiee et fonctionne.
 */
public final class StrengthRuleListener implements Listener {

    private final StrengthPotionManager potions;

    public StrengthRuleListener(StrengthPotionManager potions) {
        this.potions = potions;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        List<ItemStack> results = event.getResults();
        for (int i = 0; i < results.size(); i++) {
            ItemStack result = results.get(i);
            if (potions.isStrongStrength(result) && !potions.isAdminPotion(result)) {
                results.set(i, potions.downgrade(result));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (potions.isStrongStrength(item) && !potions.isAdminPotion(item)) {
            event.setItem(potions.downgrade(item));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion thrown)) {
            return;
        }
        ItemStack item = thrown.getItem();
        if (potions.isStrongStrength(item) && !potions.isAdminPotion(item)) {
            thrown.setItem(potions.downgrade(item));
        }
    }
}
