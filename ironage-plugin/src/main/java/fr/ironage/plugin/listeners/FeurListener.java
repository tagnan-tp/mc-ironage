package fr.ironage.plugin.listeners;

import fr.ironage.plugin.IronAgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.Listener;

/**
 * Repond automatiquement "Feur" (broadcast a tout le serveur) quand un joueur
 * ecrit une phrase se terminant par "quoi" (ponctuation finale ignoree).
 * Activable/desactivable via /coifeur (OP uniquement).
 */
public final class FeurListener implements Listener {

    private final IronAgePlugin plugin;

    public FeurListener(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("feur-enabled", true)) {
            return;
        }

        String message = event.getMessage().trim();
        if (message.isEmpty()) {
            return;
        }

        String[] words = message.split("\\s+");
        String lastWord = words[words.length - 1];

        // Retire toute ponctuation finale (?, !, ., virgule, guillemets...) du dernier mot.
        String normalizedLastWord = lastWord.replaceAll("\\p{Punct}+$", "").toLowerCase();

        if (!normalizedLastWord.equals("quoi")) {
            return;
        }

        String rawMessage = plugin.getConfig().getString("feur-message", "&bFeur !");
        String formatted = ChatColor.translateAlternateColorCodes('&', rawMessage);

        // On revient sur le thread principal pour envoyer le message a tout le monde,
        // AsyncPlayerChatEvent s'executant sur un thread asynchrone.
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(formatted));
    }
}
