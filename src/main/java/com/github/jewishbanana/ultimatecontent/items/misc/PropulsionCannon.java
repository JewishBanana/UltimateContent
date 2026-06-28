package com.github.jewishbanana.ultimatecontent.items.misc;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.MobRangedItem;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class PropulsionCannon extends BaseItem implements MobRangedItem {

	public static final String REGISTERED_KEY = "uc:propulsion_cannon";

	public PropulsionCannon(ItemStack item) {
		super(item);
	}
	// Lobs a snowball the way a snow golem does. The snowball carries the cannon item so its blast (projectile-hit) fires on impact.
	public static final Consumer<Mob> SHOOT = m -> {
		LivingEntity target = m.getTarget();
		if (target == null)
			return;
		ItemStack cannon = m.getEquipment().getItemInMainHand();
		Snowball snowball = m.launchProjectile(Snowball.class);
		snowball.setItem(cannon);
		snowball.setVelocity(MobRangedItem.vanillaShootVelocity(m, target, 1.6));
	};
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.SNOWBALL).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	@Override
	public void applyMobRangedGoal(Mob mob) {
		// A snowball isn't a held/drawn item, so it fires instantly on the interval (instantShoot = true).
		MobRangedItem.addRangedGoal(mob, 50, 0.0, 14.0, true, SHOOT);
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, PropulsionCannon.class);
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}