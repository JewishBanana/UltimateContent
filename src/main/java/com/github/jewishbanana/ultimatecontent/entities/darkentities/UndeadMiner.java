package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderBuildStaircase;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class UndeadMiner extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:undead_miner";
	private static final List<UndeadMiner> miners = new ArrayList<>();
	
	private Stack<Block> placed = new Stack<>();
	private Material placingMaterial;
	// Breach mode: an externally-assigned persistent target (e.g. the Purge disaster) the miner will keep digging toward
	// out to breachMaxDistSq, without abandoning it at the normal follow-range. Unset miners behave exactly as before.
	private LivingEntity breachTarget;
	private double breachMaxDistSq;
	private boolean breachMiner; // set once put into breach mode; makes its placed staircase linger far longer after death

	public UndeadMiner(Zombie entity) {
		super(entity, CustomEntityType.UNDEAD_MINER);
		if (entityVariant.getOffHandItem() == null) {
			List<Material> blocks = new ArrayList<>();
			Environment environment = entity.getWorld().getEnvironment();
			Material underBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
			switch (environment) {
			default:
			case NORMAL:
				if (entity.getLocation().getBlockY() < 0)
					blocks.addAll(Arrays.asList(Material.COBBLED_DEEPSLATE, Material.TUFF, Material.CALCITE, Material.AMETHYST_BLOCK));
				else
					blocks.addAll(Arrays.asList(Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE));
				if (Tag.PLANKS.isTagged(underBlock))
					blocks.add(underBlock);
				break;
			case NETHER:
				blocks.addAll(Arrays.asList(Material.NETHERRACK, Material.SOUL_SOIL, Material.BASALT));
				if (underBlock == Material.BLACKSTONE || underBlock == Material.NETHER_BRICKS)
					blocks.add(underBlock);
				break;
			case THE_END:
				blocks.addAll(Arrays.asList(Material.END_STONE));
				if (random.nextInt(20) == 0)
					blocks.add(Material.OBSIDIAN);
				break;
			}
			entity.getEquipment().setItemInOffHand(new ItemStack(blocks.get(random.nextInt(blocks.size()))));
			entity.getEquipment().setItemInOffHandDropChance(60f);
		}
		ItemStack offhand = entity.getEquipment().getItemInOffHand();
		if (offhand != null && offhand.getType().isBlock())
			placingMaterial = offhand.getType();
		final Sound placeSound = placingMaterial != null ? placingMaterial.createBlockData().getSoundGroup().getPlaceSound() : null;
		scheduleTask(new BukkitRunnable() {
			private boolean clutching;
			
			@Override
			public void run() {
				if (!entity.isValid() || clutching || entity.isOnGround())
					return;
				Block block = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
				if (entity.getVelocity().getY() < -0.5 && block.isPassable() && block.getRelative(BlockFace.DOWN).getType().isSolid()) {
					if (!DependencyUtils.isBlockProtected(block)) {
						block.setType(Material.WATER);
						ItemStack hand = entity.getEquipment().getItemInMainHand();
						entity.getEquipment().setItemInMainHand(new ItemStack(Material.WATER_BUCKET));
						clutching = true;
						new BukkitRunnable() {
							@Override
							public void run() {
								block.setType(Material.AIR);
								entity.getEquipment().setItemInMainHand(hand == null ? new ItemStack(Material.AIR) : hand);
								clutching = false;
							}
						}.runTaskLater(plugin, 9);
					}
				}
			}
		}.runTaskTimer(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			private LivingEntity target;
			private boolean breaking;
			private final double followRangeAttributeValue = entity.getAttribute(VersionUtils.getFollowRangeAttribute()).getValue();
			private final double maxDist = followRangeAttributeValue * followRangeAttributeValue;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				// While a breach target sits above and in range, the PathfinderBuildStaircase goal owns this miner (it
					// suppresses the player target and physically climbs the staircase it builds), so yield entirely — don't
					// re-assert the chase target or run the pillar/bridge code, which would fight it. Once the stairs reach the
					// target's height the pathfinder stops and this loop resumes (bridging across).
					if (breachMiner && breachTarget != null && !breachTarget.isDead() && entity.getWorld().equals(breachTarget.getWorld())
							&& breachTarget.getEyeLocation().getY() - entity.getLocation().getY() > 1.5
							&& entity.getLocation().distanceSquared(breachTarget.getLocation()) < 3600.0)
						return;
					// Breach mode forces and retains an externally-assigned target out to a larger range.
				if (breachTarget != null) {
					if (breachTarget.isDead() || !entity.getWorld().equals(breachTarget.getWorld()))
						breachTarget = null;
					else {
						target = breachTarget;
						if (!breachTarget.equals(entity.getTarget()))
							entity.setTarget(breachTarget);
					}
				}
				if (target == null || target.isDead() || !entity.getWorld().equals(target.getWorld())) {
					target = null;
					LivingEntity temp = entity.getTarget();
					if (temp != null)
						target = temp;
					return;
				}
				double distance = entity.getLocation().distanceSquared(target.getLocation());
				if (distance > (breachTarget != null ? breachMaxDistSq : maxDist)) {
					target = null;
					breachTarget = null;
					entity.setTarget(null);
					return;
				}
				LivingEntity currentTarget = entity.getTarget();
				if (currentTarget == null)
					entity.setTarget(target);
				else if (!currentTarget.equals(target))
					target = currentTarget;
				if (entity.getVelocity().lengthSquared() > 0.1 || breaking || distance < 0.49)
					return;
				Block toBreak = null;
				Location entityLoc = entity.getEyeLocation();
				Block currentBlock = entityLoc.getBlock();
				Vector vec = Utils.getVectorTowards(entityLoc, target.getEyeLocation());
				Vector dir = Math.abs(vec.getX()) > Math.abs(vec.getZ()) ? new Vector(vec.getX(), 0, 0).normalize() : new Vector(0, 0, vec.getZ()).normalize();
				double yWeight = Math.abs(vec.getY());
				if (yWeight > 0.5) {
					if (yWeight > 0.75) {
						currentBlock = vec.getY() > 0 ? currentBlock.getRelative(BlockFace.UP) : entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
						if (canBreak(currentBlock))
							toBreak = currentBlock;
					} else {
						if (vec.getY() > 0) {
							currentBlock = entityLoc.add(dir).getBlock();
							if (canBreak(currentBlock))
								toBreak = currentBlock;
							else if (entity.getHeight() > 1) {
								currentBlock = currentBlock.getRelative(BlockFace.UP);
								if (canBreak(currentBlock))
									toBreak = currentBlock;
							}
						} else {
							currentBlock = entity.getLocation().add(dir).getBlock();
							if (canBreak(currentBlock))
								toBreak = currentBlock;
							else {
								currentBlock = currentBlock.getRelative(BlockFace.DOWN);
								if (canBreak(currentBlock))
									toBreak = currentBlock;
								else if (entity.getHeight() > 1) {
									currentBlock = currentBlock.getRelative(BlockFace.UP, 2);
									if (canBreak(currentBlock))
										toBreak = currentBlock;
								}
							}
						}
					}
				} else {
					currentBlock = entityLoc.add(dir).getBlock();
					if (canBreak(currentBlock))
						toBreak = currentBlock;
					else if (entity.getHeight() > 1) {
						currentBlock = currentBlock.getRelative(BlockFace.DOWN);
						if (canBreak(currentBlock))
							toBreak = currentBlock;
					}
				}
				if (toBreak != null) {
					breaking = true;
					final Location startPos = entity.getLocation();
					final Block block = toBreak;
					final BlockData blockData = block.getType().createBlockData();
					final SoundGroup soundGroup = blockData.getSoundGroup();
					final Location blockLoc = BlockUtils.getCenterOfBlock(block);
					final ItemStack tool = entity.getEquipment().getItemInMainHand();
					final World world = startPos.getWorld();
					final Collection<Player> players = world.getPlayers();
					new BukkitRunnable() {
						final double damage = BlockUtils.getDamageOnBlock(block, tool);
						int ticks = (int) Math.ceil(1.0 / damage);
						float damageTrack;

						@Override
						public void run() {
							if (block.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(world) || entity.getLocation().distanceSquared(startPos) > 1) {
								this.cancel();
								breaking = false;
								for (Player player : players)
									player.sendBlockDamage(blockLoc, 0);
								return;
							}
							if (ticks-- <= 0) {
								block.breakNaturally(tool);
								playSound(blockLoc, soundGroup.getBreakSound(), 1, 1);
								this.cancel();
								breaking = false;
								entity.swingMainHand();
								for (Player player : players)
									player.sendBlockDamage(blockLoc, 0);
								return;
							}
							damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
							for (Player player : players)
								player.sendBlockDamage(blockLoc, damageTrack);
							if (ticks % 5 == 0)
								playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
						}
					}.runTaskTimer(plugin, 0, 1);
					playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
					return;
				}
				if (placingMaterial == null || !entity.getVelocity().setY(0).isZero())
					return;
				// Don't let an ordinary miner pillar/build while standing on a breach staircase — it would obstruct or wreck
				// the steps. Let it just walk up and use the staircase instead. (Breach miners themselves still bridge off
				// the top of their own staircase, so they're exempt.)
				if (!breachMiner && PathfinderBuildStaircase.stairBlocks.contains(entity.getLocation().getBlock().getRelative(BlockFace.DOWN)))
					return;
				entityLoc = entity.getLocation();
				if (entity.getVelocity().getY() > -0.5 && target.getLocation().getBlockY()-1 > entityLoc.getBlockY()) {
					Block block = null;
					Location temp = entity.getEyeLocation().add(0, 1, 0);
					double width = entity.getWidth() / 2.0;
					if (canBreak(temp.getBlock()))
						block = temp.getBlock();
					else {
						temp.add(width, 0, width);
						if (canBreak(temp.getBlock()))
							block = temp.getBlock();
						else {
							temp.add(0, 0, -(width*2));
							if (canBreak(temp.getBlock()))
								block = temp.getBlock();
							else {
								temp.add(-(width*2), 0, 0);
								if (canBreak(temp.getBlock()))
									block = temp.getBlock();
								else {
									temp.add(0, 0, width*2);
									if (canBreak(temp.getBlock()))
										block = temp.getBlock();
								}
							}
						}
					}
					if (block != null) {
						breaking = true;
						final Location startPos = entity.getLocation();
						final BlockData blockData = block.getType().createBlockData();
						final SoundGroup soundGroup = blockData.getSoundGroup();
						final Location blockLoc = BlockUtils.getCenterOfBlock(block);
						final ItemStack tool = entity.getEquipment().getItemInMainHand();
						final double damage = BlockUtils.getDamageOnBlock(block, tool);
						final Block breakingBlock = block;
						final World world = startPos.getWorld();
						final Collection<Player> players = world.getPlayers();
						new BukkitRunnable() {
							int ticks = (int) Math.ceil(1.0 / damage);
							float damageTrack;

							@Override
							public void run() {
								if (breakingBlock.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(world) || entity.getLocation().distanceSquared(startPos) > 1) {
									this.cancel();
									breaking = false;
									for (Player player : players)
										player.sendBlockDamage(blockLoc, 0);
									return;
								}
								if (ticks-- <= 0) {
									breakingBlock.breakNaturally(tool);
									playSound(blockLoc, soundGroup.getBreakSound(), 1, 1);
									this.cancel();
									breaking = false;
									entity.swingMainHand();
									for (Player player : players)
										player.sendBlockDamage(blockLoc, 0);
									return;
								}
								damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
								for (Player player : players)
									player.sendBlockDamage(blockLoc, damageTrack);
								if (ticks % 5 == 0)
									playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
							}
						}.runTaskTimer(plugin, 0, 1);
						playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
						return;
					}
					if (entityLoc.getBlock().isPassable() && !DependencyUtils.isBlockProtected(entityLoc.getBlock())) {
						entity.setVelocity(new Vector(0, 0.45, 0));
						playSound(entityLoc, placeSound, 1, 1);
						entity.swingOffHand();
						Block toPlace = entityLoc.getBlock();
						toPlace.setType(placingMaterial);
						placed.push(toPlace);
						return;
					}
				}
				entityLoc.add(dir);
				Block temp = entityLoc.getBlock().getRelative(BlockFace.DOWN);
				if (temp.isPassable() && !DependencyUtils.isBlockProtected(temp)) {
					playSound(BlockUtils.getCenterOfBlock(temp), placeSound, 1, 1);
					entity.swingOffHand();
					temp.setType(placingMaterial);
					placed.push(temp);
					return;
				}
			}
		}.runTaskTimer(plugin, 0, 10));
	}
	private boolean canBreak(Block block) {
		return block != null && !block.isPassable() && block.getType().isBlock() && !DependencyUtils.isBlockProtected(block)
				&& !PathfinderBuildStaircase.stairBlocks.contains(block); // never break another miner's breach staircase
	}
	/**
	 * Puts this miner into "breach mode": it will lock onto and keep digging toward the given target out to
	 * {@code maxRange} blocks without abandoning it at its normal follow-range. Used by disasters to tunnel toward a
	 * sealed-in player. Passing a null target clears breach mode.
	 */
	public void setBreachMode(LivingEntity target, double maxRange) {
		this.breachTarget = target;
		this.breachMaxDistSq = maxRange * maxRange;
		this.breachMiner = true;
		// Install the goal that physically builds + climbs a shared spiral staircase to an elevated target (skybase/pillar).
		if (target != null && placingMaterial != null)
			try {
				BukkitBrain.getBrain(getCastedEntity()).getGoalAI().put(new PathfinderBuildStaircase(getCastedEntity(), target, placingMaterial, placed), 0);
			} catch (Exception e) {
			}
	}
	/**
	 * Vanilla zombie randomization can equip a heavily-damaged iron helmet; in full daylight (and especially on a skybase,
	 * which is open sky all day) that helmet shatters quickly and the miner burns up before it can dig — so breach miners
	 * kept dying. Reset whatever helmet it spawned with to 50–100% durability. Run from spawn() (after the framework's
	 * loadout pass, which never touches HEAD for this entity) so it isn't overwritten.
	 */
	public void spawn(Entity entity) {
		super.spawn(entity);
		if (!(entity instanceof LivingEntity alive))
			return;
		ItemStack helmet = alive.getEquipment().getHelmet();
		if (helmet != null && helmet.getType().getMaxDurability() > 0 && helmet.getItemMeta() instanceof Damageable meta) {
			meta.setDamage((int) (helmet.getType().getMaxDurability() * (random.nextDouble() * 0.5)));
			helmet.setItemMeta(meta);
			alive.getEquipment().setHelmet(helmet);
		}
	}
	public void onDamaged(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.SUFFOCATION) {
			// Breach miners constantly place blocks around themselves (building stairs, then bridging over to the target);
			// during the stair->bridge transition they sometimes ended up briefly inside a freshly placed block and
			// suffocated to death. Make them immune to suffocation outright so they can never kill themselves building.
			if (breachMiner) {
				event.setCancelled(true);
				return;
			}
			if (!placed.isEmpty() && event.getEntity().getLocation().getBlock().equals(placed.peek())) {
				event.setCancelled(true);
				return;
			}
		}
		super.onDamaged(event);
	}
	public void unload() {
		super.unload();
		if (!placed.isEmpty() && getSectionBoolean("removePlacedBlocks", true)) {
			if (!plugin.isEnabled()) {
				clearPlacedBlocks(placed, placingMaterial);
				return;
			}
			miners.add(this);
			new BukkitRunnable() {
				private BlockData data = placingMaterial.createBlockData();
				private Sound breakSound = data.getSoundGroup().getBreakSound();
				private Particle blockCrack = VersionUtils.getBlockCrack();
			
				@Override
				public void run() {
					if (placed.isEmpty()) {
						this.cancel();
						return;
					}
					Block block = placed.remove(0);
					PathfinderBuildStaircase.stairBlocks.remove(block);
					if (block.getType() == placingMaterial) {
						block.setType(Material.AIR);
						Location temp = BlockUtils.getCenterOfBlock(block);
						block.getWorld().spawnParticle(blockCrack, temp, 7, 0, 0, 0, 1, data);
						playSound(temp, breakSound, 1, 1);
					}
				}
			// Breach staircases linger far longer so the horde can keep climbing after the builder dies (≈2min hold, then
			// a slow crumble); ordinary miner debris cleans up on the usual short timer.
			}.runTaskTimer(plugin, breachMiner ? 2400 : 400, breachMiner ? 60 : 40);
		}
	}
	private static void clearPlacedBlocks(Collection<Block> blocks, Material placingMaterial) {
		blocks.forEach(block -> {
			PathfinderBuildStaircase.stairBlocks.remove(block);
			if (block.getType() == placingMaterial)
				block.setType(Material.AIR);
		});
		blocks.clear();
	}
	public void setAttributes(Zombie entity) {
		if (entity.isAdult())
			entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(0.35);
		else
			entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(0.2);
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(UndeadMiner.REGISTERED_KEY, UndeadMiner.class);
		type.setRandomizeData(true);
		type.setSpawnConditions(event -> {
			if (event.getEntityType() != EntityType.ZOMBIE)
				return false;
			if (!CustomEntityType.UNDEAD_MINER.isWorldSpawnable(event.getLocation().getWorld()))
				return false;
			if (event.getLocation().getY() > 50)
				return false;
			return true;
		});
	}
	public static void clearPlacedBlocks() {
		miners.forEach(temp -> {
			if (!temp.placed.isEmpty())
				clearPlacedBlocks(temp.placed, temp.placingMaterial);
		});
		miners.clear();
	}
}
