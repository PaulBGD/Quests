package main.java.me.ultimate.Q;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class QuestTrait extends Trait {

    Quests plugin;

    public QuestTrait() {
        super("quests");
        plugin = (Quests) Bukkit.getServer().getPluginManager().getPlugin("Quests");
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) throws Exception {
        plugin.traitCounter(event.getNPC().getName(), event.getClicker());
    }

}
