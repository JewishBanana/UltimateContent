package com.github.jewishbanana.ultimatecontent.entities;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.ultimatecontent.utils.SongPlayer;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer.Song;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BossEntity<T extends Entity> extends BaseEntity<T> {
	
	private SongPlayer songPlayer;
	private BossBar bossBar;

	public BossEntity(T entity, CustomEntityType entityType) {
		super(entity, entityType);
		this.bossBar = createBossBar();
		Song song = getSongTheme();
		if (song != null && SongPlayer.isEnabled() && getSectionBoolean("playBossMusic", true)) {
			songPlayer = new SongPlayer(song);
			songPlayer.setVolume(getSectionDouble("bossMusicVolume", 1.0));
			songPlayer.setLooping();
			songPlayer.setPlaying(true);
		}
		scheduleTask(new BukkitRunnable() {
			private Consumer<Player> consumer = songPlayer == null ? temp -> {
				bossBar.addPlayer(temp);
			} : temp -> {
				bossBar.addPlayer(temp);
				songPlayer.addPlayer(temp);
			};
			
			@Override
			public void run() {
				Entity entity = getEntity();
				if (entity == null)
					return;
				bossBar.removeAll();
				if (songPlayer != null)
					songPlayer.clearPlayers();
				for (Entity temp : entity.getNearbyEntities(30.0, 30.0, 30.0))
					if (temp instanceof Player player)
						consumer.accept(player);
			}
		}.runTaskTimer(plugin, 0, 20));
	}
	public void onDamaged(EntityDamageEvent event) {
		super.onDamaged(event);
		LivingEntity entity = (LivingEntity) event.getEntity();
		bossBar.setProgress(Utils.clamp(1.0 / entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * entity.getHealth(), 0.0, 1.0));
	}
	public void unload() {
		super.unload();
		bossBar.removeAll();
		if (songPlayer != null)
			songPlayer.stopSong();
	}
	public BossBar createBossBar() {
		return Bukkit.createBossBar(this.entityVariant.displayName, BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG);
	}
	public Song getSongTheme() {
		return null;
	}
	public SongPlayer getSongPlayer() {
		return songPlayer;
	}
	public void setSongPlayer(SongPlayer songPlayer) {
		this.songPlayer = songPlayer;
	}
	public BossBar getBossBar() {
		return bossBar;
	}
}
