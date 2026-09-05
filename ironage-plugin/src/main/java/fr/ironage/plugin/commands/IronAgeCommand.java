package fr.ironage.plugin.commands;

import fr.ironage.plugin.DiamondItems;
import fr.ironage.plugin.HolderScanner;
import fr.ironage.plugin.HolderStorage;
import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * /ironage : distribue aleatoirement les 10 objets en diamant (un seul exemplaire de chacun)
 * entre les joueurs actuellement en ligne. Uniquement executable depuis la console.
 * <p>
 * Aucun message ne revele quel joueur a recu quel objet, ni en console ni dans le tchat :
 * cette information vit uniquement dans detenteur-originel.yml et detenteur-actuel.yml,
 * dans le dossier du plugin.
 * <p>
 * /ironage reset : reinitialise le flag de distribution (console uniquement).
 */
public final class IronAgeCommand implements CommandExecutor {

    private final IronAgePlugin plugin;
    private final HolderStorage holderStorage;
    private final HolderScanner holderScanner;

    public IronAgeCommand(IronAgePlugin plugin, HolderStorage holderStorage, HolderScanner holderScanner) {
        this.plugin = plugin;
        this.holderStorage = holderStorage;
        this.holderScanner = holderScanner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est reservee a la console du serveur.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            plugin.getConfig().set("ironage-distributed", false);
            plugin.saveConfig();
            sender.sendMessage("[IronAge] Flag de distribution reinitialise. "
                    + "La prochaine execution de /ironage redistribuera un exemplaire de chaque objet.");
            return true;
        }

        if (plugin.getConfig().getBoolean("ironage-distributed", false)) {
            sender.sendMessage("[IronAge] Les objets en diamant ont deja ete distribues une fois. "
                    + "Utilise \"/ironage reset\" si tu veux vraiment relancer une distribution "
                    + "(cela recreera des exemplaires uniques supplementaires).");
            return true;
        }

        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) {
            sender.sendMessage("[IronAge] Aucun joueur en ligne, impossible de distribuer les objets.");
            return true;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Material material : DiamondItems.CONTROLLED_MATERIALS) {
            Player target = online.get(random.nextInt(online.size()));
            ItemStack item = new ItemStack(material, 1);
            holderScanner.tagItem(item);

            var leftover = target.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(remaining ->
                        target.getWorld().dropItemNaturally(target.getLocation(), remaining));
            }

            // Message volontairement generique : ne mentionne pas quel objet precis c'est,
            // pour qu'aucune association nom-de-joueur / objet ne transite par le tchat.
            target.sendMessage(ChatColor.AQUA + "Tu as recu un artefact en diamant unique !");

            holderStorage.recordOriginalHolder(material, target.getName());
            holderStorage.recordCurrentHolder(material, target.getName());
        }

        plugin.getConfig().set("ironage-distributed", true);
        plugin.saveConfig();

        sender.sendMessage("[IronAge] Distribution terminee. Le detail (qui a recu quoi) est "
                + "uniquement disponible dans detenteur-originel.yml, dans le dossier du plugin.");
        return true;
    }
}
