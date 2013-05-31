package main.java.me.ultimate.Q;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuestsListener implements Listener {
    private final Quests Quests;

    public QuestsListener(final Quests plugin) {
        this.Quests = plugin;
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        if (Quests.getConfig().isSet(p.getName() + ".Quest")) {
            Quests.getConfig().addDefault(p.getName() + ".Quest", "none");
            Quests.getConfig().set(p.getName() + ".Quest", "none");
        }
        Quests.saveConfig();
        final HashMap<String, Long> timer = Quests.getTimer();
        if (timer.containsKey(p.getName()))
            timer.remove(p.getName());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player p = event.getPlayer();
        if (Quests.getConfig().isSet(p.getName() + ".Quest")) {
            if (!Quests.getConfig().getString(p.getName() + ".Quest").equalsIgnoreCase("none")) {
                final int oldx = event.getFrom().getBlockX();
                final int oldy = event.getFrom().getBlockY();
                final int oldz = event.getFrom().getBlockZ();
                final int x = event.getTo().getBlockX();
                final int y = event.getTo().getBlockY();
                final int z = event.getTo().getBlockZ();
                final String world = p.getWorld().getName();
                if (Quests.getConfig().isSet(Quests.getConfig().getString(p.getName() + ".Quest") + ".Location.z")) {
                    if (oldx != x || oldy != y || oldz != z) {
                        if (x == Quests.getConfig().getInt(
                                Quests.getConfig().getString(p.getName() + ".Quest") + ".Location.x")) {
                            if (y == Quests.getConfig().getInt(
                                    Quests.getConfig().getString(p.getName() + ".Quest") + ".Location.y")) {
                                if (z == Quests.getConfig().getInt(
                                        Quests.getConfig().getString(p.getName() + ".Quest") + ".Location.z")) {
                                    if (Quests
                                            .getConfig()
                                            .getString(
                                                    Quests.getConfig().getString(p.getName() + ".Quest")
                                                            + ".Location.World").equalsIgnoreCase(world)) {
                                        Quests.finishQuest(p);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Quests.getConfig().addDefault(p.getName() + ".Quest", "none");
            Quests.reloadConfig();
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            final Player p = event.getEntity().getKiller();
            if (!Quests.getConfig().getString(p.getName()).equalsIgnoreCase("none")
                    && Quests.getConfig().isSet(Quests.getConfig().getString(p.getName() + ".Quest") + ".Type")) {
                if (Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".Type")
                        .equalsIgnoreCase("mobkill")) {
                    if (Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".Mob")
                            .equalsIgnoreCase(event.getEntityType().getName())) {
                        if (Quests.getConfig().getInt(p.getName() + ".Kills") != 0) {
                            Quests.getConfig().set(p.getName() + ".Kills",
                                    Quests.getConfig().getInt(p.getName() + ".Kills") + 1);
                            Quests.saveConfig();
                        } else {
                            Quests.getConfig().set(p.getName() + ".Kills", 1);
                            Quests.saveConfig();
                        }
                        if (Quests.getConfig().getInt(p.getName() + ".Kills") >= Quests.getConfig().getInt(
                                Quests.getConfig().getString(p.getName() + ".Quest") + ".Required")) {
                            Quests.finishQuest(p);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPlayerLeave(final PlayerQuitEvent event) {
        if (Quests.Creators.containsKey(event.getPlayer().getName()))
            Quests.Creators.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final String msg = event.getMessage();
        final Player p = event.getPlayer();
        if (Quests.Creators.containsKey(p.getName())) {
            if (Quests.Creators.get(p.getName()) == "1") {
                if (Quests.wordCount(msg) == 1) {
                    Quests.QuestInfo.put(p.getName() + ".Name", msg);
                    p.sendMessage(cct("The name of the quest is: "
                            + Quests.QuestInfo.get(p.getName() + ".Name").replaceAll("_", " ")));
                    p.sendMessage(cct("Enter in the type of the quest. The types are: Location. Delivery. Mobkill. EpicBoss. More coming soon."));
                    Quests.Creators.put(p.getName(), "2");
                    event.setCancelled(true);
                } else {
                    p.sendMessage(cct("You have too many words! Remember, use underspaces for spaces."));
                    event.setCancelled(true);
                }
            } else if (Quests.Creators.get(p.getName()) == "2") {
                if (Quests.wordCount(msg) == 1) {
                    if (msg.equalsIgnoreCase("Location") || msg.equalsIgnoreCase("Delivery")
                            || msg.equalsIgnoreCase("Mobkill") || msg.equalsIgnoreCase("EpicBoss")) {
                        Quests.QuestInfo.put(p.getName() + ".Type", msg);
                        p.sendMessage(cct("You set the quest type to: " + Quests.QuestInfo.get(p.getName() + ".Type")));
                        Quests.Creators.put(p.getName(), "3");
                        event.setCancelled(true);
                        if (msg.equalsIgnoreCase("Location"))
                            p.sendMessage(cct("Go to the spot you want your final position to be set, and type Set in chat"));
                    } else {
                        p.sendMessage(cct("The quest type " + msg + " is not valid type!"));
                        event.setCancelled(true);
                    }
                } else {
                    p.sendMessage(cct("You have too many words!"));
                    p.sendMessage(cct("Enter in the type of the quest. The types are: Location. Delivery. Mobkill. EpicBoss. More coming soon."));
                    event.setCancelled(true);
                }
            } else if (Quests.Creators.get(p.getName()) == "3") {
                if (Quests.QuestInfo.get(p.getName() + ".Type").equalsIgnoreCase("Location")) {
                    if (msg.equalsIgnoreCase("set")) {
                        Quests.QuestInfo.put(p.getName() + ".World", p.getLocation().getWorld().getName());
                        Quests.QuestInfo.put(p.getName() + ".X", p.getLocation().getBlockX() + "");
                        Quests.QuestInfo.put(p.getName() + ".Y", p.getLocation().getBlockY() + "");
                        Quests.QuestInfo.put(p.getName() + ".Z", p.getLocation().getBlockZ() + "");
                        Quests.Creators.put(p.getName(), "Location1");
                        p.sendMessage(cct("You set the position to| World: "
                                + Quests.QuestInfo.get(p.getName() + ".World") + " X: "
                                + Quests.QuestInfo.get(p.getName() + ".X") + " Y: "
                                + Quests.QuestInfo.get(p.getName() + ".Y") + " Z: "
                                + Quests.QuestInfo.get(p.getName() + ".Z")));
                        event.setCancelled(true);
                        
                    } else {
                        p.sendMessage(cct("You did not type Set!"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    String cct(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', "&8[&bQuests&8]&7 " + msg);
    }
}
