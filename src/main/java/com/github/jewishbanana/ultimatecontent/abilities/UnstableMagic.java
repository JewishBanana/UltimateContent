package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.DarkMage;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class UnstableMagic extends AbilityAttributes {

	public static final String REGISTERED_KEY = "uc:unstable_magic";

	private static final String[] SPELL_KEYS = {
		"shadow_bolt_volley", "shade_step", "curse_of_leeching", "dark_blessing",
		"arcane_recoil", "gravity_collapse", "arcane_confusion", "hex_aura",
		"phantom_surge", "skeleton_summon"
	};

	// Tracks ShulkerBullets spawned by Shadow Bolt Volley so the hit handler can apply effects.
	private static final Set<UUID> activeBullets = ConcurrentHashMap.newKeySet();

	private static final int BULLET_COUNT = 12;
	private static final double BULLET_RANGE = 20.0;
	private static final int ORBIT_TICKS = 20;
	private static final int BULLET_LIFETIME_TICKS = 100;
	private static final int BLINDNESS_TICKS = 60;

	private int[] spellWeights;
	private int totalWeight;

	private Target target = Target.ACTIVATOR;

	public UnstableMagic(UIAbilityType type) {
		super(type);
	}

	public void activate(Entity entity, GenericItem base) {
		if (!(entity instanceof LivingEntity caster)) return;
		int spell = rollSpell();
		if (spell < 0) return;
		castSpell(caster, spell);
	}
	@Override
	public void onMobHoldTick(Mob mob, GenericItem item) {
		LivingEntity target = mob.getTarget();
		if (target == null || target.isDead())
			return;
		if (mob.getLocation().distanceSquared(target.getLocation()) > 121) // 11 blocks
			return;
		if (!canActivateInRegion(mob))
			return;
		// A Dark Mage casts only the controlled, useful spells (0-3); any other mob holding the
		// wand gets the full chaotic spread including the self-harmful spells.
		int spell = UIEntityManager.getEntity(mob) instanceof DarkMage ? random.nextInt(4) : rollSpell();
		if (spell < 0)
			return;
		if (!use(mob)) // respect the configured cooldown between casts
			return;
		castSpell(mob, spell);
	}
	private void castSpell(LivingEntity caster, int spell) {
		switch (spell) {
			case 0 -> castShadowBoltVolley(caster);
			case 1 -> castShadeStep(caster);
			case 2 -> castCurseOfLeeching(caster);
			case 3 -> castDarkBlessing(caster);
			case 4 -> castArcaneRecoil(caster);
			case 5 -> castGravityCollapse(caster);
			case 6 -> castArcaneConfusion(caster);
			case 7 -> castHexAura(caster);
			case 8 -> castPhantomSurge(caster);
			case 9 -> castSkeletonSummon(caster);
		}
	}

	private int rollSpell() {
		if (totalWeight <= 0) return -1;
		int roll = random.nextInt(totalWeight);
		int cumulative = 0;
		for (int i = 0; i < spellWeights.length; i++) {
			cumulative += spellWeights[i];
			if (roll < cumulative) return i;
		}
		return spellWeights.length - 1;
	}

	// -----------------------------------------------------------------------
	// Spell 0 – Shadow Bolt Volley
	// Two counter-rotating rings of ShulkerBullets that spiral outward, then
	// lock onto the nearest target (or scatter if none).
	// Static so it can be called by both player wand activation and mob AI.
	// -----------------------------------------------------------------------
	private static void castShadowBoltVolley(LivingEntity entity) {
		int half = BULLET_COUNT / 2;
		double angleStep = Math.PI * 2.0 / half;
		ShulkerBullet[] bullets = new ShulkerBullet[BULLET_COUNT];
		for (int i = 0; i < half; i++) {
			Vector off = new Vector(1.0, 1.6, 0).rotateAroundY(i * angleStep).normalize();
			bullets[i] = entity.getWorld().spawn(entity.getLocation().add(off), ShulkerBullet.class);
			bullets[i].setGravity(false);
			bullets[i].setShooter(entity);
			activeBullets.add(bullets[i].getUniqueId());
			scheduleBulletCleanup(bullets[i]);
		}
		for (int i = half; i < BULLET_COUNT; i++) {
			Vector off = new Vector(1.0, 1.0, 0).rotateAroundY((i - half + 0.5) * angleStep).normalize();
			bullets[i] = entity.getWorld().spawn(entity.getLocation().add(off), ShulkerBullet.class);
			bullets[i].setGravity(false);
			bullets[i].setShooter(entity);
			activeBullets.add(bullets[i].getUniqueId());
			scheduleBulletCleanup(bullets[i]);
		}
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.75f, 0.5f);

		double[] speed = {0.05};
		new BukkitRunnable() {
			private int tick;

			@Override
			public void run() {
				if (!entity.isValid() || ++tick > ORBIT_TICKS) {
					releaseBullets(entity, bullets, half);
					this.cancel();
					return;
				}
				speed[0] += 0.09;
				Location eLoc = entity.getLocation();
				double yBase = eLoc.getY() + 1.0;
				for (int i = 0; i < half; i++) {
					if (!bullets[i].isValid()) continue;
					Location temp = bullets[i].getLocation();
					temp.setY(yBase + 0.3);
					bullets[i].teleport(temp);
					bullets[i].setVelocity(new Vector(temp.getX() - eLoc.getX(), 0, temp.getZ() - eLoc.getZ())
							.rotateAroundY(1.2).normalize().multiply(speed[0]).setY(0.04));
					entity.getWorld().spawnParticle(Particle.SQUID_INK, temp, 1, 0.05, 0.05, 0.05, 0.01);
				}
				for (int i = half; i < BULLET_COUNT; i++) {
					if (!bullets[i].isValid()) continue;
					Location temp = bullets[i].getLocation();
					temp.setY(yBase - 0.3);
					bullets[i].teleport(temp);
					bullets[i].setVelocity(new Vector(temp.getX() - eLoc.getX(), 0, temp.getZ() - eLoc.getZ())
							.rotateAroundY(-1.2).normalize().multiply(speed[0]).setY(0.04));
					entity.getWorld().spawnParticle(Particle.SOUL, temp, 1, 0.05, 0.05, 0.05, 0.01);
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}

	private static void releaseBullets(LivingEntity entity, ShulkerBullet[] bullets, int half) {
		if (!entity.isValid()) {
			for (ShulkerBullet b : bullets)
				if (b.isValid()) { activeBullets.remove(b.getUniqueId()); b.remove(); }
			return;
		}
		Location eLoc = entity.getLocation();
		LivingEntity nearestTarget = findNearestTarget(entity, BULLET_RANGE);
		for (ShulkerBullet b : bullets) {
			if (!b.isValid()) continue;
			if (nearestTarget != null) {
				b.setTarget(nearestTarget);
				b.setVelocity(nearestTarget.getEyeLocation().subtract(b.getLocation()).toVector().normalize().multiply(1.2));
			} else {
				b.setVelocity(new Vector(b.getLocation().getX() - eLoc.getX(), 0.05,
						b.getLocation().getZ() - eLoc.getZ()).normalize().multiply(0.8));
			}
		}
		entity.getWorld().spawnParticle(Particle.SOUL, eLoc.clone().add(0, 1, 0), 20, 0.6, 0.6, 0.6, 0.05);
		entity.getWorld().playSound(eLoc, Sound.ENTITY_SHULKER_SHOOT, 0.8f, 0.5f);
	}
	private static void scheduleBulletCleanup(ShulkerBullet bullet) {
		final UUID uuid = bullet.getUniqueId();
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			activeBullets.remove(uuid);
			if (bullet.isValid())
				bullet.remove();
		}, BULLET_LIFETIME_TICKS);
	}

	// -----------------------------------------------------------------------
	// Spell 1 – Shade Step
	// Teleport behind the nearest enemy; brief Speed II + Invisibility flash.
	// Falls back to a random 5-block teleport when no enemy is in range.
	// -----------------------------------------------------------------------
	private static void castShadeStep(LivingEntity entity) {
		LivingEntity nearestTarget = findNearestTarget(entity, BULLET_RANGE);
		Location dest;
		if (nearestTarget != null) {
			Vector toTarget = Utils.getVectorTowards(entity.getLocation(), nearestTarget.getLocation()).normalize();
			dest = nearestTarget.getLocation().clone().add(toTarget.multiply(2));
			dest.setY(dest.getWorld().getHighestBlockAt(dest).getY() + 1);
			dest.setDirection(Utils.getVectorTowards(dest, nearestTarget.getLocation()));
		} else {
			double angle = random.nextDouble() * Math.PI * 2;
			dest = entity.getLocation().clone().add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
			dest.setY(dest.getWorld().getHighestBlockAt(dest).getY() + 1);
		}
		entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
		entity.teleport(dest);
		entity.getWorld().spawnParticle(Particle.SOUL, dest.clone().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.05);
		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20, 0, true));
		entity.getWorld().playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.8f);
	}

	// -----------------------------------------------------------------------
	// Spell 2 – Curse of Leeching
	// Drain 3 hearts from every enemy within 8 blocks and transfer the HP.
	// -----------------------------------------------------------------------
	private static void castCurseOfLeeching(LivingEntity entity) {
		List<LivingEntity> targets = new ArrayList<>();
		for (Entity e : entity.getWorld().getNearbyEntities(entity.getLocation(), 8, 8, 8)) {
			if (!(e instanceof LivingEntity le) || e.equals(entity) || EntityUtils.isEntityImmunePlayer(e))
				continue;
			targets.add(le);
		}
		double healTotal = 0;
		Location cDst = entity.getLocation().add(0, 1, 0);
		for (LivingEntity t : targets) {
			double drain = Math.min(6.0, t.getHealth());
			t.damage(6.0, entity);
			healTotal += drain;
			Location src = t.getLocation().add(0, 1, 0);
			for (int step = 1; step <= 6; step++) {
				double frac = (double) step / 6;
				Location pt = src.clone().add(
						(cDst.getX() - src.getX()) * frac,
						(cDst.getY() - src.getY()) * frac,
						(cDst.getZ() - src.getZ()) * frac);
				entity.getWorld().spawnParticle(Particle.SOUL, pt, 1, 0.05, 0.05, 0.05, 0.01);
			}
		}
		if (healTotal > 0)
			entity.setHealth(Math.min(entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), entity.getHealth() + Math.min(healTotal, 16.0)));
		entity.getWorld().spawnParticle(Particle.WITCH, cDst, 25, 0.4, 0.5, 0.4, 0.05);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 0.8f, 0.4f);
	}

	// -----------------------------------------------------------------------
	// Spell 3 – Dark Blessing
	// Regeneration III + Absorption II for 8 seconds. Pure positive outcome.
	// -----------------------------------------------------------------------
	private static void castDarkBlessing(LivingEntity entity) {
		entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 2, true));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 160, 1, true));
		entity.getWorld().spawnParticle(Particle.SOUL, entity.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.04);
		entity.getWorld().spawnParticle(Particle.WITCH, entity.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.04);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f, 0.7f);
	}

	// -----------------------------------------------------------------------
	// Spell 4 – Arcane Recoil (harmful — player only)
	// Blast the caster backward, apply Blindness + Nausea.
	// -----------------------------------------------------------------------
	private void castArcaneRecoil(LivingEntity player) {
		player.setVelocity(player.getLocation().getDirection().multiply(-2.0).setY(0.5));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true));
		player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, true));
		player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation().add(0, 1, 0), 35, 0.5, 0.5, 0.5, 0.08);
		player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.02);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 0.7f, 0.6f);
	}

	// -----------------------------------------------------------------------
	// Spell 5 – Gravity Collapse (harmful — player only)
	// Launches the caster straight up ~15 blocks — fall damage on landing.
	// -----------------------------------------------------------------------
	private void castGravityCollapse(LivingEntity player) {
		player.setVelocity(new Vector(0, 2.5, 0));
		player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 30, 0.3, 0.1, 0.3, 0.15);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 0.5f);
	}

	// -----------------------------------------------------------------------
	// Spell 6 – Arcane Confusion (harmful — player only)
	// Nausea + Darkness + Slowness II on the caster for 3 seconds.
	// -----------------------------------------------------------------------
	private void castArcaneConfusion(LivingEntity player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, true));
		player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, true));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, true));
		player.getWorld().spawnParticle(Particle.SQUID_INK, player.getLocation().add(0, 1, 0), 30, 0.4, 0.5, 0.4, 0.05);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.8f, 0.5f);
	}

	// -----------------------------------------------------------------------
	// Spell 7 – Hex Aura (mixed — player only)
	// Resistance II (good) AND Poison II (bad) for 10 seconds simultaneously.
	// -----------------------------------------------------------------------
	private void castHexAura(LivingEntity player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, true));
		player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1, true));
		player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 35, 0.3, 0.5, 0.3, 0.06);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 0.9f, 0.5f);
	}

	// -----------------------------------------------------------------------
	// Spell 8 – Phantom Surge (mixed — player only)
	// Summon 2-3 Phantoms that fight the nearest enemy for 8 seconds, then
	// turn on the caster.
	// -----------------------------------------------------------------------
	private void castPhantomSurge(LivingEntity player) {
		LivingEntity initialTarget = findNearestTarget(player, BULLET_RANGE);
		int count = 2 + random.nextInt(2);
		List<Phantom> phantoms = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			double angle = (Math.PI * 2.0 / count) * i;
			Location spawnLoc = player.getLocation().clone().add(Math.cos(angle) * 3, 3, Math.sin(angle) * 3);
			Phantom ph = player.getWorld().spawn(spawnLoc, Phantom.class);
			ph.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0));
			if (initialTarget != null) ph.setTarget(initialTarget);
			phantoms.add(ph);
		}
		player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 2, 0), 25, 0.5, 0.5, 0.5, 0.06);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.9f, 0.5f);
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Phantom ph : phantoms)
					if (ph.isValid()) ph.setTarget(player);
			}
		}.runTaskLater(plugin, 160);
	}

	// -----------------------------------------------------------------------
	// Spell 9 – Skeleton Summon (neutral — player only)
	// Two skeletons rise from the ground with a breaking-out animation.
	// They target the caster's last attacker; if none, they become regular mobs.
	// -----------------------------------------------------------------------
	private void castSkeletonSummon(LivingEntity player) {
		LivingEntity attacker = getLastAttacker(player);
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 0.8f, 0.5f);
		for (int n = 0; n < 2; n++) {
			boolean spawned = false;
			for (int attempt = 0; attempt < 15 && !spawned; attempt++) {
				double angle = random.nextDouble() * Math.PI * 2;
				double dist = 1.5 + random.nextDouble() * 2.5;
				Location check = player.getLocation().clone().add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist);
				Block ground = check.getWorld().getHighestBlockAt(check);
				if (ground.isPassable()) continue;
				Block above1 = ground.getRelative(BlockFace.UP);
				Block above2 = above1.getRelative(BlockFace.UP);
				if (!above1.isPassable() || !above2.isPassable()) continue;

				Location surfaceLoc = ground.getLocation().add(0.5, 1, 0.5);
				Location underLoc = surfaceLoc.clone().subtract(0, 1.5, 0);
				final LivingEntity finalAttacker = attacker;

				Skeleton sk = player.getWorld().spawn(underLoc, Skeleton.class);
				sk.setAI(false);
				sk.setGravity(false);
				sk.setInvulnerable(true);
				sk.setSilent(true);

				new BukkitRunnable() {
					private int tick;

					@Override
					public void run() {
						if (!sk.isValid()) { this.cancel(); return; }
						if (tick < 15) {
							sk.teleport(sk.getLocation().add(0, 0.1, 0));
							player.getWorld().spawnParticle(VersionUtils.getBlockCrack(), surfaceLoc.clone(), 4, 0.3, 0.1, 0.3, 0.1, ground.getBlockData());
							player.getWorld().spawnParticle(Particle.SMOKE, surfaceLoc.clone(), 2, 0.2, 0.1, 0.2, 0.02);
							tick++;
						} else {
							sk.setAI(true);
							sk.setGravity(true);
							sk.setInvulnerable(false);
							sk.setSilent(false);
							if (finalAttacker != null && finalAttacker.isValid() && !finalAttacker.isDead())
								sk.setTarget(finalAttacker);
							player.getWorld().playSound(sk.getLocation(), Sound.ENTITY_SKELETON_AMBIENT, 0.9f, 0.8f);
							this.cancel();
						}
					}
				}.runTaskTimer(plugin, 0, 1);
				spawned = true;
			}
		}
	}

	// -----------------------------------------------------------------------
	// Utility helpers
	// -----------------------------------------------------------------------
	private static LivingEntity findNearestTarget(LivingEntity caster, double range) {
		LivingEntity nearest = null;
		double nearestSq = range * range;
		for (Entity e : caster.getWorld().getNearbyEntities(caster.getLocation(), range, range, range)) {
			if (!(e instanceof LivingEntity le) || e.equals(caster) || EntityUtils.isEntityImmunePlayer(e))
				continue;
			double distSq = e.getLocation().distanceSquared(caster.getLocation());
			if (distSq < nearestSq) {
				nearestSq = distSq;
				nearest = le;
			}
		}
		return nearest;
	}

	private LivingEntity getLastAttacker(LivingEntity player) {
		EntityDamageEvent last = player.getLastDamageCause();
		if (last instanceof EntityDamageByEntityEvent byEntity) {
			Entity dmger = byEntity.getDamager();
			if (dmger instanceof LivingEntity le && le.isValid() && !le.isDead())
				return le;
		}
		return null;
	}

	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, UnstableMagic.class);
		plugin.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onProjectileHit(ProjectileHitEvent event) {
				if (!activeBullets.remove(event.getEntity().getUniqueId())) return;
				if (!(event.getHitEntity() instanceof LivingEntity hitTarget)) return;
				hitTarget.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, BLINDNESS_TICKS, 0, true));
				hitTarget.removePotionEffect(PotionEffectType.LEVITATION);
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
					if (hitTarget.isValid()) hitTarget.removePotionEffect(PotionEffectType.LEVITATION);
				}, 1);
				hitTarget.setVelocity(event.getEntity().getVelocity().multiply(2.5).setY(0.8));
			}
		}, plugin);
	}

	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		spellWeights = new int[SPELL_KEYS.length];
		int total = 0;
		for (int i = 0; i < SPELL_KEYS.length; i++) {
			spellWeights[i] = Math.max(0, getIntegerField("spells." + SPELL_KEYS[i], 10));
			total += spellWeights[i];
		}
		totalWeight = total;
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
}
