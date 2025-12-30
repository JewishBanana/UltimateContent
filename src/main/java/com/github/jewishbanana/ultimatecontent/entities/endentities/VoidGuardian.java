package com.github.jewishbanana.ultimatecontent.entities.endentities;

import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class VoidGuardian extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:void_guardian";
	
	private boolean rageMode;
	private UUID lastToRage;
	private double maxHealth;

	public VoidGuardian(Zombie entity) {
		super(entity, CustomEntityType.VOID_GUARDIAN);
		
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		
		makeParticleTask(entity, 1, Particle.DRAGON_BREATH, new Vector(0, entity.getHeight()/2.0, 0), 3, .25, .5, .25, .015);
		
		maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (entity.getHealth() < maxHealth) {
					entity.setHealth(Math.min(entity.getHealth()+1.0, maxHealth));
					entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0,.5,0), 5, .3, .4, .3, .001);
				}
				if (rageMode) {
					entity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getLocation().add(0,.5,0), 10, .3, .3, .3, .25);
					if (entity.getHealth() > maxHealth / 2.0) {
						rageMode = false;
						changeColor(50, 50, 50);
						if (entityVariant.loadout.armor[3] == null)
							entity.getEquipment().setHelmet(CustomHead.VOID_GUARD.getHead());
						entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityVariant.movementSpeed);
						if (lastToRage != null)
							DependencyUtils.awardAchievementProgress(lastToRage, "master.series.void_master", 1, 2);
						lastToRage = null;
					}
				}
			}
		}.runTaskTimer(plugin, 0, 20));
	}
	public void onDamaged(EntityDamageEvent event) {
		super.onDamaged(event);
		if (rageMode)
			return;
		Zombie entity = (Zombie) event.getEntity();
		if (entity.getHealth() <= maxHealth / 2.0) {
			rageMode = true;
			changeColor(76, 48, 255);
			if (entityVariant.loadout.armor[3] == null)
				entity.getEquipment().setHelmet(CustomHead.VOID_GUARD_RAGE.getHead());
			entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityVariant.movementSpeed * 2.0);
		}
	}
	public void wasHit(EntityDamageByEntityEvent event) {
		if (!rageMode && event.getDamager() instanceof Player)
			lastToRage = event.getDamager().getUniqueId();
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		event.setDamage(Math.max(18 - (((Zombie) event.getDamager()).getHealth() / 4), 4));
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0,event.getEntity().getHeight()/2.0,0), 15, .3, .5, .3, .03);
	}
	private void changeColor(int red, int green, int blue) {
		Zombie entity = getCastedEntity();
		ItemStack[] armor = entity.getEquipment().getArmorContents();
		for (int i=0; i < 3; i++)
			if (entityVariant.loadout.armor[i] == null) {
				LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
				meta.setColor(Color.fromRGB(red, green, blue));
				armor[i].setItemMeta(meta);
			}
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidGuardian.REGISTERED_KEY, VoidGuardian.class);
		
		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.THE_END))
				return false;
			return true;
		});
	}
}
