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
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Silverfish;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;
import com.github.jewishbanana.ultimatecontent.events.PlagueCarrierBiteEvent;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;

public class PathfinderPlagueBatSpreadTarget extends CustomPathfinder {

	private static final int APPROACH_POINT_TICKS = 24;
	private static final int DIVE_TIMEOUT_TICKS = 32;
	private static final int RETREAT_TICKS = 50;

	private final Map<UUID, Integer> recentTargets = new HashMap<>();
	private final double attackDamage;
	private final double searchRadius;
	private final double searchRadiusSquared;
	private final double attackRangeSquared;
	private final int retargetCooldownTicks;

	private LivingEntity target;
	private UUID focusedTargetId;
	private FlightState state = FlightState.APPROACH;
	private Location flightGoal;
	private Location avoidanceGoal;
	private Location lastProgressLocation;
	private int stateTicks;
	private int attackCooldown;
	private int goalTicks;
	private int avoidanceTicks;
	private int progressTicks;
	private int stuckTicks;
	private boolean departing;

	public PathfinderPlagueBatSpreadTarget(@NotNull Mob entity, double attackDamage, double searchRadius, double attackRange, int retargetCooldownTicks) {
		super(entity);
		this.attackDamage = attackDamage;
		this.searchRadius = searchRadius;
		this.searchRadiusSquared = searchRadius * searchRadius;
		this.attackRangeSquared = attackRange * attackRange;
		this.retargetCooldownTicks = retargetCooldownTicks;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] {PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING};
	}
	public void setInitialTarget(LivingEntity target, boolean focusedTarget) {
		if (!isUsableTarget(target))
			return;
		this.focusedTargetId = focusedTarget ? target.getUniqueId() : null;
		this.target = focusedTarget ? target : findTarget();
		if (this.target == null)
			this.target = target;
		entity.setTarget(this.target);
		beginApproach();
	}
	public void beginDeparture() {
		departing = true;
		target = null;
		entity.setTarget(null);
		state = FlightState.DEPART;
		stateTicks = 45;
		flightGoal = findDepartureGoal();
	}
	@Override
	public boolean canStart() {
		tickRecentTargets();
		if (departing)
			return true;
		if (isUsableTarget(entity.getTarget()))
			target = entity.getTarget();
		if (!isUsableTarget(target))
			target = findTarget();
		return target != null;
	}
	@Override
	public void start() {
		if (!departing && target != null) {
			entity.setTarget(target);
			beginApproach();
		}
	}
	@Override
	public void tick() {
		tickRecentTargets();
		if (entity instanceof Bat bat)
			bat.setAwake(true);
		if (departing) {
			tickDeparture();
			return;
		}
		if (attackCooldown > 0)
			attackCooldown--;
		if (state == FlightState.RETREAT) {
			tickRetreat();
			return;
		}
		if (!isUsableTarget(target)) {
			target = findTarget();
			if (target == null)
				return;
			entity.setTarget(target);
			beginApproach();
		}
		updateProgress();
		if (state == FlightState.DIVE)
			tickDive();
		else
			tickApproach();
	}
	@Override
	public boolean canContinueToUse() {
		return departing || state == FlightState.RETREAT || isUsableTarget(target);
	}
	@Override
	public void stop() {
		if (departing || state == FlightState.RETREAT)
			return;
		entity.setTarget(null);
		target = null;
	}
	private void tickApproach() {
		if (flightGoal == null || --goalTicks <= 0 || entity.getLocation().distanceSquared(flightGoal) < 1.5 || stuckTicks >= 20) {
			flightGoal = findApproachGoal(target);
			goalTicks = APPROACH_POINT_TICKS;
			stuckTicks = 0;
		}
		if (flightGoal != null)
			steerTo(flightGoal, 0.38);
		else
			steerTo(target.getLocation().add(0, target.getHeight() + 2.0, 0), 0.34);
		double distance = entity.getLocation().distanceSquared(target.getLocation().add(0, target.getHeight() * 0.5, 0));
		if (attackCooldown <= 0 && distance <= 10.0 * 10.0 && entity.hasLineOfSight(target)) {
			state = FlightState.DIVE;
			stateTicks = DIVE_TIMEOUT_TICKS;
			avoidanceGoal = null;
		}
	}
	private void tickDive() {
		Location strike = target.getLocation().add(0, Math.max(0.35, target.getHeight() * 0.55), 0);
		steerTo(strike, 0.72);
		if (entity.getLocation().distanceSquared(strike) <= attackRangeSquared && entity.hasLineOfSight(target)) {
			Bukkit.getPluginManager().callEvent(new PlagueCarrierBiteEvent(entity, target));
			target.damage(attackDamage, entity);
			recentTargets.put(target.getUniqueId(), retargetCooldownTicks);
			entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 0.38f, 1.75f);
			beginRetreat(target.getLocation());
			return;
		}
		if (--stateTicks <= 0 || !entity.hasLineOfSight(target))
			beginApproach();
	}
	private void beginRetreat(Location threat) {
		state = FlightState.RETREAT;
		stateTicks = RETREAT_TICKS;
		attackCooldown = 55;
		entity.setTarget(null);
		flightGoal = findRetreatGoal(threat);
	}
	private void tickRetreat() {
		if (flightGoal == null || entity.getLocation().distanceSquared(flightGoal) < 2.0)
			flightGoal = findRetreatGoal(target == null ? entity.getLocation() : target.getLocation());
		if (flightGoal != null)
			steerTo(flightGoal, 0.58);
		if (--stateTicks > 0)
			return;
		target = findTarget();
		if (target != null)
			entity.setTarget(target);
		beginApproach();
	}
	private void tickDeparture() {
		if (flightGoal == null || entity.getLocation().distanceSquared(flightGoal) < 2.0)
			flightGoal = findDepartureGoal();
		if (flightGoal != null)
			steerTo(flightGoal, 0.55);
		if (--stateTicks > 0)
			return;
		Location loc = entity.getLocation().add(0, 0.3, 0);
		entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc, 14, 0.3, 0.2, 0.3, 0.001);
		entity.getWorld().playSound(loc, Sound.ENTITY_BAT_TAKEOFF, 0.4f, 0.7f);
		entity.remove();
	}
	private void beginApproach() {
		state = FlightState.APPROACH;
		stateTicks = 0;
		goalTicks = 0;
		avoidanceGoal = null;
		lastProgressLocation = entity.getLocation().clone();
		progressTicks = 10;
		stuckTicks = 0;
	}
	private LivingEntity findTarget() {
		LivingEntity focusedTarget = getFocusedTarget();
		if (focusedTarget != null)
			return focusedTarget;
		return entity.getNearbyEntities(searchRadius, searchRadius * 0.7, searchRadius).stream()
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
		if (test == null || test.equals(entity) || test.isDead() || !test.isValid() || test instanceof Bat || test instanceof Silverfish || EntityUtils.isEntityImmunePlayer(test))
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
	private Location findApproachGoal(LivingEntity target) {
		Location center = target.getLocation().add(0, target.getHeight() + 2.5, 0);
		return findOpenFlightPoint(center, 3.0, 6.0, 14);
	}
	private Location findRetreatGoal(Location threat) {
		Location origin = entity.getLocation();
		Vector away = origin.toVector().subtract(threat.toVector()).setY(0);
		if (away.lengthSquared() < 0.01) {
			double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
			away = new Vector(Math.cos(angle), 0, Math.sin(angle));
		}
		away.normalize();
		for (int angle : new int[] {0, 35, -35, 70, -70}) {
			Vector direction = rotate(away, Math.toRadians(angle));
			Location candidate = origin.clone().add(direction.multiply(7.0)).add(0, 3.5, 0);
			Location open = findNearbyOpen(candidate, 3);
			if (open != null)
				return open;
		}
		return findNearbyOpen(origin.clone().add(0, 5.0, 0), 4);
	}
	private Location findDepartureGoal() {
		Location origin = entity.getLocation();
		double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
		Location desired = origin.clone().add(Math.cos(angle) * 6.0, 8.0, Math.sin(angle) * 6.0);
		return findNearbyOpen(desired, 5);
	}
	private Location findOpenFlightPoint(Location center, double minRadius, double maxRadius, int attempts) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < attempts; i++) {
			double angle = random.nextDouble(Math.PI * 2.0);
			double radius = random.nextDouble(minRadius, maxRadius);
			Location candidate = center.clone().add(Math.cos(angle) * radius, random.nextDouble(-2.0, 2.5), Math.sin(angle) * radius);
			if (isFlightOpen(candidate))
				return candidate;
		}
		return findNearbyOpen(center, 4);
	}
	private Location findNearbyOpen(Location center, int range) {
		for (int y = 0; y <= range; y++) {
			for (int[] offset : new int[][] {{0, y, 0}, {y, 0, 0}, {-y, 0, 0}, {0, 0, y}, {0, 0, -y}, {0, -y, 0}}) {
				Location candidate = center.clone().add(offset[0], offset[1], offset[2]);
				if (isFlightOpen(candidate))
					return candidate;
			}
		}
		return null;
	}
	private void steerTo(Location goal, double speed) {
		Location origin = entity.getLocation();
		Location actualGoal = avoidanceTicks > 0 && avoidanceGoal != null ? avoidanceGoal : goal;
		if (avoidanceTicks > 0)
			avoidanceTicks--;
		Vector desired = actualGoal.toVector().subtract(origin.toVector());
		if (desired.lengthSquared() < 0.01)
			return;
		desired.normalize();
		if (!isFlightOpen(origin.clone().add(desired.clone().multiply(1.4)))) {
			avoidanceGoal = findAvoidanceGoal(origin, desired);
			avoidanceTicks = 8;
			if (avoidanceGoal != null)
				desired = avoidanceGoal.toVector().subtract(origin.toVector()).normalize();
		}
		Vector velocity = entity.getVelocity().multiply(0.48).add(desired.multiply(speed * 0.52));
		if (velocity.lengthSquared() > speed * speed)
			velocity.normalize().multiply(speed);
		entity.setVelocity(velocity);
		faceDirection(velocity);
	}
	private Location findAvoidanceGoal(Location origin, Vector desired) {
		Vector horizontal = desired.clone().setY(0);
		if (horizontal.lengthSquared() < 0.01)
			horizontal = new Vector(1, 0, 0);
		horizontal.normalize();
		Vector side = new Vector(-horizontal.getZ(), 0, horizontal.getX());
		for (Vector direction : new Vector[] {
				desired.clone().add(new Vector(0, 0.8, 0)),
				side.clone().add(new Vector(0, 0.25, 0)),
				side.clone().multiply(-1).add(new Vector(0, 0.25, 0)),
				horizontal.clone().add(new Vector(0, -0.55, 0)),
				new Vector(0, 1, 0)}) {
			if (direction.lengthSquared() < 0.01)
				continue;
			Location candidate = origin.clone().add(direction.normalize().multiply(3.0));
			if (isFlightOpen(candidate) && isFlightOpen(origin.clone().add(direction.clone().multiply(1.3))))
				return candidate;
		}
		return null;
	}
	private boolean isFlightOpen(Location location) {
		if (location.getY() <= location.getWorld().getMinHeight() + 1 || location.getY() >= location.getWorld().getMaxHeight() - 1)
			return false;
		Block block = location.getBlock();
		return block.isPassable() && !block.isLiquid() && !(block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged());
	}
	private void updateProgress() {
		if (--progressTicks > 0)
			return;
		progressTicks = 10;
		Location current = entity.getLocation();
		if (lastProgressLocation != null && lastProgressLocation.getWorld().equals(current.getWorld()) && current.distanceSquared(lastProgressLocation) < 0.12 * 0.12)
			stuckTicks += 10;
		else
			stuckTicks = Math.max(0, stuckTicks - 10);
		lastProgressLocation = current.clone();
		if (stuckTicks >= 40) {
			Location escape = findNearbyOpen(current.clone().add(0, 3.0, 0), 4);
			if (escape != null) {
				avoidanceGoal = escape;
				avoidanceTicks = 12;
			}
			stuckTicks = 0;
		}
	}
	private Vector rotate(Vector vector, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new Vector(vector.getX() * cos - vector.getZ() * sin, vector.getY(), vector.getX() * sin + vector.getZ() * cos);
	}
	private void faceDirection(Vector direction) {
		Vector horizontal = direction.clone().setY(0);
		if (horizontal.lengthSquared() < 0.0001)
			return;
		float yaw = (float) Math.toDegrees(Math.atan2(-horizontal.getX(), horizontal.getZ()));
		entity.setRotation(yaw, 0.0f);
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
	private enum FlightState {
		APPROACH,
		DIVE,
		RETREAT,
		DEPART
	}
}
