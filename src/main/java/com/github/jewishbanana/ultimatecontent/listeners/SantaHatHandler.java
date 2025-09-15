package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.playerarmorchangeevent.PlayerArmorChangeEvent;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.items.christmas.SantaHat;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class SantaHatHandler implements Listener {
	
	private static Map<UUID, Set<UUID>> activePlayers = new HashMap<>();
	
	private Main plugin;
	private Map<UUID, Integer> deadElfTracker = new HashMap<>();
	
	public SantaHatHandler(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getServer().getOnlinePlayers().forEach((player) -> {
					GenericItem base = GenericItem.getItemBase(player.getEquipment().getHelmet());
					if (base == null || !(base instanceof SantaHat))
						return;
					int amount = ((SantaHat) base).elfSummonCount - deadElfTracker.getOrDefault(player.getUniqueId(), 0);
					if (amount > 0)
						summonElves(player, amount);
				});
			}
		}.runTaskLater(plugin, 1);
		new BukkitRunnable() {
			@Override
			public void run() {
				Iterator<Entry<UUID, Set<UUID>>> iterator = activePlayers.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<UUID, Set<UUID>> entry = iterator.next();
					Entity entity = Bukkit.getEntity(entry.getKey());
					if (entity == null)
						return;
					GenericItem base = GenericItem.getItemBase(((LivingEntity) entity).getEquipment().getHelmet());
					if (base == null || !(base instanceof SantaHat)) {
						entry.getValue().forEach(e -> {
							Entity elf = Bukkit.getEntity(e);
							if (elf != null)
								elf.remove();
						});
						iterator.remove();
					}
				}
			}
		}.runTaskTimer(plugin, 0, 20);
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		GenericItem base = GenericItem.getItemBase(event.getPlayer().getEquipment().getHelmet());
		if (base == null || !(base instanceof SantaHat))
			return;
		int amount = ((SantaHat) base).elfSummonCount - deadElfTracker.getOrDefault(event.getPlayer().getUniqueId(), 0);
		if (amount > 0)
			summonElves(event.getPlayer(), amount);
	}
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		removeElves(event.getPlayer().getUniqueId());
	}
	@EventHandler(ignoreCancelled = true)
	public void onArmorChange(PlayerArmorChangeEvent event) {
		if (event.getSlot() != EquipmentSlot.HEAD)
			return;
		if (event.getNewItem().getType() != Material.AIR) {
			GenericItem base = GenericItem.getItemBase(event.getNewItem());
			if (base == null || !(base instanceof SantaHat))
				return;
			int amount = ((SantaHat) base).elfSummonCount - deadElfTracker.getOrDefault(event.getPlayer().getUniqueId(), 0);
			if (amount > 0) {
				removeElves(event.getPlayer().getUniqueId());
				summonElves(event.getPlayer(), amount);
			}
		} else
			removeElves(event.getPlayer().getUniqueId());
	}
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		UUID entity = event.getEntity().getUniqueId();
		for (Entry<UUID, Set<UUID>> entry : activePlayers.entrySet())
			if (entry.getValue().remove(entity)) {
				UUID uuid = entry.getKey();
				Entity owner = Bukkit.getEntity(uuid);
				if (owner == null || !(owner instanceof LivingEntity))
					return;
				GenericItem base = GenericItem.getItemBase(((LivingEntity) owner).getEquipment().getHelmet());
				if (base == null || !(base instanceof SantaHat))
					return;
				Integer value = deadElfTracker.getOrDefault(uuid, 0);
				deadElfTracker.put(uuid, value + 1);
				new BukkitRunnable() {
					@Override
					public void run() {
						Integer value = deadElfTracker.get(uuid);
						if (value != null)
							if (value == 1)
								deadElfTracker.remove(uuid);
							else
								deadElfTracker.replace(uuid, value - 1);
						Entity owner = Bukkit.getEntity(uuid);
						if (owner == null || owner.isDead() || !activePlayers.containsKey(uuid) || (owner instanceof Player && !((Player) owner).isOnline()))
							return;
						summonElves(owner, 1);
					}
				}.runTaskLater(plugin, ((SantaHat) base).elfRespawnTimer);
				return;
			}
	}
	private void summonElves(Entity owner, int amount) {
		Set<UUID> elves = activePlayers.getOrDefault(owner.getUniqueId(), new HashSet<>());
		Location ownerLoc = owner.getLocation();
		Vector vec = Utils.getRandomizedVector().setY(0);
		double increment = (Math.PI * 2) / amount;
		for (int i=0; i < amount; i++) {
			Location loc = ownerLoc.clone().add(vec);
			Location temp = EntityUtils.findSmartYSpawn(ownerLoc, loc, 1, 3);
			if (temp == null)
				loc = ownerLoc;
			else
				loc.setY(temp.getY());
			Elf elf = UIEntityManager.spawnEntity(loc, Elf.class);
			elf.setOwner(owner.getUniqueId());
			elves.add(elf.getUniqueId());
			elf.setSquad(elves, owner.getUniqueId());
			elf.setDropsItemsOnDeath(false);
			vec.rotateAroundY(increment);
		}
		activePlayers.putIfAbsent(owner.getUniqueId(), elves);
	}
	private void removeElves(UUID owner) {
		Set<UUID> elves = activePlayers.remove(owner);
		if (elves == null)
			return;
		elves.forEach((e) -> {
			Entity elf = Bukkit.getEntity(e);
			if (elf != null)
				elf.remove();
		});
	}
	public static void removeEntities() {
		activePlayers.values().forEach((set) -> set.forEach((e) -> {
			Entity entity = Bukkit.getEntity(e);
			if (entity == null)
				return;
			entity.remove();
		}));
	}
}
