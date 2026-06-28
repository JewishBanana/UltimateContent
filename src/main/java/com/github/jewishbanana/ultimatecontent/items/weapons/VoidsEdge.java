package com.github.jewishbanana.ultimatecontent.items.weapons;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.StoredField;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.abilities.SaberParry;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidsEdge extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:voids_edge";
	private static final Color[] SLASH_COLORS = {
			Color.fromRGB(106, 27, 154),  // dark purple
			Color.fromRGB(74, 20, 140),   // deeper purple
			Color.fromRGB(49, 0, 89),     // very dark purple
			Color.fromRGB(140, 40, 180),  // brighter violet
			Color.fromRGB(20, 0, 35),     // near-black purple
			Color.fromRGB(0, 0, 0)        // black
	};

	private StoredField<Byte> particleField;
	
	public VoidsEdge(ItemStack item) {
		super(item);
	}
	public boolean interacted(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (particleField.getValue() != 2)
				triggerSlashEffect(event.getPlayer(), particleField.getValue() == 0);
			UUID uuid = event.getPlayer().getUniqueId();
			if (SaberParry.projectileParry.add(uuid))
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> SaberParry.projectileParry.remove(uuid), (int) (event.getPlayer().getAttackCooldown() * 10.0));
		}
		return true;
	}
	public boolean hitEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (event.getCause() == DamageCause.ENTITY_ATTACK && entity instanceof LivingEntity) {
			Entity damager = event.getDamager();
			SaberParry.parryMap.put(entity.getUniqueId(), damager.getUniqueId());
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> SaberParry.parryMap.remove(entity.getUniqueId()), damager instanceof Player player ? (int) (player.getAttackCooldown() * 20.0) : 20);
			if (damager instanceof LivingEntity mobCaster && !(damager instanceof Player))
				triggerSlashEffect(mobCaster, true);
		}
		return true;
	}
	private void triggerSlashEffect(LivingEntity caster, boolean showToAll) {
		Vector axis = caster.getLocation().getDirection().multiply(0.3);
		Location casterLoc = caster.getLocation().add(0, 1.3, 0);
		// A mob's slash looks slightly inside its body, so push the origin forward a bit (players already look fine).
		if (!(caster instanceof Player))
			casterLoc.add(caster.getLocation().getDirection().multiply(0.6));
		Location front = casterLoc.clone().add(axis);
		Vector vec = new Vector(axis.getZ(), 0, -axis.getX()).normalize().multiply(1.5);
		double firstAngle = random.nextDouble()*360;
		Vector angle = vec.clone().rotateAroundAxis(axis, Math.toRadians(firstAngle+180+(random.nextDouble()*50-25)));
		vec.rotateAroundAxis(axis, Math.toRadians(firstAngle));
		Vector finalAngle = Utils.getVectorTowards(front.clone().add(vec), front.clone().add(angle)).multiply(0.15);
		new BukkitRunnable() {
			private int tick;

			@Override
			public void run() {
				for (int i=0; i < 3; i++) {
					Vector inch = Utils.getVectorTowards(casterLoc, front.clone().add(vec)).multiply(0.3);
					Location particle = casterLoc.clone().add(inch);
					for (int j=0; j < 4; j++) {
						// Mixed dark-purple/black void colours, varied per particle.
						final DustOptions options = new DustOptions(SLASH_COLORS[random.nextInt(SLASH_COLORS.length)], 0.6f);
						if (showToAll)
							front.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), particle, 1, 0, 0, 0, 0.001, options);
						else
							front.getWorld().getPlayers().forEach(k -> {
								if (!k.equals(caster))
									k.spawnParticle(VersionUtils.getRedstoneDust(), particle, 1, 0, 0, 0, 0.001, options);
							});
						particle.add(inch);
					}
					vec.add(finalAngle);
				}
				if (tick++ >= 4)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public boolean inventoryClick(InventoryClickEvent event) {
		if (EntityUtils.isPlayerImmune((Player) event.getWhoClicked()))
			event.getWhoClicked().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.particleToggleError")));
		if (event.getClick() == ClickType.RIGHT) {
			byte value = particleField.getValue();
			if (value++ == 2)
				value = 0;
			setSpecialLore(PARTICLE_LORE_IDENTIFIER, Utils.convertString((value == 0 ? DataUtils.getConfigString("language.misc.all") : (value == 1 ? DataUtils.getConfigString("language.misc.others") : DataUtils.getConfigString("language.misc.none")))
					+DataUtils.getConfigString("language.items.particleToggle")));
			particleField.setValue(value);
			refreshItemLore();
		}
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.IRON_SWORD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100002).build();
	}
	public void deserializeFields(Map<String, Object> map) {
		super.deserializeFields(map);
		particleField = registerSerializedField("particleSetting", map, (byte) 0, true);
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, VoidsEdge.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
