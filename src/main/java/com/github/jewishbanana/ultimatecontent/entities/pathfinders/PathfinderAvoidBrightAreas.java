package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderAvoidBrightAreas extends CustomPathfinder {
	
	private byte maxBrightness;
	private double searchDistance;
	private double speedMod;
	private Location goal;
	private int ticks;

	public PathfinderAvoidBrightAreas(@NotNull Mob m, byte maxBrightness, double searchDistance, double speedMod) {
		super(m);
		this.maxBrightness = maxBrightness;
		this.searchDistance = searchDistance;
		this.speedMod = speedMod;
	}
	public PathfinderAvoidBrightAreas(@NotNull Mob m, byte maxBrightness, double searchDistance) {
		this(m, maxBrightness, searchDistance, 2.0);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT };
	}
	@Override
	public boolean canStart() {
		if (entity.getLocation().getBlock().getLightLevel() >= maxBrightness)
			return true;
		return false;
	}
	@Override
	public void start() {
		byte lightLevel = entity.getLocation().getBlock().getLightLevel();
		Queue<Block> queue = Utils.getBlocksInCircleRadius(entity.getLocation(), searchDistance);
		Block tempGoal = null;
		for (Block b : queue) {
			Block temp = Utils.getHighestExposedBlock(b, 3);
			if (temp == null)
				continue;
			temp = temp.getRelative(BlockFace.UP);
			if (temp.getLightLevel() > maxBrightness && (tempGoal != null || temp.getLightLevel() >= lightLevel))
				continue;
			if (tempGoal == null || temp.getLightLevel() < tempGoal.getLightLevel())
				tempGoal = temp;
		}
		if (tempGoal != null) {
			goal = tempGoal.getLocation().add(.5, 0, .5);
			BukkitBrain.getBrain(entity).getController().moveTo(goal, speedMod);
			ticks = 30;
		}
	}
	@Override
	public void tick() {
		ticks--;
		if (goal != null)
			BukkitBrain.getBrain(entity).getController().moveTo(goal, speedMod);
	}
	@Override
	public boolean canContinueToUse() {
		if (goal == null || ticks == 0 || goal.getBlock().getLightLevel() >= maxBrightness)
			return false;
		if (Utils.isLocationsWithinDistance(entity.getLocation(), goal, 2.25))
			return false;
		return true;
	}
}
