package com.github.jewishbanana.ultimatecontent.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderAnimalAttackTarget;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderPlagueAggressionTarget;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class EntityUtils {
	
	private static final JavaPlugin plugin;
	private static Set<Material> leatherArmor;
	private static final FixedMetadataValue fallingBlockData;
	private static final FixedMetadataValue damageData;
	private static final boolean isVersion192OrAbove;
	private static final Map<UUID, DrownedGoalOverride> drownedGoalOverrides = new HashMap<>();
	private static final Map<UUID, PlagueGoalOverride> plagueGoalOverrides = new HashMap<>();
	static {
		plugin = UltimateContent.getInstance();
		leatherArmor = Set.of(Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET);
		fallingBlockData = new FixedMetadataValue(plugin, "protected");
		damageData = UltimateContent.getFixedMetadata();
		isVersion192OrAbove = VersionUtils.isMCVersionOrAbove("1.19.2");
	}
	
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem, boolean silent, Sound playerHurtSound) {
		if (entity == null || entity.isDead())
			return false;
		EntityDamageEvent event = source == null ? new EntityDamageEvent(entity, cause, damage) : new EntityDamageByEntityEvent(source, entity, cause, damage);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		entity.setLastDamageCause(event);
		if (entity.getHealth()-damage <= 0) {
			if (!ignoreTotem && (entity.getEquipment().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING || entity.getEquipment().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING)) {
				if (event instanceof EntityDamageByEntityEvent damageEntityEvent)
					entity.damage(1, damageEntityEvent.getDamager());
				else
					entity.damage(1);
				return true;
			}
			if (meta != null)
				entity.setMetadata(meta, damageData);
			entity.setHealth(0);
			playDamageEffect(entity);
			if (!silent && !entity.isSilent())
				playEntityHarmSound(entity, playerHurtSound);
			if (meta != null)
				entity.removeMetadata(meta, plugin);
			return true;
		}
		entity.setHealth(Math.min(Math.max(entity.getHealth()-damage, 0), entity.getHealth()));
		playDamageEffect(entity);
		if (!silent && !entity.isSilent())
			playEntityHarmSound(entity, playerHurtSound);
		if (event instanceof EntityDamageByEntityEvent damageEntityEvent && entity instanceof Mob mob && damageEntityEvent.getDamager() instanceof LivingEntity livingDamager)
			mob.setTarget(livingDamager);
		return true;
	}
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem, boolean silent) {
		return pureDamageEntity(entity, damage, meta, cause, source, ignoreTotem, silent, Sound.ENTITY_PLAYER_HURT);
	}
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem) {
		return pureDamageEntity(entity, damage, meta, cause, source, ignoreTotem, false);
	}
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source) {
		return pureDamageEntity(entity, damage, meta, cause, source, false);
	}
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause) {
		return pureDamageEntity(entity, damage, meta, cause, null);
	}
	public static void damageArmor(LivingEntity entity, double damage) {
		int dmg = Math.max((int) (damage + 4 / 4), 1);
		for (ItemStack armor : entity.getEquipment().getArmorContents()) {
			if (armor == null || armor.getItemMeta() == null)
				continue;
			ItemMeta meta = armor.getItemMeta();
			if (((Damageable) meta).getDamage() >= armor.getType().getMaxDurability()) armor.setAmount(0);
			else ((Damageable) meta).setDamage(((Damageable) meta).getDamage()+dmg);
			armor.setItemMeta(meta);
		}
	}
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem, boolean silent, Sound playerHurtSound) {
		final double armor = entity.getAttribute(VersionUtils.getArmorAttribute()).getValue();
		final double toughness = entity.getAttribute(VersionUtils.getArmorToughnessAttribute()).getValue();
		final double actualDamage = damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25);
		if (pureDamageEntity(entity, actualDamage, meta, cause, source, ignoreTotem, silent, playerHurtSound)) {
			damageArmor(entity, actualDamage);
			return true;
		}
		return false;
	}
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem, boolean silent) {
		return damageEntity(entity, damage, meta, cause, source, ignoreTotem, silent, Sound.ENTITY_PLAYER_HURT);
	}
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source, boolean ignoreTotem) {
		return damageEntity(entity, damage, meta, cause, source, ignoreTotem, false);
	}
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, Entity source) {
		return damageEntity(entity, damage, meta, cause, source, false);
	}
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause) {
		return damageEntity(entity, damage, meta, cause, null);
	}
	@SuppressWarnings("deprecation")
	public static void playDamageEffect(LivingEntity entity) {
		if (VersionUtils.usingNewDamageEvent)
			entity.playHurtAnimation(0);
		else
			entity.playEffect(EntityEffect.HURT);
	}
	public static void markFallingBlock(FallingBlock block) {
		block.setMetadata("uc-fb", fallingBlockData);
	}
	public static void lockArmorStand(ArmorStand stand) {
		stand.addEquipmentLock(EquipmentSlot.CHEST, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.FEET, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HEAD, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.LEGS, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.OFF_HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
	}
	public static boolean rayTraceEntityConeForSolid(Entity entity, Location initial) {
		double height = entity.getHeight(), width = entity.getWidth();
		Location target = entity.getLocation().add(0,height/2.0,0);
		if (BlockUtils.rayTraceForSolid(initial, target))
			return true;
		if (BlockUtils.rayTraceForSolid(initial, target.add(0,height/2.0,0)))
			return true;
		if (BlockUtils.rayTraceForSolid(initial, target.clone().subtract(0,height/2.0,0)))
			return true;
		Vector angle = Utils.getVectorTowards(initial, target);
		try {
			angle.checkFinite();
		} catch (IllegalArgumentException err) {
			return false;
		}
		if (BlockUtils.rayTraceForSolid(initial, target.clone().add(new Vector(angle.getZ(), 0, -angle.getX()).normalize().multiply(width/2.0))))
			return true;
		if (BlockUtils.rayTraceForSolid(initial, target.clone().add(new Vector(-angle.getZ(), 0, angle.getX()).normalize().multiply(width/2.0))))
			return true;
		return false;
	}
	public static void makeEntityFaceLocation(Entity entity, Location to) {
		Vector dirBetweenLocations = to.toVector().subtract(entity.getLocation().toVector());
		entity.teleport(entity.getLocation().setDirection(dirBetweenLocations));
    }
	public static EquipmentSlot getEquipmentSlot(EntityEquipment inventory, ItemStack item) {
		for (EquipmentSlot slot : EquipmentSlot.values())
			if (inventory.getItem(slot).equals(item))
				return slot;
		return null;
	}
	public static boolean isPlayerImmune(Player player) {
		GameMode mode = player.getGameMode();
		return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
	}
	public static float getBodyRotation(Mob entity) {
		if (entity == null)
			return 0.0f;
		if (!entity.isValid() || entity.isDead())
			return entity.getLocation().getYaw();
		try {
			return BukkitBrain.getBrain(entity).getBody().getBodyRotation();
		} catch (Throwable ignored) {
			return entity.getLocation().getYaw();
		}
	}
	public static void setBodyRotation(Mob entity, float rotation) {
		if (entity == null || !entity.isValid() || entity.isDead())
			return;
		try {
			BukkitBrain.getBrain(entity).getBody().setBodyRotation(rotation);
		} catch (Throwable ignored) {}
	}
	/**
	 * Adds or removes the unconditional player targeting used by hurricane drowned. Overrides are reference counted so
	 * overlapping hurricanes do not restore vanilla behavior until the drowned has left every hurricane rain area.
	 */
	public static void manipulateDrownedGoals(Drowned entity, boolean aggressive) {
		if (entity == null)
			return;
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, () -> manipulateDrownedGoals(entity, aggressive));
			return;
		}
		final UUID uuid = entity.getUniqueId();
		if (aggressive) {
			if (!entity.isValid() || entity.isDead())
				return;
			final EntityEquipment equipment = entity.getEquipment();
			if (equipment.getItemInMainHand().getType() == Material.TRIDENT
					|| equipment.getItemInOffHand().getType() == Material.TRIDENT)
				return;
			final DrownedGoalOverride existing = drownedGoalOverrides.get(uuid);
			if (existing != null) {
				existing.references++;
				return;
			}
			final PathfinderNearestAttackableTarget<Player> targetGoal =
					new PathfinderNearestAttackableTarget<>(entity, Player.class, 5, true, false);
			final PathfinderMeleeAttack attackGoal = new PathfinderMeleeAttack(entity, 1.15);
			final EntityBrain brain = BukkitBrain.getBrain(entity);
			brain.getTargetAI().put(targetGoal, 1);
			brain.getTargetAI().put(attackGoal, 2);
			drownedGoalOverrides.put(uuid, new DrownedGoalOverride(targetGoal, attackGoal));
			return;
		}
		final DrownedGoalOverride override = drownedGoalOverrides.get(uuid);
		if (override == null || --override.references > 0)
			return;
		drownedGoalOverrides.remove(uuid);
		if (!entity.isValid() || entity.isDead())
			return;
		final EntityBrain brain = BukkitBrain.getBrain(entity);
		brain.getTargetAI().remove(override.targetGoal);
		brain.getTargetAI().remove(override.attackGoal);
		entity.setTarget(null);
	}
	/**
	 * Adds or removes the rabid plague behavior used by DeadlyDisasters. This is an overlay only: it never clears vanilla
	 * villager/animal movement goals, and it removes only the goals it added.
	 */
	public static void manipulatePlagueAggressionGoals(Mob entity, boolean aggressive, double damage, double speed, boolean preserveVillagerDoors) {
		if (entity == null)
			return;
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, () -> manipulatePlagueAggressionGoals(entity, aggressive, damage, speed, preserveVillagerDoors));
			return;
		}
		final UUID uuid = entity.getUniqueId();
		if (aggressive) {
			if (!entity.isValid() || entity.isDead())
				return;
			PlagueGoalOverride existing = plagueGoalOverrides.get(uuid);
			if (existing != null) {
				if (existing.entity == entity && isPlagueOverlayAttached(entity, existing)) {
					existing.references++;
					return;
				}
				// either the mob was unloaded and reloaded since the overlay was added (the stored goals are bound to
				// the old entity handle and no longer run) or the goals were lost from the brain - drop the stale
				// override and rebuild the overlay on the current handle
				plagueGoalOverrides.remove(uuid);
				stripPlagueGoals(entity, existing);
			}
			double attackDamage = damage > 0 ? damage : Math.max(1.0, entity.getAttribute(VersionUtils.getAttackDamageAttribute()) != null ? entity.getAttribute(VersionUtils.getAttackDamageAttribute()).getValue() : 2.0);
			PlagueGoalOverride override = new PlagueGoalOverride(entity, attackDamage);
			if (existing != null)
				override.references = Math.max(1, existing.references);
			plagueGoalOverrides.put(uuid, override);
			try {
				EntityBrain brain = BukkitBrain.getBrain(entity);
				brain.getTargetAI().put(override.targetGoal, 1);
				brain.getGoalAI().put(override.attackGoal, 2);
			} catch (Throwable t) {
				override.references = 0;
				removePlagueGoalOverride(entity, uuid, override);
			}
			return;
		}
		PlagueGoalOverride override = plagueGoalOverrides.get(uuid);
		if (override == null || --override.references > 0)
			return;
		removePlagueGoalOverride(entity, uuid, override);
	}
	/**
	 * Force-removes DeadlyDisasters plague aggression even if the overlay was reference-counted more than once.
	 */
	public static void clearPlagueAggressionGoals(Mob entity) {
		if (entity == null)
			return;
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, () -> clearPlagueAggressionGoals(entity));
			return;
		}
		UUID uuid = entity.getUniqueId();
		PlagueGoalOverride override = plagueGoalOverrides.get(uuid);
		if (override == null) {
			stripPlagueGoals(entity, null);
			return;
		}
		override.references = 0;
		removePlagueGoalOverride(entity, uuid, override);
	}
	public static void clearAllPlagueAggressionGoals() {
		if (!Bukkit.isPrimaryThread()) {
			Bukkit.getScheduler().runTask(plugin, EntityUtils::clearAllPlagueAggressionGoals);
			return;
		}
		for (org.bukkit.World world : Bukkit.getWorlds()) {
			for (LivingEntity living : world.getLivingEntities()) {
				if (living instanceof Mob mob && (living instanceof Animals || living instanceof Villager || living instanceof WanderingTrader))
					clearPlagueAggressionGoals(mob);
			}
		}
	}
	private static void removePlagueGoalOverride(Mob entity, UUID uuid, PlagueGoalOverride override) {
		if (!entity.isValid() || entity.isDead()) {
			plagueGoalOverrides.remove(uuid, override);
			return;
		}
		if (stripPlagueGoals(entity, override)) {
			plagueGoalOverrides.remove(uuid, override);
			return;
		}
		if (++override.cleanupAttempts <= 5 && plugin.isEnabled())
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (plagueGoalOverrides.get(uuid) == override && override.references <= 0)
					removePlagueGoalOverride(entity, uuid, override);
			}, 1L);
	}
	/** Whether the override's goals are actually present in the mob's brain - an attach can silently fail or be lost. */
	private static boolean isPlagueOverlayAttached(Mob entity, PlagueGoalOverride override) {
		try {
			EntityBrain brain = BukkitBrain.getBrain(entity);
			boolean targetAttached = false;
			for (var wrapped : brain.getTargetAI())
				if (wrapped.getPathfinder() == override.targetGoal) {
					targetAttached = true;
					break;
				}
			if (!targetAttached)
				return false;
			for (var wrapped : brain.getGoalAI())
				if (wrapped.getPathfinder() == override.attackGoal)
					return true;
			return false;
		} catch (Throwable t) {
			// cannot inspect the brain - assume attached rather than churn through rebuilds
			return true;
		}
	}
	private static boolean stripPlagueGoals(Mob entity, PlagueGoalOverride override) {
		EntityBrain brain;
		try {
			brain = BukkitBrain.getBrain(entity);
		} catch (Throwable t) {
			entity.setTarget(null);
			return false;
		}
		boolean success = true;
		if (override != null) {
			try {
				brain.getTargetAI().remove(override.targetGoal);
			} catch (Throwable t) {
				success = false;
			}
			try {
				brain.getGoalAI().remove(override.attackGoal);
			} catch (Throwable t) {
				success = false;
			}
		}
		try {
			for (var wrapped : Set.copyOf(brain.getTargetAI())) {
				if (wrapped.getPathfinder() instanceof PathfinderPlagueAggressionTarget)
					brain.getTargetAI().remove(wrapped.getPathfinder());
			}
		} catch (Throwable t) {
			success = false;
		}
		entity.setTarget(null);
		return success;
	}
	public static boolean isEntityImmunePlayer(Entity entity) {
		if (!(entity instanceof Player player))
			return false;
		GameMode mode = player.getGameMode();
		return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
	}
	public static void modifyLoadoutArmorColor(BaseEntity<? extends LivingEntity> base, int red, int green, int blue, LoadoutEquipmentSlot... slots) {
		LivingEntity entity = base.getCastedEntity();
		if (entity == null)
			return;
		ItemStack[] armor = entity.getEquipment().getArmorContents();
		for (LoadoutEquipmentSlot slot : slots) {
			ItemStack item = armor[slot.slotIndex];
			if (item == null || !item.hasItemMeta() || !leatherArmor.contains(item.getType()) || base.getEntityVariant().loadout.armor[slot.slotIndex] != null)
				continue;
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(Color.fromRGB(red, green, blue));
			item.setItemMeta(meta);
		}
	}
	public static boolean isEntityUnderHealth(LivingEntity entity, double value) {
		if (entity == null)
			return false;
		return entity.getHealth() < entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue() * value;
	}
	public static void playEntityHarmSound(LivingEntity entity, Location location, float volume, float pitch, Sound playerHarmSound) {
		if (entity instanceof Player) {
			if (entity.isDead())
				location.getWorld().playSound(location, Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, volume, pitch);
			else
				location.getWorld().playSound(location, playerHarmSound, SoundCategory.PLAYERS, volume, pitch);
		}
		if (!isVersion192OrAbove)
			return;
		if (entity.isDead())
			location.getWorld().playSound(location, entity.getDeathSound(), entity instanceof Monster ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL, volume, pitch);
		else
			location.getWorld().playSound(location, entity.getHurtSound(), entity instanceof Monster ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL, volume, pitch);
	}
	public static void playEntityHarmSound(LivingEntity entity, float volume, float pitch) {
		playEntityHarmSound(entity, entity.getLocation(), volume, pitch, Sound.ENTITY_PLAYER_HURT);
	}
	public static void playEntityHarmSound(LivingEntity entity, Location location, Sound playerHarmSound) {
		playEntityHarmSound(entity, location, 1, Utils.getRandomGenerator().nextFloat(0.8f, 1.2f), playerHarmSound);
	}
	public static void playEntityHarmSound(LivingEntity entity, Location location) {
		playEntityHarmSound(entity, location, 1, Utils.getRandomGenerator().nextFloat(0.8f, 1.2f), Sound.ENTITY_PLAYER_HURT);
	}
	public static void playEntityHarmSound(LivingEntity entity, Sound playerHarmSound) {
		playEntityHarmSound(entity, entity.getLocation(), 1, Utils.getRandomGenerator().nextFloat(0.8f, 1.2f), playerHarmSound);
	}
	public static void playEntityHarmSound(LivingEntity entity) {
		playEntityHarmSound(entity, entity.getLocation(), 1, Utils.getRandomGenerator().nextFloat(0.8f, 1.2f), Sound.ENTITY_PLAYER_HURT);
	}
	public static boolean isEntityOwner(Entity toCheck, UUID possibleOwner) {
		return (toCheck instanceof Tameable tameable && Utils.isNotNullAndCondition(tameable.getOwner(), t -> t.getUniqueId().equals(possibleOwner)))
				|| (UIEntityManager.getEntity(toCheck) instanceof TameableEntity tameableEntity && Utils.isNotNullAndCondition(tameableEntity.getOwner(), t -> t.equals(possibleOwner)));
	}
	public static boolean isEntityOwner(Entity toCheck, Entity possibleOwner) {
		if (possibleOwner == null)
			return false;
		return isEntityOwner(toCheck, possibleOwner.getUniqueId());
	}

	private static final class DrownedGoalOverride {
		private final PathfinderNearestAttackableTarget<Player> targetGoal;
		private final PathfinderMeleeAttack attackGoal;
		private int references = 1;

		private DrownedGoalOverride(PathfinderNearestAttackableTarget<Player> targetGoal, PathfinderMeleeAttack attackGoal) {
			this.targetGoal = targetGoal;
			this.attackGoal = attackGoal;
		}
	}
	private static final class PlagueGoalOverride {
		// the exact entity instance the goals were built for - a reloaded mob keeps its UUID but gets a fresh handle,
		// which invalidates these goals and requires the overlay to be rebuilt
		private final Mob entity;
		private final PathfinderPlagueAggressionTarget targetGoal;
		private final PathfinderAnimalAttackTarget attackGoal;
		private int references = 1;
		private int cleanupAttempts;

		private PlagueGoalOverride(Mob entity, double attackDamage) {
			this.entity = entity;
			this.targetGoal = new PathfinderPlagueAggressionTarget(entity, entity instanceof Villager || entity instanceof WanderingTrader, 18.0);
			this.attackGoal = new PathfinderAnimalAttackTarget(entity, attackDamage, 1.7f, 22, 1.08, 0.08, true);
		}
	}
}
