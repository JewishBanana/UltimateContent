package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.mojang.datafixers.util.Pair;

public class SaberThrow extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:saber_throw";
	private static Queue<SaberThrow> list = new ArrayDeque<>();
	
	private double damage;
	private double range;
	private double knockbackMultiplier;
	
	private ArmorStand stand;
	private Pair<UUID, Pair<ItemStack, EquipmentSlot>> returnItem;
	
	private Target target = Target.ACTIVATOR;
	
	public void activate(Entity entity, GenericItem base) {
		SaberThrow instance = new SaberThrow();
		list.add(instance);
		instance.stand = entity.getWorld().spawn(entity.getLocation().add(0,100,0), ArmorStand.class, temp -> {
			Utils.lockArmorStand(temp, true, false, true);
			temp.getEquipment().setItemInMainHand(base.getItem());
			temp.setRightArmPose(new EulerAngle(0, 0, 0));
//			temp.getPersistentDataContainer().set(EntityHandler.removalKey, PersistentDataType.BYTE, (byte) 0);
		});
		instance.stand.teleport(entity);
		instance.stand.teleport(instance.stand.getLocation().add(0,entity.getHeight()/2-0.3,0));
		final double travelSpeed = 0.8;
		Vector dir = entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation().getDirection().normalize().multiply(travelSpeed).add(new Vector(0,0.02,0))
				: entity.getLocation().getDirection().normalize().multiply(travelSpeed).add(new Vector(0,0.02,0));
		if (entity instanceof LivingEntity) {
			instance.returnItem = Pair.of(entity.getUniqueId(), Pair.of(base.getItem().clone(), Utils.getEquipmentSlot(((LivingEntity) entity).getEquipment(), base.getItem())));
			if (entity instanceof Player)
				((Player) entity).getInventory().remove(base.getItem());
		}
		double[] ticks = {range, 100};
		new RepeatingTask(0, 1) {
			@Override
			public void run() {
				if (instance.stand == null || instance.stand.isDead() || entity == null || ticks[1] <= 0) {
					instance.sweep();
					cancel();
					return;
				}
				if (ticks[0] <= 0) {
					ticks[1]--;
					instance.stand.teleport(instance.stand.getLocation().add(Utils.getVectorTowards(instance.stand.getLocation().add(0,1,0), entity.getLocation().add(0,entity.getHeight()/2,0)).multiply(travelSpeed)));
					if (instance.stand.getLocation().distanceSquared(entity.getLocation()) <= 0.8)
						ticks[1] = 0;
				} else {
					ticks[0] -= travelSpeed;
					instance.stand.teleport(instance.stand.getLocation().add(dir));
					if (!instance.stand.getLocation().add(0,0.7,0).getBlock().isPassable())
						ticks[0] = 0;
				}
				instance.stand.setRotation(instance.stand.getLocation().getYaw()+40, 0);
				for (Entity e : instance.stand.getWorld().getNearbyEntities(instance.stand.getLocation().add(0,1,0), 0.7, 0.7, 0.7,
						i -> !i.equals(entity) && i instanceof LivingEntity && !(i instanceof Tameable && DataUtils.isEqualsNoNull(entity, ((Tameable) i).getOwner())) && !i.getPassengers().contains(entity))) {
					ticks[0] = 0;
					if (Utils.damageEntity((LivingEntity) e, damage, "deaths.saberThrow", false, entity, DamageCause.PROJECTILE))
						e.setVelocity(e.getVelocity().add(Utils.getVectorTowards(instance.stand.getLocation().add(0,1,0), e.getLocation().add(0,e.getHeight()/2,0)).setY(0.1).multiply(0.1*knockbackMultiplier)));
				}
			}
		};
	}
	private void sweep() {
		if (returnItem != null) {
			Player p = Bukkit.getPlayer(returnItem.getFirst());
			if (p != null && p.isOnline()) {
				if (p.getInventory().firstEmpty() == -1)
					p.getWorld().dropItemNaturally(p.getLocation(), returnItem.getSecond().getFirst());
				else if (returnItem.getSecond().getSecond() != null && p.getEquipment().getItem(returnItem.getSecond().getSecond()).getType() == Material.AIR)
					p.getInventory().setItem(returnItem.getSecond().getSecond(), returnItem.getSecond().getFirst());
				else
					p.getInventory().addItem(returnItem.getSecond().getFirst());
			} else if (stand != null)
				stand.getWorld().dropItemNaturally(stand.getLocation(), returnItem.getSecond().getFirst());
			returnItem = null;
		}
		if (stand != null)
			stand.remove();
		list.remove(this);
	}
	public void clean() {
		list.forEach(e -> e.sweep());
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 9.0);
		this.range = getDoubleField("range", 11.0);
		this.knockbackMultiplier = getDoubleField("knockbackMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, SaberThrow.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("knockbackMultiplier", knockbackMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		knockbackMultiplier = (double) map.get("knockbackMultiplier");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}
