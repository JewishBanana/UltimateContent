package com.github.jewishbanana.ultimatecontent.entities.easterentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BossEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer.Song;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;
import com.mojang.datafixers.util.Pair;

public class EasterBunny extends BossEntity<Rabbit> {
	
	public static final String REGISTERED_KEY = "uc:easter_bunny";
	
	private static final Set<Material> flowers;
	static {
		flowers = EnumSet.copyOf(Tag.FLOWERS.getValues());
		flowers.addAll(Tag.SMALL_FLOWERS.getValues());
		if (!VersionUtils.isMCVersionOrAbove("1.21.4"))
			flowers.addAll(BlockUtils.getMaterialTagByName("TALL_FLOWERS").getValues());
		flowers.addAll(Arrays.asList(VersionUtils.getShortGrass(), Material.TALL_GRASS));
	}
	
	private final List<Chicken> chickens = new ArrayList<>();

	public EasterBunny(Rabbit entity) {
		super(entity, CustomEntityType.EASTER_BUNNY);
		
		entity.setRabbitType(Type.THE_KILLER_BUNNY);
		entity.setMetadata("uc-eastermobs", Main.getFixedMetadata());
		
		scheduleTask(new BukkitRunnable() {
			private int biteTicks;
			private int jumpTicks;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Vector velocity = entity.getVelocity();
				if (jumpTicks-- <= 0 && velocity.getY() > 0 && velocity.getY() < 0.3) {
					jumpTicks = 15;
					entity.setVelocity(entity.getVelocity().multiply(1.5).setY(0.6));
				}
				LivingEntity target = entity.getTarget();
				if (target == null || !target.isValid() || target.getType() == EntityType.ARMOR_STAND)
					return;
				if (biteTicks <= 0) {
					if (isTargetInRange(entity, 0, 1.2)) {
						biteTicks = 8;
						target.damage(entityVariant.damage, entity);
					}
				} else
					biteTicks--;
			}
		}.runTaskTimer(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			private int eggLayCooldown;
			private int leapCooldown;
			private int shootingCooldown;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (eggLayCooldown > 0)
					eggLayCooldown--;
				if (leapCooldown > 0)
					leapCooldown--;
				if (shootingCooldown > 0)
					shootingCooldown--;
				LivingEntity target = entity.getTarget();
				if (target == null || !target.isValid() || target.getType() == EntityType.ARMOR_STAND || !target.getWorld().equals(entity.getWorld()))
					return;
				if (eggLayCooldown == 0) {
					chickens.removeIf(temp -> !temp.isValid());
					if (chickens.size() >= 10)
						eggLayCooldown = 10;
					else {
						eggLayCooldown = 20;
						final int eggCount = entity.getHealth() >= entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue() / 2.0 ? 3 : 5;
						playSound(entity.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 0);
						World world = entity.getWorld();
						Particle itemCrack = VersionUtils.getItemCrack();
						ItemStack eggItem = new ItemStack(Material.EGG);
						new BukkitRunnable() {
							private int tick;
							private int totalEggs = 10 - eggCount < chickens.size() ? 10 - chickens.size() : eggCount;
							
							@Override
							public void run() {
								if (++tick > totalEggs) {
									this.cancel();
									return;
								}
								Item egg = world.dropItemNaturally(entity.getLocation(), eggItem, temp -> {
									temp.setPickupDelay(10000);
									EntitiesHandler.attachRemoveKey(temp);
								});
								new BukkitRunnable() {
									@Override
									public void run() {
										if (egg == null || !egg.isValid())
											return;
										Location loc = egg.getLocation();
										world.spawnParticle(itemCrack, loc, 8, .5, .5, .5, 0.001, eggItem);
										world.playSound(loc, Sound.ENTITY_TURTLE_EGG_HATCH, SoundCategory.HOSTILE, 1f, 0.7f);
										for (int i=0; i < 8; i++) {
											DustTransition dust = random.nextInt(2) == 0 ?
													new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat())
													: new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat() / 2f);
											world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.clone().add(0, .2, 0).add(random.nextFloat(-.5f, .5f), random.nextFloat(-.5f, .5f), random.nextFloat(-.5f, .5f)), 1, 0, 0, 0, 0.001, dust);
										}
										KillerChicken chicken = UIEntityManager.spawnEntity(loc, KillerChicken.class);
										Chicken temp = chicken.getCastedEntity();
										temp.setTarget(entity.getTarget());
										egg.remove();
										chickens.add(temp);
									}
								}.runTaskLater(plugin, random.nextInt(60)+20);
							}
						}.runTaskTimer(plugin, 0, 20);
					}
				}
				if (leapCooldown == 0) {
					leapCooldown = 12;
					Location entityLoc = entity.getLocation();
					Location targetLoc = target.getLocation();
					playSound(entityLoc, Sound.ENTITY_RABBIT_JUMP, 20f, 0f);
					entity.setVelocity(Utils.getVectorTowards(entityLoc, targetLoc).multiply(entityLoc.distance(targetLoc) / 10).setY(2));
					World world = entity.getWorld();
					Particle blockCrack = VersionUtils.getBlockCrack();
					new BukkitRunnable() {
						private BlockData dirtData = Material.COARSE_DIRT.createBlockData();
						private ItemStack air = new ItemStack(Material.AIR);
						
						@Override
						public void run() {
							if (!entity.isValid()) {
								this.cancel();
								return;
							}
							if (entity.isOnGround()) {
								this.cancel();
								Location entityLoc = entity.getLocation();
								playSound(entityLoc, Sound.BLOCK_GRASS_BREAK, 2f, 0f);
								List<Block> blocks = BlockUtils.getBlocksInCircleRadius(entityLoc, 4.0f);
								for (int i = 0; i < blocks.size(); i++) {
									Block b = BlockUtils.getHighestExposedBlock(blocks.get(i), 3);
									if (b == null)
										continue;
									b = b.getRelative(BlockFace.UP);
									Location temp = BlockUtils.getCenterOfBlock(b);
									world.spawnParticle(blockCrack, temp, 6, .3, .5, .3, 0.001, dirtData);
									if (flowers.contains(b.getType()) && !DependencyUtils.isBlockProtected(b))
										b.breakNaturally(air);
									Collection<Entity> nearbyEntities = world.getNearbyEntities(temp.add(0, 1, 0), .5, 1.5, .5);
									for (Entity e : nearbyEntities) {
										if (e.equals(entity) || EntityUtils.isEntityImmunePlayer(e))
											continue;
										e.setVelocity(Utils.getVectorTowards(entityLoc, e.getLocation()).multiply(0.7).setY(1.5));
										if (e instanceof LivingEntity alive)
											alive.damage(15.0, entity);
									}
								}
							}
						}
					}.runTaskTimer(plugin, 10, 1);
				}
				if (shootingCooldown == 0) {
					if (isTargetInRange(entity, 25, 325)) {
						shootingCooldown = 10;
						final int shotCount = entity.getHealth() >= entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue() / 2.0 ? 2 : 4;
						entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), shotCount * 15, 10, true, false));
						final List<Pair<Slime, ArmorStand>> projectiles = new ArrayList<>();
						World world = entity.getWorld();
						new BukkitRunnable() {
							private int tick;
							
							@Override
							public void run() {
								if (tick++ > shotCount) {
									this.cancel();
									return;
								}
								Location loc = entity.getLocation();
								projectiles.add(Pair.of(
										world.spawn(loc, Slime.class, false, temp -> {
											temp.setSize(0);
											temp.setSilent(true);
											temp.getAttribute(VersionUtils.getMaxHealthAttribute()).setBaseValue(100.0);
											temp.setHealth(100.0);
											temp.getAttribute(VersionUtils.getAttackDamageAttribute()).setBaseValue(0.0);
											setInvisible(temp);
											temp.setVelocity(new Vector(0, 1, 0));
											EntitiesHandler.attachRemoveKey(temp);
										}), world.spawn(loc, ArmorStand.class, false, temp -> {
											ComplexEntity.initStand(temp);
											temp.getEquipment().setItemInMainHand(new ItemStack(Material.CARROT));
											temp.setRightArmPose(new EulerAngle(Math.toRadians(270), 0, 0));
											EntitiesHandler.attachRemoveKey(temp);
										})));
								Firework fire = world.spawn(loc, Firework.class);
								FireworkMeta meta = fire.getFireworkMeta();
								meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), Color.fromRGB(random.nextInt(105)+150, 25, 255)).build());
								fire.setFireworkMeta(meta);
								fire.detonate();
							}
						}.runTaskTimer(plugin, 0, 8);
						new BukkitRunnable() {
							private int ticks;
							
							@Override
							public void run() {
								if (ticks++ > 100 && projectiles.isEmpty()) {
									this.cancel();
									return;
								}
								LivingEntity target = entity.getTarget();
								Iterator<Pair<Slime, ArmorStand>> it = projectiles.iterator();
								while (it.hasNext()) {
									Pair<Slime, ArmorStand> pair = it.next();
									if (!pair.getFirst().isValid() || !pair.getSecond().isValid()) {
										pair.getFirst().remove();
										pair.getSecond().remove();
										it.remove();
										continue;
									}
									Slime slime = pair.getFirst();
									Location slimeLoc = slime.getLocation();
									if (target != null && target.getWorld().equals(slime.getWorld()))
										slime.setVelocity(slime.getVelocity().add(Utils.getVectorTowards(slimeLoc, target.getLocation().add(0, target.getHeight() / 2.0, 0)).multiply(0.1)));
									ArmorStand stand = pair.getSecond();
									stand.teleport(slimeLoc.add(0, -1, 0));
									slimeLoc.subtract(0, 1, 0);
									Vector vec = slime.getVelocity();
									double pivot = Math.max(Math.abs(vec.getX()), Math.abs(vec.getZ()));
									double angle = Math.toDegrees(Math.atan2(Math.abs(vec.getY()), pivot));
									if (vec.getY() >= 0)
										stand.setRightArmPose(new EulerAngle(Math.toRadians(360 - angle), 0, 0));
									else
										stand.setRightArmPose(new EulerAngle(Math.toRadians(360 + angle), 0, 0));
									World slimeWorld = slime.getWorld();
									for (int p=0; p < 3; p++) {
										DustTransition dust = random.nextInt(2) == 0 ? new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat())
												: new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat() / 2f);
										slimeWorld.spawnParticle(Particle.DUST_COLOR_TRANSITION, slimeLoc.getX() + random.nextFloat(-.2f, .2f), slimeLoc.getY() + random.nextFloat(-.2f, .2f), slimeLoc.getZ() + random.nextFloat(-.2f, .2f), 1, 0, 0, 0, 0.001, dust);
									}
									double maxHealth = entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue();
									float explosionPower = entity.getHealth() >= maxHealth / 2.0 ? 1.5f : 2.5f;
									if (slime.isOnGround()) {
										slimeWorld.createExplosion(slimeLoc, explosionPower, false, true, entity);
										stand.remove();
										slime.remove();
										it.remove();
										continue;
									}
									Collection<Entity> nearbyEntities = slime.getNearbyEntities(.5, .5, .5);
									for (Entity e : nearbyEntities) {
										if (e.equals(entity) || e.equals(stand) || e.getType() == EntityType.FIREWORK_ROCKET)
											continue;
										slimeWorld.createExplosion(slimeLoc, explosionPower, false, true, entity);
										stand.remove();
										slime.remove();
										it.remove();
										break;
									}
								}
							}
						}.runTaskTimer(plugin, 0, 1);
					}
				}
			}
		}.runTaskTimer(plugin, 80, 20));
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				World world = entity.getWorld();
				Location entityLoc = entity.getLocation();
				for (int i=0; i < 8; i++) {
					DustTransition dust = random.nextInt(2) == 0 ?
							new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat())
							: new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat() / 2f);
					world.spawnParticle(Particle.DUST_COLOR_TRANSITION, entityLoc.getX() + random.nextFloat(-.5f, .5f), entityLoc.getY() + random.nextFloat(-.5f, .5f), entityLoc.getZ() + random.nextFloat(-.5f, .5f), 1, 0, 0, 0, 0.001, dust);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}
	public void onDamaged(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setCancelled(true);
			return;
		}
		super.onDamaged(event);
	}
	public void wasHit(EntityDamageByEntityEvent event) {
		if (event.getCause() == DamageCause.ENTITY_EXPLOSION && event.getDamager().equals(event.getEntity()))
			event.setCancelled(true);
	}
	public void setAttributes(Rabbit entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public Song getSongTheme() {
		return SongPlayer.Song.EASTER_BOSS;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(EasterBunny.REGISTERED_KEY, EasterBunny.class);
		type.setSpawnConditions(event -> {
			return false;
		});
	}
}