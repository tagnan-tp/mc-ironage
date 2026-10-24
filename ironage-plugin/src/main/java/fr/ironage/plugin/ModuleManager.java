package fr.ironage.plugin;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Regroupe les fonctions du plugin en "modules" que l'on peut activer ou couper A CHAUD
 * (sans redemarrer), via /irontest. Un module = des listeners + eventuellement une tache
 * periodique. L'etat de depart vient de config.yml (section "modules") ; les changements
 * faits via /irontest sont temporaires et ne sont PAS sauvegardes (un redemarrage
 * revient a config.yml).
 */
public final class ModuleManager {

    public static final class Module {
        private final String key;
        private final String description;
        private final List<Listener> listeners;
        private final Runnable periodicTask; // peut etre null
        private final long delayTicks;
        private final long periodTicks;
        private boolean enabled;
        private BukkitTask task;

        private Module(String key, String description, List<Listener> listeners,
                       Runnable periodicTask, long delayTicks, long periodTicks) {
            this.key = key;
            this.description = description;
            this.listeners = listeners;
            this.periodicTask = periodicTask;
            this.delayTicks = delayTicks;
            this.periodTicks = periodTicks;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    private final IronAgePlugin plugin;
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    /** Declare un module (desactive tant que applyConfig/setEnabled n'est pas appele). */
    public void add(String key, String description, List<Listener> listeners,
                    Runnable periodicTask, long delayTicks, long periodTicks) {
        modules.add(new Module(key, description, listeners, periodicTask, delayTicks, periodTicks));
    }

    public List<Module> all() {
        return modules;
    }

    /** Numero 1..N (comme affiche par /irontest) -> module, ou null si hors limites. */
    public Module byNumber(int number) {
        if (number < 1 || number > modules.size()) {
            return null;
        }
        return modules.get(number - 1);
    }

    /** Applique l'etat de depart defini dans config.yml (tout actif par defaut). */
    public void applyConfig() {
        for (Module module : modules) {
            setEnabled(module, plugin.getConfig().getBoolean("modules." + module.key, true));
        }
    }

    public void setEnabled(Module module, boolean on) {
        if (module.enabled == on) {
            return;
        }
        module.enabled = on;

        if (on) {
            for (Listener listener : module.listeners) {
                plugin.getServer().getPluginManager().registerEvents(listener, plugin);
            }
            if (module.periodicTask != null) {
                module.task = plugin.getServer().getScheduler()
                        .runTaskTimer(plugin, module.periodicTask, module.delayTicks, module.periodTicks);
            }
        } else {
            for (Listener listener : module.listeners) {
                HandlerList.unregisterAll(listener);
            }
            if (module.task != null) {
                module.task.cancel();
                module.task = null;
            }
        }
    }

    public void setAll(boolean on) {
        for (Module module : modules) {
            setEnabled(module, on);
        }
    }

    public String enabledSummary() {
        List<String> names = new ArrayList<>();
        for (Module module : modules) {
            if (module.enabled) {
                names.add(module.key);
            }
        }
        return names.isEmpty() ? "(aucun)" : String.join(", ", names);
    }

    public String disabledSummary() {
        List<String> names = new ArrayList<>();
        for (Module module : modules) {
            if (!module.enabled) {
                names.add(module.key);
            }
        }
        return String.join(", ", names);
    }
}
