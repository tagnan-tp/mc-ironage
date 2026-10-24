package fr.ironage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Tague chaque objet distribue par /ironage avec une donnee invisible (PersistentDataContainer),
 * puis scanne periodiquement les inventaires des joueurs en ligne pour savoir qui possede quoi
 * a l'instant T, sans jamais afficher cette info dans le tchat.
 * <p>
 * Quand aucune distribution n'est active (apres un /ironage reset par exemple), tout objet
 * tague trouve est considere comme une anomalie et retire automatiquement (nettoyage des
 * joueurs qui etaient hors-ligne au moment du reset).
 */
public final class HolderScanner {

    private static final String TAG_KEY = "unique-diamond-item";

    private final IronAgePlugin plugin;
    private final NamespacedKey uniqueKey;
    private final HolderStorage storage;

    public HolderScanner(IronAgePlugin plugin, HolderStorage storage) {
        this.plugin = plugin;
        this.uniqueKey = new NamespacedKey(plugin, TAG_KEY);
        this.storage = storage;
    }

    public void tagItem(ItemStack item) {
        item.editMeta(meta -> meta.getPersistentDataContainer().set(uniqueKey, PersistentDataType.STRING, "true"));
    }

    public boolean isTaggedUniqueItem(ItemStack stack) {
        if (stack == null || !DiamondItems.isControlled(stack.getType()) || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(uniqueKey, PersistentDataType.STRING);
    }

    /** Scanne tous les joueurs en ligne : met a jour le detenteur actuel, ou nettoie les
     * objets trouves en trop si aucune distribution n'est active. */
    public void scanAndUpdate() {
        boolean distributed = plugin.getConfig().getBoolean("ironage-distributed", false);
        Map<Material, String> found = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!distributed) {
                stripStrayItems(player);
                continue;
            }
            scanPlayer(player, found);
        }

        if (!found.isEmpty()) {
            storage.updateCurrentHolders(found);
        }
    }

    private void scanPlayer(Player player, Map<Material, String> found) {
        PlayerInventory inventory = player.getInventory();
        scanStack(inventory.getItemInMainHand(), player, found);
        scanStack(inventory.getItemInOffHand(), player, found);
        for (ItemStack stack : inventory.getContents()) {
            scanStack(stack, player, found);
        }
        for (ItemStack stack : inventory.getArmorContents()) {
            scanStack(stack, player, found);
        }
    }

    private void scanStack(ItemStack stack, Player player, Map<Material, String> found) {
        if (isTaggedUniqueItem(stack)) {
            found.put(stack.getType(), player.getName());
        }
    }

    /** Retire tout objet en diamant unique trouve chez un joueur (utilise apres un reset,
     * notamment pour nettoyer les joueurs qui etaient hors-ligne au moment du wipe). */
    public void stripStrayItems(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack[] contents = inventory.getContents();
        boolean changed = false;
        for (int i = 0; i < contents.length; i++) {
            if (isTaggedUniqueItem(contents[i])) {
                contents[i] = null;
                changed = true;
            }
        }
        if (changed) {
            inventory.setContents(contents);
        }

        ItemStack[] armor = inventory.getArmorContents();
        changed = false;
        for (int i = 0; i < armor.length; i++) {
            if (isTaggedUniqueItem(armor[i])) {
                armor[i] = null;
                changed = true;
            }
        }
        if (changed) {
            inventory.setArmorContents(armor);
        }

        if (isTaggedUniqueItem(inventory.getItemInOffHand())) {
            inventory.setItemInOffHand(null);
        }
    }
}
