package main.java.me.ultimate.Q;

import java.util.HashMap;

import me.ThaH3lper.com.Api.BossDeathEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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
        HashMap<String, Long> timer = Quests.getTimer();
        if(timer.containsKey(p.getName()))
            timer.remove(p.getName());
    }

    @EventHandler
    public void onBossDeath(final BossDeathEvent event) {
        final Player p = event.getPlayer();
        if (Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".Type")
                .equalsIgnoreCase("EpicBoss")) {
            if (event.getBossName().equalsIgnoreCase(
                    Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".BossName"))) {
                Quests.finishQuest(p);
            }
        }
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
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            final Player p = event.getEntity().getKiller();
            if (!Quests.getConfig().getString(p.getName()).equalsIgnoreCase("none")
                    && Quests.getConfig().isSet(Quests.getConfig().getString(p.getName() + ".Quest") + ".Type")) {
                if (Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".Type")
                        .equalsIgnoreCase("mobkill")) {
                    if (Quests.getConfig().getString(Quests.getConfig().getString(p.getName() + ".Quest") + ".Mob")
                            .equalsIgnoreCase(event.getEntityType().getName().toString())) {
                        if (Quests.getConfig().isSet(p.getName() + ".Kills")) {
                            Quests.getConfig().set(p.getName() + ".Kills",
                                    Quests.getConfig().getInt(p.getName() + ".Kills") + 1);
                            Quests.saveConfig();
                        } else {
                            Quests.getConfig().addDefault(p.getName() + ".Kills", 1);
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

}
