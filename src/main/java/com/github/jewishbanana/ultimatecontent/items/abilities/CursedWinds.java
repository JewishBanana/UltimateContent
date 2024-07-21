package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CursedWinds extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:cursed_winds";
	
	private double damage;
	private double range;
	private double particleMultiplier;
	private int fireTicks;
	
	private Target target = Target.ACTIVATOR;
	
	public void activate(Entity entity, GenericItem base) {
		int[] spellLife = {(int) (range * 2)};
		Location spell = entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
		Vector motion = spell.getDirection().multiply(3.0);
		spell.getWorld().playSound(spell, Sound.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, volume * 1, .6f);
		World tempW = spell.getWorld();
		BlockData bd = Material.SAND.createBlockData();
		final int particleCount = (int) Math.ceil(10.0 * particleMultiplier);
		Set<UUID> hitEntities = new HashSet<>();
		new RepeatingTask(0, 1) {
			@SuppressWarnings("removal")
			@Override
			public void run() {
				if (spellLife[0] <= 0) {
					cancel();
					return;
				}
				spellLife[0]--;
				spell.add(motion);
				tempW.spawnParticle(Particle.FLAME, spell, particleCount, 1, 1, 1, .05);
				tempW.spawnParticle(VersionUtils.getBlockDust(), spell, particleCount, 1, 1, 1, .1, bd);
				for (Entity e : spell.getWorld().getNearbyEntities(spell, 1.5, 1.5, 1.5))
					if (!hitEntities.contains(e.getUniqueId()) && e instanceof LivingEntity && !e.equals(entity) && !Utils.isEntityImmunePlayer(e)) {
						if (Utils.damageEntity((LivingEntity) e, damage, "deaths.cursedWinds", false, entity, new EntityDamageByEntityEvent(entity, e, DamageCause.MAGIC, damage))) {
							e.setFireTicks(fireTicks);
							e.setVelocity(motion.clone().multiply(0.5));
						}
						hitEntities.add(e.getUniqueId());
					}
			}
		};
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 4.0);
		this.range = getDoubleField("range", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.fireTicks = getIntegerField("fireTicks", 80);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, CursedWinds.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}
