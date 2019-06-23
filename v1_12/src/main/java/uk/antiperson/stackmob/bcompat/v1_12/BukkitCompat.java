package uk.antiperson.stackmob.bcompat.v1_12;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import uk.antiperson.stackmob.api.IStackMob;
import uk.antiperson.stackmob.api.bcompat.Compat;
import uk.antiperson.stackmob.bcompat.v1_12.checks.trait.VillagerTrait;

public class BukkitCompat implements Compat {

    private IStackMob sm;

    public BukkitCompat(IStackMob sm) {
        this.sm = sm;
    }

    @Override
    public void onEnable() {
        sm.getTraitManager().registerTrait(new VillagerTrait());
    }

    @Override
    public boolean checkFood(Entity entity, ItemStack food) {
        Material type = food.getType();
        switch (entity.getType()) {
            case COW:
            case SHEEP:
            case MUSHROOM_COW:
                return type == Material.WHEAT;
            case PIG:
                return (type == Material.CARROT_ITEM || type == Material.BEETROOT || type == Material.POTATO_ITEM);
            case CHICKEN:
                return type == Material.SEEDS
                        || type == Material.MELON_SEEDS
                        || type == Material.BEETROOT_SEEDS
                        || type == Material.PUMPKIN_SEEDS;
            case HORSE:
                return (type == Material.GOLDEN_APPLE || type == Material.GOLDEN_CARROT) && ((Horse) entity).isTamed();
            case WOLF:
                return (type == Material.RAW_BEEF
                        || type == Material.RAW_CHICKEN
                        || type == Material.RAW_FISH
                        || type == Material.MUTTON
                        || type == Material.PORK
                        || type == Material.RABBIT
                        || type == Material.COOKED_BEEF
                        || type == Material.COOKED_CHICKEN
                        || type == Material.COOKED_MUTTON
                        || type == Material.GRILLED_PORK
                        || type == Material.COOKED_RABBIT)
                        && ((Wolf) entity).isTamed();
            case RABBIT:
                return type == Material.CARROT_ITEM || type == Material.GOLDEN_CARROT || type == Material.YELLOW_FLOWER;
            case LLAMA:
                return type == Material.HAY_BLOCK;
            case OCELOT:
                return (type == Material.RAW_FISH) && ((Ocelot) entity).isTamed();
        }
        return false;
    }
}
