package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Bat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderPlagueBatSpreadTarget;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PlagueBat extends BaseEntity<Bat> {

	public static final String REGISTERED_KEY = "uc:plague_bat";
	private static final DustOptions BLACK_DUST = new DustOptions(Color.fromRGB(0, 0, 0), 0.5F);

	private PathfinderPlagueBatSpreadTarget spreadPathfinder;

	public PlagueBat(Bat entity) {
		super(entity, CustomEntityType.PLAGUE_BAT);
		entity.setSilent(true);
		entity.setAwake(true);
		entity.setGravity(false);
		entity.setRemoveWhenFarAway(true);
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				entity.setAwake(true);
				entity.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), entity.getLocation().add(0, 0.35, 0), 3, 0.2, 0.12, 0.2, 0.001, BLACK_DUST);
			}
		}.runTaskTimer(plugin, 0, 4));
	}
	@Override
	public void setAIGoals(Bat entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals = brain.getGoalAI();
		goals.clear();
		spreadPathfinder = new PathfinderPlagueBatSpreadTarget(entity, Math.max(1.0, entityVariant.damage), 48.0, 1.4, 120);
		goals.put(spreadPathfinder, 1);
	}
	@Override
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(VersionUtils.getNormalSmoke(), event.getEntity().getLocation().add(0, 0.3, 0), 14, 0.3, 0.2, 0.3, 0.001);
	}
	@Override
	public boolean shouldEquipBaseEntity() {
		return false;
	}
	public void setInitialTarget(LivingEntity target, boolean focusedTarget) {
		if (spreadPathfinder != null)
			spreadPathfinder.setInitialTarget(target, focusedTarget);
	}
	public boolean beginDeparture() {
		if (spreadPathfinder == null || getCastedEntity() == null || !getCastedEntity().isValid())
			return false;
		spreadPathfinder.beginDeparture();
		return true;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(REGISTERED_KEY, PlagueBat.class);
		type.setSpawnConditions(event -> false);
	}
}
