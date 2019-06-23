package uk.antiperson.stackmob.entity.death.method;

import org.bukkit.entity.LivingEntity;
import uk.antiperson.stackmob.api.IStackMob;
import uk.antiperson.stackmob.api.entity.StackTools;
import uk.antiperson.stackmob.api.entity.death.DeathStep;
import uk.antiperson.stackmob.api.entity.death.DeathType;

import java.util.concurrent.ThreadLocalRandom;

public class KillStep extends DeathStep {

    public KillStep(IStackMob sm) {
        super(sm, DeathType.KILL_STEP);
    }

    @Override
    public int calculateStep(LivingEntity dead) {
        int stackAmount = StackTools.getSize(dead);
        int maxStep = getStackMob().getCustomConfig().getInt("kill-step.max-step");
        int randomStep = ThreadLocalRandom.current().nextInt(1, maxStep);
        if (randomStep > stackAmount) {
            return stackAmount;
        }
        return randomStep;
    }
}
