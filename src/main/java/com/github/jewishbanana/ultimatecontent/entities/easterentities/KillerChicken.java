package com.github.jewishbanana.ultimatecontent.entities.easterentities;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderAnimalAttackTarget;
import com.github.jewishbanana.ultimatecontent.specialevents.EasterEvent;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class KillerChicken extends BaseEntity<Chicken> {
	
	public static final String REGISTERED_KEY = "uc:killer_chicken";

	public KillerChicken(Chicken entity) {
		super(entity, CustomEntityType.KILLER_CHICKEN);
	}
	public void setAIGoals(Chicken entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, EntityType.RABBIT), 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class), 2);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderAnimalAttackTarget(entity, entityVariant.damage, 1.0, 10), 2);
		goals.put(new PathfinderRandomStrollLand(entity), 3);
		goals.put(new PathfinderLookAtEntity<>(entity, LivingEntity.class), 4);
	}
	public void wasHit(EntityDamageByEntityEvent event) {
		if (event.getDamager().hasMetadata("uc-eastermobs"))
			event.setCancelled(true);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(KillerChicken.REGISTERED_KEY, KillerChicken.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (!EasterEvent.isEventActive)
				return false;
			return event.getEntityType() == EntityType.CHICKEN;
		});
	}
}
