package com.github.jewishbanana.ultimatecontent.specialevents;

import java.time.LocalDate;
import java.time.Month;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class ChristmasEvent {
	
	public static final Predicate<CreatureSpawnEvent> eventEntitySpawnCondition;
	static {
		LocalDate date = LocalDate.now();
		if (date.getMonth() == Month.DECEMBER) {
			eventEntitySpawnCondition = event -> {
				Location loc = event.getLocation();
				Block block = loc.getBlock();
				if (block == null || !block.isPassable())
					return false;
				block = block.getRelative(BlockFace.UP);
				if (block == null || !block.isPassable())
					return false;
				return Utils.isEnvironment(loc.getWorld(), Environment.NORMAL) && BlockUtils.isBlockColdBiome(loc.getBlock()) && SpawnUtils.canMonsterSpawn(loc);
			};
		} else
			eventEntitySpawnCondition = event -> false;
	}
}
