package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderAvoidBrightAreas extends CustomPathfinder {
	
	private final byte maxBrightness;
	private final float searchDistance;
	private final double speedMod;
	private final float goalDistanceSquared = 2.25f;
	
	private Location goal;
	private int ticks;
	private EntityController controller;
	
	public PathfinderAvoidBrightAreas(@NotNull Mob m, byte maxBrightness, float searchDistance, double speedMod) {
		super(m);
		this.maxBrightness = maxBrightness;
		this.searchDistance = searchDistance;
		this.speedMod = speedMod;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	public PathfinderAvoidBrightAreas(@NotNull Mob m, byte maxBrightness, float searchDistance) {
		this(m, maxBrightness, searchDistance, 2.0);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT };
	}
	@Override
	public boolean canStart() {
		return entity.getLocation().getBlock().getLightLevel() >= maxBrightness;
	}
	@Override
	public void start() {
		Location entityLoc = entity.getLocation();
		byte lightLevel = entityLoc.getBlock().getLightLevel();
		List<Block> queue = BlockUtils.getBlocksInCircleRadius(entityLoc, searchDistance);
		Block tempGoal = null;
		byte tempGoalLight = Byte.MAX_VALUE;
		for (int i = 0; i < queue.size(); i++) {
			Block b = queue.get(i);
			Block temp = BlockUtils.getHighestExposedBlock(b, 3);
			if (temp == null)
				continue;
			temp = temp.getRelative(BlockFace.UP);
			byte tempLight = temp.getLightLevel();
			if (tempLight > maxBrightness && (tempGoal != null || tempLight >= lightLevel))
				continue;
			if (tempGoal == null || tempLight < tempGoalLight) {
				tempGoal = temp;
				tempGoalLight = tempLight;
			}
		}
		if (tempGoal != null) {
			goal = tempGoal.getLocation().add(.5, 0, .5);
			controller.moveTo(goal, speedMod);
			ticks = 30;
		}
	}
	@Override
	public void tick() {
		ticks--;
		if (goal != null)
			controller.moveTo(goal, speedMod);
	}
	@Override
	public boolean canContinueToUse() {
		if (goal == null || ticks == 0 || goal.getBlock().getLightLevel() >= maxBrightness)
			return false;
		if (Utils.isLocationsWithinDistance(entity.getLocation(), goal, goalDistanceSquared))
			return false;
		return true;
	}
}