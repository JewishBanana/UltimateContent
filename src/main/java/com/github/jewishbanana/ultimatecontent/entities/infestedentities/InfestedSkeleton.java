package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Skeleton;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class InfestedSkeleton extends BaseEntity<Skeleton> {

	public static final String REGISTERED_KEY = "uc:infested_skeleton";

	public InfestedSkeleton(Skeleton entity) {
		super(entity, CustomEntityType.INFESTED_SKELETON);
		
		setInvisible(entity);
		entity.setCanPickupItems(false);
		
		makeParticleTask(entity, VersionUtils.getBlockCrack(), new Vector(0, .8, 0), 3, .3, 1, .3, .1, BaseEntity.infestedEntityParticles);
	}
	public void setAttributes(Skeleton entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedSkeleton.REGISTERED_KEY, InfestedSkeleton.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedSkeleton.class);
	};
}
