package com.github.jewishbanana.ultimatecontent.entities.desertentities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.abilities.CursedWinds;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class AncientSkeleton extends BaseEntity<Skeleton> {
	
	public static final String REGISTERED_KEY = "uc:ancient_skeleton";
	
	private Skeleton[] minions = new Skeleton[3];

	public AncientSkeleton(Skeleton entity) {
		super(entity, CustomEntityType.ANCIENT_SKELETON);
		
		EntitiesHandler.makeEntityNoSunlightCombust(entity);
		entity.setSilent(true);
		entity.setCanPickupItems(false);
		entity.setAI(true);
		
		scheduleTask(new BukkitRunnable() {
			private int cooldown;
			
			@Override
			public void run() {
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				if (!entity.isValid())
					return;
				if (isTargetInRange(entity, 0, 64)) {
					for (Skeleton temp : minions)
						if (temp != null && !temp.isDead()) {
							cooldown = 10;
							CursedWinds ability = UIAbilityType.createAbilityInstance(CursedWinds.class);
							ability.initFields();
							Set<UUID> immune = new HashSet<>();
							for (Skeleton minion : minions)
								if (minion != null)
									immune.add(minion.getUniqueId());
							ability.setVolume((float) entityType.volume);
							ability.activate(entity.getLocation(), entity, immune, null);
							return;
						}
					cooldown = 17;
					LivingEntity target = entity.getTarget();
					entity.setAI(false);
					entity.setVelocity(new Vector());
					playSound(entity.getLocation(), Sound.ENTITY_SKELETON_CONVERTED_TO_STRAY, 1, .5);
					Location targetLoc = entity.getTarget().getLocation();
					for (int i=0; i < 3; i++) {
						Location loc = Utils.findRandomSpotInRadius(entity.getLocation(), 1.5, 4.0, 2, 6, temp -> temp.getBlock().getRelative(BlockFace.DOWN).getType().isOccluding() && temp.getBlock().getRelative(BlockFace.DOWN, 2).getType().isOccluding());
						if (loc == null)
							continue;
						loc.setDirection(Utils.getVectorTowards(loc, targetLoc));
						minions[i] = entity.getWorld().spawn(loc.subtract(0, 2, 0), Skeleton.class, temp -> {
							temp.setAI(false);
							temp.setGravity(false);
							temp.getEquipment().setItemInMainHand(new ItemStack(Material.AIR), true);
							EntitiesHandler.makeEntityNoSunlightCombust(temp);
							EntitiesHandler.makeEntityNoSuffocate(temp);
							EntitiesHandler.attachRemoveKey(temp);
						});
					}
					new BukkitRunnable() {
						private int tick;
						private BlockData sandData = Material.SAND.createBlockData();
						
						@Override
						public void run() {
							if (tick++ == 60) {
								for (Skeleton temp : minions) {
									if (temp == null || temp.isDead())
										continue;
									temp.setAI(true);
									temp.setGravity(true);
									temp.setTarget(target);
									EntitiesHandler.removeEntitiyNoSuffocate(temp.getUniqueId());
								}
								entity.setAI(true);
								this.cancel();
								return;
							}
							for (Skeleton temp : minions) {
								if (temp == null || temp.isDead())
									continue;
								temp.teleport(temp.getLocation().clone().add(0, .035, 0));
								temp.getWorld().spawnParticle(VersionUtils.getBlockDust(), temp.getLocation(), 5, .5, .5, .5, .01, sandData);
							}
						}
					}.runTaskTimer(plugin, 0, 1);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		makeParticleTask(entity, Particle.FLAME, new Vector(0, 1.3, 0), 1, .25, .45, .25, .01);
	}
	public void unload() {
		super.unload();
		EntitiesHandler.removeEntitiyNoSunlightCombust(getUniqueId());
		for (Skeleton entity : minions)
			if (entity != null && !entity.hasAI())
				entity.setAI(true);
	}
	public void setAttributes(Skeleton entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(AncientSkeleton.REGISTERED_KEY, AncientSkeleton.class);
		
		type.setSpawnConditions(event -> {
			if (!(event.getEntity() instanceof Monster))
				return false;
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.NORMAL))
				return false;
			if (!VersionUtils.isBiomeDesert(loc.getBlock().getBiome()))
				return false;
			return true;
		});
	}
}
