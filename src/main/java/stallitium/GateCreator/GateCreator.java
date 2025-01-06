package stallitium.GateCreator;

import stallitium.Utl.U;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GateCreator implements CommandExecutor, Listener {
    JavaPlugin plugin;
    ConfigurationSection sc;
    public static ItemStack gateC;
    //ゲート一覧
    Map<Location,Location> gates = new HashMap<>();
    public GateCreator(JavaPlugin plugin, ConfigurationSection sc) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
        this.sc = sc;
        createGates();
        createG();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("gate")) {
            if (!sender.isOp()) {
                return true;
            }
            Player p = (Player) sender;
            U.addItem(p,gateC);
            sender.sendMessage("手順");
            sender.sendMessage("このアイテムを持って左クリックでテレポート先を指定");
            sender.sendMessage("このアイテムを持って右クリックでゲート設置");
            return true;
        }
        return true;
    }

    void createG() {
        gateC = new ItemStack(Material.BREEZE_ROD);
        ItemMeta meta = gateC.getItemMeta();
        meta.setDisplayName("GateCreator");
        gateC.setItemMeta(meta);
    }

    //左クリックで転送先設定右クリックでポータル設置
    @EventHandler
    public void onRClick(PlayerInteractEvent e) {
        if (!e.getPlayer().isOp()) {
            return;
        }
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getPlayer().getInventory().getItemInMainHand().hasItemMeta()) {
                ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                try {
                    if (meta.getDisplayName().equals("GateCreator")) {
                        if (meta.hasLore()) {
                            e.getPlayer().getInventory().setItemInMainHand(null);
                            Block block = e.getClickedBlock();
                            block.setType(Material.END_PORTAL);
                            gates.put(block.getLocation(),U.strToLoc(meta.getLore().get(0)));
                            sc.set(U.locToStr(block.getLocation()),meta.getLore().get(0));
                            plugin.saveConfig();
                            e.getPlayer().sendMessage("ポータルを作成しました");
                        }
                    }
                } catch (NullPointerException exception) {

                }
            }
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (e.getPlayer().getInventory().getItemInMainHand().hasItemMeta()) {
                try {
                    ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
                    if (meta.getDisplayName().equals("GateCreator")) {
                        meta.setLore(Arrays.asList(U.locToStr(e.getClickedBlock().getLocation())));
                        e.getPlayer().getInventory().getItemInMainHand().setItemMeta(meta);
                        e.setCancelled(true);
                        e.getPlayer().sendMessage("テレポート先を設定しました");
                    }
                } catch (NullPointerException exception) {

                }
            }
        }
    }

    //ポータル破壊時
    @EventHandler
    public void breakPortal(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.END_PORTAL) {
            try {
                sc.set(U.locToStr(e.getBlock().getLocation()),null);
                plugin.saveConfig();
                gates.remove(e.getBlock().getLocation());
                e.getPlayer().sendMessage("ポータルが削除されました");
            } catch (NullPointerException exception) {
                e.getPlayer().sendMessage("削除に失敗しました削除が必要な場合はファイルから削除してください");
            }
        }
    }

    void createGates() {
        for (String s : sc.getKeys(false)) {
            gates.put(U.strToLoc(s),U.strToLoc(sc.getString(s)));
        }
    }
}
