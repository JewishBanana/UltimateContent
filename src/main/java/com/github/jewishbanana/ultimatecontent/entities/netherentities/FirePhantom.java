package com.github.jewishbanana.ultimatecontent.entities.netherentities;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class FirePhantom extends BaseEntity<Phantom> {
	
	public static final String REGISTERED_KEY = "uc:fire_phantom";

	public FirePhantom(Phantom entity) {
		super(entity, CustomEntityType.FIRE_PHANTOM);
		
		setInvisible(entity);
		entity.setVisualFire(true);
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location entityLoc = entity.getLocation();
				World world = entityLoc.getWorld();
				Location loc = entityLoc.clone();
				int upRange = 1;
				for (int i=0; i < 15; i++) {
					loc.setY(loc.getY()+1);
					Block b = loc.getBlock();
					if (b != null && (!b.isPassable() || b.isLiquid()))
						break;
					world.spawnParticle(Particle.FLAME, loc, 1, .5, 1, .5, 0.3);
					upRange++;
				}
				loc = entityLoc.clone();
				int downRange = 1;
				for (int i=0; i < 15; i++) {
					Block b = loc.getBlock();
					if (b != null && (!b.isPassable() || b.isLiquid()))
						break;
					world.spawnParticle(Particle.FLAME, loc, 1, .5, 1, .5, 0.3);
					loc.setY(loc.getY()-1);
					downRange++;
				}
				downRange--;
				double totalRange = upRange + downRange;
				double centerY = entityLoc.getY() + (upRange - downRange) / 2.0;
				Location center = entityLoc.clone();
				center.setY(centerY);
				Collection<Entity> nearbyEntities = world.getNearbyEntities(center, 1, totalRange / 2.0, 1);
				for (Entity e : nearbyEntities) {
					if (EntityUtils.isEntityImmunePlayer(e))
						continue;
					Location eLoc = e.getLocation();
					double eY = eLoc.getY();
					double entityY = entityLoc.getY();
					double entityHeight = e.getHeight();
					boolean canHit = false;
					if (eY > entityY) {
						double yDiff = eY - entityY;
						if (yDiff >= upRange)
							continue;
						for (double checkHeight = 0; checkHeight <= entityHeight; checkHeight += 0.5) {
							double checkY = eY + checkHeight;
							double pillarDiff = checkY - entityY;
							if (pillarDiff >= 0 && pillarDiff < upRange) {
								Location pillarCheckLoc = entityLoc.clone().add(0, pillarDiff, 0);
								if (!BlockUtils.rayTraceForSolid(pillarCheckLoc, eLoc.clone().add(0, checkHeight, 0))) {
									canHit = true;
									break;
								}
							}
						}
					} else {
						double yDiff = entityY - eY;
						if (yDiff >= downRange)
							continue;
						for (double checkHeight = 0; checkHeight <= entityHeight; checkHeight += 0.5) {
							double checkY = eY + checkHeight;
							double pillarDiff = entityY - checkY;
							if (pillarDiff >= 0 && pillarDiff < downRange) {
								Location pillarCheckLoc = entityLoc.clone().subtract(0, pillarDiff, 0);
								if (!BlockUtils.rayTraceForSolid(pillarCheckLoc, eLoc.clone().add(0, checkHeight, 0))) {
									canHit = true;
									break;
								}
							}
						}
					}
					if (!canHit)
						continue;
					int fireTicks = Math.max(e.getFireTicks(), 80);
					EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(entity, e, (float) fireTicks);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled())
						continue;
					e.setFireTicks(fireTicks);
				}
			}
		}.runTaskTimer(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				playSound(entity.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
			}
		}.runTaskTimer(plugin, 0, 10));
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		event.getEntity().setFireTicks(Math.max(event.getEntity().getFireTicks(), 200));
	}
	public void onCombust(EntityCombustEvent event) {
		event.setCancelled(true);
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		Entity entity = event.getEntity();
		Location entityLoc = entity.getLocation();
		entity.getWorld().spawnParticle(Particle.FLAME, entityLoc, 300, 0, 0, 0, .5);
		if (getSectionBoolean("spawnFireItems", true)) {
			Item[] items = new Item[6];
			for (int i=0; i < 6; i++) {
				items[i] = entity.getWorld().dropItemNaturally(entityLoc, new ItemStack(Material.BLAZE_POWDER));
				items[i].setPickupDelay(10000);
				items[i].setVisualFire(true);
				items[i].setVelocity(items[i].getVelocity().multiply(2.0));
				EntitiesHandler.makeEntityInvulnerable(items[i]);
				EntitiesHandler.attachRemoveKey(items[i]);
				EntitiesHandler.makeItemNotMerge(items[i]);
			}
			new BukkitRunnable() {
				private int tick;
				
				@Override
				public void run() {
					if (tick++ >= 40) {
						for (Item item : items)
							if (item != null) {
								Location loc = item.getLocation();
								playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.5f);
								item.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc, 5, .3, .3, .3, .001);
								UUID uuid = item.getUniqueId();
								item.remove();
								EntitiesHandler.removeInvulnerableEntity(uuid);
								EntitiesHandler.removeItemNotMerge(uuid);
							}
						this.cancel();
						return;
					}
					for (Item item : items) {
						Location loc = item.getLocation();
						if (item != null) {
							if (loc.getBlock().getType() == Material.WATER) {
								playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.5f);
								item.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc, 5, .3, .3, .3, .001);
								UUID uuid = item.getUniqueId();
								item.remove();
								EntitiesHandler.removeInvulnerableEntity(uuid);
								EntitiesHandler.removeItemNotMerge(uuid);
								continue;
							}
							for (Entity e : item.getNearbyEntities(.3, .3, .3))
								if (!EntityUtils.isEntityImmunePlayer(e)) {
									int fireTicks = Math.max(e.getFireTicks(), 600);
									EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(item, e, 600f);
									Bukkit.getServer().getPluginManager().callEvent(event);
									if (event.isCancelled())
										continue;
									e.setFireTicks(fireTicks);
								}
						}
					}
				}
			}.runTaskTimer(plugin, 0, 5);
		}
	}
	public void setAttributes(Phantom entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(FirePhantom.REGISTERED_KEY, FirePhantom.class);
		
		type.setSpawnConditions(event -> {
			if (event.getEntityType() != EntityType.GHAST)
				return false;
			return true;
		});
	}
}
