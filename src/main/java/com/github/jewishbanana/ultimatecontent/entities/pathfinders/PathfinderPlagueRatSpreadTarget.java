package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.events.PlagueCarrierBiteEvent;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderPlagueRatSpreadTarget extends CustomPathfinder {

	private static final int PATH_REFRESH_TICKS = 8;
	private static final int PROGRESS_CHECK_TICKS = 8;
	private static final int CLIMB_STUCK_TICKS = 16;
	private static final int DETOUR_STUCK_TICKS = 32;
	private static final double PROGRESS_DISTANCE_SQUARED = 0.12 * 0.12;

	private final Map<UUID, Integer> recentTargets = new HashMap<>();
	private final double attackDamage;
	private final double searchRadius;
	private final double searchRadiusSquared;
	private final double attackRangeSquared;
	private final double leapRangeSquared;
	private final int retargetCooldownTicks;
	private final int attackCooldownTicks;
	private final EntityController controller;

	private LivingEntity target;
	private Location fleeFrom;
	private Location fleeDestination;
	private Location recoveryWaypoint;
	private Location lastProgressLocation;
	private UUID navigationTarget;
	private UUID focusedTargetId;
	private int attackCooldown;
	private int fleeTicks;
	private int leapCooldown;
	private int leapTicks;
	private int pathRefreshTicks;
	private int progressCheckTicks;
	private int stuckTicks;
	private int recoveryTicks;
	private int climbTicks;
	private int mantleTicks;
	private BlockFace climbFace;
	private boolean leaping;

	public PathfinderPlagueRatSpreadTarget(@NotNull Mob entity, double attackDamage, double searchRadius, double attackRange, int retargetCooldownTicks) {
		super(entity);
		this.attackDamage = attackDamage;
		this.searchRadius = searchRadius;
		this.searchRadiusSquared = searchRadius * searchRadius;
		this.attackRangeSquared = attackRange * attackRange;
		this.leapRangeSquared = 3.2 * 3.2;
		this.retargetCooldownTicks = retargetCooldownTicks;
		this.attackCooldownTicks = 80;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	public void setInitialTarget(LivingEntity target, boolean focusedTarget) {
		if (!isUsableTarget(target))
			return;
		this.focusedTargetId = focusedTarget ? target.getUniqueId() : null;
		this.target = focusedTarget ? target : findTarget();
		if (this.target == null)
			this.target = target;
		entity.setTarget(this.target);
		resetNavigation(this.target);
	}
	@Override
	public boolean canStart() {
		tickRecentTargets();
		if (fleeTicks > 0)
			return true;
		if (attackCooldown > 0) {
			attackCooldown--;
			return false;
		}
		if (isUsableTarget(entity.getTarget())) {
			target = entity.getTarget();
			return true;
		}
		target = findTarget();
		return target != null;
	}
	@Override
	public void start() {
		if (fleeTicks > 0) {
			entity.setTarget(null);
			return;
		}
		if (target != null && !target.getUniqueId().equals(navigationTarget))
			resetNavigation(target);
		entity.setTarget(target);
		moveTowardTarget(true);
	}
	@Override
	public void tick() {
		tickRecentTargets();
		if (fleeTicks > 0) {
			fleeTicks--;
			fleeFromTarget();
			return;
		}
		if (attackCooldown > 0) {
			attackCooldown--;
			entity.setTarget(null);
			return;
		}
		if (!isUsableTarget(target)) {
			entity.setTarget(null);
			resetNavigation(null);
			return;
		}
		if (!target.getUniqueId().equals(navigationTarget))
			resetNavigation(target);
		updateProgress();
		double distanceSquared = target.getLocation().distanceSquared(entity.getLocation());
		if (leapCooldown > 0)
			leapCooldown--;
		if (leaping) {
			if (entity.isOnGround() || --leapTicks <= 0)
				leaping = false;
			else {
				faceDirection(entity.getVelocity());
				if (distanceSquared <= attackRangeSquared)
					biteTarget();
				return;
			}
		}
		if (handleClimb())
			return;
		if (tryLeapGapToTarget())
			return;
		if (handleRecoveryWaypoint())
			return;
		if (stuckTicks >= CLIMB_STUCK_TICKS && tryJumpObstacle())
			return;
		if (stuckTicks >= DETOUR_STUCK_TICKS && tryLocalDetour())
			return;
		moveTowardTarget(false);
		if (!leaping && leapCooldown <= 0 && distanceSquared <= leapRangeSquared && distanceSquared > attackRangeSquared * 0.45) {
			leapAtTarget();
			leaping = true;
			leapTicks = 18;
			leapCooldown = 14;
		} else if (distanceSquared > leapRangeSquared)
			leaping = false;
		if (distanceSquared > attackRangeSquared)
			return;
		biteTarget();
	}
	private void biteTarget() {
		if (!isUsableTarget(target))
			return;
		if (!entity.hasLineOfSight(target)) {
			pathRefreshTicks = 0;
			return;
		}
		Bukkit.getPluginManager().callEvent(new PlagueCarrierBiteEvent(entity, target));
		target.damage(attackDamage, entity);
		recentTargets.put(target.getUniqueId(), retargetCooldownTicks);
		entity.swingMainHand();
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 0.45f, 1.65f);
		entity.setTarget(null);
		fleeFrom = target.getLocation();
		fleeDestination = findFleeDestination(fleeFrom);
		fleeTicks = 45;
		attackCooldown = attackCooldownTicks;
		fleeFromTarget();
		target = null;
		leaping = false;
		resetNavigation(null);
	}
	@Override
	public boolean canContinueToUse() {
		return fleeTicks > 0 || target != null && isUsableTarget(target) && target.getLocation().distanceSquared(entity.getLocation()) <= searchRadiusSquared;
	}
	@Override
	public void stop() {
		if (entity.getTarget() != null && entity.getTarget().equals(target))
			entity.setTarget(null);
		target = null;
		if (fleeTicks <= 0)
			resetNavigation(null);
	}
	private LivingEntity findTarget() {
		LivingEntity focusedTarget = getFocusedTarget();
		if (focusedTarget != null)
			return focusedTarget;
		return entity.getNearbyEntities(searchRadius, searchRadius * 0.5, searchRadius).stream()
				.filter(e -> e instanceof LivingEntity)
				.map(e -> (LivingEntity) e)
				.filter(this::isUsableTarget)
				.min((first, second) -> Double.compare(first.getLocation().distanceSquared(entity.getLocation()), second.getLocation().distanceSquared(entity.getLocation())))
				.orElse(null);
	}
	private boolean isUsableTarget(LivingEntity test) {
		return isUsableTarget(test, false);
	}
	private boolean isUsableTarget(LivingEntity test, boolean ignoreRecentTarget) {
		if (test == null || test.equals(entity) || test.isDead() || !test.isValid() || EntityUtils.isEntityImmunePlayer(test) || test instanceof Silverfish || test instanceof Bat)
			return false;
		if (!test.getWorld().equals(entity.getWorld()) || !ignoreRecentTarget && recentTargets.containsKey(test.getUniqueId()))
			return false;
		return test.getLocation().distanceSquared(entity.getLocation()) <= searchRadiusSquared;
	}
	private LivingEntity getFocusedTarget() {
		if (focusedTargetId == null)
			return null;
		org.bukkit.entity.Entity focused = Bukkit.getEntity(focusedTargetId);
		return focused instanceof LivingEntity living && isUsableTarget(living, true) ? living : null;
	}
	private void moveTowardTarget(boolean force) {
		if (target == null)
			return;
		Location goal = target.getLocation();
		controller.lookAt(goal.clone().add(0, target.getEyeHeight(), 0));
		if (force || pathRefreshTicks-- <= 0) {
			controller.moveTo(goal, 1.0);
			pathRefreshTicks = PATH_REFRESH_TICKS;
		}
	}
	private void leapAtTarget() {
		if (target == null || !target.isValid())
			return;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation().add(0, Math.min(0.9, target.getHeight() * 0.45), 0);
		Vector leap = Utils.getVectorTowards(entityLoc, targetLoc).multiply(0.52);
		leap.setY(Math.max(0.18, Math.min(0.42, leap.getY() + 0.12)));
		controller.stop();
		entity.setVelocity(leap);
		faceDirection(leap);
	}
	private boolean tryLeapGapToTarget() {
		if (target == null || leapCooldown > 0)
			return false;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		double targetYDelta = targetLoc.getY() - entityLoc.getY();
		if (targetYDelta > 1.1)
			return false;
		double drop = entityLoc.getY() - targetLoc.getY();
		if (drop > 16.0)
			return false;
		Vector horizontal = targetLoc.toVector().subtract(entityLoc.toVector()).setY(0);
		double horizontalDistanceSquared = horizontal.lengthSquared();
		if (horizontalDistanceSquared < 2.25 || horizontalDistanceSquared > 14.0 * 14.0 || !isSupported(entityLoc))
			return false;
		horizontal.normalize();
		Location landing = findGapLanding(entityLoc, horizontal, Math.min(6.0, Math.sqrt(horizontalDistanceSquared) + 0.5));
		if (landing == null)
			return false;

		double landingDistance = horizontalDistance(entityLoc, landing);
		double horizontalSpeed = Math.min(0.72, 0.42 + landingDistance * 0.055);
		Vector leap = horizontal.multiply(horizontalSpeed);
		double landingDrop = entityLoc.getY() - landing.getY();
		leap.setY(landingDrop >= 1.25 ? 0.12 : 0.34);
		controller.stop();
		entity.setVelocity(leap);
		faceDirection(leap);
		entity.setFallDistance(0);
		leaping = true;
		leapTicks = Math.max(12, (int) Math.ceil(landingDistance * 4.0));
		leapCooldown = 26;
		stuckTicks = 0;
		return true;
	}
	private boolean isSupported(Location loc) {
		Block below = loc.getBlock().getRelative(BlockFace.DOWN);
		return below.getType().isSolid() && !below.isLiquid();
	}
	private Location findGapLanding(Location loc, Vector direction, double maxDistance) {
		boolean foundGap = false;
		for (double distance = 0.7; distance <= maxDistance; distance += 0.45) {
			Location sample = loc.clone().add(direction.getX() * distance, 0.0, direction.getZ() * distance);
			if (!isOpen(sample.getBlock()) || !isOpen(sample.getBlock().getRelative(BlockFace.UP)))
				return null;
			if (!isSupported(sample)) {
				foundGap = true;
				continue;
			}
			if (!foundGap && distance > 1.45)
				return null;
			if (foundGap && distance >= 1.45)
				return sample.getBlock().getLocation().add(0.5, 0.05, 0.5);
		}
		return null;
	}
	private void fleeFromTarget() {
		if (fleeFrom == null)
			return;
		Location loc = entity.getLocation();
		if (fleeDestination != null && loc.distanceSquared(fleeDestination) < 0.8)
			fleeDestination = findFleeDestination(fleeFrom);
		Vector away = (fleeDestination == null ? loc.toVector().subtract(fleeFrom.toVector()) : fleeDestination.toVector().subtract(loc.toVector())).setY(0);
		if (away.lengthSquared() < 0.001) {
			double angle = ThreadLocalRandom.current().nextDouble(0.0, Math.PI * 2.0);
			away = new Vector(Math.cos(angle), 0.0, Math.sin(angle));
		} else
			away.normalize();
		faceDirection(away);
		Location look = loc.clone().add(away);
		controller.lookAt(look);
		if (fleeDestination != null) {
			if (pathRefreshTicks-- <= 0) {
				controller.moveTo(fleeDestination, 1.25);
				pathRefreshTicks = 6;
			}
		} else {
			controller.stop();
			Vector velocity = away.multiply(0.36);
			velocity.setY(Math.min(entity.getVelocity().getY(), 0.02));
			entity.setVelocity(velocity);
		}
	}
	private Location findFleeDestination(Location threat) {
		Location loc = entity.getLocation();
		Vector away = loc.toVector().subtract(threat.toVector()).setY(0);
		if (away.lengthSquared() < 0.001)
			away = new Vector(1, 0, 0);
		away.normalize();
		Location best = null;
		double bestScore = Double.MAX_VALUE;
		for (int angle : new int[] {0, 35, -35, 70, -70}) {
			Vector direction = rotate(away, Math.toRadians(angle));
			for (double distance : new double[] {6.0, 4.5}) {
				Location stand = findStand(loc.clone().add(direction.clone().multiply(distance)), loc.getBlockY(), 2);
				if (stand == null)
					continue;
				double score = stand.distanceSquared(loc) - stand.distanceSquared(threat) * 0.35;
				if (score < bestScore) {
					best = stand;
					bestScore = score;
				}
			}
		}
		return best;
	}
	private void updateProgress() {
		if (--progressCheckTicks > 0)
			return;
		progressCheckTicks = PROGRESS_CHECK_TICKS;
		Location current = entity.getLocation();
		if (lastProgressLocation != null && lastProgressLocation.getWorld().equals(current.getWorld())) {
			double dx = current.getX() - lastProgressLocation.getX();
			double dz = current.getZ() - lastProgressLocation.getZ();
			if (dx * dx + dz * dz <= PROGRESS_DISTANCE_SQUARED)
				stuckTicks += PROGRESS_CHECK_TICKS;
			else
				stuckTicks = Math.max(0, stuckTicks - PROGRESS_CHECK_TICKS * 2);
		}
		lastProgressLocation = current.clone();
	}
	private boolean handleClimb() {
		if (target == null)
			return false;
		Location loc = entity.getLocation();
		if (mantleTicks > 0) {
			Vector forward = climbFace.getDirection().multiply(0.32);
			forward.setY(0.2);
			controller.stop();
			entity.setVelocity(forward);
			faceDirection(forward);
			entity.setFallDistance(0);
			mantleTicks--;
			if (mantleTicks == 0)
				resetClimb();
			return true;
		}
		if (climbFace == null) {
			if (stuckTicks < CLIMB_STUCK_TICKS || target.getLocation().getY() <= loc.getY() + 1.0)
				return false;
			climbFace = findClimbFace(target.getLocation());
			if (climbFace == null)
				return false;
			climbTicks = 0;
		}
		if (hasWall(climbFace)) {
			if (!isOpen(entity.getLocation().getBlock().getRelative(BlockFace.UP))) {
				resetClimb();
				return false;
			}
			Vector climb = climbFace.getDirection().multiply(0.11);
			climb.setY(0.27);
			controller.stop();
			entity.setVelocity(climb);
			faceDirection(climbFace.getDirection());
			entity.setFallDistance(0);
			climbTicks++;
			if (climbTicks > 120)
				resetClimb();
			return true;
		}
		if (climbTicks >= 3 && canMantle(climbFace)) {
			mantleTicks = 4;
			return true;
		}
		resetClimb();
		return false;
	}
	private BlockFace findClimbFace(Location goal) {
		Vector toward = goal.toVector().subtract(entity.getLocation().toVector()).setY(0);
		BlockFace preferred = null;
		if (toward.lengthSquared() > 0.01)
			preferred = Math.abs(toward.getX()) > Math.abs(toward.getZ()) ? toward.getX() > 0 ? BlockFace.EAST : BlockFace.WEST : toward.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
		if (preferred != null && hasWall(preferred))
			return preferred;
		for (BlockFace face : horizontalFaces())
			if (hasWall(face))
				return face;
		return null;
	}
	private boolean hasWall(BlockFace face) {
		Block feet = entity.getLocation().getBlock();
		return isSolid(feet.getRelative(face)) || isSolid(feet.getRelative(BlockFace.UP).getRelative(face));
	}
	private boolean canMantle(BlockFace face) {
		Block front = entity.getLocation().getBlock().getRelative(face);
		return isOpen(front) && isOpen(front.getRelative(BlockFace.UP)) && isSolid(front.getRelative(BlockFace.DOWN));
	}
	private boolean tryJumpObstacle() {
		if (target == null || leapCooldown > 0 || !entity.isOnGround())
			return false;
		Vector toward = target.getLocation().toVector().subtract(entity.getLocation().toVector()).setY(0);
		if (toward.lengthSquared() < 0.01)
			return false;
		toward.normalize();
		Location ahead = entity.getLocation().clone().add(toward.clone().multiply(0.75));
		Block feet = ahead.getBlock();
		if (!isSolid(feet) || !isOpen(feet.getRelative(BlockFace.UP)) || !isOpen(feet.getRelative(BlockFace.UP, 2)))
			return false;
		Vector jump = toward.multiply(0.3).setY(0.42);
		controller.stop();
		entity.setVelocity(jump);
		faceDirection(jump);
		leaping = true;
		leapTicks = 12;
		leapCooldown = 16;
		stuckTicks = 0;
		return true;
	}
	private boolean tryLocalDetour() {
		if (target == null)
			return false;
		Location loc = entity.getLocation();
		Vector direct = target.getLocation().toVector().subtract(loc.toVector()).setY(0);
		if (direct.lengthSquared() < 0.01)
			return false;
		direct.normalize();
		Location best = null;
		double bestScore = Double.MAX_VALUE;
		for (int angle : new int[] {45, -45, 80, -80, 120, -120}) {
			Vector direction = rotate(direct, Math.toRadians(angle));
			Location stand = findStand(loc.clone().add(direction.multiply(2.5)), loc.getBlockY(), 1);
			if (stand == null)
				continue;
			double score = stand.distanceSquared(target.getLocation());
			if (score < bestScore) {
				best = stand;
				bestScore = score;
			}
		}
		if (best == null)
			return false;
		recoveryWaypoint = best;
		recoveryTicks = 18;
		controller.moveTo(best, 1.15);
		stuckTicks = 0;
		return true;
	}
	private boolean handleRecoveryWaypoint() {
		if (recoveryWaypoint == null || recoveryTicks-- <= 0) {
			recoveryWaypoint = null;
			return false;
		}
		if (entity.getLocation().distanceSquared(recoveryWaypoint) <= 0.7) {
			recoveryWaypoint = null;
			pathRefreshTicks = 0;
			return false;
		}
		controller.lookAt(recoveryWaypoint);
		if (recoveryTicks % 6 == 0)
			controller.moveTo(recoveryWaypoint, 1.15);
		return true;
	}
	private Location findStand(Location approximate, int baseY, int range) {
		for (int offset = 0; offset <= range * 2; offset++) {
			int y = baseY + (offset % 2 == 0 ? offset / 2 : -(offset / 2 + 1));
			if (y <= approximate.getWorld().getMinHeight() || y >= approximate.getWorld().getMaxHeight() - 2)
				continue;
			Block feet = approximate.getWorld().getBlockAt(approximate.getBlockX(), y, approximate.getBlockZ());
			if (isOpen(feet) && isOpen(feet.getRelative(BlockFace.UP)) && isSolid(feet.getRelative(BlockFace.DOWN)))
				return feet.getLocation().add(0.5, 0.05, 0.5);
		}
		return null;
	}
	private boolean isOpen(Block block) {
		return block.isPassable() && !block.isLiquid() && !(block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged());
	}
	private boolean isSolid(Block block) {
		return block.getType().isSolid() && !block.isLiquid();
	}
	private double horizontalDistance(Location first, Location second) {
		double dx = first.getX() - second.getX();
		double dz = first.getZ() - second.getZ();
		return Math.sqrt(dx * dx + dz * dz);
	}
	private Vector rotate(Vector vector, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vector(vector.getX() * cos - vector.getZ() * sin, 0, vector.getX() * sin + vector.getZ() * cos);
	}
	private void faceDirection(Vector direction) {
		Vector horizontal = direction.clone().setY(0);
		if (horizontal.lengthSquared() < 0.0001)
			return;
		float yaw = (float) Math.toDegrees(Math.atan2(-horizontal.getX(), horizontal.getZ()));
		entity.setRotation(yaw, 0.0f);
	}
	private void resetNavigation(LivingEntity newTarget) {
		navigationTarget = newTarget == null ? null : newTarget.getUniqueId();
		lastProgressLocation = entity.getLocation().clone();
		progressCheckTicks = PROGRESS_CHECK_TICKS;
		pathRefreshTicks = 0;
		stuckTicks = 0;
		recoveryWaypoint = null;
		recoveryTicks = 0;
		resetClimb();
	}
	private void resetClimb() {
		climbFace = null;
		climbTicks = 0;
		mantleTicks = 0;
	}
	private BlockFace[] horizontalFaces() {
		return new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
	}
	private void tickRecentTargets() {
		Iterator<Map.Entry<UUID, Integer>> iterator = recentTargets.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Integer> entry = iterator.next();
			int ticks = entry.getValue() - 1;
			if (ticks <= 0)
				iterator.remove();
			else
				entry.setValue(ticks);
		}
	}
}
