package com.github.jewishbanana.ultimatecontent.items;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.deadlydisasters.utils.Utils;
import com.github.jewishbanana.ultimatecontent.listeners.BossBlocksHandler;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;

public class BossSpawnItem extends BaseItem {
	
	private static Map<BossSpawnItem, Block> placedBlocks = new HashMap<>();
	
	protected Entity[] entities;
	
	public BossSpawnItem(ItemStack item) {
		super(item);
	}
	public boolean placeBlock(BlockPlaceEvent event) {
		if (!(event.getBlockPlaced().getBlockData() instanceof Rotatable)) {
			event.getPlayer().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.placeBossItemError")));
			event.setCancelled(true);
			return false;
		}
		return true;
	}
	protected void addToBossBlocks(Block block) {
		placedBlocks.put(this, block);
		BossBlocksHandler.bossBlocks.add(block);
	}
	protected void removeBossBlock() {
		BossBlocksHandler.bossBlocks.remove(placedBlocks.remove(this));
	}
	public void unload(Block block) {
		if (block.getType() != Material.AIR) {
			block.setType(Material.AIR);
			block.getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), getItem());
		}
		if (this.entities != null)
			for (Entity entity : this.entities)
				if (entity != null)
					entity.remove();
	}
	public static void unloadBossBlocks() {
		placedBlocks.forEach((k, v) -> k.unload(v));
	}
}
