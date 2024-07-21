package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class StasisZone extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:stasis_zone";
	
	private double range;
	private double particleMultiplier;
	
	public void activate(Entity entity, GenericItem base) {
		activate(entity.getLocation().add(0,entity.getHeight()/2,0), base);
	}
	public void activate(Location loc, GenericItem base) {
		World world = loc.getWorld();
		int[] tick = {1, 0};
		Map<UUID, Location> entities = new HashMap<>();
		DustOptions options = new DustOptions(Color.fromARGB(10, 247, 247, 25), 0.6f);
		double particleOff = 4.0 / 10.0;
		if (VersionUtils.displaysAllowed) {
			ItemDisplay[] displays = new ItemDisplay[2];
			ItemStack item = new ItemStack(Material.EMERALD);
			ItemMeta meta = item.getItemMeta();
			meta.setCustomModelData(6000);
			item.setItemMeta(meta);
			displays[0] = world.spawn(loc, ItemDisplay.class, dis -> {
				dis.setItemStack(item);
				Transformation tran = dis.getTransformation();
				tran.getTranslation().set(-.5, -.5, -.5);
				dis.setTransformation(tran);
			});
			displays[1] = world.spawn(loc, ItemDisplay.class, dis -> {
				dis.setItemStack(item);
				dis.setRotation(45, 45);
				Transformation tran = dis.getTransformation();
				tran.getTranslation().set(-.5, -.5, -.5);
				dis.setTransformation(tran);
			});
			new RepeatingTask(0, 1) {
				@Override
				public void run() {
					tick[0]++;
					if (tick[0] < 10) {
						tick[1]++;
						world.spawnParticle(VersionUtils.getRedstoneDust(), loc, (int) (particleMultiplier * 10.0), tick[1] * particleOff, tick[1] * particleOff, tick[1] * particleOff, 0.01, options);
						for (ItemDisplay dis : displays) {
							if (dis == null)
								continue;
							Transformation tran = dis.getTransformation();
							tran.getScale().mul(1.25f);
							float size = -(tran.getScale().x / 2);
							tran.getTranslation().set(size, size, size);
							dis.setTransformation(tran);
						}
						return;
					}
					for (Entity e : world.getNearbyEntities(loc, 4.0, 4.0, 4.0, entity -> !(entity instanceof Player && Utils.isPlayerImmune((Player) entity)) && !entity.isDead())) {
						Location eL = entities.get(e.getUniqueId());
						if (eL != null)
							e.teleport(eL);
						else
							entities.put(e.getUniqueId(), e.getLocation());
					}
					if (tick[0] > 120) {
						if (tick[0] > 130) {
							for (ItemDisplay dis : displays)
								if (dis != null)
									dis.remove();
							cancel();
							return;
						}
						for (ItemDisplay dis : displays) {
							if (dis == null)
								continue;
							Transformation tran = dis.getTransformation();
							tran.getScale().mul(0.75f);
							float size = -(tran.getScale().x / 2);
							tran.getTranslation().set(size, size, size);
							dis.setTransformation(tran);
						}
					}
				}
			};
		} else {
			new RepeatingTask(0, 1) {
				@Override
				public void run() {
					tick[0]++;
					if (tick[0] < 10)
						tick[1]++;
					world.spawnParticle(VersionUtils.getRedstoneDust(), loc, (int) (particleMultiplier * 10.0), tick[1] * particleOff, tick[1] * particleOff, tick[1] * particleOff, 0.01, options);
					for (Entity e : world.getNearbyEntities(loc, 4.0, 4.0, 4.0, entity -> !(entity instanceof Player && Utils.isPlayerImmune((Player) entity)) && !entity.isDead())) {
						Location eL = entities.get(e.getUniqueId());
						if (eL != null)
							e.teleport(eL);
						else
							entities.put(e.getUniqueId(), e.getLocation());
					}
					if (tick[0] > 120) {
						tick[1]--;
						if (tick[0] > 130) {
							cancel();
							return;
						}
					}
				}
			};
		}
	}
	public void initFields() {
		this.range = getDoubleField("range", 2.5);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, StasisZone.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
}
