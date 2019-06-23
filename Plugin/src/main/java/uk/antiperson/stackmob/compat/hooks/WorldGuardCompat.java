package uk.antiperson.stackmob.compat.hooks;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.StackMob;

public class WorldGuardCompat {

    private StackMob sm;
    private static final StateFlag ENTITY_FLAG = new StateFlag("entity-stacking", true);
    private WorldGuardPlugin WGplugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");

    public WorldGuardCompat(StackMob sm) {
        this.sm = sm;
    }

    public void registerFlag() {
        FlagRegistry registry = WGplugin.getFlagRegistry();
        try {
            registry.register(ENTITY_FLAG);
            sm.getLogger().info("Registered WorldGuard region flag.");
        } catch (FlagConflictException e) {
            sm.getLogger().warning("A conflict occurred while trying to register the WorldGuard flag.");
            e.printStackTrace();
        }
    }

    public boolean test(Entity entity) {
        ApplicableRegionSet ars = WGplugin.getRegionManager(entity.getWorld()).getApplicableRegions(entity.getLocation());
        return ars.testState(null, ENTITY_FLAG);
    }

}
