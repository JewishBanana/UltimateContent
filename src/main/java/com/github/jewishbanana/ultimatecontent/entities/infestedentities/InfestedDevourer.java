package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.Variant;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderBreakBlocks;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.PathfinderClimbPowderedSnow;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class InfestedDevourer extends ComplexEntity<Silverfish> {

	public static final String REGISTERED_KEY = "uc:infested_devourer";

	private static final BlockFace[] FLOOD_FACES = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	// A wall block must belong to a connected solid mass of at least this many blocks (within this flood-fill depth) for the
	// devourer to burrow into / emerge from it — keeps it out of thin player-placed panes and lone floating blocks.
	private static final int SOLID_MASS_TARGET = 24;
	private static final int SOLID_MASS_DEPTH = 6;

	public static final List<InfestedDevourer> infestedDevourers;
	static {
		infestedDevourers = new ArrayList<>();
	}
	
	private enum InfestedDevourerVariant implements Variant {
		
		ALPHA("alpha_infested_devourer") {
			@Override
	        public void initVariant() {
				EntityVariant variant = getEntityVariant();
				variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_ALPHA_DEVOURER.getHead());
				variant.movementSpeed = 0.5;
	        }
		};
		
		private InfestedDevourerVariant(String variantPathName) {
			registerVariant(CustomEntityType.INFESTED_DEVOURER, variantPathName);
		}
	}
	
	private InfestedDevourerVariant variant;
	private double speed;
	private boolean movingToBlock;
	private boolean breakingManually;
	private int wallCreepCooldown = 160;
	
	public final List<Block> blocks = new ArrayList<>();
	
	public InfestedDevourer(Silverfish entity) {
		super(entity, CustomEntityType.INFESTED_DEVOURER);
		this.variant = getEntityVariant(InfestedDevourerVariant.class);
		this.speed = entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).getValue();
		infestedDevourers.add(this);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setInvulnerable(false);
		entity.setAI(true);
		entity.setGravity(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(entityVariant.getLoadoutArmor(LoadoutEquipmentSlot.HEAD).getItem());
		}, new Vector(0, -.75, 0)));
		setHeadStand(0);
		
		scheduleTask(new BukkitRunnable() {
			private final EntityController controller = BukkitBrain.getBrain(entity).getController();
			private int damageTicks;
			private Location targetBlock;
			private int inWallTicks;
			private int exitTries;
			private Vector incrementIntoWall;
			private LivingEntity target;
			private boolean leavingWall;
			private Location lastLoc = entity.getLocation();
			
			@Override
			public void run() {
				if (!entity.isValid() || leavingWall)
					return;
				Location entityLoc = entity.getLocation();
				if (inWallTicks > 0) {
					Block block = targetBlock.getBlock();
					if (block.isPassable()) {
						inWallTicks = 0;
						entity.setAI(true);
						entity.setInvulnerable(false);
						entity.setGravity(true);
						targetBlock = null;
						return;
					}
					if (inWallTicks > 50) {
						entity.teleport(entityLoc.add(incrementIntoWall));
						entityLoc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entityLoc, 3, .2, .2, .2, .1, block.getType().createBlockData());
					}
					if (inWallTicks == 1) {
						Location targetLoc = target != null ? target.getEyeLocation().add(0, 1, 0) : blocks.isEmpty() ? null : BlockUtils.getCenterOfBlock(blocks.get(0));
						if ((target != null && (!target.isValid() || !Utils.isLocationsWithinDistance(entityLoc, target.getLocation(), 625))) || ++exitTries > 60) {
							incrementIntoWall.multiply(-1);
							leavingWall = true;
						} else if (targetLoc != null) {
							// If the target has pillared/boxed up, emerge from a solid block directly above them so the devourer
							// drops down onto them; otherwise emerge from a nearby solid block toward them. Either exit must be a
							// genuine solid mass (flood fill), so the devourer only bursts out of real walls/sculk, not thin panes.
							Block spot = target != null ? findCeilingDropSpot(target) : null;
							if (spot == null)
								// Fold the solid-mass test into the ray predicate so the ray passes through thin player walls
								// (which it must, when emerging toward a boxed-in target) and stops on the first real wall/sculk.
								for (int i = 0; i < 5; i++) {
									Block candidate = BlockUtils.rayTraceForBlock(targetLoc, Utils.getRandomizedVector(1f, 0, 1f).setY(random.nextFloat() / 2 - 0.7), 9.0, temp -> !temp.isPassable() && isSolidMass(temp));
									if (candidate != null) {
										spot = candidate;
										break;
									}
								}
							if (spot != null) {
								targetBlock = BlockUtils.getCenterOfBlock(spot);
								entity.teleport(targetBlock);
								entityLoc.setDirection(targetLoc.toVector().subtract(entityLoc.toVector()));
								ArmorStand stand = headStand.getEntity(entityLoc);
								stand.setRotation(entityLoc.getYaw(), 0);
								stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()),0,0));
								incrementIntoWall = Utils.getVectorTowards(targetBlock, targetLoc).multiply(0.1);
								leavingWall = true;
							}
						}
						if (leavingWall)
							new BukkitRunnable() {
								private int moveTicks = 10;
								
								@Override
								public void run() {
									Block block = targetBlock.getBlock();
									if (block.isPassable()) {
										inWallTicks = 0;
										entity.setAI(true);
										entity.setInvulnerable(false);
										entity.setGravity(true);
										leavingWall = false;
										targetBlock = null;
										wallCreepCooldown = 100;
										this.cancel();
										return;
									}
									if (moveTicks-- > 0) {
										Location loc = entity.getLocation();
										entity.teleport(loc.add(incrementIntoWall));
										loc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), loc, 3, .2, .2, .2, .1, block.getType().createBlockData());
									} else {
										inWallTicks = 0;
										entity.setAI(true);
										entity.setInvulnerable(false);
										entity.setGravity(true);
										entity.setTarget(target);
										leavingWall = false;
										targetBlock = null;
										wallCreepCooldown = 100;
										this.cancel();
									}
								}
							}.runTaskTimer(plugin, 0, 1);
					} else
						inWallTicks--;
					return;
				}
				LivingEntity currentTarget = entity.getTarget();
				// Prioritize eating queued blocks (e.g. the blocks a cornered player walls themselves in with) over simply
				// charging the player: hop toward the nearest nearby queued block so the break pathfinder can chew through
				// it, exactly like the legacy devourer. Far-off blocks are left to the wall-creep logic / normal targeting.
				Block nearestBreak = nearestQueuedBlock(entityLoc, 144.0);
				Location goal = nearestBreak != null ? BlockUtils.getCenterOfBlock(nearestBreak)
						: (currentTarget == null ? controller.getTargetMoveLocation() : currentTarget.getLocation().add(0, currentTarget.getHeight() / 3.0 * 2.0, 0));
				if (entity.isOnGround() && goal != null) {
					if (!blocks.isEmpty() && Utils.isLocationsWithinDistance(entityLoc, goal, 3)) {
						entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 10, 10, true, false));
						// The break pathfinder can't navigate to a queued block directly above or below the devourer's own
						// column (no walkable spot adjacent to it), which left it frozen — parked on a player's platform looking
						// down, or under a roof the player sealed looking up. Break that block ourselves (a normal, animated
						// break — not an instant removal) so the pillar gets eaten out from under them / the ceiling gets chewed
						// open above them. Below is preferred (eat the floor first); the block right above is the fallback.
						if (!breakingManually) {
							Block self = entityLoc.getBlock();
							Block below = self.getRelative(BlockFace.DOWN);
							Block above = self.getRelative(BlockFace.UP);
							if (!below.isPassable() && blocks.contains(below))
								breakAdjacentBlock(below);
							else if (!above.isPassable() && blocks.contains(above))
								breakAdjacentBlock(above);
						}
					} else {
						entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), goal).multiply(speed).setY(0.45));
						if (IS_VERSION_19_OR_ABOVE)
							playSound(entity.getLocation(), Sound.BLOCK_SCULK_STEP, 1, 1);
					}
				}
				if (goal != null)
					entityLoc.setDirection((nearestBreak == null && currentTarget == null ? goal.add(entityLoc.getDirection().setY(0).normalize().multiply(2.0)) : goal).toVector().subtract(entityLoc.toVector()));
				ArmorStand stand = headStand.getEntity(entityLoc);
				stand.setRotation(entityLoc.getYaw(), 0);
				stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()),0,0));
				if (damageTicks == 0) {
					if (isTargetInRange(entity, 0, 1.25)) {
						EntityUtils.damageEntity(currentTarget, entityVariant.damage, "infestedDevourer", DamageCause.ENTITY_ATTACK, entity);
						entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entityLoc.clone().add(entityLoc.getDirection().multiply(0.3)), 5, .2, .2, .2, .1, Material.REDSTONE_BLOCK.createBlockData());
						playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 1f, .5f);
						damageTicks = 15;
						wallCreepCooldown = 100;
					}
				} else
					damageTicks--;
				if (targetBlock != null) {
					if (targetBlock.getBlock().isPassable() || (target == null && blocks.isEmpty())) {
						targetBlock = null;
						wallCreepCooldown = 100;
						return;
					}
					if (currentTarget == null)
						controller.moveTo(targetBlock);
					if (Utils.isLocationsWithinDistance(entityLoc, targetBlock, 1.56f)) {
						entity.setAI(false);
						entity.setInvulnerable(true);
						entity.setGravity(false);
						entityLoc.setDirection(targetBlock.toVector().subtract(entityLoc.toVector()));
						stand.setRotation(entityLoc.getYaw(), 0);
						stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()), 0, 0));
						inWallTicks = 60;
						exitTries = 0;
						incrementIntoWall = Utils.getVectorTowards(entityLoc, targetBlock).multiply(entityLoc.distance(targetBlock) / 10.0);
						movingToBlock = false;
						return;
					}
				}
				if (!Utils.isLocationsWithinDistance(entityLoc, lastLoc, 2.25f)) {
					lastLoc = entityLoc;
					wallCreepCooldown = Math.max(wallCreepCooldown, 30);
				}
				if (wallCreepCooldown == 0) {
					if (targetBlock != null) {
						targetBlock = null;
						movingToBlock = false;
					}
					if (isTargetInRange(entity, 9, 625, false) || (!blocks.isEmpty() && !Utils.isLocationsWithinDistance(entityLoc, BlockUtils.getCenterOfBlock(blocks.get(0)), 2.25f)))
						for (int i=0; i < 5; i++) {
							Block b = BlockUtils.rayTraceForBlock(entityLoc.add(0, .5, 0), Utils.getRandomizedVector(1f, 0, 1f).setY(random.nextFloat() / 2 - 0.7), 8.0, block -> !block.isPassable() && isSolidMass(block));
							if (b != null) {
								target = currentTarget;
								entity.setTarget(null);
								targetBlock = BlockUtils.getCenterOfBlock(b);
								controller.moveTo(targetBlock);
								movingToBlock = true;
								wallCreepCooldown = 100;
								break;
							}
						}
					wallCreepCooldown = 20;
				} else
					wallCreepCooldown--;
			}
		}.runTaskTimer(plugin, 0, 1));
	}
	/**
	 * Returns the nearest queued break-target block to {@code from} within {@code maxDistanceSquared} (same world), pruning
	 * any blocks that have since become air. Used so the devourer hops toward and eats nearby player-placed blocks instead
	 * of fruitlessly charging an unreachable player. Returns null if there is no such block in range.
	 */
	/**
	 * Breaks a queued block immediately adjacent to the devourer (directly below its feet or directly above its head) with
	 * a normal, animated block break (damage progress + sounds), since {@link PathfinderBreakBlocks} can't path to the
	 * block in the entity's own column. Used so a devourer parked on a pillaring player's platform eats the floor out from
	 * under them, and a devourer sealed under a player's roof chews up through the ceiling, instead of freezing in place.
	 */
	private void breakAdjacentBlock(Block target) {
		breakingManually = true;
		final Silverfish self = getCastedEntity();
		final Location blockLoc = target.getLocation();
		final Location center = BlockUtils.getCenterOfBlock(target);
		final SoundGroup soundGroup = target.getType().createBlockData().getSoundGroup();
		final ItemStack tool = self.getEquipment().getItemInMainHand();
		final Collection<Player> players = target.getWorld().getPlayers();
		final double damage = BlockUtils.getDamageOnBlock(target, tool) * (variant == InfestedDevourerVariant.ALPHA ? 30.0 : 6.0);
		scheduleTask(new BukkitRunnable() {
			private int ticks = (int) Math.ceil(1.0 / damage);
			private float damageTrack;

			@Override
			public void run() {
				if (!self.isValid() || target.getType().isAir() || !blocks.contains(target)
						|| !Utils.isLocationsWithinDistance(self.getLocation(), center, 4f)) {
					breakingManually = false;
					for (Player player : players)
						player.sendBlockDamage(blockLoc, 0);
					this.cancel();
					return;
				}
				if (ticks-- <= 0) {
					target.breakNaturally(tool);
					target.getWorld().playSound(center, soundGroup.getBreakSound(), (float) entityVariant.volume, 1f);
					blocks.remove(target);
					self.swingMainHand();
					breakingManually = false;
					for (Player player : players)
						player.sendBlockDamage(blockLoc, 0);
					this.cancel();
					return;
				}
				damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
				for (Player player : players)
					player.sendBlockDamage(blockLoc, damageTrack);
				if (ticks % 5 == 0)
					target.getWorld().playSound(center, soundGroup.getHitSound(), (float) entityVariant.volume, 1f);
			}
		}.runTaskTimer(plugin, 0, 1));
		target.getWorld().playSound(center, soundGroup.getHitSound(), (float) entityVariant.volume, 1f);
	}
	private Block nearestQueuedBlock(Location from, double maxDistanceSquared) {
		if (blocks.isEmpty())
			return null;
		Block nearest = null;
		double best = maxDistanceSquared;
		Iterator<Block> it = blocks.iterator();
		while (it.hasNext()) {
			Block b = it.next();
			if (b.getType().isAir()) {
				it.remove();
				continue;
			}
			if (!b.getWorld().equals(from.getWorld()))
				continue;
			double d = BlockUtils.getCenterOfBlock(b).distanceSquared(from);
			if (d <= best) {
				best = d;
				nearest = b;
			}
		}
		return nearest;
	}
	/**
	 * True if {@code block} is part of a substantial connected solid mass — a real wall or sculk body, not a thin
	 * player-placed pane or a lone floating block — determined by a bounded flood fill (mirrors the cave-in
	 * ceiling-thickness check). Used to gate which blocks the devourer may burrow into and emerge from.
	 */
	private static boolean isSolidMass(Block block) {
		return floodFillSolid(block, 0, new HashSet<>(), 0) >= SOLID_MASS_TARGET;
	}
	private static int floodFillSolid(Block block, int count, Set<Block> passed, int distance) {
		if (count >= SOLID_MASS_TARGET || distance >= SOLID_MASS_DEPTH || block.isPassable() || !passed.add(block))
			return count;
		count++;
		for (BlockFace face : FLOOD_FACES) {
			Block other = block.getRelative(face);
			if (other == null)
				continue;
			count = floodFillSolid(other, count, passed, distance + 1);
			if (count >= SOLID_MASS_TARGET)
				break;
		}
		return count;
	}
	/**
	 * When a target has pillared/boxed up, returns the lowest solid-mass block directly above their head for the devourer
	 * to emerge from and drop down onto them. Returns null if the first solid block above is not a real mass (e.g. a thin
	 * placed roof), so the devourer falls back to a lateral emerge instead.
	 */
	private Block findCeilingDropSpot(LivingEntity target) {
		Block b = target.getEyeLocation().getBlock().getRelative(BlockFace.UP);
		for (int i = 0; i < 24; i++) {
			// Penetrate thin player-placed roofs/walls: keep scanning up past them until a genuine solid mass (the real
			// sculk ceiling) is found, rather than giving up at the first non-passable block (e.g. the box the player built).
			if (!b.isPassable() && isSolidMass(b))
				return b;
			b = b.getRelative(BlockFace.UP);
		}
		return null;
	}
	public void setAIGoals(Silverfish entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 2);
		goals.put(new PathfinderNearestAttackableTarget<>(entity, Player.class, 10, true, false), 3);
		goals.put(new PathfinderNearestAttackableTarget<>(entity, Animals.class, 10, true, false), 4);
		
		goals = brain.getGoalAI();
		goals.clear();
		PathfinderBreakBlocks pathfinder = new PathfinderBreakBlocks(entity, blocks, 10f, variant == InfestedDevourerVariant.ALPHA ? 30.0 : 6.0, 1.0, 1.8f, (float) entityVariant.volume);
		pathfinder.endAction = block -> {
			entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), block.getLocation().add(.5, .5, .5)).multiply(0.5));
			playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 1f, .5f);
			wallCreepCooldown = 50;
		};
		goals.put(pathfinder, 0);
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderClimbPowderedSnow(entity), 1);
		goals.put(new PathfinderMeleeAttack(entity, 1.0, false), 5);
	}
	public void onTargetEntity(EntityTargetEvent event) {
		if (movingToBlock && event.getTarget() != null && event.getReason() != TargetReason.TARGET_ATTACKED_ENTITY)
			event.setCancelled(true);
	}
	public void onChangeBlock(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		infestedDevourers.remove(this);
	}
	public void setAttributes(Silverfish entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(25);
		entity.getAttribute(VersionUtils.getAttackDamageAttribute()).setBaseValue(0.0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedDevourer.REGISTERED_KEY, InfestedDevourer.class);
		Variant.initVariants(InfestedDevourerVariant.class);

		type.setSpawnConditions(event -> {
			return false;
		});
	}
	/**
	 * Directs every active devourer within {@code radiusSquared} of {@code center} (same world) to break toward
	 * {@code block}, by appending it to their break-target list. Public API used by DeadlyDisasters' Infested Cave
	 * disaster to make devourers converge on blocks the player places inside the infestation. Safe to call when no
	 * devourers are present (no-op).
	 */
	public static void addBlockTargetNearby(Location center, double radiusSquared, Block block) {
		if (center == null || block == null)
			return;
		for (InfestedDevourer dev : infestedDevourers) {
			org.bukkit.entity.Entity e = dev.getEntity();
			if (e == null || e.isDead() || !e.getWorld().equals(center.getWorld()))
				continue;
			if (e.getLocation().distanceSquared(center) <= radiusSquared)
				dev.blocks.add(block);
		}
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findMonsterSpawnLocation(area, 1);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedDevourer.class);
	};
}
