package fr.ironage.plugin.commands;

import fr.ironage.plugin.DiamondHoeChargeManager;
import fr.ironage.plugin.DiamondHoeService;
import fr.ironage.plugin.HolderScanner;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /diamondhoe : reroll manuel de la cible verrouillee par la houe en diamant.
 * - /diamondhoe : consomme 1 charge de la houe (voir DiamondHoeChargeManager).
 * - /diamondhoe op : illimite, reserve aux OP, ne consomme aucune charge.
 * Doit obligatoirement etre execute par un joueur tenant la houe en main
 * (impossible depuis la console).
 */
public final class DiamondHoeCommand implements CommandExecutor {

    private final HolderScanner holderScanner;
    private final DiamondHoeService hoeService;
    private final DiamondHoeChargeManager chargeManager;

    public DiamondHoeCommand(HolderScanner holderScanner, DiamondHoeService hoeService,
                              DiamondHoeChargeManager chargeManager) {
        this.holderScanner = holderScanner;
        this.hoeService = hoeService;
        this.chargeManager = chargeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur (pas la console).");
            return true;
        }

        boolean opMode = args.length == 1 && args[0].equalsIgnoreCase("op");
        if (opMode && !player.isOp()) {
            player.sendMessage(ChatColor.RED + "Reserve aux joueurs OP.");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.DIAMOND_HOE || !holderScanner.isTaggedUniqueItem(item)) {
            player.sendMessage(ChatColor.RED + "Tu dois tenir la houe en diamant unique en main.");
            return true;
        }

        if (!opMode) {
            if (!chargeManager.consumeCharge(item)) {
                player.sendMessage(ChatColor.RED + "Plus aucune charge disponible. Recharge la houe en "
                        + "posant un bloc de diamant avec elle sur une table de craft.");
                return true;
            }
            // La charge consommee/le nom mis a jour vivent sur l'ItemStack : on repousse
            // la modification dans le slot pour qu'elle soit bien prise en compte.
            player.getInventory().setItemInMainHand(item);
        }

        hoeService.manualReroll(player);
        return true;
    }
}
