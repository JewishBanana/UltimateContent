package com.github.jewishbanana.ultimatecontent.specialevents;

import java.util.random.RandomGenerator;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;

public class SpecialEvent implements Listener {
	
	protected static final RandomGenerator random;
	static {
		random = RandomGenerator.of("SplittableRandom");
	}

	protected Main plugin;
	protected boolean notifyMessages;
	protected String eventMessage;
	
	public SpecialEvent(Main plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	public static SpecialEvent checkForEvent(Main plugin) {
		if (!DataUtils.getConfigBoolean("general.special_events.enabled", false))
			return null;
		SpecialEvent event = EasterEvent.checkIsActive(plugin);
		if (event != null)
			return event;
		return null;
	}
	public void openGUI(Player player) {
	}
	public void reload() {
		notifyMessages = DataUtils.getConfigBoolean("general.special_events.send_event_messages", true);
	}
	public void saveData() {
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (notifyMessages)
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				e.getPlayer().sendMessage(eventMessage);
			}, 20);
	}
}
