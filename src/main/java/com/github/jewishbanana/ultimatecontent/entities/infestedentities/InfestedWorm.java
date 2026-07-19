package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

/**
 * Infested Worm (ported from the legacy {@code entities.infestedcavesentities.InfestedWorm}). A burrowed ambush
 * predator: it lives inside a solid block, periodically peeks its head out toward nearby players, and when a player
 * steps in front of its exposed face it erupts in a chain of body segments, locks the victim in its mouth and chews
 * them (damaging on each swing), then retreats back into the block.
 *
 * <p>Adapted to the recoded framework: the real entity is an invisible marker {@link ArmorStand} and the visible worm
 * is built from {@link ComplexEntity} {@link CreatureStand}s driven manually each tick (the VoidWorm pattern). All
 * behavior runs on scheduled tasks that are cancelled automatically when the entity unloads.</p>
 *
 * <p>By default the worm retreats and relocates to a nearby block after an ambush. When spawned by the DeadlyDisasters
 * Infested Cave disaster, {@link #enableCaveBehavior(Entity, LivingEntity, Location, double)} can be called to restore
 * the legacy behavior: drag the victim to the wall, re-burrow next to the disaster's Warden carrying the victim, spit
 * them out beside it and anger the Warden at them.</p>
 */
public class InfestedWorm extends ComplexEntity<ArmorStand> {

	public static final String REGISTERED_KEY = "uc:infested_worm";
	private static final ItemStack headMaterial;
	static {
		headMaterial = IS_VERSION_19_OR_ABOVE ? new ItemStack(Material.SCULK_SHRIEKER) : new ItemStack(Material.STICKY_PISTON);
	}

	// IDLE/EMERGE/SWING/RETRACT/DEATH: the ambush + death animations. The cave "carry to the Warden" ritual adds: DRAG_IN
	// (pull the locked victim into the block with the worm), CARRY_WAIT (hold them hidden inside the block, suffocating,
	// while a spot near the Warden is chosen), SPIT_EMERGE (stretch fully out of the new block carrying the victim) and
	// SPIT_RETRACT (let the victim go and sink back in).
	private enum State { IDLE, EMERGE, SWING, MISS, RETRACT, DRAG_IN, CARRY_WAIT, SPIT_EMERGE, SPIT_RETRACT, DEATH }

	/**
	 * Optional hook set by DeadlyDisasters' infested-cave mastery: given (this worm's entity, a player), returns true if the
	 * worm must <b>not</b> attack that player because the player's "stealth" mastery roll succeeded. DeadlyDisasters rolls
	 * the chance exactly once per (worm, player) pair and caches the outcome, so once decided the worm never re-rolls,
	 * whether the roll passed or failed. Null (or no DeadlyDisasters) means no stealth — the worm attacks all valid prey.
	 */
	public static BiPredicate<Entity, Player> stealthCheck;

	/** Set on the main thread by {@link #setSpawnByCommandConditions} immediately before the worm is spawned, and read +
	 * cleared in the constructor, so a command-spawned worm burrows out of the exact block face the player was looking at
	 * instead of always defaulting to the top face. Null for natural spawns (they fall back to {@link #findOpenFace}). */
	private static BlockFace pendingCommandFace;

	private Block block;
	private BlockFace face;
	private Vector direction = new Vector();
	private Location loc;
	private Location tpOffset;
	private Location particleLoc;
	private Vector peekVec = new Vector();
	private Location staring;
	private Vector staringTowards = new Vector();
	private float standYaw;
	private double headPosePitch;
	private float lockPitch;
	private double playerLockOffset = 1.0;
	private double dX = .2, dY = .2, dZ = .2;
	private Material blockMaterial = Material.STONE;

	private State state = State.IDLE;
	private int peekingTicks = 80;
	private int frame;
	private int swingTimes;
	private boolean swingDirection;
	private double swing, lastSwing = 0.8, swingVel;
	private LivingEntity lockedTarget;
	private Block blockBelow;
	private int attackCooldown; // ambush attacks are disabled while > 0 (e.g. for 7s after spitting a victim out)
	private int wormPhase; // ever-incrementing animation clock that drives the writhe/shake (attack + death)
	private int deathFrame; // progress counter for the death-throes animation
	private int peekCount; // peeks completed at the current block (resets on migrate); after >=3 it may migrate on burrow-back
	private boolean migratedNoAttack; // true right after a migrate until the first peek there burrows back (can't ambush yet)
	private boolean awaitingMigratePeek; // true during the post-migrate delay before the worm peeks out at its new spot
	private int lookHoldTimer; // after a watched player/mob leaves, keeps the head gazing at the last-seen spot for this many ticks
	private Location missHeadBase; // the head's emerged position during a MISS, so the forward anti-clip nudge is stable (no drift)

	// Cave (DeadlyDisasters) warden behavior, configured via enableCaveBehavior(...).
	private boolean caveBehavior;
	private LivingEntity caveWarden;
	// The Warden's last known location, kept fresh while it's alive, so a victim is still carried to its spot (and a fresh
	// Warden is emerged there) even if the Warden despawned (e.g. the player ran far away) before the worm grabbed them.
	private Location caveWardenLastLoc;
	// Spit-out (carry-to-Warden) ritual state: a sub-tick counter and the chosen exit block/face near the Warden.
	private int ritualTicks;
	private Block pendingExitBlock;
	private BlockFace pendingExitFace;

	public InfestedWorm(ArmorStand entity) {
		super(entity, CustomEntityType.INFESTED_WORM, false);

		setInvisible(entity);
		entity.setVisible(false); // the center marker stand was only invisibility-potioned (no effect on armor stands); hide its model
		entity.setSilent(true);
		entity.setInvulnerable(true);
		entity.setCollidable(false);
		entity.setMarker(true);
		entity.setSmall(true);
		entity.setGravity(false);
		entity.getEquipment().setHelmet(new ItemStack(Material.AIR));

		// Consume the looked-at face handed off by the command spawn (null for natural spawns).
		BlockFace preferredFace = pendingCommandFace;
		pendingCommandFace = null;

		this.block = entity.getLocation().getBlock();
		this.blockMaterial = block.getType().isSolid() ? block.getType() : Material.STONE;
		if (preferredFace != null) {
			// Command spawn: burrow out of the face the player was looking at.
			updateLocation(block, findOpenFace(block, preferredFace));
		} else {
			// Natural / cave spawn: pick a believable host + face with full peek clearance, with every orientation equally
			// likely (so worms emerge from floors and ceilings as readily as walls), searching the immediate area.
			BlockFace[] outFace = new BlockFace[1];
			Block host = findClearanceHost(block, 2, false, false, outFace);
			if (host != null) {
				if (!host.equals(block)) {
					entity.teleport(BlockUtils.getCenterOfBlock(host));
					this.block = host;
					this.blockMaterial = host.getType().isSolid() ? host.getType() : Material.STONE;
				}
				updateLocation(host, outFace[0]);
			} else
				updateLocation(block, findOpenFace(block));
		}

		Consumer<ArmorStand> bodyConsumer = stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_WORM_BODY.getHead());
		};
		initStands(
				new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
					initStand(stand);
					stand.getEquipment().setHelmet(headMaterial);
				}),
				new CreatureStand<ArmorStand>(ArmorStand.class, bodyConsumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, bodyConsumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, bodyConsumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, bodyConsumer)
				);
		setHeadStand(0);

		startPeek();

		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				tick(entity);
			}
		}.runTaskTimer(plugin, 0, 1));

		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				// Keep the Warden's last known spot fresh while it's alive, so the carry-to-Warden ritual still has a target
				// (and can emerge a fresh Warden there) if it later despawns.
				if (caveBehavior && caveWarden != null && !caveWarden.isDead())
					caveWardenLastLoc = caveWarden.getLocation();
				// Host block destroyed while hiding -> play the death throes (handled per-tick too, this is the backstop).
				if (state == State.IDLE && block.getType() != blockMaterial) {
					beginDeath();
					return;
				}
				// Face got sealed over (no longer an ambush spot): quietly despawn, but never mid-animation or mid-migration.
				if (state == State.IDLE && !awaitingMigratePeek && !block.getRelative(face).isPassable()) {
					entity.remove();
					return;
				}
				// Occasionally start a fresh peek when fully idle (but not while a migration's scheduled emerge is pending).
				if (state == State.IDLE && !awaitingMigratePeek && peekingTicks <= -100 && random.nextInt(3) == 0)
					startPeek();
			}
		}.runTaskTimer(plugin, 0, 20));
	}
	private void tick(ArmorStand entity) {
		if (lockedTarget != null && (lockedTarget.isDead() || !lockedTarget.isValid())) {
			releaseTarget();
			lockedTarget = null;
		}
		switch (state) {
		case IDLE:
			tickIdle(entity);
			break;
		case EMERGE:
		case SWING:
		case RETRACT:
			tickAttack(entity);
			break;
		case MISS:
			tickMiss(entity);
			break;
		case DRAG_IN:
			tickDragIn(entity);
			break;
		case CARRY_WAIT:
			tickCarryWait(entity);
			break;
		case SPIT_EMERGE:
			tickSpitEmerge(entity);
			break;
		case SPIT_RETRACT:
			tickSpitRetract(entity);
			break;
		case DEATH:
			tickDeath();
			break;
		}
	}
	private void tickIdle(ArmorStand entity) {
		// Host block broken out from under the worm -> die on the spot (responsive; the 20-tick task is only a backstop).
		if (block.getType() != blockMaterial) {
			beginDeath();
			return;
		}
		if (attackCooldown > 0)
			attackCooldown--;
		if (peekingTicks > -100) {
			peekingTicks--;
			wormPhase++;
			if (peekingTicks > 0) {
				animatePeek();
				int elapsed = PEEK_TOTAL - peekingTicks;
				if (elapsed < PEEK_EMERGE || elapsed >= PEEK_EMERGE + PEEK_HOLD) // emerging or burrowing back in -> crumble particles
					spawnCrack();
				if (elapsed == PEEK_EMERGE + PEEK_HOLD) // peek over: straighten up and burrow back into the block
					playSound(block.getLocation().add(direction), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1f, .75f);
			} else if (peekingTicks == 0) {
				for (int i = 0; i < PEEK_SEGMENTS; i++)
					removeSeg(i);
				onPeekComplete();
			}
		}
		// Ambush check: any valid prey (see isValidWormTarget) standing in front of the exposed face triggers the attack
		// (unless on cooldown, e.g. just after spitting a victim out, or freshly migrated and not yet settled in).
		if (attackCooldown <= 0 && !migratedNoAttack && block.getType() == blockMaterial && block.getRelative(face).isPassable())
			for (int i = 1; i < 5; i++)
				if (!loc.getWorld().getNearbyEntities(loc.clone().add(direction.clone().multiply(i)), .5, .5, .5, this::isValidWormTarget).isEmpty()) {
					beginAttack();
					return;
				}
	}
	/**
	 * Valid prey for the worm: a living, non-dead entity that is not one of the worm's own armor stands, not a Warden, and
	 * not another infested mob (the worm shares the infested faction). For players it additionally excludes immune players
	 * and — when DeadlyDisasters' {@link #stealthCheck} hook is present — players whose infested-cave "stealth" mastery roll
	 * succeeds (rolled once per worm/player and cached, so a failed roll never re-rolls and the worm commits to attacking).
	 */
	private boolean isValidWormTarget(Entity e) {
		if (!(e instanceof LivingEntity living) || living.isDead() || e instanceof ArmorStand || e instanceof Warden || isInfested(e))
			return false;
		if (e instanceof Player player) {
			if (EntityUtils.isPlayerImmune(player))
				return false;
			if (stealthCheck != null && stealthCheck.test(getEntity(), player))
				return false;
		}
		return true;
	}
	/** True if {@code entity} is another UltimateContent infested mob (same faction — the worm never preys on its own kind). */
	private static boolean isInfested(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		return custom instanceof BaseEntity<?> base && base.getEntityType().category == CustomEntityType.Category.INFESTED_ENTITIES;
	}
	private void beginAttack() {
		for (int i = 0; i < getStandCount(); i++)
			removeSeg(i);
		state = State.EMERGE;
		frame = 0;
		swingTimes = 0;
		swing = 0;
		lastSwing = 0.8;
		swingVel = 0;
		swingDirection = false;
		resetPeekTowards();
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_EMERGE, 0.2F, 2);
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_ROAR, 1, 2);
	}
	private void tickAttack(ArmorStand entity) {
		spawnCrack();
		wormPhase++;
		if (state == State.EMERGE) {
			if (frame % 3 == 0) {
				int standIndex = frame / 3;
				// Spawn the segment directly at its emerge position (inside/at the block) instead of at the marker's
				// location, so a fresh segment never flashes into view away from the block for a tick.
				Location dest = emergeDest(standIndex);
				ArmorStand stand = seg(standIndex, dest);
				stand.teleport(dest);
				stand.setHeadPose(new EulerAngle(headPosePitch, 0, 0));
			}
			Vector charge = direction.clone().multiply(0.15);
			for (int i = 0; i < getStandCount(); i++) {
				ArmorStand stand = segOrNull(i);
				if (stand != null)
					stand.teleport(stand.getLocation().add(charge));
			}
			lockOntoTarget(entity);
			if (++frame >= 15) {
				if (isTargetGone()) {
					missAttack(); // emerged but grabbed nothing (or it died/left) -> don't swing at air; hiss, look, retract + relocate
					return;
				}
				state = State.SWING;
				swingVel = -0.2;
			}
			return;
		}
		if (state == State.SWING) {
			tickSwing(entity);
			lockOntoTarget(entity);
			return;
		}
		// RETRACT
		if (frame % 3 == 0)
			removeSeg(frame / 3);
		Vector charge = direction.clone().multiply(-0.2);
		for (int i = 0; i < getStandCount(); i++) {
			ArmorStand stand = segOrNull(i);
			if (stand != null)
				stand.teleport(stand.getLocation().add(charge));
		}
		frame--;
		if (frame < 0)
			finishAttack();
	}
	/** True if there is no valid victim to attack right now (none grabbed, or the grabbed one died / became invalid). */
	private boolean isTargetGone() {
		return lockedTarget == null || lockedTarget.isDead() || !lockedTarget.isValid();
	}
	/**
	 * A whiffed lunge: the worm emerged but grabbed nothing (the prey moved away / died). Instead of swinging at empty air it
	 * gives an irritated hiss, then STAYS emerged for a couple of seconds glaring at the nearest player/creature ({@link #tickMiss})
	 * before retracting and relocating.
	 */
	private void missAttack() {
		if (lockedTarget != null) { // it had locked on but the target died / became invalid -> drop it
			releaseTarget();
			lockedTarget = null;
		}
		playSound(block.getLocation().add(direction), Sound.BLOCK_FIRE_EXTINGUISH, 1.2f, 0.6f); // a hiss
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_AGITATED, 0.8f, 1.4f);
		ArmorStand head = segOrNull(0);
		missHeadBase = head != null ? head.getLocation() : null; // stable base for the forward anti-clip nudge below
		state = State.MISS;
		frame = 0;
	}
	/** Held after a missed lunge: the worm stays fully emerged for ~2s tracking the nearest player/creature, then burrows back and relocates. */
	private void tickMiss(ArmorStand entity) {
		wormPhase++;
		ArmorStand head = segOrNull(0);
		if (head != null && missHeadBase != null) {
			Vector look = resolvePeekLook(missHeadBase); // nearest player > nearest mob > last-seen > idle drift
			staringTowards = look.clone();
			Vector lookDir = look.lengthSquared() > 1.0E-6 ? look.clone().normalize() : direction.clone();
			// Push the head forward along its look — extra at hard back-angles (up to 0.8) — so the body doesn't clip the head.
			double forward = 0.1 + Math.max(0.0, -lookDir.dot(direction)) * 0.8;
			Location pos = missHeadBase.clone().add(lookDir.multiply(forward));
			pos.setDirection(look);
			head.teleport(pos);
			head.setRotation(pos.getYaw(), 0);
			double wob = Math.sin(wormPhase * 0.2) * 0.07;
			head.setHeadPose(new EulerAngle(Math.toRadians(pos.getPitch() + 90) + wob, 0, wob));
		}
		for (int i = 1; i < getStandCount(); i++) { // gentle body wobble so it isn't frozen stiff while it glares
			ArmorStand s = segOrNull(i);
			if (s != null)
				shakeSegment(s, i);
		}
		if (++frame >= 40) { // ~2 seconds glaring, then retract the way it came in -> finishAttack() relocates it
			state = State.RETRACT;
			frame = 14;
		}
	}
	private void tickSwing(ArmorStand entity) {
		if (!swingDirection) {
			swing += swingVel;
			if (swing > lastSwing) {
				swingDirection = true;
				swingTimes++;
				swingVel = -0.2;
				if (swingTimes == 3)
					playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_DIG, 0.6f, 1.2f);
				if (swingTimes >= 5) {
					playerLockOffset = face == BlockFace.DOWN ? 1.8 : 1.5;
					playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_ROAR, 1, 2);
				} else
					biteLockedTarget();
			}
		} else {
			swing -= swingVel;
			if (swing < -0.8) {
				swingDirection = false;
				swingTimes++;
				if (swingTimes >= 4) {
					lastSwing = 0;
					frame = 14;
					swingVel = -0.2;
				}
				biteLockedTarget();
			}
		}
		if (swingVel < 0.25)
			swingVel += 0.05;
		final int count = getStandCount();
		final boolean vertical = face == BlockFace.UP || face == BlockFace.DOWN;
		Vector tempVec = direction.clone().multiply(0.4);
		// Body segments: the rigid rotated fan, now layered with a travelling sine undulation + jitter (writhe) and a
		// per-segment head-pose wobble so the worm flexes and shakes organically instead of swinging as one stiff plane.
		for (int i = 1; i < count; i++) {
			ArmorStand stand = seg(i, loc.clone());
			ArmorStand next = i < count - 1 ? seg(i + 1, loc.clone()) : null;
			Vector towards = next != null ? stand.getLocation().toVector().subtract(next.getLocation().toVector()) : stand.getLocation().toVector().subtract(loc.toVector());
			Vector arc = vertical ? direction.clone().rotateAroundX((swing / 5) * (count - i)) : direction.clone().rotateAroundY((swing / 5) * (count - i));
			Location target = tpOffset.clone().add(tempVec.clone().multiply(count - i - 2)).add(arc).add(writhe(i, count, 0.1));
			target.setDirection(towards);
			stand.teleport(target);
			shakeSegment(stand, i);
		}
		ArmorStand head = seg(0, loc.clone());
		Vector headArc = vertical ? direction.clone().rotateAroundX((swing / 5) * count) : direction.clone().rotateAroundY(swing);
		Location headTarget = loc.clone().subtract(0, 1.8, 0).add(tempVec.clone().multiply(count - 1.5)).add(headArc).add(writhe(0, count, 0.06));
		headTarget.setDirection(head.getLocation().toVector().subtract(loc.toVector()));
		head.teleport(headTarget);
		shakeSegment(head, 0);
		if (swingTimes >= 5)
			endSwings();
	}
	/** After the final swing: cave worms carrying a player drag them into the block (the Warden ritual); otherwise just retract. */
	private void endSwings() {
		// The ritual runs as long as we know WHERE the Warden is/was — so a despawned Warden (player ran off) still results in
		// the victim being carried to its last spot and a fresh Warden emerging there.
		if (caveBehavior && lockedTarget instanceof Player && wardenAnchor() != null)
			beginDragIn();
		else
			state = State.RETRACT;
	}
	/** The Warden's current location if it's alive, otherwise its last known location (null only if it never existed). */
	private Location wardenAnchor() {
		if (caveWarden != null && !caveWarden.isDead())
			return caveWarden.getLocation();
		return caveWardenLastLoc;
	}
	/**
	 * A small per-segment positional offset that makes the worm body writhe: a travelling sine wave perpendicular to the
	 * body's axis (so a ripple runs down the segments), a slower second wave on the other perpendicular axis, plus a bit
	 * of random jitter. The tail segments (higher {@code i}) move more than the anchored head/neck.
	 */
	private Vector writhe(int i, int count, double amp) {
		Vector[] axes = perpAxes();
		double t = wormPhase * 0.45;
		double seg = i * 0.85;
		double wave1 = Math.sin(t + seg);
		double wave2 = Math.cos(t * 1.27 + seg * 1.6);
		double scale = amp * (0.45 + (double) i / Math.max(1, count));
		Vector off = axes[0].clone().multiply(wave1 * scale).add(axes[1].clone().multiply(wave2 * scale * 0.7));
		double j = scale * 0.4;
		off.add(axes[0].clone().multiply((random.nextDouble() - 0.5) * j)).add(axes[1].clone().multiply((random.nextDouble() - 0.5) * j));
		return off;
	}
	/** Two unit vectors spanning the plane perpendicular to the worm's facing {@link #direction}. */
	private Vector[] perpAxes() {
		if (face == BlockFace.UP || face == BlockFace.DOWN)
			return new Vector[] { new Vector(1, 0, 0), new Vector(0, 0, 1) };
		Vector up = new Vector(0, 1, 0);
		Vector side = direction.clone().crossProduct(up);
		if (side.lengthSquared() < 1.0E-6)
			side = new Vector(1, 0, 0);
		return new Vector[] { up, side.normalize() };
	}
	/** Nods/rolls a segment's head pose by a small sine wobble (offset per segment) to add shake to the body parts. */
	private void shakeSegment(ArmorStand stand, int i) {
		double t = wormPhase * 0.45 + i * 0.85;
		stand.setHeadPose(new EulerAngle(headPosePitch + Math.sin(t) * 0.2, 0, Math.sin(t * 1.4 + i) * 0.16));
	}
	// A full attack lands BITES_PER_ATTACK bites (one at each far point of the side-to-side wiggle), so each deals a
	// quarter of the configured damage and the whole attack adds up to exactly the config value.
	private static final int BITES_PER_ATTACK = 4;
	private void biteLockedTarget() {
		if (lockedTarget == null)
			return;
		EntityUtils.damageEntity(lockedTarget, entityVariant.damage / BITES_PER_ATTACK, "deaths.infestedWorm", DamageCause.ENTITY_ATTACK, getEntity());
		playSound(lockedTarget.getLocation(), Sound.ENTITY_WARDEN_ATTACK_IMPACT, 0.5F, 1.2F);
	}
	/** Detects and holds a victim at the worm's mouth (stand 0) so they cannot escape the chewing. */
	private void lockOntoTarget(ArmorStand entity) {
		ArmorStand head = segOrNull(0);
		if (head == null)
			return;
		if (lockedTarget == null) {
			for (Entity e : block.getWorld().getNearbyEntities(head.getEyeLocation(), .75, 1, .75, this::isValidWormTarget)) {
				lockedTarget = (LivingEntity) e;
				break;
			}
			return;
		}
		if (lockedTarget instanceof Player player && player.isSwimming())
			player.setSwimming(false);
		Location tpTo = head.getLocation();
		tpTo.setPitch(lockPitch);
		tpTo.add(tpTo.getDirection().multiply(playerLockOffset));
		tpTo.setDirection(head.getLocation().toVector().subtract(lockedTarget.getLocation().toVector()));
		lockedTarget.teleport(tpTo);
	}
	private void finishAttack() {
		for (int i = 0; i < getStandCount(); i++)
			removeSeg(i);
		// Cave worms carrying a player branch into the drag-in/Warden ritual earlier (see endSwings); only normal ambushes
		// (mob victims, or no cave Warden) reach finishAttack, so here the worm simply releases and relocates.
		releaseTarget();
		lockedTarget = null;
		if (!relocateToNearbyBlock()) {
			Entity self = getCastedEntity();
			if (self != null)
				self.remove();
			return;
		}
		peekingTicks = -100;
		attackCooldown = 140; // 7s: don't immediately re-grab the player we just released
		state = State.IDLE;
	}
	// === Cave "carry the victim to the Warden" ritual (DeadlyDisasters) ===
	/** Begins the drag-in: the worm pulls the locked victim into its block with it, before the carry to the Warden. */
	private void beginDragIn() {
		state = State.DRAG_IN;
		frame = 0;
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_DIG, 0.7f, 0.8f);
	}
	/**
	 * Where to hold a buried victim: their HEAD (eye) is placed at the centre of the solid host block — regardless of which
	 * face the worm sits in — so they are actually inside the wall suffocating, not standing on the block in front of it.
	 */
	private Location buryLocation(Block host, LivingEntity victim) {
		Location into = BlockUtils.getCenterOfBlock(host).subtract(0, victim.getEyeHeight(), 0);
		into.setDirection(direction.clone().multiply(-1)); // face into the wall
		return into;
	}
	/** Pulls every segment back into the block along -direction and drags the victim's head in with it; once fully in, hides them and picks an exit. */
	private void tickDragIn(ArmorStand entity) {
		spawnCrack();
		wormPhase++;
		Vector pull = direction.clone().multiply(-0.16);
		final int count = getStandCount();
		for (int i = 0; i < count; i++) {
			ArmorStand stand = segOrNull(i);
			if (stand != null) {
				stand.teleport(stand.getLocation().add(pull).add(writhe(i, count, 0.05)));
				shakeSegment(stand, i);
			}
		}
		// Drag the victim's HEAD into the wall (toward the buried position), rather than anchoring their feet to the mouth.
		if (lockedTarget != null && lockedTarget.isValid()) {
			Location bury = buryLocation(block, lockedTarget);
			Location np = lockedTarget.getLocation().add(bury.toVector().subtract(lockedTarget.getLocation().toVector()).multiply(0.4));
			np.setDirection(bury.getDirection());
			lockedTarget.teleport(np);
		}
		if (++frame < 16)
			return;
		for (int i = 0; i < count; i++)
			removeSeg(i);
		hideVictim(); // hidden from other players while buried in the wall, suffocating
		if (lockedTarget != null && lockedTarget.isValid())
			lockedTarget.teleport(buryLocation(block, lockedTarget));
		if (!selectExitSpotNearWarden()) {
			// No spot near the Warden: fall back to a plain release + relocate so the worm doesn't get stuck mid-ritual.
			releaseTarget();
			lockedTarget = null;
			if (!relocateToNearbyBlock())
				removeSelf();
			else {
				peekingTicks = -100;
				attackCooldown = 140;
				state = State.IDLE;
			}
			return;
		}
		ritualTicks = 0;
		state = State.CARRY_WAIT;
	}
	/** Holds the hidden victim inside the host block (suffocating) for a couple of seconds, then relocates next to the Warden. */
	private void tickCarryWait(ArmorStand entity) {
		if (lockedTarget == null || !lockedTarget.isValid()) {
			// Victim died (e.g. suffocated) or left while buried: abandon the ritual and go idle where we are.
			showVictim();
			lockedTarget = null;
			peekingTicks = -100;
			attackCooldown = 140;
			state = State.IDLE;
			return;
		}
		lockedTarget.teleport(buryLocation(block, lockedTarget)); // hold them head-in-wall, suffocating
		if (++ritualTicks >= 40)
			relocateForSpit();
	}
	/** Teleports the worm (and still-locked, still-buried victim) to the chosen block near the Warden, reveals the victim, then emerges. */
	private void relocateForSpit() {
		Entity self = getCastedEntity();
		if (self == null || pendingExitBlock == null) {
			releaseTarget();
			lockedTarget = null;
			removeSelf();
			return;
		}
		self.teleport(BlockUtils.getCenterOfBlock(pendingExitBlock));
		this.block = pendingExitBlock;
		this.blockMaterial = pendingExitBlock.getType().isSolid() ? pendingExitBlock.getType() : Material.STONE;
		updateLocation(pendingExitBlock, pendingExitFace);
		pendingExitBlock = null;
		pendingExitFace = null;
		if (lockedTarget != null && lockedTarget.isValid())
			lockedTarget.teleport(buryLocation(block, lockedTarget)); // still head-in the (new) block, locked to the worm
		showVictim(); // reveal them again, right before the worm stretches them out of the block
		beginSpitEmerge();
	}
	/** Begins the spit-out emerge: the worm stretches fully out of the new block carrying the victim on its head. */
	private void beginSpitEmerge() {
		for (int i = 0; i < getStandCount(); i++)
			removeSeg(i);
		state = State.SPIT_EMERGE;
		frame = 0;
		resetPeekTowards();
		playerLockOffset = face == BlockFace.DOWN ? 1.8 : 1.5; // the victim rides out ahead of the head
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_EMERGE, 0.4F, 1.6F);
		playSound(block.getLocation().add(direction), Sound.ENTITY_WARDEN_ROAR, 1, 1.8F);
	}
	/** Emerge animation for the spit-out: segments stretch out of the block (like the attack emerge) carrying the locked victim, then it lets go. */
	private void tickSpitEmerge(ArmorStand entity) {
		spawnCrack();
		wormPhase++;
		final int count = getStandCount();
		if (frame % 3 == 0) {
			int standIndex = frame / 3;
			if (standIndex < count) {
				Location dest = emergeDest(standIndex);
				ArmorStand stand = seg(standIndex, dest);
				stand.teleport(dest);
				stand.setHeadPose(new EulerAngle(headPosePitch, 0, 0));
			}
		}
		Vector charge = direction.clone().multiply(0.16);
		for (int i = 0; i < count; i++) {
			ArmorStand stand = segOrNull(i);
			if (stand != null) {
				stand.teleport(stand.getLocation().add(charge).add(writhe(i, count, 0.08)));
				shakeSegment(stand, i);
			}
		}
		lockOntoTarget(entity); // the victim is carried out of the block on the head as it stretches out
		if (++frame >= 20) {
			releaseSpitVictim(); // fully out: drop the victim here beside the Warden and anger it at them
			state = State.SPIT_RETRACT;
			frame = 14;
		}
	}
	/** Lets the carried victim go beside the Warden (angering it) once the worm has fully stretched out; if the Warden is gone, emerges a fresh one from the ground at its last spot. */
	private void releaseSpitVictim() {
		if (lockedTarget instanceof Player victim && victim.isOnline()) {
			if (caveWarden instanceof Warden warden && !warden.isDead())
				warden.increaseAnger(victim, 50);
			else
				spawnEmergingWarden(victim); // the cave's Warden despawned -> a new one bursts out of the ground at its last spot
		}
		releaseTarget();
		lockedTarget = null;
	}
	/**
	 * Emerges a fresh Warden from the ground at the cave Warden's last known location (or, failing that, beside the worm),
	 * angered at the spat-out victim — used when the original cave Warden despawned before the worm could carry the victim to
	 * it. The Warden is spawned invisible with the {@code is_emerging} brain memory so it plays the burrow-up animation.
	 */
	private void spawnEmergingWarden(Player victim) {
		final World world = block.getWorld();
		Block ground = (caveWardenLastLoc != null ? caveWardenLastLoc : block.getLocation()).getBlock();
		// Drop to the nearest solid floor below so it emerges out of the ground rather than mid-air.
		int steps = 0;
		while (ground.isPassable() && steps++ < 24 && ground.getY() > world.getMinHeight())
			ground = ground.getRelative(BlockFace.DOWN);
		final Location spawn = BlockUtils.getCenterOfBlock(ground.getRelative(BlockFace.UP));
		LivingEntity newWarden = (LivingEntity) world.spawnEntity(spawn, EntityType.WARDEN);
		newWarden.setInvisible(true);
		Utils.mergeEntityData(newWarden, "{Brain:{memories:{\"minecraft:dig_cooldown\":{ttl:1200L,value:{}},\"minecraft:is_emerging\":{ttl:134L,value:{}}}}}");
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			if (newWarden.isValid())
				newWarden.setInvisible(false);
		}, 10);
		((Warden) newWarden).setAnger(victim, 100);
		caveWarden = newWarden;
		caveWardenLastLoc = spawn;
		playSound(spawn, Sound.ENTITY_WARDEN_EMERGE, 1.5f, 0.8f);
	}
	/** Retract animation for the spit-out: segments sink back into the block (like the attack retract), then the worm goes idle. */
	private void tickSpitRetract(ArmorStand entity) {
		spawnCrack();
		wormPhase++;
		if (frame % 3 == 0)
			removeSeg(frame / 3);
		Vector charge = direction.clone().multiply(-0.2);
		for (int i = 0; i < getStandCount(); i++) {
			ArmorStand stand = segOrNull(i);
			if (stand != null)
				stand.teleport(stand.getLocation().add(charge));
		}
		if (--frame < 0) {
			for (int i = 0; i < getStandCount(); i++)
				removeSeg(i);
			state = State.IDLE;
			peekingTicks = -100;
			attackCooldown = 140; // 7s: don't immediately re-grab the player we just spat out next to the Warden
			playSound(block.getLocation().add(direction), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1f, .75f);
		}
	}
	/** The segment emerge destination for a given index — shared by the ambush emerge and the spit-out emerge so both look the same from any face. */
	private Location emergeDest(int standIndex) {
		if (standIndex == 0)
			return loc.clone().add(direction.clone().multiply(-0.5)).subtract(0, 1.8, 0).setDirection(staringTowards);
		if (face == BlockFace.UP || face == BlockFace.DOWN)
			return tpOffset.clone().add(peekVec).add(direction.clone().multiply(-0.6)).setDirection(staringTowards);
		return tpOffset.clone().add(peekVec).add(direction.clone().multiply(-0.45)).setDirection(staringTowards);
	}
	/** Hides the carried player from everyone else while they are buried in the wall. */
	private void hideVictim() {
		if (lockedTarget instanceof Player victim)
			for (Player p : plugin.getServer().getOnlinePlayers())
				if (!p.equals(victim))
					p.hidePlayer(plugin, victim);
	}
	/** Reveals the carried player to everyone again. */
	private void showVictim() {
		if (lockedTarget instanceof Player victim)
			for (Player p : plugin.getServer().getOnlinePlayers())
				p.showPlayer(plugin, victim);
	}
	private void removeSelf() {
		Entity self = getCastedEntity();
		if (self != null)
			self.remove();
	}
	/**
	 * Chooses a block near the Warden to spit the victim out of, into {@link #pendingExitBlock}/{@link #pendingExitFace}.
	 * Prefers emerging from a CEILING (face DOWN, the worm drops out downward), then a WALL (a horizontal face), then a
	 * FLOOR (face UP, it rises out) as a last resort. Returns false if no suitable enclosed block with clearance is found.
	 */
	private boolean selectExitSpotNearWarden() {
		Location anchor = wardenAnchor();
		if (anchor == null)
			return false;
		final Location wardenLoc = anchor.clone().add(0, 1, 0);
		for (int x = -1; x <= 1; x++)
			for (int z = -1; z <= 1; z++) {
				org.bukkit.Chunk chunk = wardenLoc.getWorld().getChunkAt(wardenLoc.getChunk().getX() + x, wardenLoc.getChunk().getZ() + z);
				if (!chunk.isLoaded())
					chunk.load(true);
			}
		final Block origin = wardenLoc.getBlock();
		final int reach = 6;
		// Priority tiers: ceiling (emerge downward) -> walls -> floor (emerge upward).
		final BlockFace[][] tiers = {
				{ BlockFace.DOWN },
				{ BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST },
				{ BlockFace.UP } };
		for (BlockFace[] tier : tiers) {
			Block bestBlock = null;
			BlockFace bestFace = null;
			double bestDist = Double.MAX_VALUE;
			for (int x = -reach; x <= reach; x++)
				for (int y = -reach; y <= reach; y++)
					for (int z = -reach; z <= reach; z++) {
						Block b = origin.getRelative(x, y, z);
						if (!b.getType().isSolid())
							continue;
						for (BlockFace f : tier)
							if (canEmergeFrom(b, f)) {
								double d = b.getLocation().distanceSquared(wardenLoc);
								if (d < bestDist) {
									bestDist = d;
									bestBlock = b;
									bestFace = f;
								}
								break;
							}
					}
			if (bestBlock != null) {
				pendingExitBlock = bestBlock;
				pendingExitFace = bestFace;
				return true;
			}
		}
		return false;
	}
	/** True if the worm can burrow in {@code b} and spit a carried player out of {@code f}: the block is embedded (>=3 solid sides) and the face has the wider 3x3-cross, 5-blocks-deep clearance the warden hand-off needs. */
	private boolean canEmergeFrom(Block b, BlockFace f) {
		return b.getRelative(f).isPassable() && isEmbedded(b) && hasExitClearance(b, f);
	}
	/**
	 * The host block was broken out from under the worm: it convulses out of the shattered space and starts its death
	 * throes ({@link #tickDeath()}). Releases any carried victim and erupts every segment into view before it writhes out.
	 */
	private void beginDeath() {
		if (state == State.DEATH)
			return;
		state = State.DEATH;
		deathFrame = 0;
		releaseTarget();
		lockedTarget = null;
		int count = getStandCount();
		for (int i = 0; i < count; i++) {
			Location dest = loc.clone().add(direction.clone().multiply(0.2 - i * 0.32)).subtract(0, i == 0 ? 1.8 : 1.0, 0).setDirection(direction);
			ArmorStand stand = seg(i, dest);
			stand.teleport(dest);
			stand.setHeadPose(new EulerAngle(headPosePitch, 0, 0));
		}
		Location at = loc.clone();
		playSound(at, Sound.ENTITY_WARDEN_DEATH, 1f, 1.4f);
		playSound(at, Sound.ENTITY_WARDEN_AGITATED, 1f, 0.6f);
		at.getWorld().spawnParticle(VersionUtils.getBlockCrack(), at, 25, .3, .3, .3, .1, blockMaterial.createBlockData());
	}
	/** The death-throes animation: segments thrash with a decaying writhe while burrowing back into the host block, then vanish. */
	private void tickDeath() {
		deathFrame++;
		wormPhase++;
		int count = getStandCount();
		double decay = Math.max(0, 1.0 - deathFrame / 36.0); // thrashing fades as it dies
		// Retreat along -direction (back into the block it emerged from), not straight down — so a side/ceiling worm
		// burrows into its wall/floor instead of dropping through the air. Accelerates, and the tail sinks a bit further.
		double burrow = 0.03 + deathFrame * 0.0035;
		for (int i = 0; i < count; i++) {
			ArmorStand stand = segOrNull(i);
			if (stand == null)
				continue;
			Location l = stand.getLocation();
			l.add(writhe(i, count, 0.16 * decay));
			l.add(direction.clone().multiply(-burrow * (0.6 + i * 0.12)));
			stand.teleport(l);
			shakeSegment(stand, i);
		}
		if (deathFrame % 3 == 0)
			loc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), loc.clone(), 6, .25, .25, .25, .08, blockMaterial.createBlockData());
		if (deathFrame >= 36) {
			for (int i = 0; i < count; i++)
				removeSeg(i);
			Entity self = getCastedEntity();
			if (self != null)
				self.remove();
		}
	}
	/** Releases the locked victim; if it was a hidden/carried player, restores their visibility and any ghost block. */
	private void releaseTarget() {
		if (lockedTarget == null)
			return;
		if (lockedTarget instanceof Player carried) {
			for (Player p : plugin.getServer().getOnlinePlayers())
				p.showPlayer(plugin, carried);
			if (blockBelow != null) {
				carried.sendBlockChange(blockBelow.getLocation(), blockBelow.getBlockData());
				blockBelow = null;
			}
		}
	}
	/** Finds a nearby block + face with full peek clearance (any orientation equally likely) and re-burrows there. */
	private boolean relocateToNearbyBlock() {
		Entity self = getCastedEntity();
		if (self == null)
			return false;
		BlockFace[] outFace = new BlockFace[1];
		Block target = findClearanceHost(block, 3, false, true, outFace);
		if (target == null) {
			// Fallback: any nearby solid block with any open face, so it can still escape a tight pocket.
			List<Block> candidates = new ArrayList<>();
			for (int x = -3; x <= 3; x++)
				for (int y = -3; y <= 3; y++)
					for (int z = -3; z <= 3; z++) {
						Block b = block.getRelative(x, y, z);
						if (!b.equals(block) && b.getType().isSolid() && findOpenFace(b) != null)
							candidates.add(b); // exclude the worm's own block so it actually moves
					}
			if (candidates.isEmpty())
				return false;
			target = candidates.get(random.nextInt(candidates.size()));
			outFace[0] = findOpenFace(target);
		}
		self.teleport(BlockUtils.getCenterOfBlock(target));
		this.block = target;
		this.blockMaterial = target.getType();
		updateLocation(target, outFace[0]);
		playSound(target.getLocation().add(direction), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1f, .75f);
		return true;
	}
	private BlockFace findOpenFace(Block block) {
		for (BlockFace face : new BlockFace[] { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN })
			if (block.getRelative(face).isPassable() && !block.getRelative(face.getOppositeFace()).isPassable())
				return face;
		return null;
	}
	/** Like {@link #findOpenFace(Block)} but burrows out of {@code preferred} (the face a player was looking at) when it's open and backed. */
	private BlockFace findOpenFace(Block block, BlockFace preferred) {
		if (preferred != null && block.getRelative(preferred).isPassable() && !block.getRelative(preferred.getOppositeFace()).isPassable())
			return preferred;
		return findOpenFace(block);
	}
	private static final BlockFace[] ALL_FACES = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	/** True if {@code b} is embedded in terrain (>=3 of its 6 faces are solid) — a believable block for a worm to hide in. */
	private static boolean isEmbedded(Block b) {
		int occupied = 0;
		for (BlockFace nf : ALL_FACES)
			if (!b.getRelative(nf).isPassable())
				occupied++;
		return occupied >= 3;
	}
	/** The four faces perpendicular to {@code f} (its lateral neighbours). */
	private static BlockFace[] lateralFaces(BlockFace f) {
		switch (f) {
		case UP: case DOWN:
			return new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
		case NORTH: case SOUTH:
			return new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST };
		default: // EAST / WEST
			return new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH };
		}
	}
	/** The two unit axes perpendicular to {@code f} (used to sweep the 3x3 cross-section of the wide warden-exit clearance). */
	private static int[][] perpAxes(BlockFace f) {
		switch (f) {
		case UP: case DOWN:
			return new int[][] { { 1, 0, 0 }, { 0, 0, 1 } };
		case NORTH: case SOUTH:
			return new int[][] { { 1, 0, 0 }, { 0, 1, 0 } };
		default: // EAST / WEST
			return new int[][] { { 0, 1, 0 }, { 0, 0, 1 } };
		}
	}
	/**
	 * Peek clearance for emerging out of {@code f}: an uneven plus sign of passable space — 4 blocks straight out in front of
	 * the host, plus one block on each of the four lateral sides 2 blocks out — so the head and body have room to stretch,
	 * twirl and sway. Also requires at least one solid block directly behind the host (opposite of {@code f}), so the worm
	 * always has terrain to burrow into rather than popping out of a floating or paper-thin block.
	 */
	private static boolean hasPeekClearance(Block host, BlockFace f) {
		if (host.getRelative(f.getOppositeFace()).isPassable())
			return false;
		for (int ahead = 1; ahead <= 4; ahead++)
			if (!host.getRelative(f, ahead).isPassable())
				return false;
		Block at2 = host.getRelative(f, 2);
		for (BlockFace lat : lateralFaces(f))
			if (!at2.getRelative(lat).isPassable())
				return false;
		return true;
	}
	/** Wider clearance for spitting a carried player out near the Warden: a full 3x3 cross-section extended 5 blocks out. */
	private static boolean hasExitClearance(Block host, BlockFace f) {
		final int[][] ax = perpAxes(f);
		for (int ahead = 1; ahead <= 5; ahead++) {
			Block c = host.getRelative(f, ahead);
			for (int du = -1; du <= 1; du++)
				for (int dv = -1; dv <= 1; dv++) {
					Block cell = c.getRelative(du * ax[0][0] + dv * ax[1][0], du * ax[0][1] + dv * ax[1][1], du * ax[0][2] + dv * ax[1][2]);
					if (!cell.isPassable())
						return false;
				}
		}
		return true;
	}
	/** A random face of {@code b} (all faces equally likely) that is open and has the required clearance, or null if none. */
	private static BlockFace clearFace(Block b, boolean wide) {
		BlockFace[] order = ALL_FACES.clone();
		for (int i = order.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			BlockFace t = order[i];
			order[i] = order[j];
			order[j] = t;
		}
		for (BlockFace f : order)
			if (b.getRelative(f).isPassable() && (wide ? hasExitClearance(b, f) : hasPeekClearance(b, f)))
				return f;
		return null;
	}
	/**
	 * Searches every solid, embedded block within {@code radius} of {@code around} for faces with the required clearance and
	 * returns ONE uniformly at random across all qualifying (block, face) pairs — so floors, ceilings and walls are equally
	 * likely. The chosen face is written into {@code outFace}. Returns null if nothing qualifies.
	 */
	private static Block findClearanceHost(Block around, int radius, boolean wide, boolean excludeCenter, BlockFace[] outFace) {
		List<Block> hosts = new ArrayList<>();
		List<BlockFace> faces = new ArrayList<>();
		for (int x = -radius; x <= radius; x++)
			for (int y = -radius; y <= radius; y++)
				for (int z = -radius; z <= radius; z++) {
					Block b = around.getRelative(x, y, z);
					if ((excludeCenter && b.equals(around)) || !b.getType().isSolid() || !isEmbedded(b))
						continue;
					for (BlockFace f : ALL_FACES)
						if (b.getRelative(f).isPassable() && (wide ? hasExitClearance(b, f) : hasPeekClearance(b, f))) {
							hosts.add(b);
							faces.add(f);
						}
				}
		if (hosts.isEmpty())
			return null;
		int i = random.nextInt(hosts.size());
		outFace[0] = faces.get(i);
		return hosts.get(i);
	}
	private static final Set<Material> SCULK_BLOCKS = IS_VERSION_19_OR_ABOVE
			? EnumSet.of(Material.SCULK, Material.SCULK_CATALYST, Material.SCULK_SENSOR, Material.SCULK_SHRIEKER)
			: Set.of();
	/** Natural deep-dark spawn: find a buried block (preferring sculk) a fair distance from players that has an emergence face. */
	private static Block findNaturalHost(Location area) {
		if (!IS_VERSION_19_OR_ABOVE)
			return null;
		final World world = area.getWorld();
		Block fallback = null;
		for (int i = 0; i < 26; i++) {
			double ang = random.nextDouble() * Math.PI * 2;
			double dist = 12 + random.nextDouble() * 28; // 12..40 blocks out from the player
			int x = area.getBlockX() + (int) Math.round(Math.cos(ang) * dist);
			int z = area.getBlockZ() + (int) Math.round(Math.sin(ang) * dist);
			int y = area.getBlockY() + random.nextInt(-12, 13);
			Block b = world.getBlockAt(x, y, z);
			if (!b.getType().isSolid() || b.getBiome() != Biome.DEEP_DARK || !isEmbedded(b) || !farFromPlayers(b, 16))
				continue;
			if (clearFace(b, false) == null)
				continue;
			if (SCULK_BLOCKS.contains(b.getType()))
				return b; // prefer sculk: take the first one found
			if (fallback == null)
				fallback = b;
		}
		return fallback;
	}
	private static boolean farFromPlayers(Block b, double minDist) {
		final Location c = BlockUtils.getCenterOfBlock(b);
		final double minSq = minDist * minDist;
		for (Player p : b.getWorld().getPlayers())
			if (p.getLocation().distanceSquared(c) < minSq)
				return false;
		return true;
	}
	/** Natural-spawn hook for the deep-dark biome spawner: burrows a worm into a buried (preferably sculk) block on any face. */
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Block host = findNaturalHost(area);
		if (host == null)
			return null;
		return UIEntityManager.spawnEntity(BlockUtils.getCenterOfBlock(host), InfestedWorm.class);
	};
	// Idle peek: head + this-many body segments stick out as a continuous chain. Phases (in ticks): a quick twirling EMERGE,
	// then HOLD fully out (~3s) watching the player and swaying, then a straightening RETRACT back into the block.
	private static final int PEEK_SEGMENTS = 4;
	private static final int PEEK_EMERGE = 24;
	private static final int PEEK_HOLD = 60;   // ~3 seconds peeking fully out
	private static final int PEEK_RETRACT = 20;
	private static final int PEEK_TOTAL = PEEK_EMERGE + PEEK_HOLD + PEEK_RETRACT;
	private static final double PEEK_OUT = 1.5;     // how far the head sticks out (along the face) at full peek
	private static final double PEEK_SPACING = 0.42; // gap between body segments along the worm (tight -> no air gaps)
	/** Vertical stand offset for the SMALL body segments (player-head render height), per face — matches the attack's tpOffset. */
	private double bodyStandYOffset() {
		switch (face) {
		case UP:
			return 0.95;
		case DOWN:
			return 1.2;
		default:
			return 1.1;
		}
	}
	/**
	 * Where the head should look during the peek: toward the nearest non-immune player, else the nearest other creature, else
	 * (nothing around) a subtle slow drift around the worm's outward facing. One nearby-entity scan handles all three.
	 */
	private Vector resolvePeekLook(Location from) {
		// {@code from} is the head stand's feet; its visible head is ~1.6 above that. Use that eye point for both the look
		// vector and the line-of-sight ray, otherwise (e.g. a floor worm) the origin sits inside the ground and every ray
		// is "blocked", so it never sees anyone.
		Location origin = from.clone().add(0, 1.6, 0);
		Entity self = getCastedEntity();
		Player nearestPlayer = null;
		double bestPlayer = Double.MAX_VALUE;
		LivingEntity nearestMob = null;
		double bestMob = Double.MAX_VALUE;
		for (Entity e : loc.getWorld().getNearbyEntities(loc.clone().add(direction.clone().multiply(2)), 8, 8, 8)) {
			if (!(e instanceof LivingEntity living) || e instanceof ArmorStand || e.equals(self))
				continue;
			double d = e.getLocation().distanceSquared(origin);
			if (living instanceof Player player) {
				if (EntityUtils.isPlayerImmune(player) || d >= bestPlayer || !headCanSee(origin, living))
					continue;
				bestPlayer = d;
				nearestPlayer = player;
			} else if (d < bestMob && headCanSee(origin, living)) {
				bestMob = d;
				nearestMob = living;
			}
		}
		LivingEntity target = nearestPlayer != null ? nearestPlayer : nearestMob;
		if (target != null) {
			lookHoldTimer = 60; // remember it for ~3s after it leaves
			return target.getEyeLocation().toVector().subtract(origin.toVector());
		}
		// Just lost sight of the player/mob: keep gazing where it was last seen for a moment instead of instantly snapping back.
		if (lookHoldTimer > 0) {
			lookHoldTimer--;
			return staringTowards.clone();
		}
		return idleLookDirection();
	}
	/** True if the worm's head (at {@code from}) has a clear line of sight to {@code target} — no solid block between them. */
	private boolean headCanSee(Location from, LivingEntity target) {
		Location to = target.getEyeLocation();
		Vector dir = to.toVector().subtract(from.toVector());
		double dist = dir.length();
		if (dist < 0.05)
			return true;
		RayTraceResult ray = from.getWorld().rayTraceBlocks(from, dir.multiply(1.0 / dist), dist, FluidCollisionMode.NEVER, true);
		return ray == null || ray.getHitBlock() == null; // nothing solid blocking the view
	}
	/**
	 * A slow, subtle drift in an arc around the direction the worm faces out from, for when there's nothing nearby to watch.
	 * A ceiling worm (emerging downward) would otherwise stare straight at the floor, which looks odd, so its idle gaze is
	 * tilted upward to scan around more naturally.
	 */
	private Vector idleLookDirection() {
		Vector[] axes = perpAxes();
		double a = wormPhase * 0.03;
		Vector center = face == BlockFace.DOWN ? new Vector(0, 0.35, 0) : direction.clone();
		return center
				.add(axes[0].clone().multiply(Math.sin(a) * 0.35))
				.add(axes[1].clone().multiply(Math.sin(a * 0.7 + 1.3) * 0.35));
	}
	/** Smoothstep ease for acceleration/deceleration. */
	private static double smooth(double x) {
		if (x <= 0)
			return 0;
		if (x >= 1)
			return 1;
		return x * x * (3 - 2 * x);
	}
	/**
	 * Called each time a peek finishes (the worm has fully burrowed back in). Counts the peek; the worm must peek at least
	 * three times at a block, after which each burrow-back has a 1-in-4 chance to migrate to another block in the area. The
	 * peek that immediately follows a migration re-enables attacking (the worm has now settled into its new spot).
	 */
	private void onPeekComplete() {
		peekCount++;
		if (migratedNoAttack)
			migratedNoAttack = false; // it has peeked + burrowed once at the new spot -> it can ambush again
		else if (peekCount >= 3 && random.nextInt(4) == 0)
			migrate();
	}
	/**
	 * Burrows the worm to another solid block in the area (possibly out of a different face) and, a few seconds later, peeks
	 * out there. It cannot ambush until that first peek has burrowed back in (see {@link #migratedNoAttack}). No-op (keeps
	 * peeking where it is) if no suitable nearby block is found.
	 */
	private void migrate() {
		if (!relocateToNearbyBlock())
			return; // nowhere to go right now — keep peeking here and roll again next time
		peekCount = 0;
		migratedNoAttack = true;
		awaitingMigratePeek = true; // suppress the random peek backstop until the scheduled emerge below
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				Entity self = getCastedEntity();
				if (self == null || !self.isValid()) {
					cancel();
					return;
				}
				awaitingMigratePeek = false;
				if (state == State.IDLE && block.getType() == blockMaterial)
					startPeek();
			}
		}.runTaskLater(plugin, 50 + random.nextInt(31))); // peek out ~2.5-4s after migrating
	}
	/** Pokes the head and a few body segments out of the block to begin a twirling peek. */
	private void startPeek() {
		peekingTicks = PEEK_TOTAL;
		resetPeekTowards();
		wormPhase++;
		// Spawn the exposed segments already at their (hidden, just-at-the-surface) peek positions so they never flash in.
		final Vector[] axes = perpAxes();
		for (int i = 0; i < PEEK_SEGMENTS; i++)
			seg(i, peekSegLoc(i, axes, 0.0, 0.0, wormPhase * 0.12, staringTowards)).setHeadPose(new EulerAngle(headPosePitch, 0, 0));
		playSound(block.getLocation().add(direction), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1f, .75f);
	}
	/**
	 * The idle peek animation. The worm emerges with a brief twirl (head and body twirling together), then once fully out the
	 * head turns to watch and follow the nearest player while the whole body gently sways back and forth like a stalk (sine
	 * motion = natural acceleration/deceleration). After the hold it straightens (no twirl) and burrows back into the block.
	 */
	private void animatePeek() {
		final int elapsed = PEEK_TOTAL - peekingTicks;
		final double progress;   // 0 (hidden) -> 1 (fully out)
		final double twirlStrength; // >0 only while emerging
		if (elapsed < PEEK_EMERGE) {
			progress = smooth((double) elapsed / PEEK_EMERGE);
			twirlStrength = Math.sin(Math.PI * elapsed / PEEK_EMERGE); // rises then falls across the emerge
		} else if (elapsed < PEEK_EMERGE + PEEK_HOLD) {
			progress = 1.0;
			twirlStrength = 0.0; // fully out: only gentle wobble/sway, no twirl
		} else {
			progress = smooth(Math.max(0.0, (double) (PEEK_TOTAL - elapsed) / PEEK_RETRACT));
			twirlStrength = 0.0; // straighten while burrowing back in
		}
		final double swayPhase = wormPhase * 0.12;
		final Vector[] axes = perpAxes();
		// Once (nearly) fully out, the head watches the nearest player, else the nearest mob, else drifts around idly; before
		// that it faces straight out.
		Vector look = staringTowards;
		ArmorStand head = segOrNull(0);
		if (progress > 0.85 && head != null) {
			look = resolvePeekLook(head.getLocation());
			staringTowards = look.clone();
		}
		for (int i = 0; i < PEEK_SEGMENTS; i++) {
			ArmorStand s = segOrNull(i);
			if (s == null)
				continue;
			s.teleport(peekSegLoc(i, axes, progress, twirlStrength, swayPhase, look));
			if (i > 0)
				shakeSegment(s, i); // gentle body wobble (the head's pose is handled below so it can face the player)
		}
		// Head facing: while emerging it SPINS with the body twirl (a direction rotating around the emergence axis, so the
		// head visibly twirls too instead of just orbiting while facing one way); once out it tracks the player.
		head = segOrNull(0);
		if (head != null) {
			Vector headLook;
			if (twirlStrength > 0.05) {
				double ang = wormPhase * 0.5;
				headLook = direction.clone().multiply(0.7)
						.add(axes[0].clone().multiply(Math.cos(ang) * 0.7))
						.add(axes[1].clone().multiply(Math.sin(ang) * 0.7));
			} else
				headLook = look;
			Location hl = head.getLocation().setDirection(headLook);
			head.setRotation(hl.getYaw(), 0);
			double wob = Math.sin(wormPhase * 0.18) * 0.07;
			head.setHeadPose(new EulerAngle(Math.toRadians(hl.getPitch() + 90) + wob, 0, wob));
		}
	}
	/**
	 * Position of one peek segment as a continuous chain: the head sits {@code PEEK_OUT*progress} out along the face and each
	 * body segment trails {@code PEEK_SPACING} further back ALONG the same line (no vertical offset, so there are no air gaps
	 * and it never floats). An emerge-only twirl spirals the whole chain, and a sine side-to-side sway (tip swaying most)
	 * keeps it alive once out.
	 */
	private Location peekSegLoc(int i, Vector[] axes, double progress, double twirlStrength, double swayPhase, Vector look) {
		double out = PEEK_OUT * progress - PEEK_SPACING * i;
		// The head is a FULL-size stand (block head, -1.8 offset); the body segments are SMALL stands (player head), so they
		// need the smaller per-face offset the attack uses — using -1.8 for them sinks the small body ~0.7 into the ground,
		// disconnected from the head.
		double yOff = (i == 0) ? 1.8 : bodyStandYOffset();
		Location base = loc.clone().add(direction.clone().multiply(out)).subtract(0, yOff, 0);
		if (twirlStrength > 0.001) { // emerge twirl: rotate a perpendicular offset around the face axis (head + body together)
			double ang = wormPhase * 0.5 + i * 0.7;
			double r = 0.18 * twirlStrength;
			base.add(axes[0].clone().multiply(Math.cos(ang) * r)).add(axes[1].clone().multiply(Math.sin(ang) * r));
		}
		// Gentle side-to-side sway + a subtle in/out bob, the tip (head) swaying most; scaled by how far it's out.
		double tip = (double) (PEEK_SEGMENTS - i) / PEEK_SEGMENTS;
		base.add(axes[1].clone().multiply(Math.sin(swayPhase) * 0.14 * tip * progress));
		base.add(direction.clone().multiply(Math.sin(swayPhase * 0.6) * 0.04 * tip * progress));
		if (i == 0) {
			// Nudge the head off the top body segment ALONG the direction it's looking, but only to the extent that look is
			// PERPENDICULAR to the worm's axis. When it looks straight out along its own body (e.g. a floor worm staring up
			// with no player around) no nudge is needed — adding one just lifts the head off the body and detaches it.
			Vector lookDir = look.lengthSquared() > 1.0E-6 ? look.clone().normalize() : direction.clone();
			double dot = lookDir.dot(direction);
			base.add(lookDir.multiply(0.18 * progress * (1.0 - Math.abs(dot))));
			// A small constant lean into the facing direction (+0.1) so the head reads as separate from the neck, plus a slight
			// vertical shift away from the host face: up for a floor worm, down for a ceiling worm, none for a side wall.
			double vy = face == BlockFace.UP ? 0.05 : (face == BlockFace.DOWN ? -0.05 : 0.0);
			// When it turns more than 90° back from the emergence direction (dot < 0), push the head up to 0.8 further forward
			// so the sharply-bent neck/body doesn't clip through it.
			double hardAngle = Math.max(0.0, -dot) * 0.8;
			base.add(lookDir.multiply((0.1 + hardAngle) * progress)).add(0, vy * progress, 0);
			// A floor worm staring straight up (no side target) sits too close to the body — lift the head extra, scaled by
			// how vertical its gaze is (so it doesn't apply when it's turned to look at something off to the side).
			if (face == BlockFace.UP)
				base.add(0, 0.2 * progress * Math.max(0.0, dot), 0);
			base.setDirection(look);
		} else {
			ArmorStand prev = segOrNull(i - 1);
			base.setDirection((prev != null ? prev.getLocation().toVector() : loc.toVector()).subtract(base.toVector()));
		}
		return base;
	}
	private void resetPeekTowards() {
		if (face == BlockFace.UP || face == BlockFace.DOWN)
			peekVec = new Vector(0, 0, 0);
		else
			peekVec = direction.clone().multiply(-0.1);
		staring = loc.clone().add(direction.clone().multiply(5));
		staringTowards = staring.toVector().subtract(loc.toVector());
	}
	private void spawnCrack() {
		particleLoc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), particleLoc, 3, dX, dY, dZ, 1, blockMaterial.createBlockData());
	}
	private ArmorStand seg(int index, Location spawnLoc) {
		return (ArmorStand) getCreatureStandEntity(index, spawnLoc);
	}
	private ArmorStand segOrNull(int index) {
		ArmorStand stand = (ArmorStand) getCreatureStandEntityOrNull(index);
		return stand != null && stand.isValid() ? stand : null;
	}
	private void removeSeg(int index) {
		ArmorStand stand = (ArmorStand) getCreatureStandEntityOrNull(index);
		if (stand != null)
			stand.remove();
	}
	public void unload() {
		super.unload();
		releaseTarget();
	}
	public void onChangeBlock(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	private void updateLocation(Block block, BlockFace direction) {
		if (direction == null) {
			Entity self = getCastedEntity();
			if (self != null)
				self.remove();
			return;
		}
		this.block = block;
		this.face = direction;
		this.dX = .2;
		this.dY = .2;
		this.dZ = .2;
		this.particleLoc = block.getRelative(face).getLocation();
		switch (direction) {
		default:
		case UP:
			this.direction = new Vector(0, 1, 0);
			this.standYaw = 0;
			this.headPosePitch = Math.toRadians(0);
			this.lockPitch = -90;
			this.loc = block.getLocation().add(.5, .7, .5);
			this.dY = .05;
			particleLoc.add(.5, 0, .5);
			this.tpOffset = loc.clone().subtract(0, 0.95, 0);
			this.playerLockOffset = 1.0;
			this.peekVec = new Vector(0, 0, 0);
			break;
		case DOWN:
			this.direction = new Vector(0, -1, 0);
			this.standYaw = 0;
			this.headPosePitch = Math.toRadians(180);
			this.lockPitch = 90;
			this.loc = block.getLocation().add(.5, 1, .5);
			this.dY = .05;
			particleLoc.add(.5, 1, .5);
			this.tpOffset = loc.clone().subtract(0, 1.2, 0);
			this.playerLockOffset = 1.5;
			this.peekVec = new Vector(0, 0, 0);
			break;
		case NORTH:
			this.direction = new Vector(0, 0, -1);
			this.standYaw = 180;
			this.headPosePitch = Math.toRadians(90);
			this.lockPitch = 0;
			this.loc = block.getLocation().add(.5, .9, .5);
			this.dZ = .05;
			particleLoc.add(.5, .5, 1);
			this.tpOffset = loc.clone().subtract(0, 1.1, 0);
			this.playerLockOffset = 1.15;
			this.peekVec = this.direction.clone().multiply(-0.1);
			break;
		case EAST:
			this.direction = new Vector(1, 0, 0);
			this.standYaw = -90;
			this.headPosePitch = Math.toRadians(90);
			this.lockPitch = 0;
			this.loc = block.getLocation().add(.5, .9, .5);
			this.dX = .05;
			particleLoc.add(0, .5, .5);
			this.tpOffset = loc.clone().subtract(0, 1.1, 0);
			this.playerLockOffset = 1.15;
			this.peekVec = this.direction.clone().multiply(-0.1);
			break;
		case SOUTH:
			this.direction = new Vector(0, 0, 1);
			this.standYaw = 0;
			this.headPosePitch = Math.toRadians(90);
			this.lockPitch = 0;
			this.loc = block.getLocation().add(.5, .9, .5);
			this.dZ = .05;
			particleLoc.add(.5, .5, 0);
			this.tpOffset = loc.clone().subtract(0, 1.1, 0);
			this.playerLockOffset = 1.15;
			this.peekVec = this.direction.clone().multiply(-0.1);
			break;
		case WEST:
			this.direction = new Vector(-1, 0, 0);
			this.standYaw = 90;
			this.headPosePitch = Math.toRadians(90);
			this.lockPitch = 0;
			this.loc = block.getLocation().add(.5, .9, .5);
			this.dX = .05;
			particleLoc.add(1, .5, .5);
			this.tpOffset = loc.clone().subtract(0, 1.1, 0);
			this.playerLockOffset = 1.15;
			this.peekVec = this.direction.clone().multiply(-0.1);
			break;
		}
		this.loc.setYaw(standYaw);
		this.staring = loc.clone().add(this.direction.clone().multiply(5));
		this.staringTowards = staring.toVector().subtract(loc.toVector());
	}
	/**
	 * Public API for the DeadlyDisasters Infested Cave disaster. Enables the legacy "drag to wall, carry to the Warden,
	 * spit out and anger" behavior on a specific worm. Because UltimateContent cannot reference DeadlyDisasters, the
	 * disaster passes in the Warden; call this right after spawning the worm.
	 *
	 * @param wormEntity the worm's spawned entity (the {@link ArmorStand} returned from spawning the custom entity)
	 * @param warden the disaster's Warden the worm should carry victims to
	 * @return true if the entity was a loaded InfestedWorm and the behavior was enabled
	 */
	public static boolean enableCaveBehavior(Entity wormEntity, LivingEntity warden) {
		if (wormEntity == null)
			return false;
		CustomEntity<?> custom = UIEntityManager.getEntity(wormEntity);
		if (!(custom instanceof InfestedWorm worm))
			return false;
		worm.caveBehavior = true;
		worm.caveWarden = warden;
		if (warden != null && !warden.isDead())
			worm.caveWardenLastLoc = warden.getLocation();
		return true;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedWorm.REGISTERED_KEY, InfestedWorm.class);

		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.NORMAL))
				return false;
			if (loc.getBlock().getBiome() != Biome.DEEP_DARK)
				return false;
			return true;
		});
		type.setSpawnByCommandConditions(parameters -> {
			if (!(parameters.sender instanceof Player player))
				return true;
			// Ray-trace so we learn not just which block but which *face* the player is looking at, and burrow out of it.
			RayTraceResult ray = player.rayTraceBlocks(5, FluidCollisionMode.NEVER);
			Block temp = ray == null ? null : ray.getHitBlock();
			if (temp == null || !temp.getType().isSolid()) {
				parameters.sender.sendMessage(Utils.convertString("&cYou can only summon an infested worm on a solid block! Please look at a solid block when running this command!"));
				return false;
			}
			parameters.location = BlockUtils.getCenterOfBlock(temp);
			pendingCommandFace = ray.getHitBlockFace();
			return true;
		});
	}
}
