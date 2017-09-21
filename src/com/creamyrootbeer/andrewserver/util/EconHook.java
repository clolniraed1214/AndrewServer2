package com.creamyrootbeer.andrewserver.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class EconHook {

	private Economy economy;
	private boolean isSetup;
	
	public EconHook() {
		RegisteredServiceProvider<Economy> econ = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (econ != null) {
            economy = econ.getProvider();
        }
        isSetup = (economy != null);
        if (!isSetup) {
        	Bukkit.broadcastMessage(ChatColor.YELLOW + "Vault Hook Failed!");
        }
	}
	
	public double getPlayerBalance(OfflinePlayer player) {
		if (isSetup) {
			return economy.getBalance(player);
		} else {
			return 0;
		}
	}
	
	public void setPlayerBalance(OfflinePlayer player, double amount) {
		if (isSetup) economy.depositPlayer(player, amount);
	}

}
