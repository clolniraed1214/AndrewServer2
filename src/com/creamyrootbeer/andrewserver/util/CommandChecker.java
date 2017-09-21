package com.creamyrootbeer.andrewserver.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandChecker {
	
	public static boolean checkPlayer(CommandSender sender) {
		if (sender instanceof Player) {
			return true;
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command!");
			return false;
		}
	}
	
	public static boolean checkPermission(CommandSender sender, String permission) {
		if (sender.hasPermission(permission)) {
			return true;
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "You are not allowed to do that!");
			return false;
		}
	}
	
}
