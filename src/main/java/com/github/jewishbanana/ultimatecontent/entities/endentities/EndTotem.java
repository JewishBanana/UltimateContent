package com.github.jewishbanana.ultimatecontent.entities.endentities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.PhysicsEngine;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class EndTotem extends ComplexEntity<Enderman> {
	
	public static final String REGISTERED_KEY = "uc:end_totem";
	
	private boolean hitAnimation;
	private int hitAnimationFrame;
	private double knockbackMultiplier;
	private Vector[] offsets;
	
	private static final int hitAnimationFrames = 30;

	public EndTotem(Enderman entity) {
		super(entity, CustomEntityType.END_TOTEM, false);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setCanPickupItems(false);
		
		headStand = new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(new ItemStack(Material.END_PORTAL_FRAME));
		}, new Vector(0,1,0));
		createStands(entity.getLocation(), headStand, new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.CHORUS_PLANT));
			stand.getEquipment().setItemInOffHand(new ItemStack(Material.CHORUS_FLOWER));
			stand.setRightArmPose(new EulerAngle(0,0.3,1.1));
			stand.setLeftArmPose(new EulerAngle(0,0.5,-1.8));
		}, new Vector(0,1,0)), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(new ItemStack(Material.PURPUR_PILLAR));
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.CHORUS_PLANT));
			stand.getEquipment().setItemInOffHand(new ItemStack(Material.CHORUS_FLOWER));
			stand.setRightArmPose(new EulerAngle(0,0.3,1.1));
			stand.setLeftArmPose(new EulerAngle(0,0.5,-1.8));
		}, new Vector(0,.2,0), 90f), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(new ItemStack(Material.PURPUR_PILLAR));
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.CHORUS_PLANT));
			stand.getEquipment().setItemInOffHand(new ItemStack(Material.CHORUS_FLOWER));
			stand.setRightArmPose(new EulerAngle(0,0.3,1.1));
			stand.setLeftArmPose(new EulerAngle(0.5,0.5,-1.8));
		}, new Vector(0,-.55,0)), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(new ItemStack(Material.OBSIDIAN));
		}, new Vector(0,-1.3,0)));
		
		offsets = new Vector[] { new Vector(), new Vector(), new Vector(), new Vector(), new Vector() };
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location loc = entity.getLocation();
				loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0,1,0), 7, .5, .75, .5, 0.01);
				headStand.getEntity(loc).setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
				if (hitAnimation) {
					loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0,1,0), 7, .2, .2, .2, 0.2);
					CreatureStand<?> headStand = getCreatureStand(0);
					headStand.getEntity(loc).teleport(loc.add(headStand.getOffset()));
					loc.subtract(headStand.getOffset());
					float yaw = (float) (Utils.calculateAnimationValue(hitAnimationFrame, hitAnimationFrames, 0.8, 1.5, 20) + (30 - hitAnimationFrame));
					for (int i=1; i < 5; i++) {
						CreatureStand<?> temp = getCreatureStand(i);
						Entity stand = temp.getEntity(loc);
						float standYaw = stand.getLocation().getYaw();
						stand.teleport(loc.add(temp.getOffset()));
						stand.setRotation(standYaw + (i % 2 == 0 ? -yaw : yaw), 0);
						loc.subtract(temp.getOffset());
					}
					if (++hitAnimationFrame >= hitAnimationFrames)
						hitAnimation = false;
					return;
				}
				CreatureStand<?> headStand = getCreatureStand(0);
				Vector headOffset = offsets[0];
				headStand.getEntity(loc).teleport(loc.add(headStand.getOffset()).add(headOffset));
				loc.subtract(headOffset).subtract(headStand.getOffset());
				for (int i=1; i < 5; i++) {
					CreatureStand<?> temp = getCreatureStand(i);
					Entity stand = temp.getEntity(loc);
					float standYaw = stand.getLocation().getYaw();
					Vector extraOffset = offsets[i];
					stand.teleport(loc.add(temp.getOffset()).add(extraOffset));
					stand.setRotation(standYaw + (i == 0 ? 0 : i % 2 == 0 ? -5f : 5f), 0);
					loc.subtract(extraOffset).subtract(temp.getOffset());
				}
			}
		}.runTaskTimer(plugin, 0, 1));
		
		final Vector[] velocities = new Vector[] {
				Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03),
				Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03),
				Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03),
				Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03),
				Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03)
				};
		final double[] maxVelocities = new double[] { random.nextDouble(5, 15), random.nextDouble(5, 15), random.nextDouble(5, 15), random.nextDouble(5, 15), random.nextDouble(5, 15) };
		final int[] frame = {1, 0};
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				for (int i=0; i < offsets.length; i++)
					offsets[i].add(velocities[i].clone().multiply(Utils.calculateAnimationValue(frame[0], 20, 0.01, frame[1] == 0 ? 0.01 : 0.005, maxVelocities[i])));
				if (++frame[0] == 20) {
					frame[0] = 0;
					if (frame[1]++ == 1) {
						frame[1] = 0;
						offsets = new Vector[] { new Vector(), new Vector(), new Vector(), new Vector(), new Vector() };
						for (int i=0; i < velocities.length; i++) {
							velocities[i] = Utils.getRandomizedVector(1.0, 0.2, 1.0).multiply(0.03);
							maxVelocities[i] = random.nextDouble(5, 15);
						}
						return;
					}
					for (int i=0; i < velocities.length; i++)
						velocities[i].multiply(-1);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		Location loc = event.getEntity().getLocation().add(0,.5,0);
		Vector velocity = Utils.getVectorTowards(event.getDamager().getLocation(), loc).multiply(knockbackMultiplier);
		velocity.setY(Math.min(velocity.getY(), knockbackMultiplier / 3.0));
		event.getEntity().setVelocity(velocity);
		playSound(event.getDamager().getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1, 2);
		hitAnimation = true;
		hitAnimationFrame = 0;
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(VersionUtils.getEnchantParticle(), event.getEntity().getLocation().add(0,2,0), 1000, .25, .25, .25, 20);
		if (VersionUtils.displaysAllowed && getSectionBoolean("deathRagdoll", false)) {
			Location loc = event.getEntity().getLocation();
			int lifeTicks = (int) (getSectionDouble("ragdollSeconds", 7.0) * 20.0);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(0, loc).getLocation().add(0,1.5,0), Material.END_PORTAL_FRAME, 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(2, loc).getLocation().add(0,1.5,0), Material.PURPUR_PILLAR, 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(3, loc).getLocation().add(0,1.5,0), Material.PURPUR_PILLAR, 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(4, loc).getLocation().add(0,1.5,0), Material.OBSIDIAN, 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			for (int i=1; i < 4; i++) {
				Entity stand = getCreatureStandEntity(i, loc);
				Vector direction = stand.getLocation().getDirection();
				Vector left = new Vector(direction.getZ(), 0, -direction.getX());
				Vector right = new Vector(-direction.getZ(), 0, direction.getX());
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(right).add(0,1.5,0), Material.CHORUS_PLANT, 0.35f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(left).add(0,1.5,0), Material.CHORUS_FLOWER, 0.35f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			}
		}
	}
	public void setAttributes(Enderman entity) {
		super.setAttributes(entity);
		this.knockbackMultiplier = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK).getValue();
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(EndTotem.REGISTERED_KEY, EndTotem.class);
		
		type.setSpawnConditions(event -> {
			if (event.getEntityType() != EntityType.ENDERMAN)
				return false;
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.THE_END))
				return false;
			return true;
		});
	}
}
