package com.github.jewishbanana.ultimatecontent.entities;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public abstract class ComplexEntity<T extends Entity> extends BaseEntity<T> {
	
	public CreatureStand<? extends Entity>[] stands;
	public CreatureStand<ArmorStand> headStand;
	protected boolean standsCleaned;

	public ComplexEntity(T entity, CustomEntityType type, boolean createMoveTask) {
		super(entity, type);
		if (createMoveTask)
			scheduleTask(new BukkitRunnable() {
				@Override
				public void run() {
					if (!entity.isValid()) {
						// If the body actually died/was removed but the framework never ran our unload (e.g. a victim
						// killed by a creeper explosion, which fires no EntityDeathEvent), clean up the stands here so
						// they don't linger floating in place. A merely chunk-unloaded body (not dead) is left alone.
						if (!standsCleaned && entity.isDead()) {
							standsCleaned = true;
							unload();
						}
						return;
					}
					Location loc = entity.getLocation();
					for (CreatureStand<?> temp : stands) {
						Entity stand = temp.getEntity(loc);
						stand.teleport(loc.add(temp.offset));
						stand.setRotation(loc.getYaw()+temp.yawOffset, 0);
						loc.subtract(temp.offset);
					}
					if (headStand != null)
						headStand.getEntity(loc).setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
				}
			}.runTaskTimer(plugin, 0, 1));
		else
			// Subclasses with their own move task don't get the self-clean above, so add a light death watcher: if the
			// body dies without the framework running our unload (e.g. a victim of a creeper explosion, which fires no
			// EntityDeathEvent), clean up the stands so they don't linger floating in place.
			scheduleTask(new BukkitRunnable() {
				@Override
				public void run() {
					if (!standsCleaned && !entity.isValid() && entity.isDead()) {
						standsCleaned = true;
						unload();
					}
				}
			}.runTaskTimer(plugin, 20, 10));
	}
	public ComplexEntity(T entity, CustomEntityType type) {
		this(entity, type, true);
	}
	public void unload() {
		super.unload();
		if (stands != null)
			for (CreatureStand<?> temp : stands)
				if (temp.stand != null)
					temp.stand.remove();
	}
	@SafeVarargs
	public final void initStands(CreatureStand<?>... creatureStands) {
		this.stands = creatureStands;
	}
	@SafeVarargs
	public final void createStands(Location location, CreatureStand<?>... creatureStands) {
		this.stands = creatureStands;
		for (CreatureStand<?> temp : stands)
			temp.getEntity(location);
	}
	public CreatureStand<?> getCreatureStand(int index) {
		return stands[index];
	}
	public Entity getCreatureStandEntity(int index, Location location) {
		return stands[index].getEntity(location);
	}
	public Entity getCreatureStandEntity(int index) {
		return getCreatureStandEntity(index, getEntityLocation());
	}
	public Entity getCreatureStandEntityOrNull(int index) {
		return stands[index].stand;
	}
	@SuppressWarnings("unchecked")
	public void setHeadStand(int index) {
		headStand = (CreatureStand<ArmorStand>) stands[index];
	}
	public int getStandCount() {
		return stands.length;
	}
	public static class CreatureStand<K extends Entity> {
		
		private K stand;
		private Class<K> entityClass;
		private Vector offset;
		private Consumer<K> creator;
		private float yawOffset;
		
		public CreatureStand(Class<K> entityClass, Consumer<K> creator, Vector offset, float yawOffset) {
			this.entityClass = entityClass;
			this.creator = creator;
			this.offset = offset;
			this.yawOffset = yawOffset;
		}
		public CreatureStand(Class<K> entityClass, Consumer<K> creator, Vector offset) {
			this(entityClass, creator, offset, 0);
		}
		public CreatureStand(Class<K> entityClass, Consumer<K> creator) {
			this(entityClass, creator, null, 0);
		}
		public K getEntity(Location location) {
			if (stand != null && stand.isValid())
				return stand;
			stand = location.getWorld().spawn(location, entityClass, false, creator);
			return stand;
		}
		public K getEntityOrNull() {
			return stand;
		}
		public Vector getOffset() {
			return offset;
		}
		public void setOffset(Vector offset) {
			this.offset = offset;
		}
		public float getYawOffset() {
			return yawOffset;
		}
	}
}
