package main.java.me.ultimate.Q;

import java.util.HashMap;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class QuestTrait extends Trait {

    Quests plugin = null;

    public HashMap<String, Long> cooldowns = new HashMap<String, Long>();

    public QuestTrait() {
        super("quests");
        plugin = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");
    }

    @Persist("mysettingname")
    boolean automaticallyPersistedSetting = false;

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
                plugin.traitCounter(event.getNPC().getName(), event.getClicker());
    }

}
