package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.function.Function;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Biome;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
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

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class InfestedEnderman extends ComplexEntity<Enderman> {

	public static final String REGISTERED_KEY = "uc:infested_enderman";
	
	private static final DustOptions particleEffect = new DustOptions(Color.fromRGB(9, 74, 72), 1);

	public InfestedEnderman(Enderman entity) {
		super(entity, CustomEntityType.INFESTED_ENDERMAN, false);

		// Register the head stand in stands[] so ComplexEntity.unload() actually removes it on death (otherwise it lingers
		// floating in place, since this entity assigns headStand directly instead of via createStands).
		initStands(new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_ENDERMAN.getHead());
		}, new Vector()));
		setHeadStand(0);

		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid()) {
					// Body died without the framework running our unload (e.g. killed by a creeper explosion, which fires
					// no EntityDeathEvent): clean up the orphaned stand instead of leaving it floating.
					if (!standsCleaned && entity.isDead()) {
						standsCleaned = true;
						unload();
					}
					return;
				}
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
	public void setAIGoals(Enderman entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderNearestAttackableTarget<>(entity, Player.class, 10, true, false), 2);
	}
	public void setAttributes(Enderman entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedEnderman.REGISTERED_KEY, InfestedEnderman.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findMonsterSpawnLocation(area, 3);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedEnderman.class);
	};
}
