package com.github.jewishbanana.ultimatecontent.entities.darkentities;

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
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class SwampBeast extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:swamp_beast";
	private static final Material mudType;
	private static final boolean usingMudBlock;
	static {
		if (IS_VERSION_19_OR_ABOVE) {
			mudType = Material.MUD;
			usingMudBlock = true;
		} else {
			mudType = Material.SOUL_SAND;
			usingMudBlock = false;
		}
	}
	
	private Map<Block, BlockState> mudPatchStorage = new HashMap<>();
	private double mudRange;
	
	public SwampBeast(Zombie entity) {
		super(entity, CustomEntityType.SWAMP_BEAST);
		
		entity.setCanPickupItems(false);
		makeEntityBreakDoors(entity);
		entity.setMetadata("uc-swampentity", Main.getFixedMetadata());
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
				else if (isTargetInRange(entity, 0, 36) && entity.isOnGround()) {
					abilityCooldown = 12;
					Location loc = entity.getLocation();
					World world = loc.getWorld();
					Vector vec = Utils.getVectorTowards(loc, entity.getTarget().getLocation());
					Vector angle = new Vector(vec.getZ(), 0, -vec.getX());
					Location spot = loc.clone().add(vec.clone().multiply(-1));
					Map<Block, BlockState> patches = new HashMap<>();
					vec.multiply(0.5);
					new BukkitRunnable() {
						private int tick;
						private double distance = mudRange + 1.0;
						private BlockData mudData = mudType.createBlockData();
						
						@Override
						public void run() {
							if (tick % 2 == 0 && distance > 0) {
								if (distance <= 1 || distance >= mudRange-1) {
									for (int i=-2; i < 2; i++) {
										Block b = Utils.getHighestExposedBlock(spot.clone().add(angle.clone().multiply(i)).getBlock(), 3);
										if (b == null || patches.containsKey(b) || b.getState() instanceof TileState || DependencyUtils.isBlockProtected(b))
											continue;
										BlockState state = b.getState();
										patches.put(b, state);
										mudPatchStorage.put(b, state);
										b.setType(mudType);
										world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,1,.5), 5, .4, .3, .4, 1, mudData);
									}
								} else {
									for (int i=-3; i < 3; i++) {
										Block b = Utils.getHighestExposedBlock(spot.clone().add(angle.clone().multiply(i)).getBlock(), 3);
										if (b == null || patches.containsKey(b) || b.getState() instanceof TileState || DependencyUtils.isBlockProtected(b))
											continue;
										BlockState state = b.getState();
										patches.put(b, state);
										mudPatchStorage.put(b, state);
										b.setType(mudType);
										world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,1,.5), 5, .4, .3, .4, 1, mudData);
									}
								}
								if (usingMudBlock)
									world.playSound(spot, Sound.BLOCK_MUD_BREAK, SoundCategory.BLOCKS, 1f, .5f);
								spot.add(vec);
								distance -= 0.5;
							}
							for (Entry<Block, BlockState> entry : patches.entrySet())
								if (entry.getKey().getType() == mudType) {
									world.spawnParticle(Particle.FALLING_DUST, entry.getKey().getLocation().add(.5,1,.5), 1, .3, .1, .3, 1, mudData);
									for (Entity e : world.getNearbyEntities(entry.getKey().getLocation().add(.5,1.5,.5), .5, .5, .5)) {
										if (e.hasMetadata("uc-swampentity"))
											continue;
										Vector vec = new Vector(e.getVelocity().getX()/8, e.getVelocity().getY(), e.getVelocity().getZ()/8);
										if (vec.getY() > 0)
											vec.setY(vec.getY()/8);
										e.setVelocity(vec);
										if (usingMudBlock && tick % 5 == 0 && e instanceof Player)
											world.playSound(e.getLocation(), Sound.BLOCK_MUD_PLACE, SoundCategory.BLOCKS, 0.3f, .5f);
									}
								}
							if (tick >= 200) {
								patches.forEach((k, v) -> {
									if (k.getType() == mudType)
										v.update(true);
									mudPatchStorage.remove(k);
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
						block.setType(mudType);
						mudPatchStorage.put(block, state);
					}
					new BukkitRunnable() {
						private int tick;
						private BlockState spotState;
						private BlockData mudData = mudType.createBlockData();
						
						@Override
						public void run() {
							if (entity == null || entity.isDead()) {
								if (state != null && block.getType() == mudType)
									state.update(true);
								mudPatchStorage.remove(block);
								if (spotState != null && spot.getBlock().getType() == mudType)
									spotState.update(true);
								mudPatchStorage.remove(spot.getBlock());
								this.cancel();
								return;
							}
							tick++;
							if (tick <= 30) {
								entity.teleport(entity.getLocation().add(0,-0.07,0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), block.getLocation().add(.5,1,.5), 7, .3, .2, .3, 1, mudData);
								if (usingMudBlock && tick % 5 == 0)
									playSound(block.getLocation(), Sound.BLOCK_MUD_HIT, 1, .5);
								return;
							}
							if (tick == 40) {
								Block tempBlock = spot.getBlock();
								if (!(tempBlock.getState() instanceof TileState) && !DependencyUtils.isBlockProtected(tempBlock)) {
									spotState = tempBlock.getState();
									mudPatchStorage.put(tempBlock, spotState);
									tempBlock.setType(mudType);
								}
								entity.teleport(tempBlock.getRelative(BlockFace.DOWN).getLocation().add(0.5,0,0.5));
								return;
							}
							if (tick > 40 && tick <= 60) {
								entity.teleport(entity.getLocation().add(0,0.1,0));
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), spot.clone().add(.5,1,.5), 7, .3, .2, .3, 1, mudData);
								if (usingMudBlock && tick % 5 == 0)
									playSound(spot, Sound.BLOCK_MUD_HIT, 1, .5);
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
										if (state != null && block.getType() == mudType)
											state.update(true);
										mudPatchStorage.remove(block);
										if (spotState != null && spot.getBlock().getType() == mudType)
											spotState.update(true);
										mudPatchStorage.remove(spot.getBlock());
									}
								}.runTaskLater(plugin, 200);
							}
						}
					}.runTaskTimer(plugin, 0, 1);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		BlockData mudData = mudType.createBlockData();
		BlockData soulSandData = Material.SOUL_SAND.createBlockData();
		makeParticleTask(entity, 1, () -> {
			entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation().add(0,1.1,0), 10, .3, .3, .3, 1, mudData);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0,1.2,0), 7, .3, .3, .3, 1, soulSandData);
		});
	}
	public void unload() {
		super.unload();
		mudPatchStorage.forEach((k, v) -> {
			if (k.getType() == mudType)
				v.update(true);
		});
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
		this.mudRange = getSectionDouble("mudRange", 8.0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(SwampBeast.REGISTERED_KEY, SwampBeast.class);
		
		type.setSpawnConditions(event -> {
			return VersionUtils.isBiomeSwamp(event.getLocation().getBlock().getBiome());
		});
	}
}
