package uk.antiperson.stackmob.listeners.chunk;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import uk.antiperson.stackmob.api.IStackMob;
import uk.antiperson.stackmob.api.entity.StackTools;
import uk.antiperson.stackmob.api.tools.GlobalValues;

public class LoadEvent implements Listener {

    private IStackMob sm;

    public LoadEvent(IStackMob sm) {
        this.sm = sm;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (sm.getCustomConfig().getStringList("no-stack-worlds")
                .contains(e.getWorld().getName())) {
            return;
        }
        for (Entity currentEntity : e.getChunk().getEntities()) {
            if (!(currentEntity instanceof Creature)) {
                continue;
            }
            // Check if has been cached.
            if (sm.getCache().containsKey(currentEntity.getUniqueId())) {
                int cacheSize = sm.getCache().get(currentEntity.getUniqueId());
                sm.getCache().remove(currentEntity.getUniqueId());
                StackTools.setSize(currentEntity, cacheSize);
                continue;
            }
            if (currentEntity.getCustomName() != null) {
                continue;
            }
            if (sm.getTraitManager().checkTraits(currentEntity)) {
                continue;
            }
            if (sm.getHookManager().cantStack(currentEntity)) {
                continue;
            }
            if (sm.getLogic().doChecks(currentEntity)) {
                continue;
            }
            StackTools.setSize(currentEntity, GlobalValues.NOT_ENOUGH_NEAR);
        }
    }

}
