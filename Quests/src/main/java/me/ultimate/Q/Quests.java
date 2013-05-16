package main.java.me.ultimate.Q;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Quests extends JavaPlugin implements Listener {

    private final ArrayList<String> Confirm = new ArrayList<String>();
    HashMap<String, Long> timer = new HashMap<String, Long>();

    public HashMap<String, Long> getTimer(){
        return this.timer;
    }
    @Override
    public void onEnable() {
        final File file = new File(getDataFolder() + File.separator + "config.yml");
        if (!file.exists()) {
            getLogger().warning("Config not found! Generating...");
        }
        if (getServer().getPluginManager().getPlugin("EpicBossRecoded") != null) {
            getLogger().info("Found EpicBoss! Hooking in..");
        }
        if (getServer().getPluginManager().getPlugin("Citizens") == null
                || getServer().getPluginManager().getPlugin("Citizens").isEnabled() == false) {
            getLogger().info("Citizens 2.0 not found or not enabled! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        CitizensAPI.getTraitFactory().registerTrait(
                net.citizensnpcs.api.trait.TraitInfo.create(QuestTrait.class).withName("quests"));
        getServer().getPluginManager().registerEvents(new QuestsListener(this), this);
        final FileConfiguration config = this.getConfig();
        config.options().copyDefaults(true);
        config.addDefault("NPCs.Notch.Quest", "Parkour");
        config.addDefault("NPCs.Notch.DeliveryReciever", false);
        saveConfig();
    }

    public ArrayList<String> getConfirmList() {
        return this.Confirm;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = ((Player) sender);
            if (cmd.getName().equalsIgnoreCase("quests")) {
                if (args.length > 0) {
                    if (args[0].equals("reload")) {
                        p.sendMessage(msg("Reloaded Config"));
                        reloadConfig();
                    } else if (args[0].equals("location")) {
                        final Location l = p.getLocation();
                        final int x = l.getBlockX();
                        final int y = l.getBlockY();
                        final int z = l.getBlockZ();
                        final String world = l.getWorld().getName();
                        getConfig().addDefault(args[1] + ".Location.x", x);
                        getConfig().addDefault(args[1] + ".Location.y", y);
                        getConfig().addDefault(args[1] + ".Location.z", z);
                        getConfig().addDefault(args[1] + ".Location.World", world);
                        getConfig().set(args[1] + ".Location.x", x);
                        getConfig().set(args[1] + ".Location.y", y);
                        getConfig().set(args[1] + ".Location.z", z);
                        getConfig().set(args[1] + ".Location.World", world);
                        saveConfig();
                    } else if (args[0].equalsIgnoreCase("reset")) {
                        final List<String> list = new ArrayList<>();
                        list.add("Completed Quests Below");
                        getConfig().set(p.getName() + ".Completed", list);
                    }
                }
            } else if (cmd.getName().equalsIgnoreCase("quest")) {
                if (args[0].equals("abort")) {
                    p.sendMessage(msg("You have aborted the quest "
                            + getConfig().getString(p.getName() + ".Quest").replaceAll("_", " ")));
                    setupPlayer(p);
                } else if (args[0].equals("info")) {
                    final String quest = getConfig().getString(p.getName() + ".Quest");
                    p.sendMessage(msg("Quest Info: "));
                    p.sendMessage(cct("Quest Name: " + quest.replaceAll("_", " ")));
                    if (getConfig().getString(quest + ".Type").equalsIgnoreCase("mobkill")) {
                        final int kills = getConfig().getInt(p.getName() + ".Kills");
                        p.sendMessage(cct("Kills: " + kills + "/" + getConfig().getString(quest + ".Required")));
                        p.sendMessage(cct("Mob: " + getConfig().getString(quest + ".Mob")));
                    }
                    final String s = getConfig().getString(quest + ".Reward");
                    final String[] split = s.split(":");
                    final int id = Integer.valueOf(split[0]);
                    p.sendMessage(cct("Reward: " + getConfig().getInt(quest + ".Amount")) + " "
                            + Material.getMaterial(id));
                    String yes;
                    if (getConfig().getBoolean(quest + ".Repeatable")) {
                        yes = "true";
                    } else {
                        yes = "false";
                    }
                    p.sendMessage(cct("Repeatable: " + yes));

                }
            }
        } else {
            if (cmd.getName().equalsIgnoreCase("quests")) {
                if (args.length > 0) {
                    if (args[0].equals("reload")) {
                        getLogger().info((msg("Reloaded Config")));
                        reloadConfig();
                    }
                }
            }
        }
        return true;
    }

    public static String cct(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&7" + msg);
    }

    public static String msg(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&8[&bQuests&8]&7 " + msg);
    }

    public void finishQuest(final Player p) {
        final String qn = getConfig().getString(p.getName() + ".Quest");
        final String s = getConfig().getString(qn + ".Reward");
        final String[] split = s.split(":");
        final int id = Integer.valueOf(split[0]);
        int data = 0;

        if (split.length == 2) {

            data = Integer.valueOf(split[1]);

        }
        final int a = getConfig().getInt(qn + ".Amount");
        final ItemStack item = new ItemStack(id, a, (short) data);
        p.getInventory().addItem(item);
        inv.update(p);
        p.sendMessage(msg("You completed the " + qn.replaceAll("_", " ") + " quest, for the reward of " + a + " "
                + Material.getMaterial(id).name() + "."));
        if (getConfig().isSet(p.getName() + ".Completed")) {
            final List<String> list = getConfig().getStringList(p.getName() + ".Completed");
            if (!list.contains(getConfig().getString(p.getName() + ".Quest"))) {
                list.add(getConfig().getString(p.getName() + ".Quest"));
                getConfig().set(p.getName() + ".Completed", list);
                getLogger().info(
                        "Added the " + getConfig().getString(p.getName() + ".Quest") + " to " + p.getName()
                                + "'s completion list.");
            }
        } else {
            final List<String> list = new ArrayList<>();
            list.add(getConfig().getString(p.getName() + ".Quest"));
            getConfig().addDefault(p.getName() + ".Completed", list);
            getLogger().info(
                    "Created " + p.getName() + "'s completion list, and added the "
                            + getConfig().getString(p.getName() + ".Quest") + " to it.");
        }
        setupPlayer(p);
    }

    public void setupPlayer(final Player p) {
        if (getConfig().isSet(p.getName() + ".Kills")) {
            getConfig().set(p.getName() + ".Kills", null);
        }
        getConfig().set(p.getName() + ".Quest", "none");
        saveConfig();
    }

    public void questStarter(final String npc, final Player p) {
        getConfig().addDefault(p.getName() + ".Quest", "none");
        final String name = getConfig().getString("NPCs." + npc + ".Quest");
        final List<String> list = new ArrayList<>();
        list.add("Completed Quests Below");
        getConfig().addDefault(p.getName() + ".Completed", list);
        saveConfig();
        if (getConfig().getString(p.getName() + ".Quest").equalsIgnoreCase("none")) {
            if (!getConfig().getBoolean("NPCs." + npc + ".DeliveryReciever")) {
                if (Confirm.contains(p.getName() + ":" + name)) {
                    if (getConfig().getList(p.getName() + ".Completed").contains(name)) {
                        if (!getConfig().getBoolean(name + ".Repeatable")) {
                            p.sendMessage(msg("This quest is not repeatable!"));
                        } else {
                            if (Confirm.contains(p.getName() + ":" + name)) {
                                p.sendMessage(cct(getConfig().getString(name + ".Accepted")));
                                Confirm.remove(p.getName() + ":" + name);
                                getConfig().set(p.getName() + ".Quest", name);
                                saveConfig();
                                if (getConfig().getString(name + ".Type").equalsIgnoreCase("Delivery")) {
                                    final String itemString = getConfig().getString(name + ".DeliveryItem");
                                    final String[] split = itemString.split(":");
                                    final int id = Integer.valueOf(split[0]);
                                    int data = 0;
                                    final int amount = getConfig().getInt(name + ".DeliveryAmount");
                                    if (split.length == 2) {
                                        data = Integer.valueOf(split[1]);
                                    }
                                    final ItemStack theItem = new ItemStack(Material.getMaterial(id), amount,
                                            (short) data);
                                    p.getInventory().addItem(theItem);
                                }
                            }
                        }
                    } else {
                        if (Confirm.contains(p.getName() + ":" + name)) {
                            p.sendMessage(cct(getConfig().getString(name + ".Accepted")));
                            Confirm.remove(p.getName() + ":" + name);
                            getConfig().set(p.getName() + ".Quest", name);
                            saveConfig();
                        }
                    }
                } else {
                    p.sendMessage(cct(getConfig().getString(name + ".Confirm")));
                    Confirm.add(p.getName() + ":" + name);
                }
            }
        } else if (getConfig().getBoolean("NPCs." + npc + ".DeliveryReciever")) {
            if (getConfig().getString(name + ".Type").equalsIgnoreCase("Delivery")) {
                final String itemString = getConfig().getString(name + ".DeliveryItem");
                final String[] split = itemString.split(":");
                final int id = Integer.valueOf(split[0]);
                int data = 0;
                final int amount = getConfig().getInt(name + ".DeliveryAmount");
                if (split.length == 2) {
                    data = Integer.valueOf(split[1]);
                }
                if (p.getInventory().contains(new ItemStack(Material.getMaterial(id), amount, (short) data))) {
                    p.getInventory().remove(new ItemStack(Material.getMaterial(id), amount, (short) data));
                    inv.update(p);
                    finishQuest(p);
                }
            }
        } else {
            p.sendMessage(msg("You are already doing a quest!"));
        }

    }

    public void traitCounter(final String name, final Player p) {
        if(!timer.containsKey(p.getName())){
            Long eventoccured = new Date().getTime();
            timer.put(p.getName(), eventoccured);
        } else {
            Long lapse = new Date().getTime() - timer.get(p.getName());
            if( lapse >= 500 )
            {
                Long eventoccured = new Date().getTime();
                timer.put(p.getName(), eventoccured);
                questStarter(name, p);
            }
        }
    }

}