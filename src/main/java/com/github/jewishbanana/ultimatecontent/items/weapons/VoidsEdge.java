package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemField;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidsEdge extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:voids_edge";
	
	private ItemField<Byte> particleField;

	public VoidsEdge(ItemStack item) {
		super(item);
		particleField = registerItemField(lightsaberParticleKey, PersistentDataType.BYTE, (byte) 0);
		particleField.setLore(Utils.convertString((particleField.getSetting() == 0 ? DataUtils.getConfigString("language.misc.all") : (particleField.getSetting() == 1 ? DataUtils.getConfigString("language.misc.others") : DataUtils.getConfigString("language.misc.none")))
				+DataUtils.getConfigString("language.items.particleToggle")));
	}
	public boolean interacted(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (particleField.getSetting() != 2) {
				Player player = event.getPlayer();
				Vector axis = player.getLocation().getDirection().multiply(0.3);
				Location playerLoc = player.getLocation().add(0,1.3,0);
				Location front = playerLoc.clone().add(axis);
				Vector vec = new Vector(axis.getZ(), 0, -axis.getX()).normalize().multiply(1.5);
				double firstAngle = Utils.random().nextDouble()*360;
				Vector angle = vec.clone().rotateAroundAxis(axis, Math.toRadians(firstAngle+180+(Utils.random().nextDouble()*50-25)));
				vec.rotateAroundAxis(axis, Math.toRadians(firstAngle));
				Vector finalAngle = Utils.getVectorTowards(front.clone().add(vec), front.clone().add(angle)).multiply(0.15);
				DustOptions options = new DustOptions(Color.fromARGB(10, 126, 242, 132), 0.6f);
				new BukkitRunnable() {
					private int tick;
					
					@Override
					public void run() {
						for (int i=0; i < 3; i++) {
							Vector inch = Utils.getVectorTowards(playerLoc, front.clone().add(vec)).multiply(0.3);
							Location particle = playerLoc.clone().add(inch);
							for (int j=0; j < 4; j++) {
								if (particleField.getSetting() == 0)
									front.getWorld().spawnParticle(VersionUtils.getRedstoneDust(), particle, 1, 0, 0, 0, 0.001, options);
								else
									front.getWorld().getPlayers().forEach(k -> {
										if (!k.equals(player))
											k.spawnParticle(VersionUtils.getRedstoneDust(), particle, 1, 0, 0, 0, 0.001, options);
									});
								particle.add(inch);
							}
							vec.add(finalAngle);
						}
						if (tick++ >= 4)
							cancel();
					}
				}.runTaskTimer(plugin, 0, 1);
			}
		}
		return true;
	}
	public boolean inventoryClick(InventoryClickEvent event) {
		if (EntityUtils.isPlayerImmune((Player) event.getWhoClicked()))
			event.getWhoClicked().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.particleToggleError")));
		if (event.getClick() == ClickType.RIGHT) {
			if (particleField.getSetting() == 2)
				particleField.setSetting((byte) 0, event.getCurrentItem());
			else
				particleField.setSetting((byte) (particleField.getSetting()+1), event.getCurrentItem());
			particleField.setLore(Utils.convertString((particleField.getSetting() == 0 ? DataUtils.getConfigString("language.misc.all") : (particleField.getSetting() == 1 ? DataUtils.getConfigString("language.misc.others") : DataUtils.getConfigString("language.misc.none")))
					+DataUtils.getConfigString("language.items.particleToggle")));
			refreshItemLore();
		}
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.IRON_SWORD).setHiddenEnchanted(powerEnchant).assembleLore().setCustomModelData(100002).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, VoidsEdge.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}
