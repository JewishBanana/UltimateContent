package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.mojang.datafixers.util.Pair;

public class SaberThrow extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:saber_throw";
	private static final List<SaberThrow> list = new ArrayList<>();
	
	private double damage;
	private double range;
	private double knockbackMultiplier;
	
	private ArmorStand stand;
	private Pair<UUID, Pair<ItemStack, EquipmentSlot>> returnItem;
	
	private Target target = Target.ACTIVATOR;
	
	public SaberThrow(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		SaberThrow instance = UIAbilityType.createAbilityInstance(this.getClass());
		list.add(instance);
		instance.stand = entity.getWorld().spawn(entity.getLocation().add(0, 100, 0), ArmorStand.class, temp -> {
			ComplexEntity.initStand(temp);
			temp.getEquipment().setItemInMainHand(base.getItem());
			temp.setRightArmPose(new EulerAngle(0, 0, 0));
		});
		instance.stand.teleport(entity);
		instance.stand.teleport(instance.stand.getLocation().add(0, entity.getHeight() / 2 - 0.3,0));
		final double travelSpeed = 0.8;
		Vector dir = entity instanceof LivingEntity alive ? alive.getEyeLocation().getDirection().normalize().multiply(travelSpeed).add(new Vector(0, 0.02, 0))
				: entity.getLocation().getDirection().normalize().multiply(travelSpeed).add(new Vector(0, 0.02, 0));
		if (entity instanceof LivingEntity alive) {
			instance.returnItem = Pair.of(entity.getUniqueId(), Pair.of(base.getItem().clone(), EntityUtils.getEquipmentSlot(alive.getEquipment(), base.getItem())));
			if (alive instanceof Player player)
				player.getInventory().remove(base.getItem());
		}
		new BukkitRunnable() {
			private int tick = 100;
			private double distance = range;
			
			@Override
			public void run() {
				if (instance.stand == null || instance.stand.isDead() || entity == null || tick <= 0) {
					instance.sweep();
					this.cancel();
					return;
				}
				Location standLoc = instance.stand.getLocation();
				if (distance <= 0) {
					tick--;
					Location entityLoc = entity.getLocation();
					instance.stand.teleport(standLoc.clone().add(Utils.getVectorTowards(standLoc.add(0, 1, 0), entityLoc.clone().add(0, entity.getHeight() / 2, 0)).multiply(travelSpeed)));
					standLoc.subtract(0, 1, 0);
					if (standLoc.distanceSquared(entityLoc) <= 0.8)
						tick = 0;
				} else {
					distance -= travelSpeed;
					instance.stand.teleport(standLoc.add(dir));
					standLoc.subtract(dir);
					if (!standLoc.add(0, 0.7, 0).getBlock().isPassable())
						distance = 0;
					standLoc.subtract(0, 0.7, 0);
				}
				instance.stand.setRotation(standLoc.getYaw() + 40, 0);
				for (Entity e : instance.stand.getWorld().getNearbyEntities(standLoc.add(0, 1, 0), 0.7, 0.7, 0.7)) {
					if (e.equals(entity) || !(e instanceof LivingEntity living) || !canEntityBeHarmed(e, entity))
						continue;
					distance = 0;
					if (EntityUtils.damageEntity(living, damage, "deaths.saberThrow", DamageCause.PROJECTILE, entity))
						e.setVelocity(e.getVelocity().add(Utils.getVectorTowards(standLoc, e.getLocation().add(0, e.getHeight() / 2,0)).setY(0.1).multiply(0.1 * knockbackMultiplier)));
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	private void sweep() {
		if (returnItem != null) {
			Player p = Bukkit.getPlayer(returnItem.getFirst());
			ItemStack item = returnItem.getSecond().getFirst();
			EquipmentSlot slot = returnItem.getSecond().getSecond();
			if (p != null && p.isOnline()) {
				if (p.getInventory().firstEmpty() == -1)
					p.getWorld().dropItemNaturally(p.getLocation(), item);
				else if (slot != null && p.getEquipment().getItem(slot).getType() == Material.AIR)
					p.getInventory().setItem(slot, item);
				else
					p.getInventory().addItem(item);
			} else if (stand != null)
				stand.getWorld().dropItemNaturally(stand.getLocation(), item);
			returnItem = null;
		}
		if (stand != null)
			stand.remove();
		list.remove(this);
	}
	public void clean() {
		list.forEach(e -> e.sweep());
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, SaberThrow.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = registerSerializedDoubleField("damage", map);
		range = registerSerializedDoubleField("range", map);
		knockbackMultiplier = registerSerializedDoubleField("knockbackMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}
