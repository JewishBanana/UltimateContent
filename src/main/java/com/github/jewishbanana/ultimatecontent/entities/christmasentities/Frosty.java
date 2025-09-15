package com.github.jewishbanana.ultimatecontent.entities.christmasentities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
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
					new BukkitRunnable() {
						private int tick;
						private int distance = 24;
						private BlockData ice_data = Material.PACKED_ICE.createBlockData();
						
						@Override
						public void run() {
							if (tick % 2 == 0 && distance >= 0) {
								if (distance <= 4 || distance >= 10) {
									for (int i=2; i < 6; i++) {
										Block b = Utils.getHighestExposedBlock(spot.clone().add(angle.clone().multiply(i)).getBlock(), 3);
										if (b == null || patches.containsKey(b) || b.getState() instanceof TileState || DependencyUtils.isBlockProtected(b))
											continue;
										BlockState state = b.getState();
										patches.put(b, state);
										icePatchStorage.put(b, state);
										b.setType(Material.PACKED_ICE);
										world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,1,.5), 5, .4, .3, .4, 1, ice_data);
									}
								} else {
									for (int i=0; i < 9; i++) {
										Block b = Utils.getHighestExposedBlock(spot.clone().add(angle.clone().multiply(i)).getBlock(), 3);
										if (b == null || patches.containsKey(b) || b.getState() instanceof TileState || DependencyUtils.isBlockProtected(b))
											continue;
										BlockState state = b.getState();
										patches.put(b, state);
										icePatchStorage.put(b, state);
										b.setType(Material.PACKED_ICE);
										world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,1,.5), 5, .4, .3, .4, 1, ice_data);
									}
								}
								world.playSound(spot, Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.6f, .5f);
								spot.add(vec.clone().multiply(0.5));
								distance--;
							}
							for (Entry<Block, BlockState> entry : patches.entrySet())
								if (entry.getKey().getType() == Material.PACKED_ICE) {
									if (random.nextInt(5) == 0)
										world.spawnParticle(Particle.FALLING_DUST, entry.getKey().getLocation().add(.5,1,.5), 1, .3, .1, .3, 1, ice_data);
									for (Entity e : world.getNearbyEntities(entry.getKey().getLocation().add(.5,1.5,.5), .5, .5, .5)) {
										if (e.hasMetadata("uc-christmasmobs"))
											continue;
										Vector vec = new Vector(e.getVelocity().getX()/8, e.getVelocity().getY(), e.getVelocity().getZ()/8);
										if (vec.getY() > 0)
											vec.setY(vec.getY()/8);
										e.setVelocity(vec);
										e.setFreezeTicks(220);
										if (tick % 5 == 0 && e instanceof Player)
											world.playSound(e.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.2f, .5f);
									}
								}
							if (tick >= 200) {
								patches.forEach((k, v) -> {
									if (k.getType() == Material.PACKED_ICE)
										v.update(true);
									icePatchStorage.remove(k);
								});
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
					Location loc = Utils.findRandomSpotInRadius(target.getLocation(), 4, 8, 2, 10, temp -> temp.getBlock().getRelative(BlockFace.DOWN).getType().isOccluding() && temp.getBlock().getRelative(BlockFace.DOWN, 2).getType().isOccluding());
					if (loc == null)
						return;
					tpCooldown = 15;
					final Location spot = loc.getBlock().getRelative(BlockFace.DOWN).getLocation();
					entity.setAI(false);
					entity.setGravity(false);
					Location entityLoc = entity.getLocation();
					entity.teleport(entityLoc.getBlock().getLocation().add(0.5,0,0.5));
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
								entity.teleport(entity.getLocation().add(0,-0.07,0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), block.getLocation().add(.5,1,.5), 7, .3, .2, .3, 1, ice_data);
								if (tick % 5 == 0)
									playSound(block.getLocation(), Sound.BLOCK_GLASS_HIT, 1, .5);
								return;
							}
							if (tick == 40) {
								Block tempBlock = spot.getBlock();
								if (!(tempBlock.getState() instanceof TileState) && !DependencyUtils.isBlockProtected(tempBlock)) {
									spotState = tempBlock.getState();
									icePatchStorage.put(tempBlock, spotState);
									tempBlock.setType(Material.PACKED_ICE);
								}
								entity.teleport(tempBlock.getRelative(BlockFace.DOWN).getLocation().add(0.5,0,0.5));
								return;
							}
							if (tick > 40 && tick <= 60) {
								entity.teleport(entity.getLocation().add(0,0.1,0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), spot.clone().add(.5,1,.5), 7, .3, .2, .3, 1, ice_data);
								if (tick % 5 == 0)
									playSound(spot, Sound.BLOCK_GLASS_HIT, 1, .5);
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
		playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 2, .5);
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
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Frosty.REGISTERED_KEY, Frosty.class);
		
		type.setSpawnConditions(ChristmasEvent.eventEntitySpawnCondition);
	}
}
