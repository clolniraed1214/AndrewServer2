package com.creamyrootbeer.andrewserver.event;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
			try {
				ItemStack item = Plugin.itemdb.get(sign.getLine(2));
				item.setAmount(1);

				player.getInventory().addItem(item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			player.sendMessage(ChatColor.AQUA + "You don't have the cash to buy that!");
		}
	}

	private void playerUseSellSign(Sign sign, Player player) {
		try {
			if (player.getInventory().contains(Plugin.itemdb.get(sign.getLine(2)), 1)) {
				double playerCash = Plugin.economy.getPlayerBalance(player);
				double cost = getSignPrice(sign);
				double newCash = playerCash + cost;

				Plugin.economy.setPlayerBalance(player, newCash);
				return;
			}
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.sendMessage(ChatColor.AQUA + "You don't have any of that item!");
	}

	private double getSignPrice(Sign sign) {
		try {
			ItemStack item = Plugin.itemdb.get(sign.getLine(2));
			item.setAmount(1);
			
			String itemSerial = Plugin.itemdb.serialize(item);
			String query = String.format("SELECT price FROM items WHERE econ_name = '%s' AND name = '%s'",
					sign.getLine(1), itemSerial);
			ResultSet rs = Plugin.db.query(query);
			double price = rs.getDouble("price");
			rs.close();
			
			return price;
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 100000000D;
	}

}
