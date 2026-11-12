package fr.ironage.plugin.commands;

import fr.ironage.plugin.ModuleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * /irontest : outil de diagnostic. Chaque numero applique INSTANTANEMENT une configuration
 * de modules predefinie, pour tester sans reflechir (nomme "irontest" car Minecraft possede
 * deja un /test vanilla depuis la 1.21.5).
 * <p>
 * - /irontest        : liste les configurations numerotees.
 * - /irontest &lt;n&gt;    : applique la configuration n.
 * - /irontest reset  : revient a l'etat defini dans config.yml (section "modules").
 * <p>
 * Les configurations sont generees pour permettre de retrouver le module responsable d'un
 * probleme en quelques essais, SANS avoir a decider quoi tester ensuite :
 * - la 1re coupe tout (base) ;
 * - les suivantes sont des "tests binaires" : chaque module apparait dans un ensemble de tests
 *   unique, donc la combinaison des tests qui echouent designe le module fautif ;
 * - puis "tout actif" (controle), et enfin un test "module seul" par module (verification).
 * <p>
 * Les changements sont temporaires (non sauvegardes). Reserve aux OP et a la console.
 */
public final class IronTestCommand implements TabExecutor {

    private static final class Preset {
        private final String description;
        private final Set<Integer> enabled;

        private Preset(String description, Set<Integer> enabled) {
            this.description = description;
            this.enabled = enabled;
        }
    }

    private final ModuleManager modules;
    private final List<Preset> presets = new ArrayList<>();
    private int lastApplied = 0;

    public IronTestCommand(ModuleManager modules) {
        this.modules = modules;
        buildPresets();
    }

    private void buildPresets() {
        int n = modules.all().size();

        presets.add(new Preset("BASE : tout coupe (aucun module actif)", new TreeSet<>()));

        // Tests binaires : le test "bit" active les modules dont le numero-1 a ce bit a 1.
        for (int bit = 0; (1 << bit) < n; bit++) {
            Set<Integer> set = new TreeSet<>();
            for (int i = 0; i < n; i++) {
                if ((i & (1 << bit)) != 0) {
                    set.add(i + 1);
                }
            }
            presets.add(new Preset("Test binaire " + (char) ('A' + bit) + " : seuls les modules "
                    + join(set) + " sont actifs", set));
        }

        Set<Integer> all = new TreeSet<>();
        for (int i = 1; i <= n; i++) {
            all.add(i);
        }
        presets.add(new Preset("CONTROLE : tout actif (le probleme doit reapparaitre)", all));

        for (int i = 1; i <= n; i++) {
            Set<Integer> single = new TreeSet<>();
            single.add(i);
            presets.add(new Preset("Verification : seul le module " + i + " ("
                    + modules.byNumber(i).getKey() + ") est actif", single));
        }
    }

    private String join(Set<Integer> numbers) {
        return numbers.stream().map(String::valueOf).collect(Collectors.joining(", "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Cette commande est reservee aux joueurs OP.");
            return true;
        }

        if (args.length == 0) {
            showPresets(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            modules.applyConfig();
            lastApplied = 0;
            sender.sendMessage(ChatColor.GREEN + "[IronAge TEST] Retour a la configuration de config.yml. "
                    + "Modules actifs : " + modules.enabledSummary());
            return true;
        }

        int number;
        try {
            number = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Utilise /irontest <numero> (1-" + presets.size()
                    + ") ou /irontest reset.");
            return true;
        }
        if (number < 1 || number > presets.size()) {
            sender.sendMessage(ChatColor.RED + "Numero inconnu : " + number + " (attendu : 1-"
                    + presets.size() + ").");
            return true;
        }

        Preset preset = presets.get(number - 1);
        for (int i = 1; i <= modules.all().size(); i++) {
            modules.setEnabled(modules.byNumber(i), preset.enabled.contains(i));
        }
        lastApplied = number;

        sender.sendMessage(ChatColor.AQUA + "[IronAge TEST] Config " + number + " appliquee : " + preset.description);
        sender.sendMessage(ChatColor.GRAY + "Modules actifs : " + modules.enabledSummary());
        sender.sendMessage(ChatColor.YELLOW + "Fais (re)connecter le joueur a tester MAINTENANT, puis note le resultat.");
        return true;
    }

    private void showPresets(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "[IronAge TEST] Configurations (temporaires, /irontest <n>) :");
        for (int i = 0; i < presets.size(); i++) {
            int number = i + 1;
            String marker = number == lastApplied ? ChatColor.GREEN + " <-- appliquee" : "";
            sender.sendMessage(ChatColor.GRAY + " " + number + ". " + ChatColor.WHITE
                    + presets.get(i).description + marker);
        }
        sender.sendMessage(ChatColor.GRAY + "Modules : " + moduleList());
        sender.sendMessage(ChatColor.GRAY + "/irontest reset : revient a config.yml");
    }

    private String moduleList() {
        List<String> parts = new ArrayList<>();
        int number = 1;
        for (ModuleManager.Module module : modules.all()) {
            parts.add(number + "=" + module.getKey());
            number++;
        }
        return String.join(", ", parts);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (!sender.isOp() || args.length != 1) {
            return result;
        }
        List<String> options = new ArrayList<>();
        for (int i = 1; i <= presets.size(); i++) {
            options.add(String.valueOf(i));
        }
        options.add("reset");

        String typed = args[0].toLowerCase();
        for (String option : options) {
            if (option.startsWith(typed)) {
                result.add(option);
            }
        }
        return result;
    }
}
