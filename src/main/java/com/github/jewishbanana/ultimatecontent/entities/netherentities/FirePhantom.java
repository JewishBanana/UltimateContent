package com.github.jewishbanana.ultimatecontent.entities.netherentities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
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
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
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
				Location loc = entity.getLocation();
				World world = loc.getWorld();
				for (int i=0; i < 15; i++) {
					loc.setY(loc.getY()+1);
					Block b = loc.getBlock();
					if (!b.isPassable() || b.isLiquid())
						break;
					world.spawnParticle(Particle.FLAME, loc, 1, .5, 1, .5, 0.3);
					for (Entity e : world.getNearbyEntities(loc, 1, 1, 1))
						if (!EntityUtils.isEntityImmunePlayer(e)) {
							int fireTicks = Math.max(e.getFireTicks(), 80);
							EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(entity, e, 80f);
							Bukkit.getServer().getPluginManager().callEvent(event);
							if (event.isCancelled())
								continue;
							e.setFireTicks(fireTicks);
						}
				}
				for (int i=0; i < 15; i++) {
					loc.setY(loc.getY()-1);
					Block b = loc.getBlock();
					if (!b.isPassable() || b.isLiquid())
						break;
					world.spawnParticle(Particle.FLAME, loc, 1, .5, 1, .5, 0.3);
					for (Entity e : world.getNearbyEntities(loc, 1, 1, 1))
						if (!EntityUtils.isEntityImmunePlayer(e)) {
							int fireTicks = Math.max(e.getFireTicks(), 80);
							EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(entity, e, 80f);
							Bukkit.getServer().getPluginManager().callEvent(event);
							if (event.isCancelled())
								continue;
							e.setFireTicks(fireTicks);
						}
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
		event.getEntity().getWorld().spawnParticle(Particle.FLAME, event.getEntity().getLocation(), 300, 0, 0, 0, .5);
		if (getSectionBoolean("spawnFireItems", true)) {
			Item[] items = new Item[6];
			for (int i=0; i < 6; i++) {
				items[i] = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.BLAZE_POWDER));
				items[i].setPickupDelay(10000);
				items[i].setVisualFire(true);
				items[i].setVelocity(items[i].getVelocity().multiply(2.0));
				EntitiesHandler.makeEntityInvulnerable(items[i]);
				EntitiesHandler.attachRemoveKey(items[i]);
			}
			new BukkitRunnable() {
				private int tick;
				
				@Override
				public void run() {
					if (tick++ >= 40) {
						for (Item item : items)
							if (item != null) {
								playSound(item.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.2, 1.5);
								item.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), item.getLocation(), 5, .3, .3, .3, .001);
								item.remove();
								EntitiesHandler.removeInvulnerableEntity(item.getUniqueId());
							}
						this.cancel();
						return;
					}
					for (Item item : items)
						if (item != null) {
							if (item.getLocation().getBlock().getType() == Material.WATER) {
								playSound(item.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.2, 1.5);
								item.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), item.getLocation(), 5, .3, .3, .3, .001);
								item.remove();
								EntitiesHandler.removeInvulnerableEntity(item.getUniqueId());
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
			}.runTaskTimer(plugin, 0, 5);
		}
	}
	public void setAttributes(Phantom entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(FirePhantom.REGISTERED_KEY, FirePhantom.class);
		
		type.setSpawnConditions(event -> {
			return Utils.isEnvironment(event.getLocation().getWorld(), Environment.NETHER);
		});
	}
}
