package fr.ironage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Supprime toute trace des objets en diamant controles actuellement en jeu :
 * inventaires des joueurs en ligne, objets au sol, equipement des mobs (un
 * zombie peut ramasser une armure tombee), et conteneurs des chunks charges.
 * <p>
 * Limite honnete : les joueurs hors-ligne ne peuvent pas etre nettoyes ici
 * (leurs donnees ne sont pas en memoire) ; ils le sont automatiquement des
 * leur reconnexion via HolderScanner (voir stripStrayItems).
 */
public final class DiamondWipeService {

    private final HolderScanner holderScanner;

    public DiamondWipeService(HolderScanner holderScanner) {
        this.holderScanner = holderScanner;
    }

    public int wipeAll() {
        int removed = 0;
        removed += wipeOnlinePlayers();
        removed += wipeWorlds();
        return removed;
    }

    private int wipeOnlinePlayers() {
        int removed = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            removed += stripInventory(player.getInventory());
        }
        return removed;
    }

    private int stripInventory(Inventory inventory) {
        int removed = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (holderScanner.isTaggedUniqueItem(contents[i])) {
                contents[i] = null;
                removed++;
            }
        }
        inventory.setContents(contents);

        if (inventory instanceof PlayerInventory playerInventory) {
            ItemStack[] armor = playerInventory.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (holderScanner.isTaggedUniqueItem(armor[i])) {
                    armor[i] = null;
                    removed++;
                }
            }
            playerInventory.setArmorContents(armor);

            if (holderScanner.isTaggedUniqueItem(playerInventory.getItemInOffHand())) {
                playerInventory.setItemInOffHand(null);
                removed++;
            }
        }
        return removed;
    }

    private int wipeWorlds() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (holderScanner.isTaggedUniqueItem(item.getItemStack())) {
                        item.remove();
                        removed++;
                    }
                } else if (entity instanceof LivingEntity living) {
                    EntityEquipment equipment = living.getEquipment();
                    if (equipment != null) {
                        removed += stripEquipment(equipment);
                    }
                }
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container) {
                        removed += stripInventory(container.getInventory());
                    }
                }
            }
        }
        return removed;
    }

    private int stripEquipment(EntityEquipment equipment) {
        int removed = 0;
        if (holderScanner.isTaggedUniqueItem(equipment.getHelmet())) {
            equipment.setHelmet(null);
            removed++;
        }
        if (holderScanner.isTaggedUniqueItem(equipment.getChestplate())) {
            equipment.setChestplate(null);
            removed++;
        }
        if (holderScanner.isTaggedUniqueItem(equipment.getLeggings())) {
            equipment.setLeggings(null);
            removed++;
        }
        if (holderScanner.isTaggedUniqueItem(equipment.getBoots())) {
            equipment.setBoots(null);
            removed++;
        }
        if (holderScanner.isTaggedUniqueItem(equipment.getItemInMainHand())) {
            equipment.setItemInMainHand(null);
            removed++;
        }
        if (holderScanner.isTaggedUniqueItem(equipment.getItemInOffHand())) {
            equipment.setItemInOffHand(null);
            removed++;
        }
        return removed;
    }
}
