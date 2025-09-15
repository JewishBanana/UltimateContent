package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.UUID;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creeper;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.ExplodingEntity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class InfestedCreeper extends ComplexEntity<Creeper> implements ExplodingEntity {
	
	public static final String REGISTERED_KEY = "uc:infested_creeper";
	
	private double damageMultiplier;
	private UUID uuid;

	public InfestedCreeper(Creeper entity) {
		super(entity, CustomEntityType.INFESTED_CREEPER);
		
		setInvisible(entity);
		entity.setMaxFuseTicks(15);
		entity.setExplosionRadius(5);
		this.uuid = entity.getUniqueId();
		EntitiesHandler.explodingEntities.put(uuid, this);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_CREEPER.getHead());
		}, new Vector(0, -.2, 0)));
		setHeadStand(0);
		
		makeParticleTask(entity, VersionUtils.getBlockCrack(), new Vector(0, .7, 0), 10, .25, .4, .25, .1, BaseEntity.infestedEntityParticles);
	}
	public void unload() {
		super.unload();
		EntitiesHandler.explodingEntities.remove(uuid);
	}
	public void setAttributes(Creeper entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
		this.damageMultiplier = getSectionDouble("damageMultiplier", 1.0);
	}
	public double getExplosionDamageMultiplier() {
		return damageMultiplier;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedCreeper.REGISTERED_KEY, InfestedCreeper.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedCreeper.class);
	};
}
