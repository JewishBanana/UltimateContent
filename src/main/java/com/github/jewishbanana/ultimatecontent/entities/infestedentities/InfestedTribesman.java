package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.Variant;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderEncircleLeader;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderFollowEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderRangedEntityAttack;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class InfestedTribesman extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:infested_tribesman";
	
	private enum TribesmanVariant implements Variant {
		
		ARCHER("infested_archer_tribesman");
		
		private TribesmanVariant(String variantPathName) {
			registerVariant(CustomEntityType.INFESTED_TRIBESMAN, variantPathName);
		}
	}
	
	private TribesmanVariant variant;

	public InfestedTribesman(Zombie entity) {
		super(entity, CustomEntityType.INFESTED_TRIBESMAN);
		this.variant = getEntityVariant(TribesmanVariant.class);
		
		EntitiesHandler.makeEntityNoSunlightCombust(entity);
		entity.setBaby();
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		entity.setMetadata("uc-tribesman", Main.getFixedMetadata());
		
		final int green = random.nextInt(25, 85);
		EntityUtils.modifyLoadoutArmorColor(this, 1, green, green-20, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
		
		if (variant == TribesmanVariant.ARCHER) {
			entity.setMetadata("uc-tribesmanarcher", Main.getFixedMetadata());
			if (random.nextInt(4) != 0)
				scheduleTask(new BukkitRunnable() {
					private final int maxStack = random.nextInt(2);
					
					@Override
					public void run() {
						if (!entity.isValid())
							return;
						for (Entity e : entity.getNearbyEntities(1, 1, 1))
							if (!e.equals(entity) && !e.isInsideVehicle() && e.getPassengers().size() <= maxStack && e.hasMetadata("uc-tribesman") && !e.hasMetadata("uc-tribesmanarcher"))
								if (e.getPassengers().isEmpty())
									e.addPassenger(entity);
								else if (!e.getPassengers().contains(entity) && e.getPassengers().get(0).isEmpty())
									e.getPassengers().get(0).addPassenger(entity);
					}
				}.runTaskTimer(plugin, random.nextInt(1, 10) * 20, 60));
		}
	}
	public void setAIGoals(Zombie entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class, 10, true, false), 2);
		goals.put(new PathfinderNearestAttackableTarget<Animals>(entity, Animals.class, 10, true, false), 3);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		if (variant == TribesmanVariant.ARCHER)
			goals.put(new PathfinderRangedEntityAttack(entity, 40, 5.0, 10.0, 1.0, 2.0, null, shootArrow), 2);
		else
			goals.put(new PathfinderMeleeAttack(entity), 2);
		goals.put(new PathfinderRandomStrollLand(entity), 3);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 4);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 5);
		goals.put(new PathfinderRandomLook(entity), 6);
	}
	public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null && event.getTarget().hasMetadata("uc-tribesman"))
			event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		EntitiesHandler.removeEntitiyNoSunlightCombust(getUniqueId());
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25);
	}
	public void setSquad(Set<UUID> squad, UUID leader) {
		Zombie entity = getCastedEntity();
		if (entity == null || leader == null)
			return;
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		if (variant == TribesmanVariant.ARCHER)
			goals.put(new PathfinderRangedEntityAttack(entity, 40, 5.0, 10.0, 1.0, 2.0, null, shootArrow), 2);
		else
			goals.put(new PathfinderMeleeAttack(entity), 2);
		goals.put(new PathfinderFollowEntity(entity, leader, 2, 12), 3);
		goals.put(new PathfinderEncircleLeader(entity, leader, squad, 0.7), 4);
		goals.put(new PathfinderRandomStrollLand(entity), 5);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 6);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 7);
		goals.put(new PathfinderRandomLook(entity), 8);
	}
	private static final Consumer<Mob> shootArrow = mob -> {
		LivingEntity target = mob.getTarget();
		if (target == null)
			return;
		Arrow arrow = mob.launchProjectile(Arrow.class);
		arrow.setVelocity(Utils.getParabolicVelocity(mob.getEyeLocation(), target.getLocation().add(0, target.getHeight() / 2.0, 0), 2.5, 0.05));
		arrow.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 120, 1), false);
		if (UIEntityManager.getEntity(mob) instanceof BaseEntity<?> base)
			base.playSound(mob.getLocation(), Sound.ENTITY_SKELETON_SHOOT, 1, random.nextDouble(1.4, 1.8));
		else
			mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_SKELETON_SHOOT, 1f, random.nextFloat(0.8f, 1.2f));
	};
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedTribesman.REGISTERED_KEY, InfestedTribesman.class);
		Variant.initVariants(TribesmanVariant.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area, 1);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		InfestedTribesman leader = UIEntityManager.spawnEntity(spawn, InfestedTribesman.class);
		Set<UUID> pack = new HashSet<>();
		Vector vec = Utils.getRandomizedVector().setY(0);
		double increment = (Math.PI * 2) / 3;
		for (int i=0; i < 3; i++) {
			Location loc = spawn.clone().add(vec);
			Location temp = EntityUtils.findSmartYSpawn(spawn, loc, 1, 3);
			if (temp == null)
				loc = spawn;
			else
				loc.setY(temp.getY());
			InfestedTribesman tribesman = UIEntityManager.spawnEntity(loc, InfestedTribesman.class);
			tribesman.setOwner(leader.getUniqueId());
			pack.add(tribesman.getUniqueId());
			tribesman.setSquad(pack, leader.getUniqueId());
			vec.rotateAroundY(increment);
		}
		return leader;
	};
}
