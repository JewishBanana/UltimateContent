package com.github.jewishbanana.ultimatecontent.entities.netherentities;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderFollowEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderAllyHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerTargetedByEntity;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class LostSoul extends ComplexEntity<Vex> implements TameableEntity {
	
	public static final String REGISTERED_KEY = "uc:lost_soul";

	public LostSoul(Vex entity) {
		super(entity, CustomEntityType.LOST_SOUL);
		
		setInvisible(entity);
		entity.setSilent(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(entityVariant.getLoadoutArmor(LoadoutEquipmentSlot.HEAD).getItem());
		}, new Vector(0, -.3, 0)));
		setHeadStand(0);
		
		scheduleTask(new BukkitRunnable() {
			private int attackCooldown;
			
			@Override
			public void run() {
				if (attackCooldown > 0) {
					attackCooldown--;
					return;
				}
				if (!entity.isValid())
					return;
				if (isTargetInRange(entity, 0f, 2.56f)) {
					attackCooldown = 15;
					entity.getTarget().damage(getEntityVariant().damage, entity);
				}
			}
		}.runTaskTimer(plugin, 0, 1));
		
		makeParticleTask(entity, 1, () -> {
			Location loc = entity.getLocation();
			entity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, .2, .2, .2, 0.05);
			entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc.getX(), loc.getY() + 0.4, loc.getZ(), 2, .025, .1, .025, 0.015);
		});
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0, .5, 0), 15, .3, .5, .3, .03);
	}
	public void setAttributes(Vex entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(20);
		entity.getAttribute(VersionUtils.getAttackDamageAttribute()).setBaseValue(0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(LostSoul.REGISTERED_KEY, LostSoul.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (event.getLocation().getBlock().getBiome() != Biome.SOUL_SAND_VALLEY)
				return false;
			return true;
		});
	}
	public void setProtectiveGoals(TameableEntity tameable, Mob entity) {
		UUID owner = tameable.getOwner();
		if (owner == null)
			return;
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByEntity(entity, owner), 0);
		goals.put(new PathfinderOwnerHurtByEntity(entity, tameable), 0);
		goals.put(new PathfinderOwnerHurtEntity(entity, tameable), 1);
		goals.put(new PathfinderAllyHurtByEntity(entity, tameable), 2);
		goals.put(new PathfinderOwnerTargetedByEntity(entity, tameable), 3);
		
		goals = brain.getGoalAI();
		goals.removeIf(wrapped -> wrapped.getPriority() != 4);
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderFollowEntity(entity, owner, 4, 12), 5);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 6);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 7);
		goals.put(new PathfinderRandomLook(entity), 8);
	}
}
