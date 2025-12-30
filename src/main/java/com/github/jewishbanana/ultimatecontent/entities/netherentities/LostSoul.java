package com.github.jewishbanana.ultimatecontent.entities.netherentities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class LostSoul extends ComplexEntity<Vex> implements TameableEntity {
	
	public static final String REGISTERED_KEY = "uc:lost_soul";

	public LostSoul(Vex entity) {
		super(entity, CustomEntityType.LOST_SOUL);
		
		setInvisible(entity);
		entity.setSilent(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(entityVariant.getLoadoutArmor(LoadoutEquipmentSlot.HEAD).getItem());
		}, new Vector(0, -.3, 0)));
		setHeadStand(0);
		
		makeParticleTask(entity, 1, () -> {
			Location loc = entity.getLocation();
			entity.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, .2, .2, .2, 0.05);
			entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), loc.add(0, .4, 0), 2, .025, .1, .025, 0.015);
		});
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0, .5, 0), 15, .3, .5, .3, .03);
	}
	public void setAttributes(Vex entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(20);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(LostSoul.REGISTERED_KEY, LostSoul.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (event.getLocation().getBlock().getBiome() != Biome.SOUL_SAND_VALLEY)
				return false;
			return true;
		});
	}
}
