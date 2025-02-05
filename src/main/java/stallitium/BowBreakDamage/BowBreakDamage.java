package stallitium.BowBreakDamage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import stallitium.Utl.U;

public class BowBreakDamage implements Listener, CommandExecutor {
    public static boolean bowBreakDamage = false;
    public BowBreakDamage(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler
    public void damage(PlayerItemBreakEvent event) {
        if (bowBreakDamage) {
            ItemStack item = event.getBrokenItem();
            Player player = event.getPlayer();
            //弓であるか
            if (item.getType() == Material.BOW) {
                //1/2の確率で痛い
                if (U.random.nextBoolean()) {
                    player.sendMessage("痛いっ！弓の糸が当たった！");
                    player.damage(2);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("bbd")) {
            if (sender.isOp()) {
                Player p = (Player) sender;
                ItemStack item = new ItemStack(Material.BOW);
                ItemMeta meta = item.getItemMeta();
                //ダメージ値を設定
                Damageable damageable = (Damageable) meta;
                damageable.setDamage(Material.BOW.getMaxDurability()-1);
                //間違えたと思ったらなぜかこれでうまく動いた
                item.setItemMeta(meta);
                U.addItem(p,item);
            }
        }
        return true;
    }
}

