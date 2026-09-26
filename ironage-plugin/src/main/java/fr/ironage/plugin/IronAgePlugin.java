package fr.ironage.plugin;

import fr.ironage.plugin.commands.CoifeurCommand;
import fr.ironage.plugin.commands.DiamondHoeCommand;
import fr.ironage.plugin.commands.IronAgeCommand;
import fr.ironage.plugin.listeners.ArmorStandBlockListener;
import fr.ironage.plugin.listeners.BundleBlockListener;
import fr.ironage.plugin.listeners.CraftBlockListener;
import fr.ironage.plugin.listeners.DiamondHoeListener;
import fr.ironage.plugin.listeners.DiamondHoeTargetLifecycleListener;
import fr.ironage.plugin.listeners.DiamondItemProtectionListener;
import fr.ironage.plugin.listeners.DispenserArmorStandBlockListener;
import fr.ironage.plugin.listeners.FeurListener;
import fr.ironage.plugin.listeners.HolderTrackingListener;
import fr.ironage.plugin.listeners.HopperBlockListener;
import fr.ironage.plugin.listeners.ItemFrameBlockListener;
import fr.ironage.plugin.listeners.LootBlockListener;
import fr.ironage.plugin.listeners.StorageBlockListener;
import fr.ironage.plugin.listeners.VillagerTradeListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class IronAgePlugin extends JavaPlugin {

    private HolderStorage holderStorage;
    private HolderScanner holderScanner;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        holderStorage = new HolderStorage(this);
        holderScanner = new HolderScanner(this, holderStorage);
        DiamondWipeService wipeService = new DiamondWipeService(holderScanner);
        SpecialChest specialChest = new SpecialChest(this);
        PendingAction pendingAction = new PendingAction();

        HoeTargetStorage hoeTargetStorage = new HoeTargetStorage(this);
        DiamondHoeService diamondHoeService = new DiamondHoeService(this, hoeTargetStorage);

        DiamondItemProtectionListener protectionListener = new DiamondItemProtectionListener(holderScanner);

        getServer().getPluginManager().registerEvents(new CraftBlockListener(), this);
        getServer().getPluginManager().registerEvents(new StorageBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new LootBlockListener(), this);
        getServer().getPluginManager().registerEvents(new HopperBlockListener(), this);
        getServer().getPluginManager().registerEvents(new HolderTrackingListener(holderScanner), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListener(this), this);
        getServer().getPluginManager().registerEvents(new BundleBlockListener(), this);
        getServer().getPluginManager().registerEvents(new ArmorStandBlockListener(), this);
        getServer().getPluginManager().registerEvents(new DispenserArmorStandBlockListener(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameBlockListener(), this);
        getServer().getPluginManager().registerEvents(protectionListener, this);
        getServer().getPluginManager().registerEvents(new FeurListener(this), this);
        getServer().getPluginManager().registerEvents(new DiamondHoeListener(holderScanner, diamondHoeService), this);
        getServer().getPluginManager().registerEvents(new DiamondHoeTargetLifecycleListener(diamondHoeService), this);

        PluginCommand ironAgeCommand = getCommand("ironage");
        if (ironAgeCommand != null) {
            ironAgeCommand.setExecutor(new IronAgeCommand(
                    this, holderStorage, holderScanner, wipeService, specialChest, pendingAction));
        } else {
            getLogger().warning("La commande /ironage n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand coifeurCommand = getCommand("coifeur");
        if (coifeurCommand != null) {
            coifeurCommand.setExecutor(new CoifeurCommand(this));
        } else {
            getLogger().warning("La commande /coifeur n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand diamondHoeCommand = getCommand("diamondhoe");
        if (diamondHoeCommand != null) {
            diamondHoeCommand.setExecutor(new DiamondHoeCommand(holderScanner, diamondHoeService));
        } else {
            getLogger().warning("La commande /diamondhoe n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        // Scan periodique (toutes les 30s / 600 ticks) pour tenir detenteur-actuel.yml a jour,
        // et nettoyer automatiquement les joueurs hors-ligne qui se reconnectent apres un reset.
        getServer().getScheduler().runTaskTimer(this, holderScanner::scanAndUpdate, 100L, 600L);

        // Empeche le despawn au sol des objets en diamant (toutes les 5s / 100 ticks).
        getServer().getScheduler().runTaskTimer(this, protectionListener::preventDespawnTick, 100L, 100L);

        getLogger().info("IronAge active : le diamant est desormais un artefact unique et rare.");
    }

    @Override
    public void onDisable() {
        getLogger().info("IronAge desactive.");
    }
}
