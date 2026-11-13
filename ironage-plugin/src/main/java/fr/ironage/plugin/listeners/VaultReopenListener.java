package fr.ironage.plugin.listeners;

import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Vaults des Trial Chambers (normales et ominous) rouvrables a l'infini : en vanilla, chaque
 * joueur ne peut ouvrir une vault qu'UNE fois (la vault memorise les joueurs deja recompenses).
 * Ici, des qu'un joueur fait un clic droit sur une vault, il est retire de cette liste AVANT
 * que le jeu verifie sa cle : il peut donc la rouvrir avec une autre cle.
 * <p>
 * Chaque ouverture consomme toujours une cle : l'ajout n'enleve que la limite "une fois par
 * joueur". Desactivable avec "trial-vaults-reopenable: false" dans config.yml.
 */
public final class VaultReopenListener implements Listener {

    private final IronAgePlugin plugin;

    public VaultReopenListener(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onUseVault(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.VAULT) {
            return;
        }
        if (!plugin.getConfig().getBoolean("trial-vaults-reopenable", true)) {
            return;
        }

        Player player = event.getPlayer();
        if (block.getState() instanceof Vault vault && vault.hasRewardedPlayer(player.getUniqueId())) {
            vault.removeRewardedPlayer(player.getUniqueId());
            vault.update();
        }
    }
}
