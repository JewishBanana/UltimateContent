package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.function.Function;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class InfestedEnderman extends ComplexEntity<Enderman> {

	public static final String REGISTERED_KEY = "uc:infested_enderman";
	
	private static final DustOptions particleEffect = new DustOptions(Color.fromRGB(9, 74, 72), 1);

	public InfestedEnderman(Enderman entity) {
		super(entity, CustomEntityType.INFESTED_ENDERMAN, false);
		
		headStand = new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_ENDERMAN.getHead());
		}, new Vector());
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location loc = entity.getLocation();
				ArmorStand stand = headStand.getEntity(loc);
				if (entity.getTarget() == null)
					stand.teleport(loc.add(0, .9, 0).add(loc.getDirection().multiply(0.1)));
				else
					stand.teleport(loc.add(0, 1.15, 0).add(loc.getDirection().multiply(0.2)));
				stand.setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
			}
		}.runTaskTimer(plugin, 0, 1));
		
		makeParticleTask(entity, VersionUtils.getRedstoneDust(), new Vector(0, 1.2, 0), 7, .25, .8, .25, .001, particleEffect);
	}
	public void setAttributes(Enderman entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedEnderman.REGISTERED_KEY, InfestedEnderman.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area, 3);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedEnderman.class);
	};
}
