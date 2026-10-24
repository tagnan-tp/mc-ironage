package fr.ironage.plugin;

import fr.ironage.plugin.commands.CoifeurCommand;
import fr.ironage.plugin.commands.DiamondHoeCommand;
import fr.ironage.plugin.commands.ForceCommand;
import fr.ironage.plugin.commands.IronAgeCommand;
import fr.ironage.plugin.commands.IronTestCommand;
import fr.ironage.plugin.commands.VillagerMultiplierCommand;
import fr.ironage.plugin.listeners.ArmorStandBlockListener;
import fr.ironage.plugin.listeners.BundleBlockListener;
import fr.ironage.plugin.listeners.CraftBlockListener;
import fr.ironage.plugin.listeners.DiamondHoeCooldownResetListener;
import fr.ironage.plugin.listeners.DiamondHoeListener;
import fr.ironage.plugin.listeners.DiamondHoeTargetLifecycleListener;
import fr.ironage.plugin.listeners.DiamondItemProtectionListener;
import fr.ironage.plugin.listeners.DispensedLootBlockListener;
import fr.ironage.plugin.listeners.DispenserArmorStandBlockListener;
import fr.ironage.plugin.listeners.FeurListener;
import fr.ironage.plugin.listeners.HolderTrackingListener;
import fr.ironage.plugin.listeners.HopperBlockListener;
import fr.ironage.plugin.listeners.ItemFrameBlockListener;
import fr.ironage.plugin.listeners.LootBlockListener;
import fr.ironage.plugin.listeners.MaceLootListener;
import fr.ironage.plugin.listeners.MaceRulesListener;
import fr.ironage.plugin.listeners.StrengthRuleListener;
import fr.ironage.plugin.listeners.StorageBlockListener;
import fr.ironage.plugin.listeners.VillagerBookPriceListener;
import fr.ironage.plugin.listeners.VillagerTradeListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class IronAgePlugin extends JavaPlugin {

    private HolderStorage holderStorage;
    private HolderScanner holderScanner;
    private WindBurstBookManager windBurstBooks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Ajoute au config.yml existant les cles apparues dans une nouvelle version
        // (sans jamais ecraser les valeurs deja presentes).
        getConfig().options().copyDefaults(true);
        saveConfig();

        holderStorage = new HolderStorage(this);
        holderScanner = new HolderScanner(this, holderStorage);
        DiamondWipeService wipeService = new DiamondWipeService(holderScanner);
        SpecialChest specialChest = new SpecialChest(this);
        PendingAction pendingAction = new PendingAction();

        HoeTargetStorage hoeTargetStorage = new HoeTargetStorage(this);
        DiamondHoeService diamondHoeService = new DiamondHoeService(this, hoeTargetStorage);
        DiamondHoeChargeManager hoeChargeManager = new DiamondHoeChargeManager(this);

        DiamondItemProtectionListener protectionListener = new DiamondItemProtectionListener(holderScanner);

        StrengthPotionManager strengthPotions = new StrengthPotionManager(this);
        windBurstBooks = new WindBurstBookManager(this);
        // Recettes de tailleuse de pierre : oeuf de dragon -> livre Wind Burst I/II/III/IV.
        windBurstBooks.registerRecipes();

        // Les fonctions du plugin sont regroupees en 8 modules numerotes (l'ordre ci-dessous =
        // les numeros de /irontest). L'etat de depart vient de config.yml > modules ; /irontest
        // permet de les activer/couper a chaud pour diagnostiquer un probleme.
        ModuleManager modules = new ModuleManager(this);

        modules.add("craft-blocking", "craft des objets diamant bloque",
                List.of(new CraftBlockListener()), null, 0L, 0L);

        modules.add("container-blocking", "stockage bloque (coffres, sac, cadre, statue, entonnoir...)",
                List.of(new StorageBlockListener(this), new BundleBlockListener(), new ItemFrameBlockListener(),
                        new ArmorStandBlockListener(), new DispenserArmorStandBlockListener(),
                        new HopperBlockListener()),
                null, 0L, 0L);

        modules.add("loot-filter", "retrait du loot (coffres de structures, vaults)",
                List.of(new LootBlockListener(), new DispensedLootBlockListener()), null, 0L, 0L);

        // Scan periodique (toutes les 30s / 600 ticks) pour tenir detenteur-actuel.yml a jour,
        // et nettoyer automatiquement les joueurs hors-ligne qui se reconnectent apres un reset.
        modules.add("holder-tracking", "suivi du detenteur (scan a la connexion + toutes les 30s)",
                List.of(new HolderTrackingListener(holderScanner)), holderScanner::scanAndUpdate, 100L, 600L);

        // Empeche le despawn au sol des objets en diamant (toutes les 5s / 100 ticks).
        modules.add("item-protection", "anti-despawn/anti-feu + filet anti-objet non tague (scan 5s)",
                List.of(protectionListener), protectionListener::preventDespawnTick, 100L, 100L);

        modules.add("villagers", "commerces villageois (diamant -> cuivre) + prix des livres",
                List.of(new VillagerTradeListener(this), new VillagerBookPriceListener(this)), null, 0L, 0L);

        modules.add("hoe", "houe en diamant (radar, charges, recharge)",
                List.of(new DiamondHoeListener(holderScanner, diamondHoeService),
                        new DiamondHoeTargetLifecycleListener(diamondHoeService),
                        new DiamondHoeCooldownResetListener(this, holderScanner, hoeChargeManager)),
                null, 0L, 0L);

        modules.add("feur", "reponse \"Feur\" dans le tchat",
                List.of(new FeurListener(this)), null, 0L, 0L);

        modules.add("strength-rule", "pas de potion de Force II (sauf celle de /force give)",
                List.of(new StrengthRuleListener(strengthPotions)), null, 0L, 0L);

        modules.add("mace-rules", "masse : enchantement interdit sauf Wind Burst (livre = oeuf de dragon)",
                List.of(new MaceRulesListener(windBurstBooks), new MaceLootListener()), null, 0L, 0L);

        modules.applyConfig();

        // /ironage et /ironquick partagent le meme executeur (le nom de la commande les distingue).
        IronAgeCommand ironAgeExecutor = new IronAgeCommand(
                this, holderStorage, holderScanner, wipeService, specialChest, pendingAction,
                hoeTargetStorage, hoeChargeManager);

        PluginCommand ironAgeCommand = getCommand("ironage");
        if (ironAgeCommand != null) {
            ironAgeCommand.setExecutor(ironAgeExecutor);
        } else {
            getLogger().warning("La commande /ironage n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand ironQuickCommand = getCommand("ironquick");
        if (ironQuickCommand != null) {
            ironQuickCommand.setExecutor(ironAgeExecutor);
        } else {
            getLogger().warning("La commande /ironquick n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand coifeurCommand = getCommand("coifeur");
        if (coifeurCommand != null) {
            coifeurCommand.setExecutor(new CoifeurCommand(this));
        } else {
            getLogger().warning("La commande /coifeur n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand forceCommand = getCommand("force");
        if (forceCommand != null) {
            forceCommand.setExecutor(new ForceCommand(strengthPotions));
        } else {
            getLogger().warning("La commande /force n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand villagerMultiplierCommand = getCommand("villagermultiplier");
        if (villagerMultiplierCommand != null) {
            villagerMultiplierCommand.setExecutor(new VillagerMultiplierCommand(this));
        } else {
            getLogger().warning("La commande /villagermultiplier n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand diamondHoeCommand = getCommand("diamondhoe");
        if (diamondHoeCommand != null) {
            diamondHoeCommand.setExecutor(new DiamondHoeCommand(holderScanner, diamondHoeService, hoeChargeManager));
        } else {
            getLogger().warning("La commande /diamondhoe n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        PluginCommand ironTestCommand = getCommand("irontest");
        if (ironTestCommand != null) {
            ironTestCommand.setExecutor(new IronTestCommand(modules));
        } else {
            getLogger().warning("La commande /irontest n'a pas pu etre enregistree (verifie plugin.yml).");
        }

        getLogger().info("Modules actifs : " + modules.enabledSummary());
        String disabled = modules.disabledSummary();
        if (!disabled.isEmpty()) {
            getLogger().warning("Modules DESACTIVES (config.yml > modules) : " + disabled);
        }

        getLogger().info("IronAge active : le diamant est desormais un artefact unique et rare.");
    }

    @Override
    public void onDisable() {
        if (windBurstBooks != null) {
            windBurstBooks.unregisterRecipes();
        }
        getLogger().info("IronAge desactive.");
    }
}
