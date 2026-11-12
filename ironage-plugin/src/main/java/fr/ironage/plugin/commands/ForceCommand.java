package fr.ironage.plugin.commands;

import fr.ironage.plugin.StrengthPotionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /force give [joueur] [quantite] : donne une splash potion de Force II, la seule qui
 * fonctionne (les autres sont ramenees a Force I, voir StrengthRuleListener).
 * Reserve aux OP et a la console. En jeu, sans joueur indique, la potion est donnee a
 * celui qui tape la commande ; depuis la console, le joueur est obligatoire.
 */
public final class ForceCommand implements TabExecutor {

    private static final int MAX_AMOUNT = 16;

    private final StrengthPotionManager potions;

    public ForceCommand(StrengthPotionManager potions) {
        this.potions = potions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Cette commande est reservee aux joueurs OP.");
            return true;
        }

        if (args.length < 1 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage("[IronAge] Usage : /force give [joueur] [quantite]");
            return true;
        }

        Player target;
        int amount = 1;

        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Joueur introuvable ou hors-ligne : " + args[1]);
                return true;
            }
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Quantite invalide : " + args[2]);
                    return true;
                }
                if (amount < 1 || amount > MAX_AMOUNT) {
                    sender.sendMessage(ChatColor.RED + "La quantite doit etre entre 1 et " + MAX_AMOUNT + ".");
                    return true;
                }
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            sender.sendMessage("[IronAge] Depuis la console, indique le joueur : /force give <joueur> [quantite]");
            return true;
        }

        // Les potions ne se cumulent pas dans un meme emplacement : une par une.
        for (int i = 0; i < amount; i++) {
            ItemStack potion = potions.createAdminSplashPotion();
            var leftover = target.getInventory().addItem(potion);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(remaining ->
                        target.getWorld().dropItemNaturally(target.getLocation(), remaining));
            }
        }

        target.sendMessage(ChatColor.AQUA + "Tu as recu " + amount + " splash potion(s) de Force II.");
        if (sender != target) {
            sender.sendMessage(ChatColor.GREEN + "[IronAge] " + amount + " splash potion(s) de Force II donnee(s) a "
                    + target.getName() + ".");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.isOp()) {
            return result;
        }
        if (args.length == 1) {
            if ("give".startsWith(args[0].toLowerCase())) {
                result.add("give");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(player.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (String suggestion : List.of("1", "4", "16")) {
                if (suggestion.startsWith(args[2])) {
                    result.add(suggestion);
                }
            }
        }
        return result;
    }
}
