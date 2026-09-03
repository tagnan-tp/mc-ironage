package fr.ironage.plugin;

import fr.ironage.plugin.commands.IronAgeCommand;
import fr.ironage.plugin.listeners.CraftBlockListener;
import fr.ironage.plugin.listeners.HopperBlockListener;
import fr.ironage.plugin.listeners.LootBlockListener;
import fr.ironage.plugin.listeners.StorageBlockListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class IronAgePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new CraftBlockListener(), this);
        getServer().getPluginManager().registerEvents(new StorageBlockListener(), this);
        getServer().getPluginManager().registerEvents(new LootBlockListener(), this);
        getServer().getPluginManager().registerEvents(new HopperBlockListener(), this);

        PluginCommand ironAgeCommand = getCommand("ironage");
        if (ironAgeCommand != null) {
            ironAgeCommand.setExecutor(new IronAgeCommand(this));
        } else {
            getLogger().warning("La commande /ironage n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        getLogger().info("IronAge active : le diamant est desormais un artefact unique et rare.");
    }

    @Override
    public void onDisable() {
        getLogger().info("IronAge desactive.");
    }
}
