package fr.ironage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
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
 * wipeAll() traite les 10 objets ; wipeMaterial(Material) ne cible qu'un seul
 * objet precis (utilise par /ironage give pour garantir l'unicite avant de
 * recreer un exemplaire).
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
        return wipe(null);
    }

    public int wipeMaterial(Material material) {
        return wipe(material);
    }

    private boolean matches(ItemStack stack, Material filter) {
        if (filter == null) {
            return holderScanner.isTaggedUniqueItem(stack);
        }
        return stack != null && stack.getType() == filter && holderScanner.isTaggedUniqueItem(stack);
    }

    private int wipe(Material filter) {
        int removed = 0;
        removed += wipeOnlinePlayers(filter);
        removed += wipeWorlds(filter);
        return removed;
    }

    private int wipeOnlinePlayers(Material filter) {
        int removed = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            removed += stripInventory(player.getInventory(), filter);
        }
        return removed;
    }

    private int stripInventory(Inventory inventory, Material filter) {
        int removed = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (matches(contents[i], filter)) {
                contents[i] = null;
                removed++;
            }
        }
        inventory.setContents(contents);

        if (inventory instanceof PlayerInventory playerInventory) {
            ItemStack[] armor = playerInventory.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (matches(armor[i], filter)) {
                    armor[i] = null;
                    removed++;
                }
            }
            playerInventory.setArmorContents(armor);

            if (matches(playerInventory.getItemInOffHand(), filter)) {
                playerInventory.setItemInOffHand(null);
                removed++;
            }
        }
        return removed;
    }

    private int wipeWorlds(Material filter) {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (matches(item.getItemStack(), filter)) {
                        item.remove();
                        removed++;
                    }
                } else if (entity instanceof LivingEntity living) {
                    EntityEquipment equipment = living.getEquipment();
                    if (equipment != null) {
                        removed += stripEquipment(equipment, filter);
                    }
                }
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container) {
                        removed += stripInventory(container.getInventory(), filter);
                    }
                }
            }
        }
        return removed;
    }

    private int stripEquipment(EntityEquipment equipment, Material filter) {
        int removed = 0;
        if (matches(equipment.getHelmet(), filter)) {
            equipment.setHelmet(null);
            removed++;
        }
        if (matches(equipment.getChestplate(), filter)) {
            equipment.setChestplate(null);
            removed++;
        }
        if (matches(equipment.getLeggings(), filter)) {
            equipment.setLeggings(null);
            removed++;
        }
        if (matches(equipment.getBoots(), filter)) {
            equipment.setBoots(null);
            removed++;
        }
        if (matches(equipment.getItemInMainHand(), filter)) {
            equipment.setItemInMainHand(null);
            removed++;
        }
        if (matches(equipment.getItemInOffHand(), filter)) {
            equipment.setItemInOffHand(null);
            removed++;
        }
        return removed;
    }
}
