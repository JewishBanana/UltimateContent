package com.github.jewishbanana.ultimatecontent.items.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.LostSoul;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;

public class SoulRipper extends Weapon {

	public static final String REGISTERED_KEY = "uc:soul_ripper";
	
	private final float soulSummonChance;
	private final float soulDurabilityCost;
	private final int maxSoulCount;
	private final int soulDespawnTime;
	
	private final List<LostSoul> souls = new ArrayList<>();

	public SoulRipper(ItemStack item) {
		super(item);
		soulSummonChance = (float) (getDoubleField("soulSummonChance") / 100.0);
		soulDurabilityCost = (float) getDoubleField("soulDurabilityCost");
		maxSoulCount = getIntegerField("maxSoulCount");
		soulDespawnTime = (int) Math.ceil(getDoubleField("soulDespawnTime") * 20);
	}
	public boolean hitEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity) || EntityUtils.isEntityOwner(entity, event.getDamager()))
			return true;
		souls.removeIf(soul -> {
			Entity temp = soul.getEntity();
			return temp == null || !temp.isValid();
		});
		if (souls.size() >= maxSoulCount || random.nextFloat() >= soulSummonChance)
			return true;
		Entity damager = event.getDamager();
		Location spawn = SpawnUtils.findMonsterSpawnLocation(damager.getLocation(), 1, 3f, 7f, t -> {
			Block block = t.getWorld().getBlockAt(t.getBlockX(), t.getBlockY() - 1, t.getBlockZ());
			return block.getType().isOccluding();
		});
		if (spawn == null)
			return true;
		spawn.setY(spawn.getY() - 1);
		LostSoul soul = UIEntityManager.spawnEntity(spawn, LostSoul.class);
		soul.setOwner(damager.getUniqueId());
		soul.getCastedEntity().setTarget(entity);
		souls.add(soul);
		damageItem(soulDurabilityCost, true, damager);
		new BukkitRunnable() {
			@Override
			public void run() {
				Vex temp = soul.getCastedEntity();
				if (temp != null)
					temp.setHealth(0);
			}
		}.runTaskLater(plugin, soulDespawnTime);
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.IRON_HOE).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100010).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, SoulRipper.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
