package fr.ironage.plugin.commands;

import fr.ironage.plugin.DiamondItems;
import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Bukkit;
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
 * /ironage reset : reinitialise le flag de distribution (console uniquement), pour permettre
 * une nouvelle distribution volontaire (par exemple en debut de nouvelle saison de serveur).
 */
public final class IronAgeCommand implements CommandExecutor {

    private final IronAgePlugin plugin;

    public IronAgeCommand(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("\u00A7cCette commande est reservee a la console du serveur.");
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
        StringBuilder recap = new StringBuilder("[IronAge] Distribution terminee :\n");

        // La commande console s'execute deja sur le thread principal : pas besoin de scheduler.
        for (Material material : DiamondItems.CONTROLLED_MATERIALS) {
            Player target = online.get(random.nextInt(online.size()));
            ItemStack item = new ItemStack(material, 1);
            giveItem(target, item, material, recap);
        }

        plugin.getConfig().set("ironage-distributed", true);
        plugin.saveConfig();

        sender.sendMessage(recap.toString());
        return true;
    }

    private void giveItem(Player target, ItemStack item, Material material, StringBuilder recap) {
        var leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(remaining ->
                    target.getWorld().dropItemNaturally(target.getLocation(), remaining));
        }

        target.sendMessage("\u00A7bTu as recu un objet en diamant unique : \u00A7f" + prettyName(material));
        recap.append(" - ").append(material.name()).append(" -> ").append(target.getName()).append('\n');
    }

    private String prettyName(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
