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

import java.util.ArrayList;
import java.util.List;

/**
 * Retire les objets en diamant controles actuellement en jeu : inventaires des joueurs
 * en ligne, objets au sol, equipement des mobs (un zombie peut ramasser une armure
 * tombee), et conteneurs des chunks charges.
 * <p>
 * - wipeAll() : supprime les 10 objets (utilise par /ironage reset).
 * - wipeMaterial(Material) : supprime un seul objet (utilise par /ironage give).
 * - collectAll() : retire les objets ET en renvoie des copies completes (enchantements,
 *   durabilite, charges de la houe...) pour pouvoir les redistribuer (/ironage reroll).
 * <p>
 * Limite honnete : les joueurs hors-ligne et les chunks non charges ne peuvent pas etre
 * atteints ici ; les joueurs hors-ligne sont nettoyes a leur reconnexion (HolderScanner).
 */
public final class DiamondWipeService {

    private final HolderScanner holderScanner;

    public DiamondWipeService(HolderScanner holderScanner) {
        this.holderScanner = holderScanner;
    }

    public int wipeAll() {
        return wipe(null, null);
    }

    public int wipeMaterial(Material material) {
        return wipe(material, null);
    }

    /** Retire tous les exemplaires et les retourne (copies conservant tout leur etat). */
    public List<ItemStack> collectAll() {
        List<ItemStack> sink = new ArrayList<>();
        wipe(null, sink);
        return sink;
    }

    private boolean matches(ItemStack stack, Material filter) {
        if (filter == null) {
            return holderScanner.isTaggedUniqueItem(stack);
        }
        return stack != null && stack.getType() == filter && holderScanner.isTaggedUniqueItem(stack);
    }

    /** Note l'objet dans le collecteur (si present) avant qu'il soit retire. */
    private void take(ItemStack stack, List<ItemStack> sink) {
        if (sink != null && stack != null) {
            sink.add(stack.clone());
        }
    }

    private int wipe(Material filter, List<ItemStack> sink) {
        int removed = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            removed += stripInventory(player.getInventory(), filter, sink);
        }
        removed += wipeWorlds(filter, sink);
        return removed;
    }

    private int stripInventory(Inventory inventory, Material filter, List<ItemStack> sink) {
        int removed = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (matches(contents[i], filter)) {
                take(contents[i], sink);
                contents[i] = null;
                removed++;
            }
        }
        inventory.setContents(contents);

        if (inventory instanceof PlayerInventory playerInventory) {
            ItemStack[] armor = playerInventory.getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (matches(armor[i], filter)) {
                    take(armor[i], sink);
                    armor[i] = null;
                    removed++;
                }
            }
            playerInventory.setArmorContents(armor);

            ItemStack offHand = playerInventory.getItemInOffHand();
            if (matches(offHand, filter)) {
                take(offHand, sink);
                playerInventory.setItemInOffHand(null);
                removed++;
            }
        }
        return removed;
    }

    private int wipeWorlds(Material filter, List<ItemStack> sink) {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item item) {
                    if (matches(item.getItemStack(), filter)) {
                        take(item.getItemStack(), sink);
                        item.remove();
                        removed++;
                    }
                } else if (entity instanceof LivingEntity living) {
                    EntityEquipment equipment = living.getEquipment();
                    if (equipment != null) {
                        removed += stripEquipment(equipment, filter, sink);
                    }
                }
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container) {
                        removed += stripInventory(container.getInventory(), filter, sink);
                    }
                }
            }
        }
        return removed;
    }

    private int stripEquipment(EntityEquipment equipment, Material filter, List<ItemStack> sink) {
        int removed = 0;
        if (matches(equipment.getHelmet(), filter)) {
            take(equipment.getHelmet(), sink);
            equipment.setHelmet(null);
            removed++;
        }
        if (matches(equipment.getChestplate(), filter)) {
            take(equipment.getChestplate(), sink);
            equipment.setChestplate(null);
            removed++;
        }
        if (matches(equipment.getLeggings(), filter)) {
            take(equipment.getLeggings(), sink);
            equipment.setLeggings(null);
            removed++;
        }
        if (matches(equipment.getBoots(), filter)) {
            take(equipment.getBoots(), sink);
            equipment.setBoots(null);
            removed++;
        }
        if (matches(equipment.getItemInMainHand(), filter)) {
            take(equipment.getItemInMainHand(), sink);
            equipment.setItemInMainHand(null);
            removed++;
        }
        if (matches(equipment.getItemInOffHand(), filter)) {
            take(equipment.getItemInOffHand(), sink);
            equipment.setItemInOffHand(null);
            removed++;
        }
        return removed;
    }
}
