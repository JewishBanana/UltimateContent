package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.entities.endentities.BabyEndTotem;
import com.github.jewishbanana.ultimatecontent.entities.endentities.EndTotem;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidGuardian;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidStalker;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidWorm;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidTear extends BaseItem {

	public static final String REGISTERED_KEY = "uc:void_tear";

	public VoidTear(ItemStack item) {
		super(item);
	}
	public boolean interacted(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			Player player = event.getPlayer();
			Location target = player.getEyeLocation().add(player.getLocation().getDirection().multiply(6));
			// Only tear a rift where there is genuine open space to open into and solid ground below it.
			if (!isValidRiftLocation(target))
				return false;
			createRift(target);
			if (item.getAmount() == 1)
				GenericItem.removeBaseItem(item);
			item.setAmount(item.getAmount() - 1);
		}
		return true;
	}
	/** A rift needs a pocket of open air to tear into and solid ground below so the summoned mobs land instead of voiding out. */
	private boolean isValidRiftLocation(Location loc) {
		if (!Utils.isAreaClear(loc, 1.0f, 2.0f))
			return false;
		return BlockUtils.rayTraceForBlock(loc, new Vector(0, -1, 0), 16.0, block -> !block.isPassable()) != null;
	}
	/**
	 * Opens an unstable rift that slowly births a void onslaught (guardians, stalkers, a worm and an end totem) and finally
	 * a wild, tameable {@link BabyEndTotem}, mirroring the legacy void tear. Players standing in the rift take void damage.
	 */
	private void createRift(Location loc) {
		final World world = loc.getWorld();
		// var[0] = ticks until the next summon (counts down), var[1] = remaining summons (drives which mob spawns next).
		final int[] var = { 60, 7 };
		new BukkitRunnable() {
			@Override
			public void run() {
				world.spawnParticle(Particle.PORTAL, loc, 20, .2, .2, .2, 1.5);
				world.spawnParticle(Particle.SQUID_INK, loc.clone().add(0, 0.5, 0), 30, .25, .25, .25, 0.0001);
				world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, SoundCategory.AMBIENT, .7f, 1);
				for (Entity e : world.getNearbyEntities(loc, .5, .5, .5))
					if (e instanceof Player && !EntityUtils.isEntityImmunePlayer(e))
						EntityUtils.pureDamageEntity((LivingEntity) e, 1.0, "deaths.unstableRift", DamageCause.VOID);
				if (var[0] > 0) {
					var[0] -= 5;
					return;
				}
				var[0] = 60;
				if (var[1] >= 6)
					UIEntityManager.spawnEntity(loc, VoidGuardian.class);
				else if (var[1] >= 4)
					UIEntityManager.spawnEntity(loc, VoidStalker.class);
				else if (var[1] == 3)
					UIEntityManager.spawnEntity(loc, VoidWorm.class);
				else if (var[1] == 2)
					UIEntityManager.spawnEntity(loc, EndTotem.class);
				else
					// The finale: a wild baby end totem the player can tame with a ghast tear.
					UIEntityManager.spawnEntity(loc, BabyEndTotem.class);
				if (--var[1] <= 0)
					cancel();
			}
		}.runTaskTimer(plugin, 0, 5);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.GHAST_TEAR).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100001).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, VoidTear.class);
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}
