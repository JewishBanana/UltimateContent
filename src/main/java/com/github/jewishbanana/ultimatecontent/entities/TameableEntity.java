package com.github.jewishbanana.ultimatecontent.entities;

import java.util.UUID;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderFollowEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderAllyHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerTargetedByEntity;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public interface TameableEntity {
	
	public Entity getEntity();
	
	public UUID getOwner();
	
	default void setOwner(TameableEntity tameable, Mob test) {
		Entity entity = getEntity();
		if (entity == null || !(entity instanceof Mob mob))
			return;
		setProtectiveGoals(tameable, mob);
		PathfindersHandler.addTamedMob(entity.getUniqueId(), tameable);
	}
	public static void setProtectiveGoals(TameableEntity tameable, Mob entity) {
		UUID owner = tameable.getOwner();
		if (owner == null)
			return;
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByEntity(entity, owner), 1);
		goals.put(new PathfinderOwnerHurtByEntity(entity, tameable), 1);
		goals.put(new PathfinderOwnerHurtEntity(entity, tameable), 2);
		goals.put(new PathfinderAllyHurtByEntity(entity, tameable), 3);
		goals.put(new PathfinderOwnerTargetedByEntity(entity, tameable), 4);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		if (entity instanceof Creature creature)
			goals.put(new PathfinderMeleeAttack(creature), 2);
		goals.put(new PathfinderFollowEntity(entity, owner, 2, 12), 3);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 4);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 5);
		goals.put(new PathfinderRandomLook(entity), 6);
	}
}
