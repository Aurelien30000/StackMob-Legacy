package uk.antiperson.stackmob.entity.drops;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import uk.antiperson.stackmob.StackMob;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to calculate the correct amount of entity drops.
 */
public class DropTools {

    private StackMob sm;
    public DropTools(StackMob sm){
        this.sm = sm;
    }

    public void calculateDrops(List<ItemStack> drops, int deadAmount, LivingEntity dead, ItemStack itemInHand){
        for(ItemStack itemStack : drops){
            if(itemStack == null){
                continue;
            }
            if(dropIsArmor(dead, itemStack)){
                continue;
            }
            if(sm.getCustomConfig().getStringList("multiply-drops.drops-blacklist")
                    .contains(itemStack.toString())){
                continue;
            }
            if(deadAmount > sm.getCustomConfig().getInt("multiply-drops.entity-limit")){
                deadAmount = sm.getCustomConfig().getInt("multiply-drops.entity-limit");
            }

            int itemAmount = calculateAmount(deadAmount, itemStack, itemInHand);
            dropDrops(itemStack, itemAmount, dead.getLocation());
        }
    }

    private int calculateAmount(int deadAmount, ItemStack current, ItemStack hand){
        if(sm.getCustomConfig().getStringList("multiply-drops.drop-one-per")
                .contains(current.getType().toString())){
            return deadAmount;
        }
        if(hand != null && hand.getEnchantments().containsKey(Enchantment.LOOT_BONUS_MOBS)) {
            double enchantmentTimes = 1 + hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) * 0.5;
            return (int) Math.round(calculateAmount(deadAmount) * enchantmentTimes);
        }
        return calculateAmount(deadAmount);
    }

    // Calculate a random drop amount.
    public int calculateAmount(int multiplier){
        return (int) Math.round((0.75 + ThreadLocalRandom.current().nextDouble(2)) * multiplier);
    }

    public void dropDrops(ItemStack drop, int amount, Location dropLocation){
        dropAllDrops(drop, amount, dropLocation, false);
    }

    public void dropEggs(ItemStack drop, int amount, Location dropLocation){
        dropAllDrops(drop, amount, dropLocation, true);
    }

    // Method to drop the correct amount of drops.
    private void dropAllDrops(ItemStack drop, int amount, Location dropLocation, boolean addEnchantment){
        double inStacks = (double) amount / (double) drop.getMaxStackSize();
        double floor = Math.floor(inStacks);
        double leftOver = inStacks - floor;
        for(int i = 1; i <= floor; i++){
            ItemStack newStack = drop.clone();
            newStack.setAmount(drop.getMaxStackSize());
            if(addEnchantment){
                newStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            }
            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
        if(leftOver > 0){
            ItemStack newStack = drop.clone();
            newStack.setAmount((int) Math.round(leftOver * drop.getMaxStackSize()));
            if(addEnchantment){
                newStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            }
            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
    }

    private boolean dropIsArmor(LivingEntity entity, ItemStack drop){
        if(entity.getEquipment().getItemInMainHand().equals(drop) || entity.getEquipment().getItemInOffHand().equals(drop)){
            return true;
        }
        for(ItemStack itemStack : entity.getEquipment().getArmorContents()){
            if(itemStack.equals(drop)){
                return true;
            }
        }
        return false;
    }


}
