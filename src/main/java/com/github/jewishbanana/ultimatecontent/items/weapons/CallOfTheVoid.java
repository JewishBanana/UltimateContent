package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CallOfTheVoid extends Weapon {
	
	public static String REGISTERED_KEY = "ui:call_of_the_void";
	
	private boolean arrowParticles;

	public CallOfTheVoid(ItemStack item) {
		super(item);
		this.arrowParticles = getBooleanField("projectileParticles", true);
	}
	public boolean shotBow(EntityShootBowEvent event) {
		if (!arrowParticles)
			return true;
		Entity entity = event.getProjectile();
		new RepeatingTask(0, 1) {
			@Override
			public void run() {
				if (entity == null || entity.isDead() || entity.isOnGround()) {
					cancel();
					return;
				}
				entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), entity.getLocation(), 3, 0.1, 0.1, 0.1, 0.0001);
			}
		};
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.BOW).setHiddenEnchanted(protectionEnchant).assembleLore().setCustomModelData(100004).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, CallOfTheVoid.class);
	}
}
