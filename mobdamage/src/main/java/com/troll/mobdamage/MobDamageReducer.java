package com.troll.mobdamage;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MobDamageReducer extends JavaPlugin implements Listener, CommandExecutor {

    private boolean enabled = true;

    // Pourcentage de réduction (0.0 à 1.0), lu depuis config.yml (reduction-percent, 0-100)
    private double reduction = 0.40;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadState();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mobdamage").setExecutor(this);

        getLogger().info("MobDamageReducer activé. Réduction des dégâts mobs : "
                + (enabled ? "ON (-" + Math.round(reduction * 100) + "%)" : "OFF"));
    }

    @Override
    public void onDisable() {
        saveState();
    }

    private void reloadState() {
        // Relit entièrement config.yml depuis le disque (utile après édition manuelle du fichier)
        reloadConfig();
        FileConfiguration cfg = getConfig();

        this.enabled = cfg.getBoolean("enabled", true);

        double percent = cfg.getDouble("reduction-percent", 40.0);
        // Clamp de sécurité : on reste toujours entre 0 et 100
        percent = Math.max(0.0, Math.min(100.0, percent));
        this.reduction = percent / 100.0;
    }

    private void saveState() {
        getConfig().set("enabled", enabled);
        getConfig().set("reduction-percent", reduction * 100.0);
        saveConfig();
    }

    // ---------------------------------------------------------------
    // Réduction des dégâts infligés par un mob (LivingEntity non-joueur)
    // à un joueur, de 40%.
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobHitPlayer(EntityDamageByEntityEvent event) {
        if (!enabled) return;

        // La victime doit être un joueur
        if (!(event.getEntity() instanceof Player)) return;

        // L'agresseur direct doit être un mob (LivingEntity, mais pas un joueur).
        // Couvre aussi le cas des projectiles tirés par un mob (squelette, etc.)
        // via getDamager() -> shooter.
        if (!isMobDamager(event)) return;

        double original = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        double reduced = original * (1.0 - reduction);
        event.setDamage(EntityDamageEvent.DamageModifier.BASE, reduced);
    }

    private boolean isMobDamager(EntityDamageByEntityEvent event) {
        var damager = event.getDamager();

        // Cas direct : mob au corps à corps
        if (damager instanceof LivingEntity && !(damager instanceof Player)) {
            return true;
        }

        // Cas projectile (flèche de squelette, boule de feu de blaze, etc.)
        if (damager instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof LivingEntity shooter
                    && !(shooter instanceof Player)) {
                return true;
            }
        }

        return false;
    }

    // ---------------------------------------------------------------
    // Commande /mobdamage <on|off|status>
    // Restreinte à la console et aux joueurs OP, indépendamment
    // de tout plugin de permissions tiers.
    // ---------------------------------------------------------------
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            if (!(sender instanceof Player player) || !player.isOp()) {
                sender.sendMessage(ChatColor.RED + "Seule la console ou un joueur OP peut utiliser cette commande.");
                return true;
            }
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /mobdamage <on|off|status|reload|set <0-100>>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                enabled = true;
                saveState();
                sender.sendMessage(ChatColor.GREEN + "Réduction des dégâts mobs : ACTIVÉE (-"
                        + Math.round(reduction * 100) + "%).");
            }
            case "off" -> {
                enabled = false;
                saveState();
                sender.sendMessage(ChatColor.GREEN + "Réduction des dégâts mobs : DÉSACTIVÉE.");
            }
            case "status" -> sender.sendMessage(ChatColor.AQUA + "Statut : "
                    + (enabled ? "ACTIVÉE (-" + Math.round(reduction * 100) + "%)" : "DÉSACTIVÉE"));
            case "reload" -> {
                // Relit enabled + reduction-percent depuis config.yml sur le disque
                reloadState();
                sender.sendMessage(ChatColor.GREEN + "Config rechargée depuis config.yml : "
                        + (enabled ? "ACTIVÉE (-" + Math.round(reduction * 100) + "%)" : "DÉSACTIVÉE"));
            }
            case "set" -> {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /mobdamage set <0-100>");
                    return true;
                }
                try {
                    double percent = Double.parseDouble(args[1]);
                    percent = Math.max(0.0, Math.min(100.0, percent));
                    reduction = percent / 100.0;
                    saveState();
                    sender.sendMessage(ChatColor.GREEN + "Pourcentage de réduction réglé sur -"
                            + Math.round(reduction * 100) + "%.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Valeur invalide. Exemple : /mobdamage set 40");
                }
            }
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /mobdamage <on|off|status|reload|set <0-100>>");
        }

        return true;
    }
}
