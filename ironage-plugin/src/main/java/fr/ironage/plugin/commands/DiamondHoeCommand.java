package fr.ironage.plugin.commands;

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
 * - /diamondhoe : reroll limite a 2 fois par heure, joueur normal.
 * - /diamondhoe op : reroll illimite, reserve aux OP.
 * Doit obligatoirement etre execute par un joueur tenant la houe en main
 * (impossible depuis la console).
 */
public final class DiamondHoeCommand implements CommandExecutor {

    private final HolderScanner holderScanner;
    private final DiamondHoeService hoeService;

    public DiamondHoeCommand(HolderScanner holderScanner, DiamondHoeService hoeService) {
        this.holderScanner = holderScanner;
        this.hoeService = hoeService;
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

        hoeService.manualReroll(player, opMode);
        return true;
    }
}
