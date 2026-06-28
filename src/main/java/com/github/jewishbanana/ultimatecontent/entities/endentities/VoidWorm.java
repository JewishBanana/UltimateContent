package com.github.jewishbanana.ultimatecontent.entities.endentities;

import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.PhysicsEngine;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidWorm extends ComplexEntity<Silverfish> {
	
	public static final String REGISTERED_KEY = "uc:void_worm";
	
	private boolean attackAnimation;
	private int cooldown;
	private WitherSkeleton damageEntity;
	private Vector[] offsets = new Vector[11];
	private BukkitTask attackTask;
	// Spleef tracking: the worm cannot be attacked, so a player "kills" it by breaking the block beneath it and dropping it
	// into the void. We remember who knocked it loose and when; the credit only stands if it dies to the void before either
	// the 10s window elapses or it lands on solid ground again.
	private UUID spleefTracker;
	private long spleefTime;
	private boolean spleefLeftGround;

	public VoidWorm(Silverfish entity) {
		super(entity, CustomEntityType.VOID_WORM, false);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setInvulnerable(true);
		entity.setCollidable(false);
		entity.setAI(true);
		
		Consumer<ArmorStand> consumer = (stand) -> {
			initStand(stand);
			stand.getEquipment().setHelmet(new ItemStack(Material.CRYING_OBSIDIAN));
			stand.setRotation(random.nextInt(90), 0);
		};
		initStands(
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer)
				);

		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location loc = entity.getLocation();
				Block block = loc.getBlock().getRelative(BlockFace.DOWN);
				if (block == null || block.isPassable())
					return;
				if (!attackAnimation)
					entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), loc.getX(), block.getY()+1.1, loc.getZ(), 5, .2, .2, .2, 0.1, block.getBlockData());
				else
					entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), loc.getX(), block.getY()+1.1, loc.getZ(), 40, .4, .3, .4, 0.1, block.getBlockData());
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				playSound(entity.getLocation(), Sound.BLOCK_CHORUS_FLOWER_DEATH, .15f, .5f);
			}
		}.runTaskTimer(plugin, 0, 5));
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (cooldown > 0)
					cooldown--;
			}
		}.runTaskTimer(plugin, 0, 20));
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (spleefTracker == null)
					return;
				if (!entity.isValid()) {
					// Invulnerable entities removed out-of-world never fire EntityDeathEvent; award here instead.
					if (spleefLeftGround && System.currentTimeMillis() - spleefTime <= 10000) {
						DependencyUtils.awardAchievementProgress(spleefTracker, "mobs.slayer.void_mobs", 1, -1);
						DependencyUtils.awardAchievementProgress(spleefTracker, "master.series.void_master", 1, 4);
					}
					spleefTracker = null;
					return;
				}
				if (System.currentTimeMillis() - spleefTime > 10000) {
					spleefTracker = null;
					return;
				}
				if (!entity.isOnGround())
					spleefLeftGround = true;
				else if (spleefLeftGround)
					spleefTracker = null;
			}
		}.runTaskTimer(plugin, 1, 1));
	}
	/** Flags this worm as knocked loose by a player who broke the block beneath it (see {@link EntitiesHandler} block break). */
	public void markSpleefed(UUID player) {
		this.spleefTracker = player;
		this.spleefTime = System.currentTimeMillis();
		this.spleefLeftGround = false;
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		Silverfish entity = (Silverfish) event.getDamager();
		if (entity.hasAI())
			event.setCancelled(true);
		if (attackAnimation || cooldown > 0 || !event.getDamager().isOnGround())
			return;
		entity.setAI(false);
		attackAnimation = true;
		final Location start = entity.getLocation();
		playSound(start, Sound.BLOCK_GRAVEL_BREAK, 1f, .5f);
		playSound(start, Sound.BLOCK_NETHER_SPROUTS_BREAK, 1f, .5f);
		playSound(start, Sound.ENTITY_HOGLIN_DEATH, 1f, .5f);
		BlockData bd = entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getBlockData();
		for (int i=0; i < offsets.length; i++)
			offsets[i] = new Vector();
		VoidWorm instance = this;
		attackTask = new BukkitRunnable() {
			private int index, biteTicks;
			private Location loc = start.clone().add(0,0.5,0);
			private Vector velocity = new Vector(0, .38, 0);
			private EvokerFangs fangs;
			private final double bodyGapDistance = 0.58;
			private double distanceSinceSpawn = bodyGapDistance;
			private boolean reverse;
			
			@Override
			public void run() {
				for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0, e ->
						!e.equals(entity) &&
						e instanceof LivingEntity &&
						!(e instanceof ArmorStand) &&
						!EntityUtils.isEntityImmunePlayer(e)))
					e.setVelocity(Utils.getVectorTowards(e.getLocation(), loc).multiply(0.3));
				if (!reverse) {
					start.getWorld().spawnParticle(VersionUtils.getBlockDust(), start, 5, .4, .4, .4, 0.1, bd);
					if (fangs != null)
						fangs.remove();
					fangs = start.getWorld().spawn(loc.clone().add(offsets[0]), EvokerFangs.class, (e) -> e.setSilent(true));
					loc.add(velocity);
					if (index != 10 && (index == 0 || distanceSinceSpawn >= bodyGapDistance))
						do {
							distanceSinceSpawn -= bodyGapDistance;
							getCreatureStandEntity(index, start.clone().subtract(random.nextDouble(-0.2, 0.2), 2, random.nextDouble(-0.2, 0.2)));
							index++;
							if (index == 5)
								damageEntity = start.getWorld().spawn(start, WitherSkeleton.class, false, (e) -> {
									setInvisible(e);
									e.setSilent(true);
									e.setAI(false);
									e.setGravity(false);
									EntitiesHandler.voidWormEntities.put(e.getUniqueId(), instance);
									EntitiesHandler.attachRemoveKey(e);
								});
						} while (distanceSinceSpawn >= bodyGapDistance);
					for (int i=0; i < index; i++) {
						Entity stand = getCreatureStandEntity(i, loc);
						Location standLoc = stand.getLocation();
						stand.teleport(new Location(start.getWorld(), start.getX(), start.getY() + (standLoc.getY() - start.getY()), start.getZ(), standLoc.getYaw(), 0).add(velocity).add(offsets[i+1]));
						stand.getWorld().spawnParticle(VersionUtils.getBlockDust(), standLoc.add(0,1.5,0), 5, .2, .2, .2, 0.1, bd);
					}
					distanceSinceSpawn += velocity.getY();
					velocity.multiply(0.95);
					if (velocity.lengthSquared() < 0.01) {
						reverse = true;
						velocity.zero();
						fangs.setSilent(false);
						EntitiesHandler.voidWormFangs.put(fangs.getUniqueId(), instance);
						loc = getCreatureStandEntity(0, loc).getLocation().add(0,2.5,0);
					}
					return;
				}
				for (int i=index-1; i >= 0; i--) {
					Entity stand = getCreatureStandEntity(i, loc);
					Location standLoc = stand.getLocation();
					Location to = new Location(start.getWorld(), start.getX(), start.getY() + (standLoc.getY() - start.getY()), start.getZ(), standLoc.getYaw(), 0).add(velocity).add(offsets[i+1]);
					if (to.getY() + 2 < start.getY()) {
						stand.remove();
						index--;
						if (index == 5 && damageEntity != null) {
							EntitiesHandler.voidWormEntities.remove(damageEntity.getUniqueId());
							damageEntity.remove();
						}
						continue;
					}
					stand.teleport(to);
				}
				if (biteTicks != 16) {
					biteTicks++;
					start.getWorld().spawnParticle(VersionUtils.getBlockDust(), start, 1, .2, .2, .2, 0.1, bd);
					return;
				}
				start.getWorld().spawnParticle(VersionUtils.getBlockDust(), start, 5, .4, .4, .4, 0.1, bd);
				if (fangs != null)
					fangs.remove();
				fangs = start.getWorld().spawn(loc.clone().add(offsets[0]), EvokerFangs.class, (e) -> e.setSilent(true));
				loc.add(velocity);
				if (loc.getY() < start.getY()) {
					this.cancel();
					fangs.remove();
					attackAnimation = false;
					cooldown = 6;
					Silverfish entity = getCastedEntity();
					if (entity != null)
						entity.setAI(true);
					for (int i=0; i < getStandCount(); i++) {
						Entity stand = getCreatureStandEntityOrNull(i);
						stand.remove();
					}
					return;
				}
				Entity temp = getCreatureStandEntityOrNull(0);
				if (temp != null && !temp.isDead())
					loc = temp.getLocation().add(0, 3.5, 0);
				velocity.setY(velocity.getY() - 0.01);
			}
		}.runTaskTimer(plugin, 5, 1);
		final Vector[] velocities = new Vector[11];
		for (int i=0; i < velocities.length; i++)
			velocities[i] = Utils.getRandomizedVector(1f, 0.01f, 1f).multiply(0.05);
		final float[] maxVelocities = new float[11];
		for (int i=0; i < maxVelocities.length; i++)
			maxVelocities[i] = random.nextFloat(5, 10);
		scheduleTask(new BukkitRunnable() {
			private int frame;
			private boolean flipped;
			@Override
			public void run() {
				if (!attackAnimation) {
					this.cancel();
					return;
				}
				for (int i=0; i < offsets.length; i++)
					offsets[i].add(velocities[i].clone().multiply(Utils.calculateAnimationValue(frame, 10, 0.01, !flipped ? 0.01 : 0.005, maxVelocities[i])));
				if (++frame == 10) {
					frame = 0;
					if (flipped) {
						flipped = false;
						for (int i=0; i < offsets.length; i++)
							offsets[i] = new Vector();
						for (int i=0; i < velocities.length; i++) {
							velocities[i] = Utils.getRandomizedVector(1f, 0.01f, 1f).multiply(0.05);
							maxVelocities[i] = random.nextFloat(5, 10);
						}
						return;
					}
					flipped = true;
					for (int i=0; i < velocities.length; i++)
						velocities[i].multiply(-1);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation(), 10, .3, .3, .3, .03);
		Player killer = event.getEntity().getKiller();
		if (killer != null)
			DependencyUtils.awardAchievementProgress(killer.getUniqueId(), "mobs.slayer.void_mobs", 1, -1);
		// "Burrow A Grave" mastery tier: the worm is unattackable, so the only kill is spleefing its supporting block and
		// dropping it into the void. Credit the spleefer if it died to the void within the tracking window (see markSpleefed).
		EntityDamageEvent lastCause = event.getEntity().getLastDamageCause();
		if (spleefTracker != null && lastCause != null && lastCause.getCause() == EntityDamageEvent.DamageCause.VOID
				&& System.currentTimeMillis() - spleefTime <= 10000) {
			DependencyUtils.awardAchievementProgress(spleefTracker, "mobs.slayer.void_mobs", 1, -1);
			DependencyUtils.awardAchievementProgress(spleefTracker, "master.series.void_master", 1, 4);
			spleefTracker = null;
		}
		if (attackTask != null)
			attackTask.cancel();
		int lifeTicks = (int) (getSectionDouble("ragdollSeconds", 7.0) * 20.0);
		for (int i=0; i < getStandCount(); i++) {
			Entity stand = getCreatureStandEntityOrNull(i);
			if (stand != null)
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(0,1.5,0), Material.CRYING_OBSIDIAN, 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
		}
	}
	public void onChangeBlock(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		if (damageEntity != null) {
			EntitiesHandler.voidWormEntities.remove(damageEntity.getUniqueId());
			damageEntity.remove();
		}
	}
	public void setAttributes(Silverfish entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
		entity.getAttribute(VersionUtils.getAttackDamageAttribute()).setBaseValue(0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidWorm.REGISTERED_KEY, VoidWorm.class);
		
		type.setSpawnConditions(event -> {
			if (!CustomEntityType.VOID_WORM.isWorldSpawnable(event.getLocation().getWorld()))
				return false;
			return true;
		});
	}
}
