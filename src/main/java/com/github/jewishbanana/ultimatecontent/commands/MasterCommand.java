package com.github.jewishbanana.ultimatecontent.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class MasterCommand implements CommandExecutor, TabCompleter {

	private static final String usage;
	static {
		usage = Utils.convertString("&cUsage: /ultimatecontent <event|help|reload>");
	}
	
	public MasterCommand(Main plugin) {
		plugin.getCommand("ultimatecontent").setExecutor(this);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Utils.convertString("&cUsage: /ultimatecontent help"));
			return true;
		}
		switch (args[0].toLowerCase()) {
		case "event":
			if (!(sender instanceof Player)) {
				sender.sendMessage(Utils.convertString("&cYou cannot do this here!"));
				return true;
			}
			if (sender instanceof Player && !sender.hasPermission("ultimatecontent.event")) {
				sender.sendMessage(Utils.convertString(DataUtils.getConfigString("language.commands.permissionError")));
				return true;
			}
			if (Main.getSpecialEvent() == null) {
				sender.sendMessage(Utils.convertString(DataUtils.getConfigString("language.events.noEvent")));
				return true;
			}
			Main.getSpecialEvent().openGUI((Player) sender);
			return true;
		case "help":
			if (sender instanceof Player && !sender.hasPermission("ultimatecontent.help")) {
				sender.sendMessage(Utils.convertString(DataUtils.getConfigString("language.commands.permissionError")));
				return true;
			}
			if (args.length == 1) {
				sender.sendMessage(Utils.convertString("&9UltimateContent &ahelp menu:"));
				sender.sendMessage(Utils.convertString("&6/uc event &8- &7"+DataUtils.getConfigString("language.help.event")));
				sender.sendMessage(Utils.convertString("&6/uc help &8- &7"+DataUtils.getConfigString("language.help.help")));
				sender.sendMessage(Utils.convertString("&6/uc reload &8- &7"+DataUtils.getConfigString("language.help.reload")));
				sender.sendMessage(Utils.convertString("&d"+DataUtils.getConfigString("language.help.info")));
				return true;
			}
			switch (args[1].toLowerCase()) {
			case "event":
				sender.sendMessage(Utils.convertString("&6/uc event &8- &7"+DataUtils.getConfigString("language.help.eventDescription")));
				return true;
			case "help":
				sender.sendMessage(Utils.convertString("&6/uc help &8- &7"+DataUtils.getConfigString("language.help.helpDescription")));
				return true;
			case "reload":
				sender.sendMessage(Utils.convertString("&6/uc reload &8- &7"+DataUtils.getConfigString("language.help.reloadDescription")));
				return true;
			}
			sender.sendMessage(Utils.convertString("&cUnknown command argument '"+args[1]+"'!"));
			return true;
		case "reload":
			if (sender instanceof Player && !sender.hasPermission("ultimatecontent.reload")) {
				sender.sendMessage(Utils.convertString(DataUtils.getConfigString("language.commands.permissionError")));
				return true;
			}
			Main.reload();
			sender.sendMessage(Utils.convertString("&aSuccessfully reloaded the &9UltimateContent &aconfig!"));
			sender.sendMessage(Utils.convertString("&ePlease keep in mind that it is highly recommended to do reloads through UltimateContent instead! &d(/ui reload)"));
			return true;
		}
		if (sender instanceof Player && !sender.hasPermission("ultimatecontent.*")) {
			sender.sendMessage(Utils.convertString(DataUtils.getConfigString("language.commands.permissionError")));
			return true;
		}
		sender.sendMessage(usage);
		return true;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new ArrayList<>();
		String keyword;
		switch (args.length) {
		default:
		case 0:
			return list;
		case 1:
			keyword = args[0].toLowerCase();
			if (sender.hasPermission("ultimatecontent.event"))
				list.add("event");
			if (sender.hasPermission("ultimatecontent.help"))
				list.add("help");
			if (sender.hasPermission("ultimatecontent.reload"))
				list.add("reload");
			list.removeIf(e -> !e.contains(keyword));
			break;
		case 2:
			keyword = args[1].toLowerCase();
			if (args[0].equalsIgnoreCase("help") && sender.hasPermission("ultimatecontent.help")) {
				list.add("help");
				if (sender.hasPermission("ultimatecontent.event"))
					list.add("event");
				if (sender.hasPermission("ultimatecontent.reload"))
					list.add("reload");
			}
			list.removeIf(e -> !e.contains(keyword));
			break;
		}
		return list;
	}
}
