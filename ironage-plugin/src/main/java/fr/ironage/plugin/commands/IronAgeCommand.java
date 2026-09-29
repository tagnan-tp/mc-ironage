package fr.ironage.plugin.commands;

import fr.ironage.plugin.DiamondHoeChargeManager;
import fr.ironage.plugin.DiamondItems;
import fr.ironage.plugin.DiamondWipeService;
import fr.ironage.plugin.HoeTargetStorage;
import fr.ironage.plugin.HolderScanner;
import fr.ironage.plugin.HolderStorage;
import fr.ironage.plugin.IronAgePlugin;
import fr.ironage.plugin.PendingAction;
import fr.ironage.plugin.SpecialChest;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * /ironage : commande d'administration principale, reservee a la console.
 * <p>
 * - /ironage : distribue les 10 objets en diamant aux joueurs en ligne (une fois).
 * - /ironage reset : supprime TOUS les exemplaires existants et remet a zero.
 * - /ironage chestgive &lt;joueur&gt; : donne un coffre special autorise a stocker
 *   ces objets.
 * - /ironage give &lt;OBJET&gt; &lt;joueur&gt; : donne/reassigne manuellement un objet
 *   precis (materiau Bukkit, ex: DIAMOND_SWORD) a un joueur, en retirant
 *   d'abord tout exemplaire existant pour garantir l'unicite.
 * - /ironage confirm : confirme et execute la derniere action en attente
 *   (distribution, reset, chestgive ou give), qui expire au bout de 30 secondes.
 */
public final class IronAgeCommand implements CommandExecutor {

    private static final long CONFIRM_WINDOW_MILLIS = 30_000L;

    private final IronAgePlugin plugin;
    private final HolderStorage holderStorage;
    private final HolderScanner holderScanner;
    private final DiamondWipeService wipeService;
    private final SpecialChest specialChest;
    private final PendingAction pendingAction;
    private final HoeTargetStorage hoeTargetStorage;
    private final DiamondHoeChargeManager hoeChargeManager;

    public IronAgeCommand(IronAgePlugin plugin, HolderStorage holderStorage, HolderScanner holderScanner,
                           DiamondWipeService wipeService, SpecialChest specialChest, PendingAction pendingAction,
                           HoeTargetStorage hoeTargetStorage, DiamondHoeChargeManager hoeChargeManager) {
        this.plugin = plugin;
        this.holderStorage = holderStorage;
        this.holderScanner = holderScanner;
        this.wipeService = wipeService;
        this.specialChest = specialChest;
        this.pendingAction = pendingAction;
        this.hoeTargetStorage = hoeTargetStorage;
        this.hoeChargeManager = hoeChargeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Cette commande est reservee a la console du serveur.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            handleConfirm(sender);
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("chestgive")) {
            handleChestGive(sender, args);
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("give")) {
            handleGive(sender, args);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            queueReset(sender);
            return true;
        }

        if (args.length == 0) {
            queueDistribute(sender);
            return true;
        }

        sender.sendMessage("[IronAge] Sous-commande inconnue. Utilise /ironage, /ironage reset, "
                + "/ironage chestgive <joueur>, /ironage give <OBJET> <joueur> ou /ironage confirm.");
        return true;
    }

    private void handleConfirm(CommandSender sender) {
        if (!pendingAction.hasPending()) {
            sender.sendMessage("[IronAge] Aucune action en attente de confirmation (ou elle a expire).");
            return;
        }
        String description = pendingAction.getDescription();
        pendingAction.confirmAndClear();
        sender.sendMessage("[IronAge] Action confirmee et executee : " + description);
    }

    private void queueDistribute(CommandSender sender) {
        if (plugin.getConfig().getBoolean("ironage-distributed", false)) {
            sender.sendMessage("[IronAge] Les objets en diamant ont deja ete distribues. "
                    + "Utilise \"/ironage reset\" (puis /ironage confirm) pour tout remettre a zero d'abord.");
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            sender.sendMessage("[IronAge] Aucun joueur en ligne, impossible de distribuer les objets.");
            return;
        }

        pendingAction.set(this::performDistribute,
                "Distribution des 10 objets en diamant aux joueurs en ligne",
                CONFIRM_WINDOW_MILLIS);
        sender.sendMessage("[IronAge] Pret a distribuer les 10 objets aux joueurs en ligne. "
                + "Tape \"/ironage confirm\" dans les 30 secondes pour confirmer.");
    }

    private void performDistribute() {
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (online.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage("[IronAge] Plus aucun joueur en ligne, distribution annulee.");
            return;
        }

        // Distribution equitable par construction : on melange l'ordre des objets ET des
        // joueurs, puis on distribue en tournante. L'ecart entre le joueur le mieux et le
        // moins bien loti est ainsi garanti a 1 objet maximum, quel que soit leur nombre -
        // tout en gardant le hasard sur qui recoit precisement quel objet.
        List<Material> materials = new ArrayList<>(DiamondItems.CONTROLLED_MATERIALS);
        Collections.shuffle(materials, ThreadLocalRandom.current());
        Collections.shuffle(online, ThreadLocalRandom.current());

        int playerCount = online.size();
        for (int i = 0; i < materials.size(); i++) {
            Material material = materials.get(i);
            Player target = online.get(i % playerCount);
            giveTaggedItem(material, target);
        }

        plugin.getConfig().set("ironage-distributed", true);
        plugin.saveConfig();
    }

    private void queueReset(CommandSender sender) {
        pendingAction.set(this::performReset,
                "Suppression de TOUS les objets en diamant existants et remise a zero de la distribution",
                CONFIRM_WINDOW_MILLIS);
        sender.sendMessage(ChatColor.YELLOW + "[IronAge] Attention : cela va supprimer definitivement tous les "
                + "objets en diamant actuellement en jeu (inventaires, sol, conteneurs charges). "
                + "Tape \"/ironage confirm\" dans les 30 secondes pour confirmer.");
    }

    private void performReset() {
        int removed = wipeService.wipeAll();

        plugin.getConfig().set("ironage-distributed", false);
        plugin.saveConfig();

        holderStorage.clearAll();
        hoeTargetStorage.setTarget(null);

        Bukkit.getConsoleSender().sendMessage("[IronAge] Reset termine : " + removed + " objet(s) en diamant "
                + "supprime(s) (joueurs en ligne, sol, mobs, conteneurs charges). Les joueurs hors-ligne "
                + "porteurs d'un objet seront nettoyes automatiquement a leur reconnexion.");
    }

    private void handleChestGive(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("[IronAge] Usage : /ironage chestgive <joueur>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("[IronAge] Joueur introuvable ou hors-ligne : " + args[1]);
            return;
        }

        pendingAction.set(() -> performChestGive(target),
                "Don d'un coffre special (autorise a stocker les objets en diamant) a " + target.getName(),
                CONFIRM_WINDOW_MILLIS);
        sender.sendMessage("[IronAge] Pret a donner un coffre special a " + target.getName()
                + ". Tape \"/ironage confirm\" dans les 30 secondes pour confirmer.");
    }

    private void performChestGive(Player target) {
        ItemStack chest = specialChest.createItem();
        var leftover = target.getInventory().addItem(chest);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(remaining ->
                    target.getWorld().dropItemNaturally(target.getLocation(), remaining));
        }
        target.sendMessage(ChatColor.AQUA + "Tu as recu un coffre special, capable de stocker des objets en diamant uniques.");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("[IronAge] Usage : /ironage give <OBJET> <joueur> "
                    + "(OBJET = nom Bukkit exact, ex: DIAMOND_SWORD, DIAMOND_HOE...)");
            return;
        }

        Material material;
        try {
            material = Material.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("[IronAge] Objet invalide : " + args[1]);
            return;
        }

        if (!DiamondItems.isControlled(material)) {
            sender.sendMessage("[IronAge] Cet objet ne fait pas partie des 10 objets en diamant controles.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage("[IronAge] Joueur introuvable ou hors-ligne : " + args[2]);
            return;
        }

        pendingAction.set(() -> performGive(material, target),
                "Don manuel de " + material.name() + " a " + target.getName()
                        + " (tout exemplaire existant sera retire au prealable)",
                CONFIRM_WINDOW_MILLIS);
        sender.sendMessage("[IronAge] Pret a donner " + material.name() + " a " + target.getName()
                + ". Tape \"/ironage confirm\" dans les 30 secondes pour confirmer.");
    }

    private void performGive(Material material, Player target) {
        int removed = wipeService.wipeMaterial(material);
        giveTaggedItem(material, target);

        Bukkit.getConsoleSender().sendMessage("[IronAge] " + material.name() + " donne a " + target.getName()
                + " (" + removed + " exemplaire(s) existant(s) retire(s) au prealable).");
    }

    /** Cree, tague et donne un exemplaire d'un objet controle a un joueur, et met a jour
     * les fichiers de suivi (detenteur actuel, et originel si pas deja enregistre). */
    private void giveTaggedItem(Material material, Player target) {
        ItemStack item = new ItemStack(material, 1);
        holderScanner.tagItem(item);
        if (material == Material.DIAMOND_HOE) {
            hoeChargeManager.initialize(item);
        }

        var leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            leftover.values().forEach(remaining ->
                    target.getWorld().dropItemNaturally(target.getLocation(), remaining));
        }

        target.sendMessage(ChatColor.AQUA + "Tu as recu un artefact en diamant unique !");

        holderStorage.recordCurrentHolder(material, target.getName());
        holderStorage.recordOriginalHolderIfAbsent(material, target.getName());
    }
}
