package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.events.CustomEntitySpawnEvent;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.ExplodingEntity;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Frosty;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SkeletonKnight;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ZombieKnight;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class EntitiesHandler implements Listener {
	
	private static final NamespacedKey removeKey;
	private static final String lastAttackerKey = "uc-last-attacker";
	private static final Set<UUID> noBurnMobs;
	private static final Set<UUID> noSuffocateMobs;
	private static final Set<UUID> invulnerableEntities;
	private static final Set<UUID> noItemMergeEntities;
	
	public static final Map<UUID, VoidWorm> voidWormFangs;
	public static final Map<UUID, VoidWorm> voidWormEntities;
	public static final Map<UUID, Elf> elfArrows;
	public static final Map<UUID, Frosty> frostySnowballs;
	public static final Map<UUID, ExplodingEntity> explodingEntities;
	
	static {
		removeKey = new NamespacedKey(UltimateContent.getInstance(), "uck");
		noBurnMobs = new HashSet<>();
		noSuffocateMobs = new HashSet<>();
		invulnerableEntities = new HashSet<>();
		noItemMergeEntities = new HashSet<>();
		
		voidWormFangs = new HashMap<>();
		voidWormEntities = new HashMap<>();
		elfArrows = new HashMap<>();
		frostySnowballs = new HashMap<>();
		explodingEntities = new HashMap<>();
	}
	
	public EntitiesHandler(UltimateContent plugin) {
		plugin.getServer().getWorlds().forEach(world -> world.getEntities().stream().filter(e -> e.getPersistentDataContainer().has(removeKey, PersistentDataType.BYTE)).forEach(e -> e.remove()));

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		startWardenFactionAngerReset(plugin);
	}
	/**
	 * Wardens use their own anger/disturbance system rather than the normal target event, so {@link #onEntityTarget}'s
	 * cancellation isn't enough to keep them from attacking infested mobs (a creeper blast, vibration, etc. can still anger
	 * them at the swarm). This timer keeps the infested faction intact by continuously zeroing every nearby Warden's anger
	 * toward infested mobs. Wardens are rare, so iterating them every two seconds is cheap.
	 */
	private void startWardenFactionAngerReset(UltimateContent plugin) {
		if (!VersionUtils.isMCVersionOrAbove("1.19"))
			return;
		plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
			for (World world : plugin.getServer().getWorlds())
				for (Entity warden : world.getEntities()) {
					if (!isWarden(warden))
						continue;
					for (Entity nearby : warden.getNearbyEntities(40, 40, 40))
						if (isInfested(nearby) && getWardenAnger(warden, nearby) > 0)
							setWardenAnger(warden, nearby, 0);
				}
		}, 40L, 40L);
	}
	/**
	 * Custom (non-UC) mobs spawned holding UC items - most notably via the {@code /ui summon ui:debug_mob} testing command -
	 * should still use their items' abilities. UC's own custom entities drive themselves from {@code BaseEntity.spawn}/
	 * {@code postLoad}; this only bridges the remaining spawn-equipped vanilla mobs. Equipment is applied right after the spawn
	 * event so the check is deferred one tick.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onMobSpawnWithItems(CreatureSpawnEvent event) {
		SpawnReason reason = event.getSpawnReason();
		if (reason != SpawnReason.CUSTOM && reason != SpawnReason.COMMAND && reason != SpawnReason.SPAWNER_EGG && reason != SpawnReason.DISPENSE_EGG)
			return;
		if (!(event.getEntity() instanceof Mob mob))
			return;
		JavaPlugin plugin = UltimateContent.getInstance();
		plugin.getServer().getScheduler().runTask(plugin, () -> {
			if (mob.isValid() && UIEntityManager.getEntity(mob) == null)
				BaseEntity.attachMobItemAbilities(mob);
		});
	}
	@EventHandler
	public void onEntitiesLoad(EntitiesLoadEvent event) {
		event.getEntities().forEach(e -> {
			if (e.getPersistentDataContainer().has(removeKey, PersistentDataType.BYTE))
				e.remove();
		});
	}
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		UUID uuid = event.getEntity().getUniqueId();
		if (invulnerableEntities.contains(uuid)) {
			event.setCancelled(true);
			return;
		}
		if (voidWormEntities.containsKey(uuid)) {
			LivingEntity entity = voidWormEntities.get(uuid).getCastedEntity();
			if (entity != null)
				entity.setHealth(Math.max(entity.getHealth() - event.getFinalDamage(), 0));
			event.setDamage(0);
		}
		if (event.getCause() == DamageCause.SUFFOCATION && noSuffocateMobs.contains(uuid))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
		// Remember who last hit a mob (resolving projectiles back to their shooter) so the infested-retaliation logic can
		// tell a "provoked" mob — one a player/entity attacked first — from one that merely wandered onto another target.
		if (event.getEntity() instanceof Mob hurt) {
			Entity attacker = event.getDamager();
			if (attacker instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter)
				attacker = shooter;
			hurt.setMetadata(lastAttackerKey, new org.bukkit.metadata.FixedMetadataValue(UltimateContent.getInstance(), attacker.getUniqueId().toString()));
		}
		UUID uuid = event.getDamager().getUniqueId();
		VoidWorm worm = voidWormFangs.remove(uuid);
		if (worm != null) {
			event.setCancelled(true);
			if (event.getEntity() instanceof LivingEntity living)
				living.damage(worm.getEntityVariant().damage, worm.getEntity());
		}
		Elf elf = elfArrows.get(uuid);
		if (elf != null && (event.getEntity().hasMetadata("uc-christmasmobs") || event.getEntity().getUniqueId().equals(elf.getOwner())))
			event.setCancelled(true);
		Frosty frosty = frostySnowballs.remove(uuid);
		if (frosty != null)
			event.setDamage(frosty.getEntityVariant().damage);
		ExplodingEntity explodingEntity = explodingEntities.get(uuid);
		if (explodingEntity != null) {
			double multiplier = explodingEntity.getExplosionDamageMultiplier();
			// Infested creepers only deal a fraction of their blast damage to fellow infested mobs and the Warden, so
			// they stop wiping out their own swarm.
			if (explodingEntity instanceof InfestedCreeper creeper && isInfestedOrWarden(event.getEntity()))
				multiplier *= creeper.getFriendlyFireMultiplier();
			event.setDamage(event.getDamage() * multiplier);
		}
	}
	/** True if the entity is the Warden or one of our infested custom mobs (the creeper's "friendly" blast targets). */
	private static boolean isInfestedOrWarden(Entity entity) {
		if (isWarden(entity))
			return true;
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		return custom instanceof BaseEntity<?> base && base.getEntityType().category == CustomEntityType.Category.INFESTED_ENTITIES;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityExplode(EntityExplodeEvent event) {
		// A creeper that explodes is removed without ever firing EntityDeathEvent, so UIFramework's death handler never
		// unloads its custom entity and the head armor stand is left behind until the chunk unloads. Detect the explosion
		// of one of our exploding custom entities and unload it now so its stands are cleaned up immediately.
		if (!explodingEntities.containsKey(event.getEntity().getUniqueId()))
			return;
		CustomEntity<? extends Entity> custom = UIEntityManager.removeEntity(event.getEntity().getUniqueId());
		if (custom != null)
			custom.unload();
	}
	@EventHandler(ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile entity = event.getEntity();
		if (entity.hasMetadata("uc-elfarrow")) {
			entity.getWorld().createExplosion(entity.getLocation(), 1.5f, false, false, entity);
			elfArrows.remove(entity.getUniqueId());
			entity.remove();
			if (event.getHitEntity() instanceof LivingEntity hitEntity)
				hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false));
		}
	}
//	@EventHandler
//	public void onInteract(PlayerInteractEvent event) {
//		if (event.getAction() == Action.LEFT_CLICK_AIR) {
//			PhysicsEngine.dropBlockWithPhysics(event.getPlayer().getEyeLocation(), Material.DIAMOND_BLOCK, 0.5f, event.getPlayer().getLocation().getDirection(), 0.04, 600);
//			Bukkit.broadcastMessage("SPAWNED");
//			Location exact = Utils.getCenterOfBlock(event.getPlayer().getLocation().getBlock());
//			exact.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, exact, 1, 0, 0, 0, 0.0001);
//			for (Block b : Utils.getBlocksInCylinderRadius(event.getPlayer().getLocation(), 1.8f, 2.5f))
//				b.getWorld().spawnParticle(Particle.FLAME, Utils.getCenterOfBlock(b), 1, 0, 0, 0, 0.0001);
//			event.getPlayer().sendMessage("is clear ? "+Utils.isAreaClear(event.getPlayer().getLocation(), 1.8f, 2.5f));
//		}
//	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onCombust(EntityCombustEvent event) {
		if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent)
			return;
		if (noBurnMobs.contains(event.getEntity().getUniqueId()))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onItemMerge(ItemMergeEvent event) {
		if (noItemMergeEntities.contains(event.getEntity().getUniqueId()) || noItemMergeEntities.contains(event.getTarget().getUniqueId()))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomEntitySpawn(CustomEntitySpawnEvent event) {
		if (event.getEntityClass() instanceof SkeletonKnight casted)
			casted.spawnHorse();
		if (event.getEntityClass() instanceof ZombieKnight casted)
			casted.spawnHorse();
		// Infested mobs are hostile to everything that isn't infested. Replace their default (player-only) target goals
		// with a single goal over all LivingEntities (players and other mobs at equal priority) that excludes fellow
		// infested mobs and the Warden, so they naturally attack anything non-infested.
		if (event.getEntityClass() instanceof BaseEntity<?> base
				&& base.getEntityType().category == CustomEntityType.Category.INFESTED_ENTITIES
				&& base.getEntity() instanceof Mob mob) {
			EntityBrain brain = BukkitBrain.getBrain(mob);
			EntityAI targets = brain.getTargetAI();
			targets.removeIf(p -> p.getPathfinder().getName().equals("PathfinderNearestAttackableTarget"));
			targets.put(new PathfinderNearestAttackableTarget<>(mob, LivingEntity.class, 10, true, false, EntitiesHandler::isValidInfestedTarget), 2);
		}
	}
	/** A warden never targets (and so never attacks/angers at) infested mobs, and infested mobs never target each other. */
	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetLivingEntityEvent event) {
		LivingEntity target = event.getTarget();
		if (target == null)
			return;
		if (isWarden(event.getEntity())) {
			if (isInfested(target))
				event.setCancelled(true);
			return;
		}
		if (isInfested(event.getEntity()) && (isWarden(target) || isInfested(target)))
			event.setCancelled(true);
		// Infested mobs target every non-infested mob, so a swarm will set upon a player's iron golems, wandering zombies,
		// etc. Make those hostile/neutral victims fight back against the infested attacker instead of standing idle: a victim
		// with no target locks onto the attacker; a victim already fighting another mob swaps to the attacker only if it is
		// closer; but a victim provoked by a player/entity that hit it first stays aggro on that attacker.
		else if (event.getEntity() instanceof Mob infested && isInfested(infested)
				&& target instanceof Mob victim && !isInfested(victim) && canRetaliate(victim)) {
			LivingEntity current = victim.getTarget();
			if (current == null || current.isDead())
				victim.setTarget(infested);
			else if (!current.equals(infested) && !wasProvokedBy(victim, current)
					&& infested.getLocation().distanceSquared(victim.getLocation()) < current.getLocation().distanceSquared(victim.getLocation()))
				victim.setTarget(infested);
		}
	}
	/** A hostile or neutral mob that can meaningfully fight back (e.g. zombie, iron golem) — not a purely passive animal. */
	private static boolean canRetaliate(Mob mob) {
		return mob instanceof org.bukkit.entity.Monster
				|| mob instanceof org.bukkit.entity.IronGolem
				|| mob instanceof org.bukkit.entity.Wolf
				|| mob instanceof org.bukkit.entity.PolarBear
				|| mob instanceof org.bukkit.entity.Bee
				|| mob instanceof org.bukkit.entity.Llama
				|| mob instanceof org.bukkit.entity.Panda
				|| mob instanceof org.bukkit.entity.Goat
				|| mob instanceof org.bukkit.entity.Dolphin;
	}
	/** True if {@code mob}'s current {@code target} is the same entity that last damaged it (so it is genuinely provoked). */
	private static boolean wasProvokedBy(Mob mob, LivingEntity target) {
		for (org.bukkit.metadata.MetadataValue value : mob.getMetadata(lastAttackerKey))
			if (target.getUniqueId().toString().equals(value.asString()))
				return true;
		return false;
	}
	/** True if the entity is one of our infested custom mobs. */
	private static boolean isInfested(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		return custom instanceof BaseEntity<?> base && base.getEntityType().category == CustomEntityType.Category.INFESTED_ENTITIES;
	}
	/** Valid prey for an infested mob: anything alive that isn't the Warden or another infested mob. */
	private static boolean isValidInfestedTarget(LivingEntity entity) {
		return !isWarden(entity) && !isInfested(entity);
	}
	private static boolean isWarden(Entity entity) {
		return entity != null && "WARDEN".equals(entity.getType().name());
	}
	private static int getWardenAnger(Entity warden, Entity target) {
		try {
			return (int) warden.getClass().getMethod("getAnger", Entity.class).invoke(warden, target);
		} catch (ReflectiveOperationException ignored) {
			return 0;
		}
	}
	private static void setWardenAnger(Entity warden, Entity target, int anger) {
		try {
			warden.getClass().getMethod("setAnger", Entity.class, int.class).invoke(warden, target, anger);
		} catch (ReflectiveOperationException ignored) {
			// Warden API is unavailable before 1.19; callers are version/type guarded.
		}
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		// The void worm can't be damaged; a player kills it by breaking its supporting block so it drops into the void. If a
		// void worm is standing on the block being broken, tag it as spleefed by this player (VoidWorm grants the credit if it
		// then dies to the void in time without landing again).
		Block broken = event.getBlock();
		for (Entity e : broken.getWorld().getNearbyEntities(broken.getLocation().add(0.5, 1.0, 0.5), 0.6, 0.6, 0.6))
			if (UIEntityManager.getEntity(e) instanceof VoidWorm worm
					&& e.getLocation().getBlock().getRelative(BlockFace.DOWN).equals(broken))
				worm.markSpleefed(event.getPlayer().getUniqueId());
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		if (!InfestedDevourer.infestedDevourers.isEmpty()) {
			Location loc = event.getBlock().getLocation().add(.5, .5, .5);
			new ArrayList<>(InfestedDevourer.infestedDevourers).forEach(e -> {
				if (Utils.isLocationsWithinDistance(e.getEntityLocation(), loc, 225))
					e.blocks.add(event.getBlock());
			});
		}
	}
	public static void attachRemoveKey(Entity entity) {
		if (entity != null)
			entity.getPersistentDataContainer().set(removeKey, PersistentDataType.BYTE, (byte) 0);
	}
	public static void makeEntityNoSunlightCombust(Entity entity) {
		if (entity != null)
			noBurnMobs.add(entity.getUniqueId());
	}
	public static void removeEntitiyNoSunlightCombust(UUID uuid) {
		noBurnMobs.remove(uuid);
	}
	public static void makeEntityNoSuffocate(Entity entity) {
		if (entity != null)
			noSuffocateMobs.add(entity.getUniqueId());
	}
	public static void removeEntitiyNoSuffocate(UUID uuid) {
		noSuffocateMobs.remove(uuid);
	}
	public static void makeEntityInvulnerable(Entity entity) {
		invulnerableEntities.add(entity.getUniqueId());
	}
	public static void removeInvulnerableEntity(UUID uuid) {
		invulnerableEntities.remove(uuid);
	}
	public static void makeItemNotMerge(Item entity) {
		noItemMergeEntities.add(entity.getUniqueId());
	}
	public static void removeItemNotMerge(UUID uuid) {
		noItemMergeEntities.remove(uuid);
	}
}
