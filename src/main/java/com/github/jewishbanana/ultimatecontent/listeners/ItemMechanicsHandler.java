package com.github.jewishbanana.ultimatecontent.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.items.easter.BlueEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.GoldenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.GreenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.OrangeEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.PurpleEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.RedEgg;

public class ItemMechanicsHandler implements Listener {

	public ItemMechanicsHandler(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getItemInHand().getType() == Material.TURTLE_EGG) {
			GenericItem base = GenericItem.getItemBase(event.getItemInHand());
			if (base == null)
				return;
			Class<?> clazz = base.getClass();
			if (clazz.equals(GreenEgg.class)
					|| clazz.equals(BlueEgg.class)
					|| clazz.equals(RedEgg.class)
					|| clazz.equals(OrangeEgg.class)
					|| clazz.equals(PurpleEgg.class)
					|| clazz.equals(GoldenEgg.class))
				event.setCancelled(true);
		}
	}
}
