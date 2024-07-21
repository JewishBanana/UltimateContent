package com.github.jewishbanana.ultimatecontent.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.github.jewishbanana.ultimatecontent.Main;

public abstract class AsyncRepeatingTask implements Runnable {

	private static JavaPlugin javaPlugin;
	static {
		javaPlugin = Main.getInstance();
	}
    private BukkitTask taskId;

    public AsyncRepeatingTask(int arg1, int arg2) {
        taskId = javaPlugin.getServer().getScheduler().runTaskTimerAsynchronously(javaPlugin, this, arg1, arg2);
    }
    public void cancel() {
        taskId.cancel();
    }
}