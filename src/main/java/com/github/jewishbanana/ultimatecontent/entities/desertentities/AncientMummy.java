package com.github.jewishbanana.ultimatecontent.entities.desertentities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class AncientMummy extends BaseEntity<Husk> {
	
	public static final String REGISTERED_KEY = "uc:ancient_mummy";

	public AncientMummy(Husk entity) {
		super(entity, CustomEntityType.ANCIENT_MUMMY);
		
		entity.setSilent(true);
		entity.setCanPickupItems(false);
		makeEntityBreakDoors(entity);
		
		scheduleTask(new BukkitRunnable() {
			private int cooldown;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (cooldown > 0) {
					if (cooldown-- == 9)
						entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityType.movementSpeed);
					return;
				}
				if (isTargetInRange(entity, 0, 100)) {
					cooldown = 12;
					entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Math.max(entityType.movementSpeed, 0.45));
					playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.5, .5);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		makeParticleTask(entity, Particle.FALLING_DUST, new Vector(0, 1.6, 0), 4, .4, .5, .4, 1, Material.OBSIDIAN.createBlockData());
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 4, true));
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0,1.5,0), 5, .3, .3, .3, .0001);
	}
	public void setAttributes(Husk entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(AncientMummy.REGISTERED_KEY, AncientMummy.class);
		
		type.setSpawnConditions(event -> {
			if (!(event.getEntity() instanceof Monster))
				return false;
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.NORMAL))
				return false;
			if (!VersionUtils.isBiomeDesert(loc.getBlock().getBiome()))
				return false;
			return true;
		});
	}
}
