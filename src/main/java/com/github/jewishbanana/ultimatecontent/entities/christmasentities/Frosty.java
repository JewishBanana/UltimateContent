package com.github.jewishbanana.ultimatecontent.entities.christmasentities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.specialevents.ChristmasEvent;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class Frosty extends BaseEntity<Snowman> {
	
	public static final String REGISTERED_KEY = "uc:frosty";
	
	private Map<Block, BlockState> icePatchStorage = new HashMap<>();

	public Frosty(Snowman entity) {
		super(entity, CustomEntityType.FROSTY);
		
		entity.setCanPickupItems(false);
		entity.setDerp(true);
		entity.setMetadata("uc-christmasmobs", Main.getFixedMetadata());
		entity.setAI(true);
		
		scheduleTask(new BukkitRunnable() {
			private int abilityCooldown;
			private int tpCooldown;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (abilityCooldown > 0)
					abilityCooldown--;
				else if (isTargetInRange(entity, 0, 81) && entity.isOnGround()) {
					abilityCooldown = 12;
					Location loc = entity.getLocation();
					World world = loc.getWorld();
					Vector vec = Utils.getVectorTowards(loc, entity.getTarget().getLocation());
					Vector angle = new Vector(vec.getZ(), 0, -vec.getX());
					Location spot = loc.clone().add(vec.clone().multiply(-2)).add(angle.clone().multiply(-4));
					Map<Block, BlockState> patches = new HashMap<>();
					List<Block> patchList = new ArrayList<>();
					new BukkitRunnable() {
						private int tick;
						private int distance = 24;
						private BlockData ice_data = Material.PACKED_ICE.createBlockData();
						private int lastEntityCheck = 0;
						private Location minBound = null;
						private Location maxBound = null;
						
						@Override
						public void run() {
							if (tick % 2 == 0 && distance >= 0) {
								int start, end;
								if (distance <= 4 || distance >= 10) {
									start = 2;
									end = 6;
								} else {
									start = 0;
									end = 9;
								}
								for (int i = start; i < end; i++) {
									Location tempLoc = spot.clone().add(angle.clone().multiply(i));
									Block b = BlockUtils.getHighestExposedBlock(tempLoc.getBlock(), 3);
									if (b == null || patches.containsKey(b) || b.getState() instanceof TileState || DependencyUtils.isBlockProtected(b))
										continue;
									BlockState state = b.getState();
									patches.put(b, state);
									patchList.add(b);
									icePatchStorage.put(b, state);
									b.setType(Material.PACKED_ICE);
									world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5, 1, .5), 5, .4, .3, .4, 1, ice_data);
									Location bLoc = b.getLocation();
									if (minBound == null) {
										minBound = bLoc.clone();
										maxBound = bLoc.clone();
									} else {
										minBound.setX(Math.min(minBound.getX(), bLoc.getX()));
										minBound.setY(Math.min(minBound.getY(), bLoc.getY()));
										minBound.setZ(Math.min(minBound.getZ(), bLoc.getZ()));
										maxBound.setX(Math.max(maxBound.getX(), bLoc.getX()));
										maxBound.setY(Math.max(maxBound.getY(), bLoc.getY()));
										maxBound.setZ(Math.max(maxBound.getZ(), bLoc.getZ()));
									}
								}
								world.playSound(spot, Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.6f, .5f);
								spot.add(vec.clone().multiply(0.5));
								distance--;
							}
							if (tick - lastEntityCheck >= 2 && minBound != null) {
								lastEntityCheck = tick;
								Location center = minBound.clone().add(maxBound).multiply(0.5);
								double xRadius = (maxBound.getX() - minBound.getX()) / 2.0 + 1.5;
								double yRadius = (maxBound.getY() - minBound.getY()) / 2.0 + 2.0;
								double zRadius = (maxBound.getZ() - minBound.getZ()) / 2.0 + 1.5;
								Collection<Entity> nearbyEntities = world.getNearbyEntities(center, xRadius, yRadius, zRadius);
								for (int i = 0; i < patchList.size(); i++) {
									Block block = patchList.get(i);
									if (block.getType() != Material.PACKED_ICE)
										continue;
									if (random.nextInt(5) == 0)
										world.spawnParticle(Particle.FALLING_DUST, block.getLocation().add(.5, 1, .5), 1, .3, .1, .3, 1, ice_data);
									Location blockCenter = block.getLocation().add(.5, 1.5, .5);
									for (Entity e : nearbyEntities) {
										if (e.hasMetadata("uc-christmasmobs"))
											continue;
										Location eLoc = e.getLocation();
										double dx = Math.abs(eLoc.getX() - blockCenter.getX());
										double dy = Math.abs(eLoc.getY() - blockCenter.getY());
										double dz = Math.abs(eLoc.getZ() - blockCenter.getZ());
										if (dx <= 0.5 && dy <= 0.5 && dz <= 0.5) {
											Vector vel = e.getVelocity();
											Vector vec = new Vector(vel.getX() / 8, vel.getY(), vel.getZ() / 8);
											if (vec.getY() > 0)
												vec.setY(vec.getY() / 8);
											e.setVelocity(vec);
											e.setFreezeTicks(220);
											if (tick % 5 == 0 && e instanceof Player)
												world.playSound(e.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.2f, .5f);
										}
									}
								}
							}
							if (tick >= 200) {
								for (int i = 0; i < patchList.size(); i++) {
									Block k = patchList.get(i);
									BlockState v = patches.get(k);
									if (k.getType() == Material.PACKED_ICE)
										v.update(true);
									icePatchStorage.remove(k);
								}
								this.cancel();
							}
							tick++;
						}
					}.runTaskTimer(plugin, 0, 1);
				}
				if (tpCooldown > 0)
					tpCooldown--;
				else if (isTargetInRange(entity, 100, 400, false) && entity.isOnGround()
						&& entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isOccluding() && entity.getLocation().getBlock().getRelative(BlockFace.DOWN, 2).getType().isOccluding()) {
					LivingEntity target = entity.getTarget();
					Location loc = Utils.findRandomSpotInRadius(target.getLocation(), 4f, 8f, 2, 10, () -> Utils.getRandomizedVector(), temp -> temp.getBlock().getRelative(BlockFace.DOWN).getType().isOccluding() && temp.getBlock().getRelative(BlockFace.DOWN, 2).getType().isOccluding());
					if (loc == null)
						return;
					tpCooldown = 15;
					final Location spot = loc.getBlock().getRelative(BlockFace.DOWN).getLocation();
					entity.setAI(false);
					entity.setGravity(false);
					Location entityLoc = entity.getLocation();
					entity.teleport(entityLoc.getBlock().getLocation().add(0.5, 0, 0.5));
					entity.setRotation(entityLoc.getYaw(), 70);
					Block block = entityLoc.getBlock().getRelative(BlockFace.DOWN);
					BlockState state = block.getState() instanceof TileState || DependencyUtils.isBlockProtected(block) ? null : block.getState();
					if (state != null) {
						block.setType(Material.PACKED_ICE);
						icePatchStorage.put(block, state);
					}
					new BukkitRunnable() {
						private int tick;
						private BlockState spotState;
						private BlockData ice_data = Material.PACKED_ICE.createBlockData();
						
						@Override
						public void run() {
							if (entity == null || entity.isDead()) {
								if (state != null && block.getType() == Material.PACKED_ICE)
									state.update(true);
								icePatchStorage.remove(block);
								if (spotState != null && spot.getBlock().getType() == Material.PACKED_ICE)
									spotState.update(true);
								icePatchStorage.remove(spot.getBlock());
								this.cancel();
								return;
							}
							tick++;
							if (tick <= 30) {
								entity.teleport(entity.getLocation().add(0, -0.07, 0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), block.getLocation().add(.5, 1, .5), 7, .3, .2, .3, 1, ice_data);
								if (tick % 5 == 0)
									playSound(block.getLocation(), Sound.BLOCK_GLASS_HIT, 1f, .5f);
								return;
							}
							if (tick == 40) {
								Block tempBlock = spot.getBlock();
								if (!(tempBlock.getState() instanceof TileState) && !DependencyUtils.isBlockProtected(tempBlock)) {
									spotState = tempBlock.getState();
									icePatchStorage.put(tempBlock, spotState);
									tempBlock.setType(Material.PACKED_ICE);
								}
								entity.teleport(tempBlock.getRelative(BlockFace.DOWN).getLocation().add(0.5, 0, 0.5));
								return;
							}
							if (tick > 40 && tick <= 60) {
								entity.teleport(entity.getLocation().add(0, 0.1, 0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), spot.clone().add(.5, 1, .5), 7, .3, .2, .3, 1, ice_data);
								if (tick % 5 == 0)
									playSound(spot, Sound.BLOCK_GLASS_HIT, 1f, .5f);
								if (target != null && target.getWorld().equals(entity.getWorld()))
									EntityUtils.makeEntityFaceLocation(entity, target.getLocation());
								return;
							}
							if (tick > 60) {
								entity.setAI(true);
								entity.setGravity(true);
								entity.setTarget(target);
								this.cancel();
								new BukkitRunnable() {
									@Override
									public void run() {
										if (state != null && block.getType() == Material.PACKED_ICE)
											state.update(true);
										icePatchStorage.remove(block);
										if (spotState != null && spot.getBlock().getType() == Material.PACKED_ICE)
											spotState.update(true);
										icePatchStorage.remove(spot.getBlock());
									}
								}.runTaskLater(plugin, 200);
							}
						}
					}.runTaskTimer(plugin, 0, 1);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		BlockData ice_data = Material.PACKED_ICE.createBlockData();
		makeParticleTask(entity, 1, () -> {
			entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation().add(0, 1.1, 0), 5, .3, .3, .3, 1, ice_data);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, 1.2, 0), 2, .3, .4, .3, 1, ice_data);
		});
	}
	public void setAIGoals(Snowman entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.removeIf(pathfinder -> pathfinder.getPathfinder().getName().equals("PathfinderNearestAttackableTarget") && pathfinder.getPriority() == 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class), 1);
	}
	public void launchProjectile(ProjectileLaunchEvent event) {
		EntitiesHandler.frostySnowballs.put(event.getEntity().getUniqueId(), this);
	}
	public void projectileHit(ProjectileHitEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				EntitiesHandler.frostySnowballs.remove(event.getEntity().getUniqueId());
			}
		}.runTaskLater(plugin, 1);
		if (event.getHitEntity() == null || event.getHitEntity().hasMetadata("uc-christmasmobs"))
			return;
		Entity entity = event.getHitEntity();
		playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 2f, .5f);
		if (entity instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false));
	}
	public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null
				&& event.getTarget().hasMetadata("uc-christmasmobs")
				&& !(UIEntityManager.getEntity(event.getTarget()) instanceof TameableEntity tameable && tameable.getOwner() != null && !tameable.getOwner().equals(event.getEntity().getUniqueId())))
			event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		icePatchStorage.forEach((k, v) -> {
			if (k.getType() == Material.PACKED_ICE)
				v.update(true);
		});
	}
	public void setAttributes(Snowman entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Frosty.REGISTERED_KEY, Frosty.class);
		
		type.setSpawnConditions(ChristmasEvent.eventEntitySpawnCondition);
	}
}
