package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class DeathHandler implements Listener {

	private Map<String, String> deathMessages = new HashMap<>();
	
	public DeathHandler(Main plugin) {
		for (String s : plugin.getConfig().getConfigurationSection("language.deaths").getKeys(false))
			deathMessages.put("deaths."+s, Utils.convertString(DataUtils.getConfigString("language.deaths."+s)));
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		for (Map.Entry<String, String> entry : deathMessages.entrySet()) {
			if (!player.hasMetadata(entry.getKey()))
				continue;
			String killer = "Unknown";
			if (player.getLastDamageCause() != null && player.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
				if (damageEvent.getDamager() != null) {
					if (damageEvent.getDamager() instanceof Player damager)
						killer = Utils.convertString(damager.getDisplayName());
					else if (damageEvent.getDamager() instanceof Projectile projectile) {
						if (projectile.getShooter() != null && projectile.getShooter() instanceof LivingEntity shooter) {
							if (projectile.getShooter() instanceof Player shootingPlayer)
								killer = Utils.convertString(shootingPlayer.getDisplayName());
							else
								killer = shooter.getCustomName() != null ? Utils.convertString(shooter.getCustomName()) : shooter.getType().getEntityClass().getName();
						} else
							killer = damageEvent.getDamager().getType().getEntityClass().getName();
					} else
						killer = damageEvent.getDamager().getCustomName() != null ? Utils.convertString(damageEvent.getDamager().getCustomName()) : damageEvent.getDamager().getType().getEntityClass().getName();
				}
			}
			event.setDeathMessage(Utils.convertString(entry.getValue().replace("%player%", player.getDisplayName())
					.replace("%killer%", player.getKiller() == null ? killer : player.getKiller().getDisplayName())));
			return;
		}
	}
}
