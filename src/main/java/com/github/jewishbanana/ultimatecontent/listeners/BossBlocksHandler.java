package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.items.BossSpawnItem;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;

public class BossBlocksHandler implements Listener {
	
	public static Map<Block, BossSpawnItem> bossBlocks = new HashMap<>();
	
	public BossBlocksHandler(UltimateContent plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent event) {
		BossSpawnItem item = bossBlocks.remove(event.getBlock());
		if (item != null)
			item.spawnBoss(BlockUtils.getCenterOfBlock(event.getBlock()));
	}
	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (bossBlocks.containsKey(event.getBlock()))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Iterator<Block> it = event.blockList().iterator();
		while (it.hasNext())
			if (bossBlocks.containsKey(it.next()))
				it.remove();
	}
	@EventHandler(ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		Iterator<Block> it = event.blockList().iterator();
		while (it.hasNext())
			if (bossBlocks.containsKey(it.next()))
				it.remove();
	}
	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks())
			if (bossBlocks.containsKey(b)) {
				event.setCancelled(true);
				return;
			}
	}
	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		for (Block b : event.getBlocks())
			if (bossBlocks.containsKey(b)) {
				event.setCancelled(true);
				return;
			}
	}
}
