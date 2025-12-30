package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.events.CustomEntitySpawnEvent;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.ExplodingEntity;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Frosty;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SkeletonKnight;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ZombieKnight;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class EntitiesHandler implements Listener {
	
	private static NamespacedKey removeKey;
	private static Set<UUID> noBurnMobs;
	private static Set<UUID> noSuffocateMobs;
	private static Set<UUID> invulnerableEntities;
	
	public static Map<UUID, VoidWorm> voidWormFangs;
	public static Map<UUID, VoidWorm> voidWormEntities;
	public static Map<UUID, Elf> elfArrows;
	public static Map<UUID, Frosty> frostySnowballs;
	public static Map<UUID, ExplodingEntity> explodingEntities;
	
	static {
		removeKey = new NamespacedKey(Main.getInstance(), "uck");
		noBurnMobs = new HashSet<>();
		noSuffocateMobs = new HashSet<>();
		invulnerableEntities = new HashSet<>();
		voidWormFangs = new HashMap<>();
		voidWormEntities = new HashMap<>();
		elfArrows = new HashMap<>();
		frostySnowballs = new HashMap<>();
		explodingEntities = new HashMap<>();
	}
	
	public EntitiesHandler(Main plugin) {
		plugin.getServer().getWorlds().forEach(world -> world.getEntities().stream().filter(e -> e.getPersistentDataContainer().has(removeKey, PersistentDataType.BYTE)).forEach(e -> e.remove()));
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
		UUID uuid = event.getDamager().getUniqueId();
		if (voidWormFangs.containsKey(uuid)) {
			event.setCancelled(true);
			VoidWorm worm = voidWormFangs.remove(uuid);
			if (event.getEntity() instanceof LivingEntity)
				((LivingEntity) event.getEntity()).damage(worm.getEntityVariant().damage, worm.getEntity());
		}
		Elf elf = elfArrows.get(uuid);
		if (elf != null && (event.getEntity().hasMetadata("uc-christmasmobs") || event.getEntity().getUniqueId().equals(elf.getOwner())))
			event.setCancelled(true);
		if (frostySnowballs.containsKey(uuid))
			event.setDamage(frostySnowballs.remove(uuid).getEntityVariant().damage);
		if (explodingEntities.containsKey(uuid))
			event.setDamage(event.getDamage() * explodingEntities.get(uuid).getExplosionDamageMultiplier());
	}
	@EventHandler(ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile entity = event.getEntity();
		if (entity.hasMetadata("uc-elfarrow")) {
			entity.getWorld().createExplosion(entity.getLocation(), 1.5f, false, false, entity);
			entity.remove();
			elfArrows.remove(entity.getUniqueId());
			if (event.getHitEntity() instanceof LivingEntity hitEntity)
				hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false));
		}
	}
//	@EventHandler
//	public void onInteract(PlayerInteractEvent event) {
//		if (event.getAction() == Action.LEFT_CLICK_AIR) {
//			PhysicsEngine.dropBlockWithPhysics(event.getPlayer().getEyeLocation(), Material.DIAMOND_BLOCK, 0.5f, event.getPlayer().getLocation().getDirection(), 0.04, 600);
//			Bukkit.broadcastMessage("SPAWNED");
//		}
//	}
	@EventHandler(ignoreCancelled = true)
	public void onCombust(EntityCombustEvent event) {
		if (noBurnMobs.contains(event.getEntity().getUniqueId()) && event.getEventName().equals("EntityCombustEvent"))
			event.setCancelled(true);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCustomEntitySpawn(CustomEntitySpawnEvent event) {
		if (event.getEntityClass() instanceof SkeletonKnight casted)
			casted.spawnHorse();
		if (event.getEntityClass() instanceof ZombieKnight casted)
			casted.spawnHorse();
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		Location loc = event.getBlock().getLocation().add(.5, .5, .5);
		InfestedDevourer.infestedDevourers.forEach(e -> {
			if (Utils.isLocationsWithinDistance(e.getEntityLocation(), loc, 225))
				e.blocks.add(event.getBlock());
		});
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
}
