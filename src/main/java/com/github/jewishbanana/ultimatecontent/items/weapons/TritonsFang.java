package com.github.jewishbanana.ultimatecontent.items.weapons;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.MobRangedItem;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class TritonsFang extends Weapon implements MobRangedItem {

	public static final String REGISTERED_KEY = "uc:tritons_fang";

	public TritonsFang(ItemStack item) {
		super(item);
	}
	// Throws the trident the way a drowned does, carrying the fang's item and firing the launch event so its ability runs.
	public static final Consumer<Mob> SHOOT = m -> {
		LivingEntity target = m.getTarget();
		if (target == null)
			return;
		ItemStack fang = m.getEquipment().getItemInMainHand();
		Trident trident = m.launchProjectile(Trident.class);
		trident.setVelocity(MobRangedItem.vanillaShootVelocity(m, target, 1.6));
		trident.setItem(fang);
		Bukkit.getPluginManager().callEvent(new ProjectileLaunchEvent(trident));
	};
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.TRIDENT).assembleLore().setCustomModelData(100014).build();
	}
	@Override
	public void applyMobRangedGoal(Mob mob) {
		// Drowned already have their native trident-throw goal - leave them on it.
		if (mob instanceof Drowned)
			return;
		// A trident is held/drawn (like a bow), so it uses the draw timing rather than instant shoot.
		MobRangedItem.addRangedGoal(mob, 50, 0.0, 14.0, false, SHOOT);
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, TritonsFang.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
