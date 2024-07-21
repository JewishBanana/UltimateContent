package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import com.github.jewishbanana.ultimatecontent.Main;

public class RegionHandler implements Listener {
	
	public static Set<Block> fluidChangeBlocks = new HashSet<>();

	public RegionHandler(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void onBlockForm(EntityChangeBlockEvent event) {
		if (event.isCancelled() || !(event.getEntity() instanceof FallingBlock) || !event.getEntity().hasMetadata("ui-fb"))
			return;
		event.setCancelled(true);
	}
	@EventHandler
	public void onFlow(FluidLevelChangeEvent e) {
		if (fluidChangeBlocks.contains(e.getBlock()))
			e.setCancelled(true);
	}
}
