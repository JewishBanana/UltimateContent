package com.github.jewishbanana.ultimatecontent.utils;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.ultimatecontent.Main;

public class SongPlayer {
	
	private static JavaPlugin plugin;
	private static boolean isNBEnabled;
	static {
		plugin = Main.getInstance();
		isNBEnabled = plugin.getServer().getPluginManager().isPluginEnabled("NoteBlockAPI");
		if (!isNBEnabled)
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eThere is a soft dependency on NoteBlockAPI, without it the only feature that will be disabled is custom note block themed music (Bosses have theme music)."));
	}
	
	public enum Song {
		
		HALLOWEEN_BOSS("SpookySkeletons.nbs"),
		CHRISTMAS_BOSS("SleighRide.nbs"),
		EASTER_BOSS("SummerBreeze.nbs");
		
		private String file;
		
		private Song(String file) {
			this.file = file;
		}
		private com.xxmicloxx.NoteBlockAPI.model.Song getNBSSong() {
			return com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder.parse(plugin.getResource("songs/"+file));
		}
	}
	
	private com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer songPlayer;
	
	public SongPlayer(Song song) {
		songPlayer = new com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer(song.getNBSSong());
	}
	public void addPlayer(Player player) {
		songPlayer.addPlayer(player.getUniqueId());
	}
	public void removePlayer(Player player) {
		songPlayer.removePlayer(player.getUniqueId());
	}
	public void clearPlayers() {
		for (UUID uuid : songPlayer.getPlayerUUIDs())
			songPlayer.removePlayer(uuid);
	}
	public void setPlaying(boolean value) {
		songPlayer.setPlaying(value);
	}
	public void stopSong() {
		songPlayer.destroy();
	}
	public void setLooping() {
		songPlayer.setRepeatMode(com.xxmicloxx.NoteBlockAPI.model.RepeatMode.ALL);
	}
	public void setVolume(double volume) {
		songPlayer.setVolume((byte) (volume * 50.0));
	}
	public static boolean isEnabled() {
		return isNBEnabled;
	}
}
