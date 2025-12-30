package com.github.jewishbanana.ultimatecontent.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.UndeadMiner;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.SoundEffect;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.goal.WrappedPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public abstract class BaseEntity<T extends Entity> extends CustomEntity<T> {
	
	protected static final JavaPlugin plugin;
	protected static final RandomGenerator random;
	private static final NamespacedKey tameOwner;
	protected static final boolean IS_VERSION_19_OR_ABOVE;
	protected static final BlockData infestedEntityParticles;
	static {
		plugin = Main.getInstance();
		random = RandomGenerator.of("SplittableRandom");
		tameOwner = new NamespacedKey(plugin, "uc-tameowner");
		IS_VERSION_19_OR_ABOVE = VersionUtils.isMCVersionOrAbove("1.19");
		infestedEntityParticles = VersionUtils.isMCVersionOrAbove("1.19") ? Material.SCULK.createBlockData() : Material.NETHERRACK.createBlockData();
	}
	
	protected final CustomEntityType entityType;
	protected final EntityVariant entityVariant;
	
	private UUID owner;
	private boolean dropsItemsOnDeath = true;
	private final Variant exactVariant;
	
	public BaseEntity(T entity, CustomEntityType type) {
		super(entity);
		this.entityType = type;
		this.exactVariant = type.determineVariant(entity);
		this.entityVariant = exactVariant != null ? exactVariant.getEntityVariant() : type.normalVariant;
		setAttributes(entity);
		if (entityVariant.ambientSounds != null) {
			final int[] timer = {random.nextInt(entityVariant.ambientSoundFrequency)+entityVariant.ambientSoundFrequency};
			scheduleTask(new BukkitRunnable() {
				@Override
				public void run() {
					if (--timer[0] == 0) {
						timer[0] = random.nextInt(entityVariant.ambientSoundFrequency)+entityVariant.ambientSoundFrequency;
						playRandomSoundEffect(entityVariant.ambientSounds);
					}
				}
			}.runTaskTimer(plugin, 0, 20));
		}
		String ownerID = entity.getPersistentDataContainer().get(tameOwner, PersistentDataType.STRING);
		if (ownerID != null)
			setOwner(owner);
	}
	@SuppressWarnings("unchecked")
	public void spawn(Entity entity) {
		if (entity instanceof LivingEntity alive) {
			if (alive instanceof Mob mob) {
				setAIGoals((T) mob);
				EntityEquipment equipment = alive.getEquipment();
				equipment.setItemInMainHandDropChance(0);
				equipment.setItemInOffHandDropChance(0);
				equipment.setHelmetDropChance(0);
				equipment.setChestplateDropChance(0);
				equipment.setLeggingsDropChance(0);
				equipment.setBootsDropChance(0);
			}
			if (shouldEquipBaseEntity())
				entityVariant.equipEntityWithLoadout(alive);
		}
	}
	public void setAIGoals(T entity) {
	}
	public void onDeath(EntityDeathEvent event) {
		if (!dropsItemsOnDeath)
			event.getDrops().clear();
		else {
			Set<ItemStack> slots = new HashSet<>();
			EntityEquipment equipment = event.getEntity().getEquipment();
			slots.addAll(Arrays.asList(equipment.getArmorContents()));
			slots.add(equipment.getItemInMainHand());
			slots.add(equipment.getItemInOffHand());
			Iterator<ItemStack> iterator = event.getDrops().iterator();
			while (iterator.hasNext())
				if (!slots.contains(iterator.next()))
					iterator.remove();
			entityVariant.populateDropList(event.getDrops());
		}
		playRandomSoundEffect(entityVariant.deathSounds);
	}
	public void onDamaged(EntityDamageEvent event) {
		playRandomSoundEffect(entityVariant.hurtSounds);
	}
	public void unload() {
		super.unload();
		PathfindersHandler.removePathfinders(this);
	}
	public void setAttributes(T entity) {
		if (entity instanceof LivingEntity) {
			LivingEntity alive = (LivingEntity) entity;
			AttributeInstance instance = null;
			if (entityVariant.health > 0) {
				instance = alive.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				if (instance != null)
					instance.setBaseValue(entityVariant.health);
			}
			if (entityVariant.damage >= 0) {
				instance = alive.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
				if (instance != null)
					instance.setBaseValue(entityVariant.damage);
			}
			if (entityVariant.knockback >= 0) {
				instance = alive.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
				if (instance != null)
					instance.setBaseValue(entityVariant.knockback);
			}
			if (entityVariant.movementSpeed >= 0) {
				instance = alive.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
				if (instance != null)
					instance.setBaseValue(entityVariant.movementSpeed);
			}
		}
		entity.setCustomNameVisible(entityVariant.nameVisible);
	}
	public UUID getOwner() {
		return owner;
	}
	public void setOwner(UUID uuid) {
		if (!(this instanceof TameableEntity instance))
			return;
		Entity entity = getEntity();
		if (entity == null)
			return;
		if (uuid == null) {
			entity.getPersistentDataContainer().remove(tameOwner);
			setAIGoals(getCastedEntity());
			this.owner = null;
			return;
		}
		if (!entity.getPersistentDataContainer().has(tameOwner, PersistentDataType.STRING))
			entity.getPersistentDataContainer().set(tameOwner, PersistentDataType.STRING, uuid.toString());
		this.owner = uuid;
		if (entity instanceof Mob mob)
			instance.setOwner(instance, mob);
	}
	private void playSoundEffect(SoundEffect effect) {
		Entity entity = getEntity();
		if (entity != null)
			entity.getWorld().playSound(entity.getLocation(), effect.getSound(), getSoundCategory(), effect.getVolume(), effect.getPitch());
	}
	private void playRandomSoundEffect(SoundEffect[] effects) {
		if (effects != null)
			playSoundEffect(effects[random.nextInt(effects.length)]);
	}
	public void playSound(Location location, Sound sound, double volume, double pitch) {
		location.getWorld().playSound(location, sound, getSoundCategory(), (float) (volume * entityVariant.volume), (float) pitch);
	}
	public void makeParticleTask(Entity entity, int ticks, Particle particle, Vector offset, int count, double dx, double dy, double dz, double speed) {
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				entity.getWorld().spawnParticle(particle, entity.getLocation().add(offset), count, dx, dy, dz, speed);
			}
		}.runTaskTimerAsynchronously(plugin, 0, ticks));
	}
	public void makeParticleTask(Entity entity, Particle particle, Vector offset, int count, double dx, double dy, double dz, double speed) {
		makeParticleTask(entity, 1, particle, offset, count, dx, dy, dz, speed);
	}
	public void makeParticleTask(Entity entity, int ticks, Particle particle, int count, double dx, double dy, double dz, double speed) {
		makeParticleTask(entity, ticks, particle, new Vector(), count, dx, dy, dz, speed);
	}
	public void makeParticleTask(Entity entity, Particle particle, int count, double dx, double dy, double dz, double speed) {
		makeParticleTask(entity, 1, particle, count, dx, dy, dz, speed);
	}
	public <K> void makeParticleTask(Entity entity, int ticks, Particle particle, Vector offset, int count, double dx, double dy, double dz, double speed, K data) {
		final Vector finalOffset = offset != null ? offset : new Vector();
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				entity.getWorld().spawnParticle(particle, entity.getLocation().add(finalOffset), count, dx, dy, dz, speed, data);
			}
		}.runTaskTimerAsynchronously(plugin, 0, ticks));
	}
	public <K> void makeParticleTask(Entity entity, Particle particle, Vector offset, int count, double dx, double dy, double dz, double speed, K data) {
		makeParticleTask(entity, 1, particle, offset, count, dx, dy, dz, speed, data);
	}
	public void makeParticleTask(Entity entity, int ticks, Runnable runnable) {
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				runnable.run();
			}
		}.runTaskTimerAsynchronously(plugin, 0, ticks));
	}
	public void makeStepSoundTask(Entity entity, double distanceSquared, Sound sound, double volume, double pitch) {
		scheduleTask(new BukkitRunnable() {
			private Location step = entity.getLocation();
			
			@Override
			public void run() {
				if (!entity.isValid() || step.distanceSquared(entity.getLocation()) < distanceSquared)
					return;
				step = entity.getLocation();
				playSound(step, sound, volume, pitch);
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}
	public void setInvisible(LivingEntity entity) {
		entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0, true, false));
	}
	public void makeEntityBreakDoors(Zombie entity) {
		if (IS_VERSION_19_OR_ABOVE)
			entity.setCanBreakDoors(true);
	}
	public static boolean isTargetInRange(Mob mob, double minSquared, double maxSquared, boolean lineOfSight) {
		LivingEntity target = mob.getTarget();
		if (target == null || target.isDead() || !target.getWorld().equals(mob.getWorld()) || (lineOfSight && !mob.hasLineOfSight(target)))
			return false;
		double distance = target.getLocation().distanceSquared(mob.getLocation());
		return distance >= minSquared && distance <= maxSquared;
	}
	public static boolean isTargetInRange(Mob mob, double minSquared, double maxSquared) {
		return isTargetInRange(mob, minSquared, maxSquared, true);
	}
	public static void initStand(ArmorStand stand) {
		stand.setInvisible(true);
		stand.setInvulnerable(true);
		stand.setArms(true);
		stand.setGravity(false);
		stand.setMarker(true);
		stand.setCollidable(false);
		stand.setBasePlate(false);
		stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HAND, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
		EntitiesHandler.attachRemoveKey(stand);
	}
	public Location getEntityLocation() {
		Entity entity = getEntity();
		return entity != null ? entity.getLocation() : null;
	}
	public SoundCategory getSoundCategory() {
		return SoundCategory.HOSTILE;
	}
	public EntityVariant getEntityVariant() {
		return entityVariant;
	}
	public int getSectionInteger(String value, int defaultValue) {
		return DataUtils.getConfigInt(entityVariant.configPath+value, defaultValue);
	}
	public double getSectionDouble(String value, double defaultValue) {
		return DataUtils.getConfigDouble(entityVariant.configPath+value, defaultValue);
	}
	public boolean getSectionBoolean(String value, boolean defaultValue) {
		return DataUtils.getConfigBoolean(entityVariant.configPath+value, defaultValue);
	}
	public String getSectionString(String value, String defaultValue) {
		return DataUtils.getConfigString(entityVariant.configPath+value, defaultValue);
	}
	public String getDisplayName() {
		return entityVariant.displayName;
	}
	public boolean dropsItemsOnDeath() {
		return dropsItemsOnDeath;
	}
	public void setDropsItemsOnDeath(boolean dropsItemsOnDeath) {
		this.dropsItemsOnDeath = dropsItemsOnDeath;
	}
	public CustomEntityType getEntityType() {
		return entityType;
	}
	@SuppressWarnings("unchecked")
	public <K extends Variant> K getEntityVariant(Class<K> variantClass) {
		return (K) exactVariant;
	}
	public boolean shouldEquipBaseEntity() {
		return true;
	}
	public static void readEntityGoals(Mob entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		List<WrappedPathfinder> targetGoals = new ArrayList<>(brain.getTargetAI());
	    targetGoals.sort(Comparator.comparingInt(WrappedPathfinder::getPriority));
	    for (WrappedPathfinder goal : targetGoals)
	        Bukkit.broadcastMessage(goal.getPriority() + " target " + goal.getPathfinder().getName());
	    List<WrappedPathfinder> goalGoals = new ArrayList<>(brain.getGoalAI());
	    goalGoals.sort(Comparator.comparingInt(WrappedPathfinder::getPriority));
	    for (WrappedPathfinder goal : goalGoals)
	        Bukkit.broadcastMessage(goal.getPriority() + " goal " + goal.getPathfinder().getName());
	}
	public static void cleanAllEntities() {
		UndeadMiner.clearPlacedBlocks();
	}
}
