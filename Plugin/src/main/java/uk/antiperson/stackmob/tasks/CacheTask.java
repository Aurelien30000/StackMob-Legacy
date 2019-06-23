package uk.antiperson.stackmob.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import uk.antiperson.stackmob.StackMob;

import java.util.Map;
import java.util.UUID;

public class CacheTask extends BukkitRunnable {

    private StackMob sm;

    public CacheTask(StackMob sm) {
        this.sm = sm;
    }

    @Override
    public void run() {
        Map<UUID, Integer> values = sm.getStorageManager().getCombinedMap();
        sm.getServer().getScheduler().runTaskAsynchronously(sm, () -> sm.getStorageManager().getStackStorage().saveStorage(values));
    }
}
