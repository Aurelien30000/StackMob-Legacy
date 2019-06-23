package uk.antiperson.stackmob;

import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import uk.antiperson.stackmob.api.IStackMob;
import uk.antiperson.stackmob.api.bcompat.Compat;
import uk.antiperson.stackmob.api.cache.IStorageManager;
import uk.antiperson.stackmob.api.checks.ITraitManager;
import uk.antiperson.stackmob.api.compat.IHookManager;
import uk.antiperson.stackmob.api.compat.PluginCompat;
import uk.antiperson.stackmob.api.entity.IEntityTools;
import uk.antiperson.stackmob.api.entity.IStackLogic;
import uk.antiperson.stackmob.api.entity.death.IDeathManager;
import uk.antiperson.stackmob.api.entity.multiplication.IDropTools;
import uk.antiperson.stackmob.api.entity.multiplication.IExperienceTools;
import uk.antiperson.stackmob.api.stick.IStickTools;
import uk.antiperson.stackmob.api.tools.BukkitVersion;
import uk.antiperson.stackmob.api.tools.GlobalValues;
import uk.antiperson.stackmob.api.tools.IUpdateChecker;
import uk.antiperson.stackmob.api.tools.VersionHelper;
import uk.antiperson.stackmob.cache.StorageManager;
import uk.antiperson.stackmob.checks.TraitManager;
import uk.antiperson.stackmob.compat.HookManager;
import uk.antiperson.stackmob.config.ConfigFile;
import uk.antiperson.stackmob.config.EntityLangFile;
import uk.antiperson.stackmob.config.GeneralLangFile;
import uk.antiperson.stackmob.entity.EntityTools;
import uk.antiperson.stackmob.entity.StackLogic;
import uk.antiperson.stackmob.entity.death.DeathManager;
import uk.antiperson.stackmob.entity.multiplication.DropTools;
import uk.antiperson.stackmob.entity.multiplication.ExperienceTools;
import uk.antiperson.stackmob.listeners.ServerLoad;
import uk.antiperson.stackmob.listeners.chunk.LoadEvent;
import uk.antiperson.stackmob.listeners.entity.*;
import uk.antiperson.stackmob.listeners.player.ChatEvent;
import uk.antiperson.stackmob.listeners.player.QuitEvent;
import uk.antiperson.stackmob.listeners.player.StickInteractEvent;
import uk.antiperson.stackmob.stick.StickTools;
import uk.antiperson.stackmob.tasks.CacheTask;
import uk.antiperson.stackmob.tasks.ShowTagTask;
import uk.antiperson.stackmob.tasks.TagTask;
import uk.antiperson.stackmob.tools.UpdateChecker;

import java.util.Map;
import java.util.UUID;

/**
 * Created by nathat on 23/07/17.
 */
public class StackMob extends JavaPlugin implements IStackMob {

    private ConfigFile config;
    private EntityLangFile entityLang;
    private GeneralLangFile generalLang;
    private IEntityTools entityTools;
    private IDropTools dropTools;
    private IStickTools stickTools;
    private IExperienceTools expTools;
    private IStackLogic logic;
    private IStorageManager storageManager;
    private IHookManager hookManager;
    private ITraitManager traitManager;
    private IDeathManager deathManager;
    private IUpdateChecker updater;
    private Compat compat;

    @Override
    public void onLoad() {
        hookManager = new HookManager(this);
        getHookManager().onServerLoad();
    }

    @Override
    public void onEnable() {
        // Startup messages
        getLogger().info("StackMob v" + getDescription().getVersion() + " created by antiPerson (pas_francais)");
        getLogger().info("Documentation can be found at " + getDescription().getWebsite());
        getLogger().info("GitHub repository can be found at " + GlobalValues.GITHUB);

        if (VersionHelper.getVersion() == BukkitVersion.UNSUPPORTED) {
            getLogger().warning("A bukkit version that is not supported has been detected! (" + Bukkit.getBukkitVersion() + ")");
            getLogger().warning("The features of this version are not supported, so the plugin will not enable!");
            return;
        }
        getLogger().info("Detected server version: " + VersionHelper.getVersion());
        initVariables();

        // Loads configuration file into memory, and if not found, file is copied from the jar file.
        getConfigFile().reloadCustomConfig();
        getTranslationFile().reloadCustomConfig();
        getGeneralFile().reloadCustomConfig();

        // Initialize support for other plugins.
        getHookManager().registerHooks();
        // Register traits for entity comparison.
        getTraitManager().registerTraits();

        initBukkitCompat();

        if (getCustomConfig().isBoolean("plugin.loginupdatechecker")) {
            getLogger().info("An old version of the configuration file has been detected!");
            getLogger().info("A new one will be generated and the old one will be renamed to config.old");
            getConfigFile().generateNewVersion();
        }

        // Load the storage.
        getLogger().info("Loading cached entities...");
        getStorageManager().onServerEnable();

        // Essential listeners/tasks that are needed for the plugin to function correctly.
        getLogger().info("Registering listeners, tasks and commands...");
        registerEssentialEvents();

        // Events that are not required for the plugin to function, however they make a better experience.
        registerNotEssentialEvents();

        getLogger().info("Starting metrics (if enabled)...");
        new MetricsLite(this);

        getLogger().info(getUpdater().updateString());
    }


    @Override
    public void onDisable() {
        getLogger().info("Cancelling currently running tasks...");
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("Saving entity amount storage...");
        // Save the storage so entity amounts aren't lost on restarts
        getStorageManager().onServerDisable();
    }

    private void initVariables() {
        config = new ConfigFile(this);
        entityLang = new EntityLangFile(this);
        generalLang = new GeneralLangFile(this);
        entityTools = new EntityTools(this);
        dropTools = new DropTools(this);
        stickTools = new StickTools(this);
        expTools = new ExperienceTools(this);
        logic = new StackLogic(this);
        storageManager = new StorageManager(this);
        traitManager = new TraitManager(this);
        deathManager = new DeathManager(this);
        updater = new UpdateChecker(this);
    }

    private void registerEssentialEvents() {
        getServer().getPluginManager().registerEvents(new SpawnEvent(this), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(this), this);
        getServer().getPluginManager().registerEvents(new LoadEvent(this), this);
        getServer().getPluginManager().registerEvents(new EntityRemoveListener(this), this);
        new ServerLoad(this).onServerLoad();
        getCommand("stackmob").setExecutor(new Commands(this));
        startTasks();
    }

    private void registerNotEssentialEvents() {
        if (getCustomConfig().getBoolean("multiply.creeper-explosion")) {
            getServer().getPluginManager().registerEvents(new ExplodeEvent(), this);
        }
        if (getCustomConfig().getBoolean("multiply.chicken-eggs")) {
            getServer().getPluginManager().registerEvents(new ItemDrop(this), this);
        }
        if (getCustomConfig().getBoolean("divide-on.sheep-dye")) {
            getServer().getPluginManager().registerEvents(new DyeEvent(this), this);
        }
        if (getCustomConfig().getBoolean("divide-on.breed")) {
            getServer().getPluginManager().registerEvents(new InteractEvent(this), this);
        }
        if (getCustomConfig().getBoolean("multiply.small-slimes")) {
            getServer().getPluginManager().registerEvents(new SlimeEvent(), this);
        }
        if (getCustomConfig().getBoolean("multiply-damage-done")) {
            getServer().getPluginManager().registerEvents(new DealtDamageEvent(), this);
        }
        if (getCustomConfig().getBoolean("multiply-damage-received.enabled")) {
            getServer().getPluginManager().registerEvents(new ReceivedDamageEvent(this), this);
        }
        if (getCustomConfig().getBoolean("no-targeting.enabled")) {
            getServer().getPluginManager().registerEvents(new TargetEvent(this), this);
        }
        if (getCustomConfig().getBoolean("divide-on.tame")) {
            getServer().getPluginManager().registerEvents(new TameEvent(this), this);
        }
        getServer().getPluginManager().registerEvents(new ShearEvent(this), this);
        getServer().getPluginManager().registerEvents(new StickInteractEvent(this), this);
        getServer().getPluginManager().registerEvents(new ChatEvent(this), this);
        getServer().getPluginManager().registerEvents(new QuitEvent(this), this);
        //getServer().getPluginManager().registerEvents(new ConvertEvent(), this); Useless in 1.12.2
    }

    private void startTasks() {
        new TagTask(this).runTaskTimer(this, 0, getCustomConfig().getInt("tag.interval"));
        if (getHookManager().isHookRegistered(PluginCompat.PROCOTOLLIB)) {
            new ShowTagTask(this).runTaskTimer(this, 5, getCustomConfig().getInt("tag.interval"));
        }
        if (getCustomConfig().getInt("storage.delay") > 0) {
            new CacheTask(this).runTaskTimer(this, 0, getCustomConfig().getInt("storage.delay") * 20);
        }
    }

    private void initBukkitCompat() {
        compat = VersionHelper.getBukkitCompat(this);
        if (compat == null) {
            getLogger().warning("An error occurred while trying to load bukkit compatibility measures.");
            getLogger().warning("This will mean that certain features of your Mincraft version won't be supported, and some errors may occur.");
            getLogger().warning("Make sure that the plugin is fully updated, by running the command '/sm check'");
        }
        compat.onEnable();
    }

    @Override
    public FileConfiguration getCustomConfig() {
        return config.getCustomConfig();
    }

    @Override
    public ConfigFile getConfigFile() {
        return config;
    }

    @Override
    public IHookManager getHookManager() {
        return hookManager;
    }

    @Override
    public IStorageManager getStorageManager() {
        return storageManager;
    }

    @Override
    public ITraitManager getTraitManager() {
        return traitManager;
    }

    @Override
    public IDeathManager getDeathManager() {
        return deathManager;
    }

    @Override
    public IEntityTools getTools() {
        return entityTools;
    }

    @Override
    public IDropTools getDropTools() {
        return dropTools;
    }

    @Override
    public IStickTools getStickTools() {
        return stickTools;
    }

    @Override
    public FileConfiguration getTranslationConfig() {
        return entityLang.getCustomConfig();
    }

    @Override
    public EntityLangFile getTranslationFile() {
        return entityLang;
    }

    @Override
    public IExperienceTools getExpTools() {
        return expTools;
    }

    @Override
    public FileConfiguration getGeneralConfig() {
        return generalLang.getCustomConfig();
    }

    @Override
    public GeneralLangFile getGeneralFile() {
        return generalLang;
    }

    @Override
    public IUpdateChecker getUpdater() {
        return updater;
    }

    @Override
    public IStackLogic getLogic() {
        return logic;
    }

    @Override
    public Compat getCompat() {
        return compat;
    }

    @Override
    public Map<UUID, Integer> getCache() {
        return getStorageManager().getAmountCache();
    }
}
