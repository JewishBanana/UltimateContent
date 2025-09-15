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
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer.Song;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
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
					playSound(entity.getLocation(), Sound.ENTITY_SNOW_GOLEM_SHOOT, 2, .5);
					int count = entity.getNearbyEntities(30, 30, 30).stream().filter(temp -> temp.hasMetadata("uc-christmasmobs")).count() < 10 ? 5 : 2;
					Location entityLoc = entity.getLocation();
					spawnPresent(entityLoc, random.nextInt(count), new Vector(0.5, 0.8, 0.0), entity, entity.getTarget());
					spawnPresent(entityLoc, random.nextInt(count), new Vector(-0.5, 0.8, 0.5), entity, entity.getTarget());
					spawnPresent(entityLoc, random.nextInt(count), new Vector(-0.5, 0.8, -0.5), entity, entity.getTarget());
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		BlockData snowData = Material.SNOW.createBlockData();
		BlockData redWool = Material.RED_WOOL.createBlockData();
		makeParticleTask(entity, 1, () -> {
			if (random.nextInt(4) != 0)
				return;
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, 1.2, 0), 1, .3, .5, .3, 1, snowData);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, 1.2, 0), 1, .3, .5, .3, 1, redWool);
		});
	}
	public void spawnPresent(Location location, int type, Vector direction, Entity source, LivingEntity target) {
		ArmorStand stand = location.getWorld().spawn(location.clone().add(0, 100, 0), ArmorStand.class, temp -> {
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
		Slime slime = location.getWorld().spawn(location.clone().add(0, 100, 0), Slime.class, temp -> {
			temp.setSize(0);
			temp.setSilent(true);
			temp.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
			temp.setHealth(100.0);
			temp.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
			temp.setInvisible(true);
			temp.teleport(location);
			temp.teleport(temp.getLocation().add(0, 1, 0));
			temp.setVelocity(direction);
			EntitiesHandler.attachRemoveKey(temp);
		});
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
				stand.getWorld().spawnParticle(VersionUtils.getBlockCrack(), stand.getLocation().add(0,2,0), 2, .1, .1, .1, 1, trail);
				if (slime.isOnGround()) {
					stand.getWorld().spawnParticle(VersionUtils.getBlockCrack(), stand.getLocation().add(0,2,0), 30, .7, .7, .7, 1, trail);
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
						World world = loc.getWorld();
						Map<Block, BlockState> patches = new HashMap<>();
						new BukkitRunnable() {
							private int tick;
							private int distance;
							private BlockData ice_data = Material.PACKED_ICE.createBlockData();
							
							@Override
							public void run() {
								if (distance % 2 == 0 && tick <= 5) {
									for (Block temp : Utils.getBlocksInCircleCircumference(loc, tick)) {
										Block b = Utils.getHighestExposedBlock(temp, 3);
										if (b == null || patches.containsKey(b) || DependencyUtils.isBlockProtected(b))
											continue;
										patches.put(b, b.getState());
										b.setType(Material.PACKED_ICE);
										world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,1,.5), 5, .4, .3, .4, 1, ice_data);
									}
									world.playSound(loc, Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.6f, .5f);
									tick++;
								}
								for (Entry<Block, BlockState> entry : patches.entrySet()) {
									Block b = entry.getKey();
									if (b.getType() == Material.PACKED_ICE) {
										if (random.nextInt(5) == 0)
											world.spawnParticle(Particle.FALLING_DUST, b.getLocation().add(.5,1,.5), 1, .3, .1, .3, 1, ice_data);
										for (Entity e : world.getNearbyEntities(b.getLocation().add(.5,1.5,.5), .5, .5, .5)) {
											if (e.hasMetadata("uc-christmasmobs"))
												continue;
											Vector vec = new Vector(e.getVelocity().getX()/8, e.getVelocity().getY(), e.getVelocity().getZ()/8);
											if (vec.getY() > 0)
												vec.setY(vec.getY()/8);
											e.setVelocity(vec);
											e.setFreezeTicks(220);
											if (e instanceof Player && distance % 5 == 0)
												world.playSound(e.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.BLOCKS, 0.2f, .5f);
										}
									}
								}
								if (distance >= 200) {
									for (Entry<Block, BlockState> entry : patches.entrySet())
										if (entry.getKey().getType() == Material.PACKED_ICE)
											entry.getValue().update(true);
									this.cancel();
								}
								distance++;
							}
						}.runTaskTimer(plugin, 0, 1);
						break;
					default:
					case 0:
						slime.getWorld().createExplosion(slime.getLocation(), 3.5f, true, true, source);
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
