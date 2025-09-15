package com.github.jewishbanana.ultimatecontent.items.misc;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidTear extends BaseItem {
	
	public static final String REGISTERED_KEY = "uc:void_tear";
	
	public VoidTear(ItemStack item) {
		super(item);
	}
	public boolean interacted(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Location target = event.getPlayer().getEyeLocation().add(event.getPlayer().getLocation().getDirection().multiply(6));
			if (target.getBlock().getType() != Material.AIR)
				return false;
			createRift(target);
			if (item.getAmount() == 1)
				GenericItem.removeBaseItem(item);
			item.setAmount(item.getAmount()-1);
		}
		return true;
	}
	private void createRift(Location loc) {
//		int[] var = {60, 7};
//		World world = loc.getWorld();
//		boolean custom = (boolean) com.github.jewishbanana.deadlydisasters.handlers.WorldObject.findWorldObject(loc.getWorld()).settings.get("custom_mob_spawning");
//		ItemStack[] armor = {new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)};
//		if (!custom) {
//			for (int i=0; i < 3; i++) {
//				LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
//				meta.setColor(Color.fromBGR(50, 50, 50));
//				armor[i].setItemMeta(meta);
//			}
//		}
//		final Random rand = new Random();
//		new BukkitRunnable() {
//			@Override
//			public void run() {
//				world.spawnParticle(Particle.PORTAL, loc, 20, .2, .2, .2, 1.5);
//				world.spawnParticle(Particle.SQUID_INK, loc.clone().add(0,0.5,0), 30, .25, .25, .25, 0.0001);
//				world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, SoundCategory.AMBIENT, .7f, 1);
//				for (Entity e : world.getNearbyEntities(loc, .5, .5, .5))
//					if (e instanceof Player && !EntityUtils.isEntityImmunePlayer(e))
//						EntityUtils.pureDamageEntity((LivingEntity) e, 1.0, "deaths.unstableRift", DamageCause.VOID);
//				if (var[0] > 0)
//					var[0]-=5;
//				else {
//					var[0] = 60;
//					Mob entity = null;
//					com.github.jewishbanana.deadlydisasters.entities.CustomEntity ce = null;
//					if (!custom) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//						entity.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
//						entity.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
//						entity.getEquipment().setArmorContents(armor);
//						
//						var[1]--;
//						if (var[1] <= 0) cancel();
//						return;
//					}
//					if (var[1] >= 6 && com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.VOIDGUARDIAN.canSpawn()) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//						ce = new com.github.jewishbanana.deadlydisasters.entities.endstormentities.VoidGuardian(entity, com.github.jewishbanana.deadlydisasters.Main.getInstance(), rand);
//					} else if (var[1] >= 4 && com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.VOIDSTALKER.canSpawn()) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.PHANTOM);
//						ce = new com.github.jewishbanana.deadlydisasters.entities.endstormentities.VoidStalker(entity, com.github.jewishbanana.deadlydisasters.Main.getInstance(), rand);
//					} else if (var[1] == 3 && com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.ENDWORM.canSpawn()) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
//						ce = new com.github.jewishbanana.deadlydisasters.entities.endstormentities.EndWorm(entity, com.github.jewishbanana.deadlydisasters.Main.getInstance(), rand);
//					} else if (var[1] == 2 && com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.ENDTOTEM.canSpawn()) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
//						entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
//						ce = new com.github.jewishbanana.deadlydisasters.entities.endstormentities.EndTotem(entity, com.github.jewishbanana.deadlydisasters.Main.getInstance(), rand);
//					} else if (com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.BABYENDTOTEM.canSpawn()) {
//						entity = (Mob) loc.getWorld().spawnEntity(loc, EntityType.WOLF);
////						ce = new deadlydisasters.entities.endstormentities.BabyEndTotem(entity, deadlydisasters.Main.getInstance(), random);
//					}
//					var[1]--;
//					if (var[1] <= 0) cancel();
//					if (entity == null)
//						return;
//					com.github.jewishbanana.deadlydisasters.entities.CustomEntity.handler.addEntity(ce);
//				}
//			}
//		}.runTaskTimer(plugin, 0, 5);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.GHAST_TEAR).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, VoidTear.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
