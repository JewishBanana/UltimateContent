package com.github.jewishbanana.ultimatecontent.entities.endentities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.listeners.PathfindersHandler;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.PhysicsEngine;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/**
 * A tameable pet ported from the legacy DeadlyDisasters "Baby End Totem" — a small mirror of the {@link EndTotem}. It is a
 * floating obelisk (a stack of small armor stands riding an invisible {@link Wolf}). A wild one wanders harmlessly — its
 * sheep-hunting wolf AI is stripped out and replaced with passive goals. Right-clicking it with a ghast tear
 * ({@link #onInteracted}) has a chance to tame it; once tamed it follows and fights for its owner ({@link TameableEntity}'s
 * protective goals), can be healed with chorus fruit, and toggles sitting on an empty-hand right-click. Like the adult
 * totem it idly drifts and slowly rotates; when DeadlyDisasters is present it spins fast and spews dragon's breath while a
 * disaster is imminent for its owner (see {@code warning_seconds}).
 */
public class BabyEndTotem extends ComplexEntity<Wolf> implements TameableEntity {

	public static final String REGISTERED_KEY = "uc:baby_end_totem";
	private static final NamespacedKey sittingKey = new NamespacedKey(UltimateContent.getInstance(), "uc-totem-sitting");
	// The player-given name, persisted separately from the wolf's own (default) custom name so the floating label only
	// appears once the totem has actually been renamed via a name tag — never for the default display name.
	private static final NamespacedKey nameKey = new NamespacedKey(UltimateContent.getInstance(), "uc-totem-name");

	// Per-stand vertical offsets (index 0 = portal-frame head, 1 = chorus-flower arms, 2 = purpur pillar, 3 = obsidian base,
	// 4 = name label), mirroring the legacy baby totem's small-stand spacing — standing vs sitting (it settles lower sitting).
	private static final double[] STAND_Y = { 0.3, 0.3, -0.19, -0.68, 1.15 };
	private static final double[] SIT_Y = { 0.13, 0.13, -0.31, -0.75, 0.95 };
	private static final int NAME_INDEX = 4;
	private static final Vector ZERO = new Vector();
	// One-shot "decaying spin" lengths (in ticks): a short twirl when the pet sits/stands, a longer one when it lands a hit.
	private static final int SIT_SPIN_FRAMES = 15;
	private static final int HIT_SPIN_FRAMES = 30;

	private boolean sitting;
	private boolean warning;
	private Vector[] offsets;
	private double knockbackMultiplier;
	private int spinFrame = -1; // -1 = inactive; otherwise the current frame of a one-shot decaying spin
	private int spinMax;

	public BabyEndTotem(Wolf entity) {
		super(entity, CustomEntityType.BABY_END_TOTEM, false);

		setInvisible(entity);
		entity.setSilent(true);
		entity.setCanPickupItems(false);

		// A smaller mirror of the adult End Totem's obelisk: a portal-frame head (tilts with pitch) and a separate chorus-arm
		// stand at the same height, a rotating purpur pillar, and an obsidian base.
		headStand = new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(new ItemStack(Material.END_PORTAL_FRAME));
		}, new Vector(0, STAND_Y[0], 0));
		createStands(entity.getLocation(), headStand, new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.CHORUS_PLANT));
			stand.getEquipment().setItemInOffHand(new ItemStack(Material.CHORUS_FLOWER));
			stand.setRightArmPose(new EulerAngle(0, 0.3, 1.1));
			stand.setLeftArmPose(new EulerAngle(0, 0.5, -1.8));
		}, new Vector(0, STAND_Y[1], 0)), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(new ItemStack(Material.PURPUR_PILLAR));
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.CHORUS_PLANT));
			stand.getEquipment().setItemInOffHand(new ItemStack(Material.CHORUS_FLOWER));
			stand.setRightArmPose(new EulerAngle(0, 0.3, 1.1));
			stand.setLeftArmPose(new EulerAngle(0.5, 0.5, -1.8));
		}, new Vector(0, STAND_Y[2], 0), 90f), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(new ItemStack(Material.OBSIDIAN));
		}, new Vector(0, STAND_Y[3], 0)), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			// Name label stand: floats above the totem and shows its name, so the label sits above the structure rather than
			// above the (invisible) wolf. Only shown once the totem has been given a custom name via a name tag.
			initStand(stand);
			stand.setSmall(true);
			String custom = entity.getPersistentDataContainer().get(nameKey, PersistentDataType.STRING);
			if (custom != null) {
				stand.setCustomName(custom);
				stand.setCustomNameVisible(true);
			}
		}, new Vector(0, STAND_Y[NAME_INDEX], 0)));

		offsets = new Vector[] { new Vector(), new Vector(), new Vector(), new Vector() };

		scheduleTask(new BukkitRunnable() {
			private int warningCheck;

			@Override
			public void run() {
				if (!entity.isValid())
					return;
				// If the wolf acquired a target while sitting, stand it up so it can actually move to fight.
				if (sitting && entity.getTarget() != null && !entity.getTarget().isDead())
					setSitting(false);
				// Keep our sitting state in sync with the wolf's real state and persist any change so it survives reloads.
				if (sitting != entity.isSitting()) {
					sitting = entity.isSitting();
					entity.getPersistentDataContainer().set(sittingKey, PersistentDataType.BYTE, (byte) (sitting ? 1 : 0));
				}
				// Refresh the imminent-disaster warning state roughly once a second (cheap; only relevant when tamed).
				if (++warningCheck >= 20) {
					warningCheck = 0;
					updateWarning();
				}
				Location loc = entity.getLocation();
				loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 0.4, 0), 3, .15, .25, .15, 0.01);
				double[] base = sitting ? SIT_Y : STAND_Y;
				// Head: tilt the portal frame with the wolf's pitch (no yaw spin), like the adult.
				ArmorStand head = headStand.getEntity(loc);
				head.setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
				// Name label: held steady above the totem (no spin/drift) so it stays readable.
				((ArmorStand) getCreatureStand(NAME_INDEX).getEntity(loc)).teleport(loc.clone().add(0, base[NAME_INDEX], 0));
				if (spinFrame >= 0) {
					// One-shot decaying twirl (on sit/stand or after landing a hit): fast at first, easing out, with dragon's
					// breath — mirrors the adult End Totem's hit animation.
					VersionUtils.spawnDragonBreathParticle(loc.clone().add(0, 0.3, 0), 3, .15, .15, .15, 0.08, 1f);
					float yaw = (float) (Utils.calculateAnimationValue(spinFrame, spinMax, 0.8, 1.5, 20) + (spinMax - spinFrame));
					head.teleport(loc.clone().add(0, base[0], 0));
					for (int i = 1; i < NAME_INDEX; i++) {
						ArmorStand stand = (ArmorStand) getCreatureStand(i).getEntity(loc);
						float sy = stand.getLocation().getYaw();
						stand.teleport(loc.clone().add(0, base[i], 0));
						stand.setRotation(sy + (i % 2 == 0 ? -yaw : yaw), 0);
					}
					if (++spinFrame >= spinMax)
						spinFrame = -1;
					return;
				}
				if (warning) {
					VersionUtils.spawnDragonBreathParticle(loc.clone().add(0, 0.3, 0), 3, .15, .15, .15, 0.08, 1f);
					if (random.nextInt(15) == 0)
						playSound(loc, Sound.BLOCK_CONDUIT_AMBIENT_SHORT, .5f, random.nextBoolean() ? .5f : 2f);
				}
				float spin = warning ? 30f : 5f;
				Vector ho = warning ? ZERO : offsets[0];
				head.teleport(loc.clone().add(ho.getX(), base[0] + ho.getY(), ho.getZ()));
				// Body stands: drift with the idle offset and slowly counter-rotate (fast while warning).
				for (int i = 1; i < NAME_INDEX; i++) {
					ArmorStand stand = (ArmorStand) getCreatureStand(i).getEntity(loc);
					float yaw = stand.getLocation().getYaw();
					Vector o = warning ? ZERO : offsets[i];
					stand.teleport(loc.clone().add(o.getX(), base[i] + o.getY(), o.getZ()));
					stand.setRotation(yaw + (i % 2 == 0 ? -spin : spin), 0);
				}
			}
		}.runTaskTimer(plugin, 0, 1));

		// Idle drift: each stand's offset oscillates a little on a randomized vector, then reverses and re-randomizes — the
		// same gentle floating the adult End Totem uses. Runs async because it only mutates plain vectors.
		final Vector[] velocities = new Vector[] {
				Utils.getRandomizedVector(1f, 0.2f, 1f).multiply(0.03),
				Utils.getRandomizedVector(1f, 0.2f, 1f).multiply(0.03),
				Utils.getRandomizedVector(1f, 0.2f, 1f).multiply(0.03),
				Utils.getRandomizedVector(1f, 0.2f, 1f).multiply(0.03) };
		final float[] maxVelocities = new float[] { random.nextFloat(5, 15), random.nextFloat(5, 15), random.nextFloat(5, 15), random.nextFloat(5, 15) };
		final int[] frame = { 1, 0 };
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = 0; i < offsets.length; i++)
					offsets[i].add(velocities[i].clone().multiply(Utils.calculateAnimationValue(frame[0], 20, 0.01, frame[1] == 0 ? 0.01 : 0.005, maxVelocities[i])));
				if (++frame[0] == 20) {
					frame[0] = 0;
					if (frame[1]++ == 1) {
						frame[1] = 0;
						offsets = new Vector[] { new Vector(), new Vector(), new Vector(), new Vector() };
						for (int i = 0; i < velocities.length; i++) {
							velocities[i] = Utils.getRandomizedVector(1f, 0.2f, 1f).multiply(0.03);
							maxVelocities[i] = random.nextFloat(5, 15);
						}
						return;
					}
					for (int i = 0; i < velocities.length; i++)
						velocities[i].multiply(-1);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));

		// Restore persisted sitting state when this entity is reloaded from a chunk.
		if (entity.getPersistentDataContainer().getOrDefault(sittingKey, PersistentDataType.BYTE, (byte) 0) == (byte) 1)
			setSitting(true);
	}
	/** Updates {@link #warning}: true while DeadlyDisasters reports the owner is within {@code warning_seconds} of a disaster. */
	private void updateWarning() {
		UUID owner = getOwner();
		double threshold = getSectionDouble("warning_seconds", 30.0);
		if (owner == null || threshold <= 0) {
			warning = false;
			return;
		}
		Player player = Bukkit.getPlayer(owner);
		if (player == null) {
			warning = false;
			return;
		}
		int seconds = DependencyUtils.getSecondsUntilDisaster(player);
		warning = seconds >= 0 && seconds <= threshold;
	}
	@Override
	public void onInteracted(PlayerInteractEntityEvent event) {
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		Wolf entity = getCastedEntity();
		if (entity == null)
			return;
		event.setCancelled(true);
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		UUID owner = getOwner();
		if (owner == null) {
			// Wild: a ghast tear has a chance to tame it (awards the "Pet From The Abyss" achievement).
			if (item.getType() != Material.CHORUS_FRUIT)
				return;
			// Cannot tame while the totem is actively targeting the player — taming mid-combat
			// leaves the wolf with stale NMS goal-flag locks that freeze its AI completely.
			if (player.equals(entity.getTarget())) {
				entity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, entity.getLocation().add(0, .3, 0), 3, .3, .3, .3, 0);
				return;
			}
			if (player.getGameMode() != GameMode.CREATIVE)
				item.setAmount(item.getAmount() - 1);
			entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), entity.getLocation().add(0, .6, 0), 8, .2, .3, .2, .02);
			if (random.nextInt(3) == 0) {
				// Vanilla taming first so NMS can fire any data-observer callbacks (e.g. goal-selector rebuild on
				// tame-bit change in Paper 1.21) before we install our custom goals.
				entity.setTamed(true);
				entity.setOwner(player);
				setOwner(player.getUniqueId());
				// Re-apply one tick later: vanilla taming may rebuild the wolf's NMS goal selector via a
				// data-observer callback that runs after setTamed/setOwner returns, wiping the goals we just set.
				// The deferred call flushes the Java signal maps and reinstalls everything cleanly.
				scheduleTask(Bukkit.getScheduler().runTask(plugin, () -> {
					Wolf e = getCastedEntity();
					if (e == null || getOwner() == null)
						return;
					e.setSitting(false);
					PathfindersHandler.removePathfinders(BabyEndTotem.this);
					setOwner(BabyEndTotem.this, e);
				}));
				entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0, .7, 0), 7, .25, .3, .25, .03);
				playSound(entity.getLocation(), Sound.BLOCK_CONDUIT_ATTACK_TARGET, .6f, .8f);
				DependencyUtils.awardAchievementProgress(player.getUniqueId(), "master.series.void_master", 1, 5);
			}
			return;
		}
		// Tamed: only the owner may interact further.
		if (!owner.equals(player.getUniqueId()))
			return;
		if (item.getType() == Material.CHORUS_FRUIT) {
			double max = entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue();
			if (entity.getHealth() < max) {
				if (player.getGameMode() != GameMode.CREATIVE)
					item.setAmount(item.getAmount() - 1);
				entity.setHealth(Math.min(entity.getHealth() + 4, max));
				entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation().add(0, .7, 0), 6, .25, .3, .25, .02);
			}
			return;
		}
		if (item.getType() == Material.NAME_TAG) {
			// Name the totem from the tag: keep the wolf's own name hidden and show it on the floating label stand instead.
			if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
				changeName(item.getItemMeta().getDisplayName());
				if (player.getGameMode() != GameMode.CREATIVE)
					item.setAmount(item.getAmount() - 1);
			}
			return;
		}
		// Empty hand (or any non-food item): toggle sitting, twirling the body parts as it settles/rises.
		setSitting(!sitting);
		triggerSpin(SIT_SPIN_FRAMES);
		playSound(entity.getLocation(), Sound.BLOCK_CONDUIT_AMBIENT_SHORT, .5f, sitting ? .5f : 2f);
	}
	/** Heavy knockback (and a spin) when this totem lands a melee hit, like the adult End Totem. */
	public void hitEntity(EntityDamageByEntityEvent event) {
		Location loc = event.getEntity().getLocation().add(0, .5, 0);
		Vector velocity = Utils.getVectorTowards(event.getDamager().getLocation(), loc).multiply(knockbackMultiplier);
		velocity.setY(Math.min(velocity.getY(), knockbackMultiplier / 3.0));
		event.getEntity().setVelocity(velocity);
		playSound(event.getDamager().getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1, 2);
		triggerSpin(HIT_SPIN_FRAMES);
	}
	/** Starts a one-shot decaying spin of the body parts that plays over the next {@code frames} ticks. */
	private void triggerSpin(int frames) {
		spinFrame = 0;
		spinMax = frames;
	}
	/** Names the totem: persists the player-given name (keeping the wolf's own nameplate hidden) and shows it on the floating label stand. */
	public void changeName(String name) {
		Wolf entity = getCastedEntity();
		if (entity != null) {
			entity.setCustomName(name);
			entity.setCustomNameVisible(false);
			entity.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, name);
		}
		Entity nameStand = getCreatureStandEntity(NAME_INDEX);
		if (nameStand != null) {
			nameStand.setCustomName(name);
			nameStand.setCustomNameVisible(true);
		}
	}
	/** Toggles the pet's sitting state: a sitting totem stays put (movement goals removed) and persists across reloads. */
	public void setSitting(boolean value) {
		this.sitting = value;
		Wolf entity = getCastedEntity();
		if (entity == null)
			return;
		entity.setSitting(value);
		entity.getPersistentDataContainer().set(sittingKey, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
		if (value)
			entity.setTarget(null);
	}
	public boolean isSitting() {
		return sitting;
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		Player killer = event.getEntity().getKiller();
		if (killer != null)
			DependencyUtils.awardAchievementProgress(killer.getUniqueId(), "mobs.slayer.void_mobs", 1, -1);
		Location loc = event.getEntity().getLocation();
		loc.getWorld().spawnParticle(VersionUtils.getEnchantParticle(), loc.clone().add(0, 0.6, 0), 60, .2, .2, .2, 5);
		// Tumble the totem's body parts onto the ground as small physics blocks, like the adult End Totem's death ragdoll.
		if (VersionUtils.displaysAllowed && getSectionBoolean("deathRagdoll", false)) {
			int lifeTicks = (int) (getSectionDouble("ragdollSeconds", 7.0) * 20.0);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(0, loc).getLocation().add(0, 0.75, 0), Material.END_PORTAL_FRAME, 0.35f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(2, loc).getLocation().add(0, 0.75, 0), Material.PURPUR_PILLAR, 0.35f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			PhysicsEngine.dropBlockWithPhysics(getCreatureStandEntity(3, loc).getLocation().add(0, 0.75, 0), Material.OBSIDIAN, 0.35f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			// Chorus arms from the two stands that hold them (the arm stand and the purpur pillar).
			for (int i = 1; i < 3; i++) {
				Entity stand = getCreatureStandEntity(i, loc);
				Vector direction = stand.getLocation().getDirection();
				Vector left = new Vector(direction.getZ(), 0, -direction.getX());
				Vector right = new Vector(-direction.getZ(), 0, direction.getX());
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(right).add(0, 0.75, 0), Material.CHORUS_PLANT, 0.2f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(left).add(0, 0.75, 0), Material.CHORUS_FLOWER, 0.2f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			}
		}
	}
	/** Wild (untamed) AI: strip the wolf's sheep-hunting/combat goals so it just wanders harmlessly until tamed.
	 *  If already tamed (reloaded from a chunk), skip — BaseEntity's setOwner already applied protective goals
	 *  during construction; clearing and re-adding here would double-register pathfinders in PathfindersHandler. */
	public void setAIGoals(Wolf entity) {
		if (getOwner() != null)
			return;
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 1);

		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderMeleeAttack(entity), 4);
		goals.put(new PathfinderRandomStrollLand(entity), 5);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 6);
		goals.put(new PathfinderRandomLook(entity), 8);
	}
	public void setAttributes(Wolf entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(20);
		this.knockbackMultiplier = entity.getAttribute(VersionUtils.getAttackKnockbackAttribute()).getValue();
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(BabyEndTotem.REGISTERED_KEY, BabyEndTotem.class);
		
		type.setSpawnConditions(event -> false);
	}
}
