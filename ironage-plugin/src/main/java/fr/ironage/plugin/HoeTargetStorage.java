package fr.ironage.plugin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stocke la cible actuellement verrouillee par la houe en diamant (il n'y en a
 * qu'une possible puisque l'objet est unique), ainsi que l'historique des
 * reroll manuels (/diamondhoe) pour appliquer la limite de 2 par heure.
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

    public List<Long> getRerollTimestamps() {
        List<Long> result = new ArrayList<>();
        for (Object obj : config.getList("reroll-timestamps", new ArrayList<>())) {
            if (obj instanceof Number number) {
                result.add(number.longValue());
            }
        }
        return result;
    }

    public void addRerollTimestamp(long timestamp) {
        List<Long> timestamps = getRerollTimestamps();
        timestamps.add(timestamp);
        config.set("reroll-timestamps", timestamps);
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
