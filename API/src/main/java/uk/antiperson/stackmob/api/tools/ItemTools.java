package uk.antiperson.stackmob.api.tools;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemTools {

    public static void damageItemInHand(Player player, int stackSize) {
        ItemStack is = damageItem(player.getInventory().getItemInMainHand(), stackSize);
        player.getInventory().setItemInMainHand(is);
    }

    public static ItemStack damageItem(ItemStack item, int stackSize) {
        item.setDurability((short) (item.getDurability() + stackSize));
        return item;
    }

    public static boolean hasEnoughDurability(Player player, int stackSize) {
        if (!hasEnoughDurability(player.getInventory().getItemInMainHand(), stackSize)) {
            player.sendActionBar(ChatColor.RED + "Item is too damaged to shear all!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, 1);
            return false;
        }
        return true;
    }

    public static boolean hasEnoughDurability(ItemStack item, int stackSize) {
        int newDamage = item.getDurability() + stackSize;
        return newDamage <= item.getType().getMaxDurability();
    }
}
