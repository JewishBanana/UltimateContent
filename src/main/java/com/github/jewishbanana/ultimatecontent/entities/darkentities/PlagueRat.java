package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderPlagueRatSpreadTarget;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PlagueRat extends BaseEntity<Silverfish> {

	public static final String REGISTERED_KEY = "uc:plague_rat";
	private static final DustOptions BLACK_DUST = new DustOptions(Color.fromRGB(0, 0, 0), 0.55F);
	private static final int NAVIGATION_TASK_INTERVAL_TICKS = 3;
	private static final int BURROW_TIMEOUT_TICKS = 200;
	private static final int BURROW_RETRY_TICKS = 30;

	private boolean burrowing;
	private List<BurrowSpot> burrowSpots;
	private int burrowSpotIndex;
	private Location burrowStandLocation;
	private Block burrowBlock;
	private Location lastBurrowLocation;
	private int burrowTicks;
	private int burrowNoProgressTicks;
	private int burrowPathRefreshTicks;
	private int burrowEnterTicks;
	private PathfinderPlagueRatSpreadTarget spreadPathfinder;

	public PlagueRat(Silverfish entity) {
		super(entity, CustomEntityType.PLAGUE_RAT);
		entity.setSilent(true);
		entity.setRemoveWhenFarAway(true);
		if (entity.getAttribute(VersionUtils.getMovementSpeedAttribute()) != null)
			entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(Math.min(entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).getBaseValue(), 0.28));

		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				entity.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), entity.getLocation().add(0, 0.25, 0), 4, 0.18, 0.1, 0.18, 0.001, BLACK_DUST);
				if (burrowing) {
					handleBurrow(entity);
					return;
				}
			}
		}.runTaskTimer(plugin, 0, NAVIGATION_TASK_INTERVAL_TICKS));
	}
	@Override
	public void setAIGoals(Silverfish entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();

		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 0);
		spreadPathfinder = new PathfinderPlagueRatSpreadTarget(entity, Math.max(1.0, entityVariant.damage), 48.0, 1.35, 120);
		goals.put(spreadPathfinder, 2);
		goals.put(new PathfinderRandomStrollLand(entity), 4);
		goals.put(new PathfinderLookAtEntity<>(entity, LivingEntity.class, 8f, 0.05f), 5);
	}
	@Override
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(VersionUtils.getNormalSmoke(), event.getEntity().getLocation().add(0, 0.25, 0), 18, 0.25, 0.18, 0.25, 0.001);
	}
	@Override
	public boolean shouldEquipBaseEntity() {
		return false;
	}
	public void setInitialTarget(LivingEntity target, boolean focusedTarget) {
		if (spreadPathfinder != null)
			spreadPathfinder.setInitialTarget(target, focusedTarget);
	}
	public boolean beginBurrowing() {
		Silverfish entity = getCastedEntity();
		if (entity == null || !entity.isValid() || entity.isDead())
			return false;
		if (burrowing)
			return true;
		burrowSpots = findBurrowSpots(entity.getLocation());
		if (burrowSpots.isEmpty()) {
			finishBurrow(entity);
			return true;
		}
		burrowing = true;
		burrowTicks = 0;
		burrowSpotIndex = -1;
		selectNextBurrowSpot(entity);
		entity.setTarget(null);
		EntityBrain brain = BukkitBrain.getBrain(entity);
		brain.getTargetAI().clear();
		brain.getGoalAI().clear();
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 0.35f, 0.55f);
		return true;
	}
	private void handleBurrow(Silverfish entity) {
		burrowTicks += NAVIGATION_TASK_INTERVAL_TICKS;
		if (burrowStandLocation == null || burrowBlock == null || burrowTicks > BURROW_TIMEOUT_TICKS) {
			finishBurrow(entity);
			return;
		}
		entity.setTarget(null);
		EntityController controller = BukkitBrain.getBrain(entity).getController();
		if (!isClimbable(burrowBlock) || !isDryStand(burrowStandLocation.getBlock())) {
			if (!selectNextBurrowSpot(entity))
				finishBurrow(entity);
			return;
		}
		Location wallCenter = burrowBlock.getLocation().add(0.5, 0.35, 0.5);
		controller.lookAt(wallCenter);
		if (burrowEnterTicks > 0) {
			controller.stop();
			Vector intoWall = wallCenter.toVector().subtract(entity.getLocation().toVector()).setY(0);
			if (intoWall.lengthSquared() > 0.001) {
				intoWall.normalize().multiply(0.2).setY(0.025);
				entity.setVelocity(intoWall);
				faceDirection(entity, intoWall);
			}
			burrowEnterTicks += NAVIGATION_TASK_INTERVAL_TICKS;
			if (burrowEnterTicks >= 12)
				finishBurrow(entity);
			return;
		}
		Location current = entity.getLocation();
		if (current.distanceSquared(burrowStandLocation) <= 0.7) {
			burrowEnterTicks = 1;
			return;
		}
		if (lastBurrowLocation != null) {
			double dx = current.getX() - lastBurrowLocation.getX();
			double dz = current.getZ() - lastBurrowLocation.getZ();
			if (dx * dx + dz * dz <= 0.1 * 0.1)
				burrowNoProgressTicks += NAVIGATION_TASK_INTERVAL_TICKS;
			else
				burrowNoProgressTicks = Math.max(0, burrowNoProgressTicks - NAVIGATION_TASK_INTERVAL_TICKS * 2);
		}
		lastBurrowLocation = current.clone();
		if (burrowNoProgressTicks >= BURROW_RETRY_TICKS) {
			if (!selectNextBurrowSpot(entity))
				finishBurrow(entity);
			return;
		}
		if ((burrowPathRefreshTicks -= NAVIGATION_TASK_INTERVAL_TICKS) <= 0) {
			controller.moveTo(burrowStandLocation, 1.1);
			burrowPathRefreshTicks = 12;
		}
	}
	private boolean selectNextBurrowSpot(Silverfish entity) {
		if (burrowSpots == null || ++burrowSpotIndex >= burrowSpots.size())
			return false;
		BurrowSpot spot = burrowSpots.get(burrowSpotIndex);
		burrowStandLocation = spot.standLocation;
		burrowBlock = spot.wallBlock;
		lastBurrowLocation = entity.getLocation().clone();
		burrowNoProgressTicks = 0;
		burrowPathRefreshTicks = 0;
		burrowEnterTicks = 0;
		BukkitBrain.getBrain(entity).getController().stop();
		return true;
	}
	private void finishBurrow(Silverfish entity) {
		if (entity == null || !entity.isValid())
			return;
		burrowing = false;
		Location loc = entity.getLocation().add(0, 0.25, 0);
		entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc, 18, 0.25, 0.15, 0.25, 0.001);
		entity.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 0.45f, 1.35f);
		entity.remove();
	}
	private List<BurrowSpot> findBurrowSpots(Location origin) {
		List<BurrowSpot> candidates = new ArrayList<>();
		Set<Block> candidateBlocks = new HashSet<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < 40; i++) {
			double angle = random.nextDouble(0.0, Math.PI * 2.0);
			double distance = random.nextDouble(2.0, 10.0);
			int x = origin.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
			int z = origin.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
			int baseY = origin.getBlockY();
			for (int yOffset = 0; yOffset <= 3; yOffset++) {
				int y = baseY + (yOffset % 2 == 0 ? yOffset / 2 : -(yOffset / 2 + 1));
				if (y <= origin.getWorld().getMinHeight() || y >= origin.getWorld().getMaxHeight() - 2)
					continue;
				Block feet = origin.getWorld().getBlockAt(x, y, z);
				Block wall = findWallBlock(feet);
				if (wall != null && candidateBlocks.add(feet))
					candidates.add(new BurrowSpot(feet.getLocation().add(0.5, 0.05, 0.5), wall));
			}
		}
		if (candidates.isEmpty()) {
			Block current = origin.getBlock();
			Block currentWall = findWallBlock(current);
			if (currentWall != null)
				candidates.add(new BurrowSpot(current.getLocation().add(0.5, 0.05, 0.5), currentWall));
		}
		candidates.sort(Comparator.comparingDouble(spot -> spot.standLocation.distanceSquared(origin)));
		return candidates.size() > 12 ? new ArrayList<>(candidates.subList(0, 12)) : candidates;
	}
	private Block findWallBlock(Block feet) {
		if (!isDryStand(feet))
			return null;
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		int start = ThreadLocalRandom.current().nextInt(faces.length);
		for (int i = 0; i < faces.length; i++) {
			Block wall = feet.getRelative(faces[(start + i) % faces.length]);
			if (isClimbable(wall))
				return wall;
		}
		return null;
	}
	private boolean isDryStand(Block feet) {
		Block head = feet.getRelative(BlockFace.UP);
		Block ground = feet.getRelative(BlockFace.DOWN);
		return feet.isPassable() && head.isPassable() && ground.getType().isSolid() && isDryBlock(feet) && isDryBlock(head) && isDryBlock(ground);
	}
	private boolean isDryBlock(Block block) {
		return !block.isLiquid() && !(block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged());
	}
	private void faceDirection(Silverfish entity, Vector direction) {
		Vector horizontal = direction.clone().setY(0);
		if (horizontal.lengthSquared() < 0.0001)
			return;
		float yaw = (float) Math.toDegrees(Math.atan2(-horizontal.getX(), horizontal.getZ()));
		entity.setRotation(yaw, 0.0f);
	}
	private boolean isClimbable(Block block) {
		return block.getType().isSolid() && !block.isLiquid();
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(REGISTERED_KEY, PlagueRat.class);
		type.setSpawnConditions(event -> false);
	}
	private static final class BurrowSpot {
		private final Location standLocation;
		private final Block wallBlock;

		private BurrowSpot(Location standLocation, Block wallBlock) {
			this.standLocation = standLocation;
			this.wallBlock = wallBlock;
		}
	}
}
