package fr.ironage.plugin.commands;

import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * /coifeur : active ou desactive la reponse automatique "Feur" (voir FeurListener).
 * Reserve aux joueurs OP (et a la console).
 */
public final class CoifeurCommand implements CommandExecutor {

    private final IronAgePlugin plugin;

    public CoifeurCommand(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowed = sender instanceof ConsoleCommandSender || sender.isOp();
        if (!allowed) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs OP (ou la console) peuvent utiliser cette commande.");
            return true;
        }

        boolean current = plugin.getConfig().getBoolean("feur-enabled", true);
        boolean newState = !current;
        plugin.getConfig().set("feur-enabled", newState);
        plugin.saveConfig();

        sender.sendMessage("[IronAge] Le \"Feur\" est maintenant "
                + (newState ? ChatColor.GREEN + "active" : ChatColor.RED + "desactive")
                + ChatColor.RESET + ".");
        return true;
    }
}
