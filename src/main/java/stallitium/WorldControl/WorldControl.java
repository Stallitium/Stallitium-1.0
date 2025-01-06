package stallitium.WorldControl;

import stallitium.Utl.U;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldControl implements CommandExecutor, Listener {
    ItemStack environment,worldType,generateStructures;
    ItemStack eNormal,eNether,eEnd,wtNormal,wtFlat,wtLargeBiomes,wtAmp;
    ItemStack create;
    JavaPlugin plugin;
    String guiName = "worldcreate";
    public WorldControl(JavaPlugin plugin) {
        createSettingItems();
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("worldc")) {
            if (!sender.isOp()) {
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("/worldc c <name> : ワールド作成GUIが出ます");
                sender.sendMessage("作りが雑なため簡単にエラーが出ます");
                sender.sendMessage("↓クリックでテレポートコマンドが入力されます");
                for (World wor : Bukkit.getWorlds()) {
                    TextComponent w = new net.md_5.bungee.api.chat.TextComponent(wor.getName());
                    w.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/tp "+wor.getName()));
                    sender.spigot().sendMessage(w);
                }
                return true;
            }
            if (args[0].equals("c")) {
                if (args.length < 2) {
                    sender.sendMessage("引数が違います");
                    return true;
                }
                if (Bukkit.getWorld(args[1]) != null) {
                    sender.sendMessage(args[1]+"は既に存在しています");
                    return true;
                }
                Inventory inv = Bukkit.createInventory(null,9*6,guiName);
                inv.setItem(9,environment);
                inv.setItem(10,worldType);
                inv.setItem(11,generateStructures);
                if (args.length == 3) {
                    try {
                        Long s = Long.valueOf(args[2]);
                        inv.setItem(12,U.createItem(Material.WHEAT_SEEDS, String.valueOf(s),Arrays.asList()));
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Long型の数字でお願いします");
                        return true;
                    }

                }
                inv.setItem(18,eNormal);
                inv.setItem(19,wtNormal);
                inv.setItem(27,eNether);
                inv.setItem(28,wtFlat);
                inv.setItem(36,eEnd);
                inv.setItem(37,wtLargeBiomes);
                inv.setItem(46,wtAmp);
                inv.setItem(13,U.createItem(Material.NAME_TAG,args[1], Arrays.asList()));
                inv.setItem(53,create);
                Player player = (Player) sender;
                player.openInventory(inv);
                return true;
            }
        }
        if (command.getName().equals("tp")) {
            if (!sender.isOp()) {
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("引数不足です");
                return true;
            }
            Player p = (Player) sender;
            p.teleport(Bukkit.getWorld(args[0]).getSpawnLocation());
            return true;
        }
        return true;
    }

    void createSettingItems() {
        List<String> lore = new ArrayList<>();
        environment = U.createItem(Material.ENDER_EYE,"環境",lore);
        worldType = U.createItem(Material.GRASS_BLOCK,"タイプ",lore);
        generateStructures = U.createItem(Material.OAK_DOOR,"構造物",lore);
        eNormal = U.createItem(Material.GRASS_BLOCK,"ノーマル",lore);
        eNether = U.createItem(Material.NETHERRACK,"ネザー",lore);
        eEnd = U.createItem(Material.END_STONE,"エンド",lore);
        wtNormal = U.createItem(Material.OAK_STAIRS,"ノーマル",lore);
        wtFlat = U.createItem(Material.OAK_SLAB,"フラット",lore);
        wtLargeBiomes = U.createItem(Material.OAK_PLANKS,"ラージバイオーム",lore);
        wtAmp = U.createItem(Material.STONE_STAIRS,"AMPLIFIED",Arrays.asList("読み方わからん"));
        create = U.createItem(Material.END_CRYSTAL,"作成",lore);
    }

    @EventHandler
    public void clickEvent(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(guiName)) {
            try {
                if (e.getCurrentItem().getType() == Material.END_CRYSTAL) {
                    World.Environment env = World.Environment.NORMAL;
                    WorldType wtype = WorldType.NORMAL;
                    boolean structure = false;
                    if (e.getInventory().getItem(0).getType() == Material.GRASS_BLOCK) {
                        env = World.Environment.NORMAL;
                    } else if (e.getInventory().getItem(0).getType() == Material.NETHERRACK) {
                        env = World.Environment.NETHER;
                    } else if (e.getInventory().getItem(0).getType() == Material.END_STONE) {
                        env = World.Environment.THE_END;
                    }
                    if (e.getInventory().getItem(1).getType() == Material.OAK_STAIRS) {
                        wtype = WorldType.NORMAL;
                    } else if (e.getInventory().getItem(1).getType() == Material.OAK_SLAB) {
                        wtype = WorldType.FLAT;
                    } else if (e.getInventory().getItem(1).getType() == Material.OAK_PLANKS) {
                        wtype = WorldType.LARGE_BIOMES;
                    } else if (e.getInventory().getItem(1).getType() == Material.STONE_STAIRS) {
                        wtype = WorldType.AMPLIFIED;
                    }
                    if (e.getInventory().getItem(2) != null) {
                        structure = true;
                    }

                    WorldCreator creator = new WorldCreator(e.getInventory().getItem(13).getItemMeta().getDisplayName());
                    creator.environment(env);
                    creator.type(wtype);
                    creator.generateStructures(structure);
                    if (e.getInventory().getItem(12) != null) {
                        creator.seed(Long.parseLong(e.getInventory().getItem(12).getItemMeta().getDisplayName()));
                    }
                    creator.createWorld();
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage("ワールドを作成しました");
                }
            } catch (Exception exception) {
                e.getWhoClicked().sendMessage("何かが違うか何もないところをクリックしましたね");
                exception.printStackTrace();
            }
        }
    }
}
