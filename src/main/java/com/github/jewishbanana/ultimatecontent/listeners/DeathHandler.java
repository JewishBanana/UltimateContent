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

	private Main plugin;
	private Map<String, String> deathMessages = new HashMap<>();
	
	public DeathHandler(Main plugin) {
		this.plugin = plugin;
		for (String s : plugin.getConfig().getConfigurationSection("language.deaths").getKeys(false))
			deathMessages.put("deaths."+s, Utils.convertString(DataUtils.getConfigString("language.deaths."+s)));
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		deathMessages.forEach((k, v) -> {
			if (e.getEntity().hasMetadata(k)) {
				e.getEntity().removeMetadata(k, plugin);
				String killer = "Unknown";
				if (e.getEntity().getLastDamageCause() != null && e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
					if (event.getDamager() != null) {
						if (event.getDamager() instanceof Player)
							killer = Utils.convertString(((Player) event.getDamager()).getDisplayName());
						else if (event.getDamager() instanceof Projectile) {
							Projectile projectile = (Projectile) event.getDamager();
							if (projectile.getShooter() != null && projectile.getShooter() instanceof LivingEntity) {
								if (projectile.getShooter() instanceof Player)
									killer = Utils.convertString(((Player) projectile.getShooter()).getDisplayName());
								else
									killer = ((LivingEntity) projectile.getShooter()).getCustomName() != null ? Utils.convertString(((LivingEntity) projectile.getShooter()).getCustomName()) : ((LivingEntity) projectile.getShooter()).getType().getEntityClass().getName();
							} else
								killer = event.getDamager().getType().getEntityClass().getName();
						} else
							killer = event.getDamager().getCustomName() != null ? Utils.convertString(event.getDamager().getCustomName()) : event.getDamager().getType().getEntityClass().getName();
					}
				}
				e.setDeathMessage(Utils.convertString(v.replace("%victim%", e.getEntity().getDisplayName())
						.replace("%killer%", e.getEntity().getKiller() == null ? killer : e.getEntity().getKiller().getDisplayName())));
				return;
			}
		});
	}
}
