package stallitium;

import stallitium.CropsManager.CropsManager;
import stallitium.CustomMob.ActionTimer;
import stallitium.Bank.Bank;
import stallitium.CustomMob.CustomMob;
import stallitium.Bank.DbBank;
import stallitium.DailyLoginBonus.DailyLoginBonus;
import stallitium.DailyLoginBonus.DbDailyLB;
import stallitium.GateCreator.GateCreator;
import stallitium.WorldControl.WorldControl;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    //bankのDB
    DbBank bankDB;
    //customMobクラス
    CustomMob customMob;
    //config.yml
    FileConfiguration config;
    //デイリーログインボーナスdb
    DbDailyLB dbdlb;
    //ワールド
    WorldControl worldControl;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();


        //銀行
        bankDB = new DbBank(this);
        Bukkit.getPluginCommand("atm").setExecutor(new Bank(this,bankDB));
        //カスタムモブ
        customMob = new CustomMob(this,config);
        Bukkit.getPluginCommand("cmob").setExecutor(customMob);
        //デイリーログインボーナス
        dbdlb = new DbDailyLB(this);
        Bukkit.getPluginCommand("dlb").setExecutor(new DailyLoginBonus(this,dbdlb));
        //ゲート
        Bukkit.getPluginCommand("gate").setExecutor(new GateCreator(this,config.getConfigurationSection("GateCreator")));
        //ワールド
        worldControl = new WorldControl(this);
        Bukkit.getPluginCommand("worldc").setExecutor(worldControl);
        Bukkit.getPluginCommand("tp").setExecutor(worldControl);
        //作物
        new CropsManager(this);
        CropsManager.plantC = config.getBoolean("power.PlantCrops",false);



        //銀行利用可能
        Bank.bank = config.getBoolean("power.bank",false);
        //カスタムモブ利用可能
        CustomMob.cMob = config.getBoolean("power.customMob",false);
        //デイリーログインボーナス可
        DailyLoginBonus.dailyLB = config.getBoolean("power.dlb");

        //全てが終わったらタイマ起動再起動保持なし
        new ActionTimer(this,customMob).runTaskTimer(this,20,20);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("as")) {
            //利用可能かの一覧
            if (args.length == 0) {
                sender.sendMessage("bank: " + Bank.bank);
                sender.sendMessage("cmob: " + CustomMob.cMob);
                return true;
            }

            //以下op用
            if (!sender.isOp()) {
                return true;
            }
            if (args[0].equals("bank")) {
                if (args.length == 1) {
                    sender.sendMessage("bank: " + Bank.bank);
                    return true;
                }
                if (args.length == 2) {
                    if (args[1].equals("true")) {
                        Bank.bank = true;
                        config.set("power.bank",true);
                        saveConfig();
                        sender.sendMessage("bankを起動");
                        return true;
                    } else {
                        Bank.bank = false;
                        config.set("power.bank",false);
                        saveConfig();
                        sender.sendMessage("bankを停止");
                        return true;
                    }
                }
            }
            if (args[0].equals("cmob")) {
                if (args.length == 1) {
                    sender.sendMessage("cmob");
                    return true;
                }
                if (args.length == 2) {
                    if (args[1].equals("true")) {
                        CustomMob.cMob = true;
                        config.set("power.customMob",true);
                        saveConfig();
                        sender.sendMessage("cmobを起動");
                        return true;
                    } else {
                        CustomMob.cMob = false;
                        config.set("power.customMob",false);
                        saveConfig();
                        sender.sendMessage("cmobを停止");
                        return true;
                    }
                }
            }
            if (args[0].equals("dlb")) {
                if (args.length == 2) {
                    if (args[1].equals("true")) {
                        DailyLoginBonus.dailyLB = true;
                        config.set("power.dlb",true);
                        saveConfig();
                        sender.sendMessage("起動");
                        return true;
                    } else if (args[1].equals("false")) {
                        DailyLoginBonus.dailyLB = false;
                        config.set("poer.dlb",false);
                        saveConfig();
                        sender.sendMessage("停止");
                        return true;
                    }
                }
            }
        }
        return true;
    }
}
