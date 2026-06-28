package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Zombie;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderReturnToInfestedPod;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/**
 * Bridge API for DeadlyDisasters' "Warden's Heart" infested pod. All infested-mob AI manipulation for the pod lives here
 * (and in {@link PathfinderReturnToInfestedPod}) so DeadlyDisasters never has to touch MobChip/pathfinders directly &mdash;
 * it only calls these static methods. Every method is null-safe and a no-op on non-{@link Mob} entities.
 */
public final class InfestedPodBridge {

	private InfestedPodBridge() {}

	/**
	 * Configures a freshly-spawned pod mob's combat AI: it locks onto the pod's chosen target ({@code primaryTarget}), and
	 * once that target is dead or out of range it attacks any nearby living entity that isn't a player, isn't tamed by the
	 * owner (per {@link EntityUtils#isEntityOwner}, which covers UltimateContent's custom tameables too), and isn't itself
	 * infested or a Warden. DeadlyDisasters re-asserts the primary target each tick while it is alive and in range, so the
	 * goal installed here only takes over as the fallback once the primary is gone.
	 */
	public static void setActiveAI(Entity mobEntity, LivingEntity primaryTarget, Player owner) {
		if (!(mobEntity instanceof Mob mob))
			return;
		final UUID ownerId = owner == null ? null : owner.getUniqueId();
		EntityBrain brain = BukkitBrain.getBrain(mob);
		EntityAI targets = brain.getTargetAI();
		targets.clear();
		targets.put(new PathfinderNearestAttackableTarget<>(mob, LivingEntity.class, 10, true, false, e -> isValidPodTarget(e, ownerId)), 2);
		if (primaryTarget != null && !primaryTarget.isDead())
			mob.setTarget(primaryTarget);
	}
	/**
	 * Switches a pod mob into "return home" mode: clears its combat/target AI and installs a top-priority pathfinder that
	 * walks it back to {@code podCenter}, no longer stopping to fight.
	 */
	public static void setReturnAI(Entity mobEntity, Location podCenter, double insideRadius) {
		if (!(mobEntity instanceof Mob mob) || podCenter == null)
			return;
		mob.setTarget(null);
		// A returning mob has no need to break doors, and — critically — MobChip cannot wrap the vanilla break-door goal
		// (it throws "Break Time must be greater than 0"), which makes getGoalAI() below blow up and previously hung the
		// whole return phase. Dropping the door-breaking ability removes that goal before we read the goal AI.
		if (mob instanceof Zombie zombie)
			zombie.setCanBreakDoors(false);
		try {
			EntityBrain brain = BukkitBrain.getBrain(mob);
			brain.getTargetAI().clear();
			EntityAI goals = brain.getGoalAI();
			goals.removeIf(p -> p.getPathfinder().getName().equals("PathfinderReturnToInfestedPod"));
			goals.put(new PathfinderReturnToInfestedPod(mob, podCenter, insideRadius), 0);
		} catch (Throwable t) {
			// Last-resort guard: a single mob's AI quirk must never hang the pod's return phase.
		}
	}
	/** Valid fallback prey for a pod mob: a living non-player, non-infested, non-Warden entity not tamed by the owner. */
	private static boolean isValidPodTarget(LivingEntity e, UUID ownerId) {
		if (e == null || e.isDead() || e instanceof Player || e instanceof Warden || isInfested(e))
			return false;
		return ownerId == null || !EntityUtils.isEntityOwner(e, ownerId);
	}
	private static boolean isInfested(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		return custom instanceof BaseEntity<?> base && base.getEntityType().category == CustomEntityType.Category.INFESTED_ENTITIES;
	}
}
