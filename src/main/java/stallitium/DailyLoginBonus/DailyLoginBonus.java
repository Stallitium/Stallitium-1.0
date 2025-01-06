package stallitium.DailyLoginBonus;

import stallitium.Utl.U;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DailyLoginBonus implements CommandExecutor, Listener {
    JavaPlugin plugin;
    //db
    DbDailyLB db;
    //仕様可
    public static boolean dailyLB = false;
    //アイテム
    public static ItemStack dlbItem;
    //タイマ
    Timer timer = new Timer();
    public DailyLoginBonus(JavaPlugin plugin,DbDailyLB db) {
        this.plugin = plugin;
        this.db = db;
        createdlb();
        timerStart();
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("dlb")) {
            if (args.length == 0) {
                sender.sendMessage("/dlb gift");
                sender.sendMessage("/dlb reset");
                return true;
            }
            if (args[0].equals("gift")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    //少々変なコードになるものの面倒であることから流用
                    U.addItem(p,dlbItem);
                }
                return true;
            }
            if (args[0].equals("reset")) {
                db.reset();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    //少々変なコードになるものの面倒であることから流用
                    db.check(p.getName());
                    U.addItem(p,dlbItem);
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (dailyLB) {
            //チェックとともに追加
            if (!db.check(e.getPlayer().getName())) {
                U.addItem(e.getPlayer(),dlbItem);
            }
        }

    }

    void createdlb() {
        dlbItem = new ItemStack(Material.FLINT);
        ItemMeta meta = dlbItem.getItemMeta();
        meta.setDisplayName("§a§lデイリーログインボーナスアイテム");
        meta.setLore(Arrays.asList("今日もログインありがとう"));
        dlbItem.setItemMeta(meta);
    }

    void timerStart() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (dailyLB) {
                    //テーブル再生成
                    db.reset();
                    //ログイン中のプレイヤーに配る
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        //少々変なコードになるものの面倒であることから流用
                        db.check(p.getName());
                        U.addItem(p,dlbItem);
                    }
                }

            }
            //1時間おきに配布
        },0,1000*60*60);
    }
}
