//package com.github.jewishbanana.ultimatecontent.entities.halloweenentities;
//
//import org.bukkit.Color;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.Particle.DustOptions;
//import org.bukkit.Sound;
//import org.bukkit.attribute.Attribute;
//import org.bukkit.entity.ArmorStand;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.LivingEntity;
//import org.bukkit.entity.Slime;
//import org.bukkit.entity.Zombie;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.potion.PotionEffect;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.util.EulerAngle;
//import org.bukkit.util.Vector;
//
//import com.github.jewishbanana.uiframework.entities.UIEntityManager;
//import com.github.jewishbanana.ultimatecontent.entities.AnimatedEntity;
//import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity.CreatureStand;
//import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
//import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
//import com.github.jewishbanana.ultimatecontent.utils.Utils;
//import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;
//
//public class Scarecrow extends AnimatedEntity<Zombie> {
//	
//	public static final String REGISTERED_KEY = "uc:scarecrow";
//	
//	private AnimationHandler throwAnimation;
//	private boolean isThrowing;
//
//	public Scarecrow(Zombie entity) {
//		super(entity, CustomEntityType.SCARECROW);
//		
//		setInvisible(entity);
//		entity.setCanPickupItems(false);
//		entity.setSilent(true);
//		makeEntityBreakDoors(entity);
//		
//		setAnimatedStand(new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
//			initStand(stand);
//			entityVariant.equipEntityWithLoadout(stand);
//			stand.setInvisible(false);
//			if (!entity.isAdult())
//				stand.setSmall(true);
//		}));
//		
//		this.walkAnimation = new AnimationHandler(true, true, true);
//		this.walkAnimation.setContinueFrame(1);
//		this.walkAnimation.setAnimations(this.walkAnimation.new Animation(
//					this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -30, 0, 10, 12, false, 0.5, 0.5),
//					this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 30, 0, -10, 12, false, 0.5, 0.5),
//					this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, 20, 0, 10, 12, false, 0.5, 0.5),
//					this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -20, 0, -10, 12, false, 0.5, 0.5),
//					this.walkAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, 30, 12, false, 0.5, 0.5)
//				),
//				this.walkAnimation.new Animation(
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, -30, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 30, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 20, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, -20, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 30, 0, 0, 0, 12, false, 0.5, 0)
//				),
//				this.walkAnimation.new Animation(
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, 30, 0, 10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, -30, 0, -10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -20, 0, 10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, 20, 0, -10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, -30, 12, false, 0, 0.5)
//				),
//				this.walkAnimation.new Animation(
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 30, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, -30, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, -20, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 20, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, -30, 0, 0, 0, 12, false, 0.5, 0)
//				),
//				this.walkAnimation.new Animation(
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -30, 0, 10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 30, 0, -10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, 20, 0, 10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -20, 0, -10, 12, false, 0, 0.5),
//						this.walkAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, 30, 12, false, 0, 0.5)
//				)
//			);
//		
//		this.throwAnimation = new AnimationHandler(true, true, false);
//		this.throwAnimation.setAnimations(this.throwAnimation.new Animation(
//					this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -10, 0, 5, 8, true, 0.5, 0.5),
//					this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 10, 0, -5, 8, true, 0.5, 0.5),
//					this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -360, 0, 15, 20, false, 0.5, 0.5),
//					this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -10, 0, -5, 8, true, 0.5, 0.5),
//					this.throwAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, 20, 8, true, 0.5, 0.5)
//				),
//				this.throwAnimation.new Animation(
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, 10, 0, 5, 6, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, -10, 0, -5, 6, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -360, 0, 15, 12, false, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, 10, 0, -5, 6, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, -15, 6, true, 0.5, 0.5)
//					),
//				this.throwAnimation.new Animation(
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -10, 0, 5, 4, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 10, 0, -5, 4, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -380, 0, 15, 6, false, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -10, 0, -5, 4, true, 0.5, 0.5),
//						this.throwAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, 0, 0, 0, 15, 4, true, 0.5, 0.5)
//					)
//			);
//		
//		scheduleTask(new BukkitRunnable() {
//			@Override
//			public void run() {
//				if (!entity.isValid())
//					return;
//				if (entity.getVelocity().lengthSquared() > 0.005)
//					walkAnimation.setFrameRate(2);
//				else
//					walkAnimation.setFrameRate(1);
//				if (isThrowing) {
//					throwAnimation.tick(getAnimatedStand().getEntity(entity.getLocation()));
//					if (throwAnimation.isFinished()) {
//						isThrowing = false;
//						if (isTargetInRange(entity, 0, 400))
//							throwPumpkin(entity, entity.getTarget());
//					}
//				}
//			}
//		}.runTaskTimer(plugin, 0, 1));
//		
//		scheduleTask(new BukkitRunnable() {
//			private int cooldown = 8;
//			
//			@Override
//			public void run() {
//				if (!entity.isValid() || getAnimatedStand().getEntityOrNull() == null)
//					return;
//				if (cooldown > 0) {
//					cooldown--;
//					return;
//				}
//				if (getAnimatedStand().getEntityOrNull().getEquipment().getItemInMainHand().getType() == Material.JACK_O_LANTERN && isTargetInRange(entity, 49, 225)) {
//					cooldown = 8;
//					entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 60, 5, true, false, false));
//					Location entityLoc = entity.getLocation();
//					throwAnimation.startAnimation(getAnimatedStand().getEntity(entityLoc));
//					isThrowing = true;
//					playSound(entityLoc, Sound.ENTITY_WITHER_AMBIENT, 2, .5);
//				}
//			}
//		}.runTaskTimer(plugin, 0, 20));
//		
//		makeStepSoundTask(entity, 6, Sound.ENTITY_WITHER_SKELETON_STEP, 1, .5);
//		
//		makeParticleTask(entity, VersionUtils.getBlockDust(), new Vector(0, 1.1, 0), 1, .2, .4, .2, 1, Material.JACK_O_LANTERN.createBlockData());
//	}
//	public void spawn(Entity entity) {
//		super.spawn(entity);
//		if (random.nextInt(5) == 0)
//			((Zombie) entity).setBaby();
//	}
//	public void setAttributes(Zombie entity) {
//		super.setAttributes(entity);
//		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
//	}
//	private void throwPumpkin(Entity shooter, LivingEntity target) {
//		ArmorStand pumpkin = shooter.getWorld().spawn(shooter.getLocation(), ArmorStand.class, stand -> {
//			initStand(stand);
//			stand.getEquipment().setHelmet(new ItemStack(Material.JACK_O_LANTERN));
//		});
//		Slime projectile = shooter.getWorld().spawn(shooter.getLocation(), Slime.class, slime -> {
//			slime.setSize(0);
//			slime.setSilent(true);
//			slime.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
//			slime.setHealth(100.0);
//			slime.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
//			setInvisible(slime);
//			slime.teleport(slime.getLocation().add(0, 1, 0));
//			slime.setGravity(false);
//		});
//		Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0);
//		Vector vec = Utils.getVectorTowards(projectile.getLocation(), targetLoc);
//		vec.setX(vec.getX() / 3);
//		vec.setZ(vec.getZ() / 3);
//		vec.setY((targetLoc.distance(shooter.getLocation()) / 13) + ((targetLoc.getY() - shooter.getLocation().getY()) / 30));
//		getAnimatedStand().getEntity(shooter.getLocation()).getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
//		swingArms();
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() {
//				if (pumpkin == null || pumpkin.isDead() || projectile == null || projectile.isDead() || shooter == null) {
//					if (pumpkin != null)
//						pumpkin.remove();
//					if (projectile != null)
//						projectile.remove();
//					ArmorStand stand = getAnimatedStand().getEntityOrNull();
//					if (stand != null && !stand.isDead())
//						stand.getEquipment().setItemInMainHand(new ItemStack(Material.JACK_O_LANTERN));
//					this.cancel();
//					return;
//				}
//				projectile.teleport(projectile.getLocation().add(vec));
//				pumpkin.teleport(projectile.getLocation().add(0, -2, 0));
//				double pivot = Math.abs(vec.getX());
//				if (Math.abs(vec.getZ()) > pivot)
//					pivot = Math.abs(vec.getZ());
//				double angle = Math.toDegrees(Math.atan2(Math.abs(vec.getY()), pivot));
//				if (vec.getY() >= 0)
//					pumpkin.setHeadPose(new EulerAngle(Math.toRadians(360 - angle), 0, 0));
//				else
//					pumpkin.setHeadPose(new EulerAngle(Math.toRadians(360 + angle), 0, 0));
//				pumpkin.getWorld().spawnParticle(VersionUtils.getBlockCrack(), pumpkin.getLocation().add(0,2,0), 2, .1, .1, .1, 1, Material.JACK_O_LANTERN.createBlockData());
//				if (!projectile.getLocation().getBlock().isPassable()) {
//					pumpkin.remove();
//					projectile.remove();
//					ArmorStand stand = getAnimatedStand().getEntityOrNull();
//					if (stand != null && !stand.isDead())
//						stand.getEquipment().setItemInMainHand(new ItemStack(Material.JACK_O_LANTERN));
//					projectile.getWorld().createExplosion(projectile.getLocation(), 4f, true, true, shooter);
//					this.cancel();
//					new BukkitRunnable() {
//						private Location explosion = projectile.getLocation();
//						private int tick = 20;
//						
//						@Override
//						public void run() {
//							explosion.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), explosion, 30, 2, 2, 2, .001, new DustOptions(Color.ORANGE, 1f));
//							explosion.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), explosion, 30, 2, 2, 2, .001, new DustOptions(Color.BLACK, 1f));
//							if (--tick <= 0)
//								this.cancel();
//						}
//					}.runTaskTimer(plugin, 0, 2);
//					return;
//				}
//				vec.multiply(0.99);
//				vec.setY(vec.getY() - 0.05000000074505806D);
//				for (Entity e : projectile.getNearbyEntities(.5, .5, .5))
//					if (e instanceof LivingEntity && !(e instanceof ArmorStand) && !e.equals(shooter) && !EntityUtils.isEntityImmunePlayer(e)) {
//						pumpkin.remove();
//						projectile.remove();
//						ArmorStand stand = getAnimatedStand().getEntityOrNull();
//						if (stand != null && !stand.isDead())
//							stand.getEquipment().setItemInMainHand(new ItemStack(Material.JACK_O_LANTERN));
//						projectile.getWorld().createExplosion(projectile.getLocation(), 4f, true, true, shooter);
//						this.cancel();
//						new BukkitRunnable() {
//							private Location explosion = projectile.getLocation();
//							private int tick = 20;
//							
//							@Override
//							public void run() {
//								explosion.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), explosion, 30, 2, 2, 2, .001, new DustOptions(Color.ORANGE, 1f));
//								explosion.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), explosion, 30, 2, 2, 2, .001, new DustOptions(Color.BLACK, 1f));
//								if (--tick <= 0)
//									this.cancel();
//							}
//						}.runTaskTimer(plugin, 0, 2);
//						return;
//					}
//			}
//		}.runTaskTimer(plugin, 0, 1);
//	}
//	public static void register() {
//		UIEntityManager type = UIEntityManager.registerEntity(Scarecrow.REGISTERED_KEY, Scarecrow.class);
//		
//		type.setSpawnConditions(event -> {
//			return false;
//		});
//	}
//}
