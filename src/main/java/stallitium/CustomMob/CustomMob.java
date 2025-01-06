package stallitium.CustomMob;

import stallitium.Utl.U;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CustomMob implements CommandExecutor, Listener {
    //config.ymlのCustomMob以下
    ConfigurationSection sc;
    //利用可能
    public static boolean cMob = false;
    //plugin
    JavaPlugin plugin;
    //クールダウン
    HashMap<String, Integer> cd = new HashMap<>();
    //ダメージを与えたプレイヤーのプール
    Map<Entity,Map<Player,Double>> damageTracker = new HashMap<>();

    public CustomMob(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        //前回終了時生存したmob削除
        deleteMobs();
        sc = config.getConfigurationSection("CustomMob");
        //mob一覧
        for (String str : sc.getKeys(false)) {
            Bukkit.getPluginManager().registerEvents(this,plugin);
            String[] lo = sc.getString(str+".spawn").split("/");
//            mobs.put(str,new Location(Bukkit.getWorld(lo[0]),Integer.parseInt(lo[1]),Integer.parseInt(lo[2]),Integer.parseInt(lo[3])));
            //カウントダウン可能に
            cd.put(str,sc.getInt(str+".cd"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("cmob")) {
            //現在op専用のみ
            if (!sender.isOp()) {
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("/cmob set <mob識別名称>");
                return true;
            }
            if (args[0].equals("set")) {
                if (args.length != 2) {
                    sender.sendMessage("引数の数が違います");
                    return true;
                }
                if (!sc.contains(args[1])) {
                    sender.sendMessage("その名称のmobは居ませんでした");
                    return true;
                }
                Player p = (Player) sender;
                Location spawnLoc = p.getLocation(); // モブのスポーン地点
                Location barrierLoc = spawnLoc.clone(); // バリアブロックの設置地点
                barrierLoc.setY(barrierLoc.getBlockY() - 1); // 1ブロック下

                // バリアブロックを設置
                barrierLoc.getBlock().setType(Material.BARRIER);
                plugin.getLogger().info("BARRIER block placed at: " + locToStr(barrierLoc));

                // スポーン座標を保存
                sc.set(args[1] + ".spawn", locToStr(spawnLoc));
                plugin.saveConfig();
                sender.sendMessage("mobのスポーン地点を " + locToStr(spawnLoc) + " に設定しました");
                cd.put(args[1], sc.getInt(args[1] + ".cd"));
                return true;
            }
        }
        return true;
    }

    //召喚
    void summon(String key) {
        //エンティティ召喚
        Location loc = strToLoc(sc.getString(key+".spawn"));
        Entity entity = Bukkit.getWorld(sc.getString(key+".location","world")).spawnEntity(loc, EntityType.valueOf(sc.getString(key+".def")));
        LivingEntity e = (LivingEntity) entity;
        e.setRemoveWhenFarAway(false);

        //名前セット
        entity.setCustomName(key);
        //タイプごとに設定しないといけないため現在ゾンビとスケルトンのみ
        if (entity.getType() == EntityType.ZOMBIE) {
            Zombie z = (Zombie) entity;
            z.setHealth(sc.getInt(key+".hp"));
            z.damage(sc.getInt(key+".at"));
        } else if (entity.getType() == EntityType.SKELETON) {
            Skeleton s = (Skeleton) entity;
            s.setHealth(sc.getInt(key+".hp"));
            s.damage(sc.getInt(key+".at"));
        }
    }

    //カウント時
    public void cd() {
        for (String key : cd.keySet()) {
            if (cd.get(key) <= 0) {
                cd.remove(key);
                Location spawnLocation = strToLoc(sc.getString(key + ".spawn"));
                Location underLocation = under1(spawnLocation);

                if (underLocation.getBlock().getType() != Material.BARRIER) {
                    plugin.getLogger().info("No BARRIER block at: " + underLocation);
                    return;
                }

                summon(key); // モブをスポーン
                return;
            }

            // カウントダウン処理
            cd.put(key, cd.get(key) - 1);
        }
    }

    //ロケーション変換
    Location strToLoc(String loc) {
        String[] str = loc.split("/");
        return new Location(Bukkit.getWorld(str[0]),Integer.parseInt(str[1]),Integer.parseInt(str[2]),Integer.parseInt(str[3]));
    }

    //上の逆
    String locToStr(Location loc) {
        return loc.getWorld().getName()+"/"+loc.getBlockX()+"/"+loc.getBlockY()+"/"+loc.getBlockZ();
    }

    //1ブロック下
    Location under1(Location loc) {
        return new Location(loc.getWorld(),loc.getX(),loc.getY()-1,loc.getZ());
    }

    @EventHandler
    public void death(EntityDeathEvent e) {
        if (e.getEntity().getCustomName() == null) {
            return;
        }
        try {
            if (sc.contains(e.getEntity().getCustomName())) {
                cd.put(e.getEntity().getCustomName(), sc.getInt(e.getEntity().getCustomName() + ".cd"));
                // プレイヤー関与
                if (damageTracker.containsKey(e.getEntity())) {
                    Map<Player, Double> playersDamage = damageTracker.get(e.getEntity());
                    // 分配処理
                    int amo = sc.getInt(e.getEntity().getCustomName()+".drop", 0);
                    ItemStack item = new ItemStack(Material.EMERALD);
                    item.setAmount(amo);
                    for (Player p : playersDamage.keySet()) {
                        U.addItem(p, item);
                    }

                    // mobのデスとともに削除
                    damageTracker.remove(e.getEntity());
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        // エンティティがカスタムネームを持つ場合
        if (e.getEntity().getCustomName() != null) {

            // 攻撃者がプレイヤーの場合
            Entity damager = e.getDamager();
            if (damager instanceof Player) {
                Player attacker = (Player) damager;

                // DamageTracker 処理
                Map<Player, Double> map = damageTracker.get(e.getEntity());
                if (map == null) {
                    map = new HashMap<>();
                }

                double previousDamage = map.getOrDefault(attacker, 0.0);
                map.put(attacker, previousDamage + e.getDamage());

                damageTracker.put(e.getEntity(), map);
            }
        }
    }

    //前回終了時生き残ったmobの削除
    void deleteMobs() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity && entity.getCustomName() != null) {
                        entity.remove();
                    }
                }
            }
        });
    }
}
