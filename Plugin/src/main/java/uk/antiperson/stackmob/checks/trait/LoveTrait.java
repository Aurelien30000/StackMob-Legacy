package uk.antiperson.stackmob.checks.trait;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import uk.antiperson.stackmob.api.checks.ApplicableTrait;

public class LoveTrait implements ApplicableTrait {

    public String getConfigPath() {
        return "compare.love-mode";
    }

    @Override
    public boolean checkTrait(Entity original, Entity nearby) {
        if (original instanceof Animals) {
            return ((Animals) original).canBreed() || ((Animals) nearby).canBreed();
        }
        return false;
    }

    @Override
    public void applyTrait(Entity original, Entity spawned) {
        if (original instanceof Animals) {
            ((Animals) spawned).setBreed(((Animals) original).canBreed());
        }
    }
}
