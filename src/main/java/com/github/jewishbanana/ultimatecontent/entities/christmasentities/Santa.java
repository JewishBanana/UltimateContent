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
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BossEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer.Song;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class Santa extends BossEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:santa";

	public Santa(Zombie entity) {
		super(entity, CustomEntityType.SANTA);
		
		entity.setRemoveWhenFarAway(false);
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		entity.setMetadata("uc-christmasmobs", Main.getFixedMetadata());
		
		scheduleTask(new BukkitRunnable() {
			private int abilityCooldown;
			
			@Override
			public void run() {
				if (abilityCooldown > 0) {
					abilityCooldown--;
					return;
				}
				if (!entity.isValid())
					return;
				if (isTargetInRange(entity, 0, 225)) {
					abilityCooldown = 15;
					entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 40, 10, true, false));
					entity.swingMainHand();
					playSound(entity.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 2f, .5f);
					long nearbyCount = entity.getNearbyEntities(30, 30, 30).stream().filter(temp -> temp.hasMetadata("uc-christmasmobs")).count();
					int count = nearbyCount < 10 ? 5 : 2;
					Location entityLoc = entity.getLocation();
					LivingEntity target = entity.getTarget();
					spawnPresent(entityLoc, random.nextInt(count), new Vector(0.5, 0.8, 0.0), entity, target);
					spawnPresent(entityLoc, random.nextInt(count), new Vector(-0.5, 0.8, 0.5), entity, target);
					spawnPresent(entityLoc, random.nextInt(count), new Vector(-0.5, 0.8, -0.5), entity, target);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		BlockData snowData = Material.SNOW.createBlockData();
		BlockData redWool = Material.RED_WOOL.createBlockData();
		makeParticleTask(entity, 1, () -> {
			if (random.nextInt(4) != 0)
				return;
			Location particleLoc = entity.getLocation().add(0, 1.2, 0);
			World world = entity.getWorld();
			world.spawnParticle(Particle.FALLING_DUST, particleLoc, 1, .3, .5, .3, 1, snowData);
			world.spawnParticle(Particle.FALLING_DUST, particleLoc, 1, .3, .5, .3, 1, redWool);
		});
	}
	public void spawnPresent(Location location, int type, Vector direction, Entity source, LivingEntity target) {
		World world = location.getWorld();
		Location spawnLoc = location.clone().add(0, 100, 0);
		ArmorStand stand = world.spawn(spawnLoc, ArmorStand.class, temp -> {
			ComplexEntity.initStand(temp);
		});
		BlockData trail;
		switch (type) {
		case 4:
			stand.getEquipment().setHelmet(CustomHead.GREEN_GIFT.getHead());
			trail = Material.LIME_WOOL.createBlockData();
			break;
		case 3:
			stand.getEquipment().setHelmet(CustomHead.BLUE_GIFT.getHead());
			trail = Material.LIGHT_BLUE_WOOL.createBlockData();
			break;
		case 2:
			stand.getEquipment().setHelmet(CustomHead.YELLOW_GIFT.getHead());
			trail = Material.YELLOW_WOOL.createBlockData();
			break;
		case 1:
			stand.getEquipment().setHelmet(CustomHead.PURPLE_GIFT.getHead());
			trail = Material.PURPLE_WOOL.createBlockData();
			break;
		default:
		case 0:
			stand.getEquipment().setHelmet(CustomHead.WHITE_GIFT.getHead());
			trail = Material.RED_WOOL.createBlockData();
			break;
		}
		Slime slime = world.spawn(spawnLoc, Slime.class, temp -> {
			temp.setSize(0);
			temp.setSilent(true);
			temp.getAttribute(VersionUtils.getMaxHealthAttribute()).setBaseValue(100.0);
			temp.setHealth(100.0);
			temp.getAttribute(VersionUtils.getAttackDamageAttribute()).setBaseValue(0.0);
			temp.setInvisible(true);
			temp.teleport(location);
			temp.teleport(temp.getLocation().add(0, 1, 0));
			temp.setVelocity(direction);
			EntitiesHandler.attachRemoveKey(temp);
		});
		Particle blockCrack = VersionUtils.getBlockCrack();
		new BukkitRunnable() {
			private int tick;
			
			@Override
			public void run() {
				if (stand == null || stand.isDead() || slime == null || slime.isDead() || ++tick > 200) {
					if (stand != null)
						stand.remove();
					if (slime != null)
						slime.remove();
					this.cancel();
					return;
				}
				stand.teleport(slime.getLocation().add(0, -2, 0));
				world.spawnParticle(blockCrack, stand.getLocation().add(0, 2, 0), 2, .1, .1, .1, 1, trail);
				if (slime.isOnGround()) {
					world.spawnParticle(blockCrack, stand.getLocation().add(0, 2, 0), 30, .7, .7, .7, 1, trail);
					switch (type) {
					case 4:
						Grinch grinch = UIEntityManager.spawnEntity(slime.getLocation(), Grinch.class);
						if (target != null && !target.isDead())
							grinch.getCastedEntity().setTarget(target);
						break;
					case 3:
						Frosty frosty = UIEntityManager.spawnEntity(slime.getLocation(), Frosty.class);
						if (target != null && !target.isDead())
							frosty.getCastedEntity().setTarget(target);
						break;
					case 2:
						for (int i=0; i < 3; i++) {
							Elf elf = UIEntityManager.spawnEntity(slime.getLocation(), Elf.class);
							if (target != null && !target.isDead())
								elf.getCastedEntity().setTarget(target);
						}
						break;
					case 1:
						Location loc = slime.getLocation();
						Map<Block, BlockState> patches = new HashMap<>();
						List<Block> patchList = new ArrayList<>();
						new BukkitRunnable() {
							private int tick;
							private int distance;
							private BlockData ice_data = Material.PACKED_ICE.createBlockData();
							private int lastEntityCheck = 0;
							private Location minBound = null;
							private Location maxBound = null;
							
							@Override
							public void run() {
								if (distance % 2 == 0 && tick <= 5) {
									for (Block temp : BlockUtils.getBlocksInCircleCircumference(loc, tick)) {
										Block b = BlockUtils.getHighestExposedBlock(temp, 3);
										if (b == null || patches.containsKey(b) || DependencyUtils.isBlockProtected(b))
											continue;
										BlockState state = b.getState();
										patches.put(b, state);
										patchList.add(b);
										b.setType(Material.PACKED_ICE);
										world.spawnParticle(blockCrack, b.getLocation().add(.5, 1, .5), 5, .4, .3, .4, 1, ice_data);
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
									world.playSound(loc, Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.6f, .5f);
									tick++;
								}
								if (distance - lastEntityCheck >= 2 && minBound != null) {
									lastEntityCheck = distance;
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
												if (e instanceof Player && distance % 5 == 0)
													world.playSound(e.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.2f, .5f);
											}
										}
									}
								}
								if (distance >= 200) {
									for (int i = 0; i < patchList.size(); i++) {
										Block k = patchList.get(i);
										if (k.getType() == Material.PACKED_ICE)
											patches.get(k).update(true);
									}
									this.cancel();
								}
								distance++;
							}
						}.runTaskTimer(plugin, 0, 1);
						break;
					default:
					case 0:
						world.createExplosion(slime.getLocation(), 3.5f, true, true, source);
						break;
					}
					stand.remove();
					slime.remove();
					this.cancel();
					return;
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null
				&& event.getTarget().hasMetadata("uc-christmasmobs")
				&& !(UIEntityManager.getEntity(event.getTarget()) instanceof TameableEntity tameable && tameable.getOwner() != null && !tameable.getOwner().equals(event.getEntity().getUniqueId())))
			event.setCancelled(true);
	}
	public Song getSongTheme() {
		return SongPlayer.Song.CHRISTMAS_BOSS;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Santa.REGISTERED_KEY, Santa.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
}
