package fr.ironage.plugin.listeners;

import fr.ironage.plugin.DiamondHoeService;
import fr.ironage.plugin.HolderScanner;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class DiamondHoeListener implements Listener {

    private final HolderScanner holderScanner;
    private final DiamondHoeService hoeService;

    public DiamondHoeListener(HolderScanner holderScanner, DiamondHoeService hoeService) {
        this.holderScanner = holderScanner;
        this.hoeService = hoeService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!event.getAction().isRightClick()) {
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() != Material.DIAMOND_HOE || !holderScanner.isTaggedUniqueItem(item)) {
            return;
        }

        hoeService.onRightClick(event.getPlayer());
    }
}
