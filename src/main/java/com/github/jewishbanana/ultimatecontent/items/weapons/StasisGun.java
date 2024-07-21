package com.github.jewishbanana.ultimatecontent.items.weapons;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;

public class StasisGun extends Weapon {
	
	public static String REGISTERED_KEY = "ui:stasis_gun";

	private double range;
	private int cooldown;
	
	public StasisGun(ItemStack item) {
		super(item);
		this.range = getDoubleField("range", 25.0);
		this.cooldown = (int) (getDoubleField("shot_cooldown", 2.0) * 20.0);
	}
	public boolean interacted(PlayerInteractEvent event) {
		event.setCancelled(true);
		if (isOnCooldown() || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK))
			return true;
		setCooldown(cooldown);
		Player entity = event.getPlayer();
		Vector vel = entity.getEyeLocation().getDirection().multiply(0.5);
		ShulkerBullet shot = entity.getWorld().spawn(entity.getLocation().add(0,1.4,0).add(vel), ShulkerBullet.class);
		shot.setGravity(false);
		shot.setVelocity(vel);
		assignProjectile(shot);
		Location start = shot.getLocation();
		double distance = range * range;
		UUID uuid = shot.getUniqueId();
		new RepeatingTask(0, 1) {
			@Override
			public void run() {
				if (shot == null || shot.isDead() || shot.getLocation().distanceSquared(start) >= distance) {
					if (shot != null)
						shot.remove();
					removeProjectile(uuid);
					cancel();
					return;
				}
				shot.setVelocity(vel);
			}
		};
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.GOLDEN_HOE).assembleLore().setCustomModelData(7000).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, StasisGun.class);
	}
}
