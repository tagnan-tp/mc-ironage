package fr.ironage.plugin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stocke la cible actuellement verrouillee par la houe en diamant (il n'y en a
 * qu'une possible puisque l'objet est unique). Le systeme de reroll est
 * desormais base sur des charges stockees directement sur l'objet (voir
 * DiamondHoeChargeManager), plus sur un cooldown temporel.
 */
public final class HoeTargetStorage {

    private final IronAgePlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public HoeTargetStorage(IronAgePlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "diamondhoe-target.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public UUID getTarget() {
        String raw = config.getString("target-uuid");
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setTarget(UUID uuid) {
        config.set("target-uuid", uuid == null ? null : uuid.toString());
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder diamondhoe-target.yml", e);
        }
    }
}
