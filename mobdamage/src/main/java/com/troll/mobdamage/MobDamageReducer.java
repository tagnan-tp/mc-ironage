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

    // Pourcentage de réduction des dégâts infligés par les mobs (0.40 = -40%)
    private static final double REDUCTION = 0.40;

    private boolean enabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadState();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("mobdamage").setExecutor(this);

        getLogger().info("MobDamageReducer activé. Réduction des dégâts mobs : "
                + (enabled ? "ON (-40%)" : "OFF"));
    }

    @Override
    public void onDisable() {
        saveState();
    }

    private void reloadState() {
        FileConfiguration cfg = getConfig();
        this.enabled = cfg.getBoolean("enabled", true);
    }

    private void saveState() {
        getConfig().set("enabled", enabled);
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
        double reduced = original * (1.0 - REDUCTION);
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

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /mobdamage <on|off|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on" -> {
                enabled = true;
                saveState();
                sender.sendMessage(ChatColor.GREEN + "Réduction des dégâts mobs : ACTIVÉE (-40%).");
            }
            case "off" -> {
                enabled = false;
                saveState();
                sender.sendMessage(ChatColor.GREEN + "Réduction des dégâts mobs : DÉSACTIVÉE.");
            }
            case "status" -> sender.sendMessage(ChatColor.AQUA + "Statut : "
                    + (enabled ? "ACTIVÉE (-40%)" : "DÉSACTIVÉE"));
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /mobdamage <on|off|status>");
        }

        return true;
    }
}
