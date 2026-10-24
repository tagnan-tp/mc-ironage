package fr.ironage.plugin;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Coffre special (donne via /ironage chestgive) qui a le droit, lui, de stocker
 * les objets en diamant controles, contrairement aux conteneurs normaux.
 * <p>
 * Le tag est pose sur l'etat de bloc embarque dans l'item (BlockStateMeta), pour
 * qu'il se retrouve automatiquement sur le bloc une fois le coffre pose. Le PDC
 * n'est accessible que via l'interface TileState (les BlockState "simples" ne
 * l'exposent pas), d'ou la verification/cast explicite.
 */
public final class SpecialChest {

    private static final String TAG_KEY = "ironage-special-chest";

    private final NamespacedKey key;

    public SpecialChest(IronAgePlugin plugin) {
        this.key = new NamespacedKey(plugin, TAG_KEY);
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.CHEST);
        item.editMeta(meta -> {
            meta.setDisplayName("\u00A7bCoffre de l'Age de Fer");
            meta.setLore(List.of("\u00A77Peut contenir des objets en diamant uniques."));
            if (meta instanceof BlockStateMeta blockStateMeta) {
                BlockState blockState = blockStateMeta.getBlockState();
                if (blockState instanceof TileState tileState) {
                    tileState.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
                    blockStateMeta.setBlockState(tileState);
                }
            }
        });
        return item;
    }

    public boolean isSpecialChestBlock(BlockState state) {
        if (!(state instanceof Container) || !(state instanceof TileState tileState)) {
            return false;
        }
        return tileState.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
