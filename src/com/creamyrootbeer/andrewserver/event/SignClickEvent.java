package com.creamyrootbeer.andrewserver.event;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.creamyrootbeer.andrewserver.Plugin;

import net.md_5.bungee.api.ChatColor;

public class SignClickEvent implements Listener {

	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {		
		if (e.getClickedBlock().getType().equals(Material.SIGN_POST)
				|| e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) e.getClickedBlock().getState();

				if (sign.getLine(0).equals("[EconBuy]"))
					playerUseBuySign(sign, e.getPlayer());
				if (sign.getLine(0).equals("[EconSell]"))
					playerUseSellSign(sign, e.getPlayer());
			}
		}

	}

	private void playerUseBuySign(Sign sign, Player player) {
		double playerCash = Plugin.economy.getPlayerBalance(player);
		double cost = getSignPrice(sign);
		if (playerCash >= cost) {
			double newCash = playerCash - cost;
			Plugin.economy.setPlayerBalance(player, newCash);
		} else {
			player.sendMessage(ChatColor.AQUA + "You don't have the cash to buy that!");
		}
		
		try {
			player.getInventory().addItem(Plugin.itemdb.get(sign.getLine(2)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void playerUseSellSign(Sign sign, Player player) {
		double playerCash = Plugin.economy.getPlayerBalance(player);
		double cost = getSignPrice(sign);
		double newCash = playerCash + cost;
		
		Plugin.economy.setPlayerBalance(player, newCash);
	}

	private double getSignPrice(Sign sign) {
		try {
			String query = String.format("SELECT price FROM signs WHERE xpos = %d AND ypos = %d AND zpos = %d",
					sign.getX(), sign.getY(), sign.getZ());
			ResultSet rs = Plugin.db.query(query);
			return rs.getDouble("price");
		} catch (SQLException e) {
			e.printStackTrace();
			return 100000000D;
		}
	}

}
