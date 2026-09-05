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
 */
public final class HolderScanner {

    private static final String TAG_KEY = "unique-diamond-item";

    private final NamespacedKey uniqueKey;
    private final HolderStorage storage;

    public HolderScanner(IronAgePlugin plugin, HolderStorage storage) {
        this.uniqueKey = new NamespacedKey(plugin, TAG_KEY);
        this.storage = storage;
    }

    public void tagItem(ItemStack item) {
        item.editMeta(meta -> meta.getPersistentDataContainer().set(uniqueKey, PersistentDataType.STRING, "true"));
    }

    /** Scanne tous les joueurs en ligne et met a jour detenteur-actuel.yml en consequence. */
    public void scanAndUpdate() {
        Map<Material, String> found = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
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

        if (!found.isEmpty()) {
            storage.updateCurrentHolders(found);
        }
    }

    private void scanStack(ItemStack stack, Player player, Map<Material, String> found) {
        if (stack == null || !DiamondItems.isControlled(stack.getType()) || !stack.hasItemMeta()) {
            return;
        }
        if (stack.getItemMeta().getPersistentDataContainer().has(uniqueKey, PersistentDataType.STRING)) {
            found.put(stack.getType(), player.getName());
        }
    }
}
