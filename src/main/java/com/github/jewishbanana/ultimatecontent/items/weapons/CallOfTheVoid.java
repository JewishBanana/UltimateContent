package com.github.jewishbanana.ultimatecontent.items.weapons;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.MobRangedItem;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CallOfTheVoid extends Weapon implements MobRangedItem {

	public static final String REGISTERED_KEY = "uc:call_of_the_void";

	private boolean arrowParticles;

	public CallOfTheVoid(ItemStack item) {
		super(item);
		this.arrowParticles = getBooleanField("projectileParticles");
	}
	public boolean shotBow(EntityShootBowEvent event) {
		if (!arrowParticles)
			return true;
		Entity entity = event.getProjectile();
		new BukkitRunnable() {
			@Override
			public void run() {
				if (entity == null || entity.isDead() || entity.isOnGround()) {
					this.cancel();
					return;
				}
				entity.getWorld().spawnParticle(VersionUtils.getNormalSmoke(), entity.getLocation(), 3, 0.1, 0.1, 0.1, 0.0001);
			}
		}.runTaskTimer(plugin, 0, 1);
		return true;
	}
	// Fires an arrow at vanilla-skeleton velocity, then fires EntityShootBowEvent so UIFramework handles the bow's abilities.
	public static final Consumer<Mob> SHOOT = m -> {
		LivingEntity target = m.getTarget();
		if (target == null)
			return;
		ItemStack bow = m.getEquipment().getItemInMainHand();
		Arrow arrow = m.launchProjectile(Arrow.class);
		arrow.setVelocity(MobRangedItem.vanillaShootVelocity(m, target, 1.6));
		EntityShootBowEvent shootEvent = new EntityShootBowEvent(m, bow, null, arrow, EquipmentSlot.HAND, (float) arrow.getVelocity().length(), false);
		Bukkit.getPluginManager().callEvent(shootEvent);
		if (shootEvent.isCancelled())
			arrow.remove();
	};
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.BOW).setHiddenEnchanted(protectionEnchant).assembleLore().setCustomModelData(100004).build();
	}
	@Override
	public void applyMobRangedGoal(Mob mob) {
		// Skeleton-type mobs already have their native bow goal - leave them on it (its vanilla shot fires EntityShootBowEvent).
		if (mob instanceof AbstractSkeleton)
			return;
		// Otherwise give it the custom ranged goal with min range 0 so it holds its ground and faces the target.
		MobRangedItem.addRangedGoal(mob, 40, 0.0, 16.0, false, SHOOT);
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, CallOfTheVoid.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
