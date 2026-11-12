package fr.ironage.plugin.commands;

import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * /villagermultiplier [valeur] : regle le multiplicateur applique au prix des livres
 * enchantes chez les villageois (voir VillagerBookPriceListener).
 * <p>
 * Reserve aux joueurs OP (la console y a aussi acces, par nature). Les valeurs proposees
 * par defaut a l'auto-completion sont x1, x2, x0.5 et x0.25, mais n'importe quel nombre
 * positif est accepte. Sans argument, affiche le multiplicateur actuel.
 */
public final class VillagerMultiplierCommand implements TabExecutor {

    private static final List<String> SUGGESTIONS = List.of("1", "2", "0.5", "0.25");
    private static final double MAX_MULTIPLIER = 100.0;

    private final IronAgePlugin plugin;

    public VillagerMultiplierCommand(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Cette commande est reservee aux joueurs OP.");
            return true;
        }

        double current = plugin.getConfig().getDouble("villager-book-price-multiplier", 1.0);

        if (args.length == 0) {
            sender.sendMessage("[IronAge] Multiplicateur actuel du prix des livres chez les villageois : x"
                    + format(current) + ". Usage : /villagermultiplier <valeur> (ex: 1, 2, 0.5, 0.25)");
            return true;
        }

        double value;
        try {
            String raw = args[0].trim().toLowerCase().replace(',', '.');
            if (raw.startsWith("x")) {
                raw = raw.substring(1);
            }
            value = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Valeur invalide : " + args[0]
                    + ". Exemples : 1, 2, 0.5, 0.25 (ou x2, x0.5).");
            return true;
        }

        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0 || value > MAX_MULTIPLIER) {
            sender.sendMessage(ChatColor.RED + "Le multiplicateur doit etre superieur a 0 et au maximum x"
                    + format(MAX_MULTIPLIER) + ".");
            return true;
        }

        plugin.getConfig().set("villager-book-price-multiplier", value);
        plugin.saveConfig();

        sender.sendMessage(ChatColor.GREEN + "[IronAge] Prix des livres chez les villageois : x" + format(value)
                + " (s'applique des la prochaine ouverture d'un menu de commerce).");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.isOp() || args.length != 1) {
            return result;
        }
        String typed = args[0].toLowerCase();
        for (String suggestion : SUGGESTIONS) {
            if (suggestion.startsWith(typed)) {
                result.add(suggestion);
            }
        }
        return result;
    }

    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
