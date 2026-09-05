package fr.ironage.plugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Gere deux fichiers YAML dans le dossier du plugin :
 * - detenteur-originel.yml : qui a recu chaque objet lors du tirage /ironage (fige).
 * - detenteur-actuel.yml : qui possede chaque objet en ce moment (mis a jour en continu).
 * <p>
 * Aucune de ces informations n'est jamais affichee dans le tchat ou la console :
 * elles ne vivent que dans ces deux fichiers, pour garder l'anonymat des tirages.
 */
public final class HolderStorage {

    private final IronAgePlugin plugin;
    private final File originalFile;
    private final File currentFile;
    private final YamlConfiguration originalConfig;
    private final YamlConfiguration currentConfig;

    public HolderStorage(IronAgePlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.originalFile = new File(plugin.getDataFolder(), "detenteur-originel.yml");
        this.currentFile = new File(plugin.getDataFolder(), "detenteur-actuel.yml");
        this.originalConfig = YamlConfiguration.loadConfiguration(originalFile);
        this.currentConfig = YamlConfiguration.loadConfiguration(currentFile);
    }

    public void recordOriginalHolder(Material material, String playerName) {
        originalConfig.set(material.name(), playerName);
        save(originalConfig, originalFile);
    }

    public void recordCurrentHolder(Material material, String playerName) {
        currentConfig.set(material.name(), playerName);
        save(currentConfig, currentFile);
    }

    /**
     * Met a jour le fichier "detenteur actuel" a partir d'un scan des joueurs en ligne.
     * Un objet non retrouve (joueur deconnecte, objet perdu au sol...) garde son dernier
     * detenteur connu.
     */
    public void updateCurrentHolders(Map<Material, String> foundHolders) {
        boolean changed = false;
        for (Map.Entry<Material, String> entry : foundHolders.entrySet()) {
            String existing = currentConfig.getString(entry.getKey().name());
            if (!entry.getValue().equals(existing)) {
                currentConfig.set(entry.getKey().name(), entry.getValue());
                changed = true;
            }
        }
        if (changed) {
            save(currentConfig, currentFile);
        }
    }

    private void save(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Impossible de sauvegarder " + file.getName(), e);
        }
    }
}
