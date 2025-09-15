package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.github.jewishbanana.ultimatecontent.Main;

public class BossBlocksHandler implements Listener {
	
	public static Set<Block> bossBlocks = new HashSet<>();
	
	public BossBlocksHandler(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (bossBlocks.contains(event.getBlock()))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		Iterator<Block> it = event.blockList().iterator();
		while (it.hasNext())
			if (bossBlocks.contains(it.next()))
				it.remove();
	}
	@EventHandler(ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		Iterator<Block> it = event.blockList().iterator();
		while (it.hasNext())
			if (bossBlocks.contains(it.next()))
				it.remove();
	}
	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		for (Block b : event.getBlocks())
			if (bossBlocks.contains(b)) {
				event.setCancelled(true);
				return;
			}
	}
	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		for (Block b : event.getBlocks())
			if (bossBlocks.contains(b)) {
				event.setCancelled(true);
				return;
			}
	}
}
