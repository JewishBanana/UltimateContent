package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.Tag;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class UndeadMiner extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:undead_miner";
	private static Queue<UndeadMiner> miners = new ArrayDeque<>();
	
	private Stack<Block> placed = new Stack<>();
	private Material placingMaterial;

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
			private double maxDist = entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue() * entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (target == null || target.isDead() || !entity.getWorld().equals(target.getWorld())) {
					target = null;
					LivingEntity temp = entity.getTarget();
					if (temp != null)
						target = temp;
					return;
				}
				double distance = entity.getLocation().distanceSquared(target.getLocation());
				if (distance > maxDist) {
					target = null;
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
					final SoundGroup soundGroup = block.getType().createBlockData().getSoundGroup();
					final Location blockLoc = block.getLocation().add(.5, .5, .5);
					new BukkitRunnable() {
						final double damage = Utils.getDamageOnBlock(block, entity.getEquipment().getItemInMainHand());
						int ticks = (int) Math.ceil(1.0 / damage);
						float damageTrack;

						@Override
						public void run() {
							if (block.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(startPos.getWorld()) || entity.getLocation().distanceSquared(startPos) > 1) {
								this.cancel();
								breaking = false;
								for (Player player : startPos.getWorld().getPlayers())
									player.sendBlockDamage(block.getLocation(), 0);
								return;
							}
							if (ticks-- <= 0) {
								block.breakNaturally(entity.getEquipment().getItemInMainHand());
								playSound(blockLoc, soundGroup.getBreakSound(), 1, 1);
								this.cancel();
								breaking = false;
								entity.swingMainHand();
								for (Player player : startPos.getWorld().getPlayers())
									player.sendBlockDamage(block.getLocation(), 0);
								return;
							}
							damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
							for (Player player : startPos.getWorld().getPlayers())
								player.sendBlockDamage(block.getLocation(), damageTrack);
							if (ticks % 5 == 0)
								playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
						}
					}.runTaskTimer(plugin, 0, 1);
					playSound(blockLoc, soundGroup.getHitSound(), 1, 1);
					return;
				}
				if (placingMaterial == null || !entity.getVelocity().setY(0).isZero())
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
						final SoundGroup soundGroup = block.getType().createBlockData().getSoundGroup();
						final Location blockLoc = block.getLocation().add(.5, .5, .5);
						final double damage = Utils.getDamageOnBlock(block, entity.getEquipment().getItemInMainHand());
						final Block breakingBlock = block;
						new BukkitRunnable() {
							int ticks = (int) Math.ceil(1.0 / damage);
							float damageTrack;

							@Override
							public void run() {
								if (breakingBlock.getType().isAir() || !entity.isValid() || !entity.getWorld().equals(startPos.getWorld()) || entity.getLocation().distanceSquared(startPos) > 1) {
									this.cancel();
									breaking = false;
									for (Player player : startPos.getWorld().getPlayers())
										player.sendBlockDamage(breakingBlock.getLocation(), 0);
									return;
								}
								if (ticks-- <= 0) {
									breakingBlock.breakNaturally(entity.getEquipment().getItemInMainHand());
									playSound(blockLoc, soundGroup.getBreakSound(), 1, 1);
									this.cancel();
									breaking = false;
									entity.swingMainHand();
									for (Player player : startPos.getWorld().getPlayers())
										player.sendBlockDamage(breakingBlock.getLocation(), 0);
									return;
								}
								damageTrack = Utils.clamp((float) (damageTrack + damage), 0f, 1f);
								for (Player player : startPos.getWorld().getPlayers())
									player.sendBlockDamage(breakingBlock.getLocation(), damageTrack);
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
					playSound(temp.getLocation().add(.5, .5, .5), placeSound, 1, 1);
					entity.swingOffHand();
					temp.setType(placingMaterial);
					placed.push(temp);
					return;
				}
			}
		}.runTaskTimer(plugin, 0, 10));
	}
	private boolean canBreak(Block block) {
		return block != null && !block.isPassable() && block.getType().isBlock() && !DependencyUtils.isBlockProtected(block);
	}
	public void onDamaged(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.SUFFOCATION && !placed.isEmpty() && event.getEntity().getLocation().getBlock().equals(placed.peek())) {
			event.setCancelled(true);
			return;
		}
		super.onDamaged(event);
	}
	public void unload() {
		super.unload();
		if (!placed.isEmpty() && getSectionBoolean("removePlacedBlocks", true)) {
			miners.add(this);
			new BukkitRunnable() {
				private BlockData data = placingMaterial.createBlockData();
				private Sound breakSound = data.getSoundGroup().getBreakSound();
			
				@Override
				public void run() {
					if (placed.isEmpty()) {
						this.cancel();
						return;
					}
					Block block = placed.remove(0);
					if (block.getType() == placingMaterial) {
						block.setType(Material.AIR);
						Location temp = block.getLocation().add(.5, .5, .5);
						block.getWorld().spawnParticle(VersionUtils.getBlockCrack(), temp, 7, 0, 0, 0, 1, data);
						playSound(temp, breakSound, 1, 1);
					}
				}
			}.runTaskTimer(plugin, 400, 40);
		}
	}
	public void setAttributes(Zombie entity) {
		if (entity.isAdult())
			entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
		else
			entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(UndeadMiner.REGISTERED_KEY, UndeadMiner.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (event.getEntityType() != EntityType.ZOMBIE)
				return false;
			if (!Utils.isEnvironment(event.getLocation().getWorld(), Environment.NORMAL) || event.getLocation().getY() > 50)
				return false;
			return true;
		});
	}
	public static void clearPlacedBlocks() {
		miners.forEach(temp -> {
			if (!temp.placed.isEmpty())
				temp.placed.forEach(block -> {
					if (block.getType() == temp.placingMaterial)
						block.setType(Material.AIR);
				});
		});
	}
}
