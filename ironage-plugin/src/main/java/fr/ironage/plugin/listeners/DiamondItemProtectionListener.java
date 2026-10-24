package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondItems;
import fr.ironage.plugin.HolderScanner;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Empeche les objets en diamant controles de despawn au sol (en reinitialisant
 * regulierement leur duree de vie via preventDespawnTick, appelee par un
 * scheduler dans IronAgePlugin) et d'etre detruits par le feu ou la lave.
 */
public final class DiamondItemProtectionListener implements Listener {

    private final HolderScanner holderScanner;

    public DiamondItemProtectionListener(HolderScanner holderScanner) {
        this.holderScanner = holderScanner;
    }

    /**
     * Filet de securite global : tout objet en diamant controle qui apparait au sol SANS
     * le tag d'unicite (donc qui ne vient pas de /ironage) est annule. Couvre les sources
     * de loot imprevues (vaults, mobs spawnes avec une armure diamant qui la lachent a
     * leur mort, etc.). Les vrais exemplaires gardent leur tag meme quand ils sont jetes.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();
        if (DiamondItems.isControlled(stack.getType()) && !holderScanner.isTaggedUniqueItem(stack)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) {
            return;
        }
        if (!holderScanner.isTaggedUniqueItem(item.getItemStack())) {
            return;
        }

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA) {
            event.setCancelled(true);
        }
    }

    /** A appeler periodiquement (toutes les quelques secondes) pour empecher le despawn
     * naturel au sol (technique classique et fiable : reinitialiser la duree de vie). */
    public void preventDespawnTick() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item && holderScanner.isTaggedUniqueItem(item.getItemStack())) {
                    item.setTicksLived(1);
                }
            }
        }
    }
}
