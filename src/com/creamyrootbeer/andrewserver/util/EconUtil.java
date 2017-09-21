package com.creamyrootbeer.andrewserver.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.creamyrootbeer.andrewserver.Plugin;

public class EconUtil {

	public static void buy(String itemName, String economy) {
		transaction(itemName, economy, true);
	}

	public static void sell(String itemName, String economy) {
		transaction(itemName, economy, false);
		double sold = getTime(getItemID(itemName, economy), false, 30);
		double bought = getTime(getItemID(itemName, economy), false, 30);
		calcMult(bought / sold, .25, 20);
	}

	private static void transaction(String itemName, String economy, boolean bought) {
		try {

			ResultSet rs = Plugin.db.query(String
					.format("SELECT item_id FROM items WHERE name = '%s' AND econ_name = '%s'", itemName, economy));
			int itemID = rs.getInt("item_id");

			PreparedStatement stmt = Plugin.db.getConn()
					.prepareStatement("INSERT INTO purchases (time, item_id, bought) VALUES (?, ?, ?)");
			stmt.setLong(1, System.currentTimeMillis());
			stmt.setInt(2, itemID);
			stmt.setInt(3, bought ? 1 : 0);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static double getTime(int itemID, boolean bought, int count) {
		try {
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement(
					String.format(
							"SELECT purchase_id, time FROM purchases WHERE item_id = ? AND bought = %d ORDER BY time DESC LIMIT 0,%d"),
					bought ? 1 : 0, count);
			stmt.setInt(1, itemID);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
			rs.last();
			double value = rs.getLong("time") / rs.getRow();
			rs.close();
			return value;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@SuppressWarnings("unused")
	private static double calcMult(double ratio, double maxChance, double xMod) {
		return (2 * maxChance) / (1 + Math.pow(Math.E, -ratio / xMod)) + (1 - maxChance);
	}

	public static void buy(Material mat, String economy) {
		ItemStack item = new ItemStack(mat, 1);
		buy(Plugin.itemdb.serialize(item), economy);
	}

	public static void buy(ItemStack item, String economy) {
		buy(item.getType(), economy);
	}

	public static void sell(Material mat, String economy) {
		ItemStack item = new ItemStack(mat, 1);
		sell(Plugin.itemdb.serialize(item), economy);
	}

	public static void sell(ItemStack item, String economy) {
		sell(item.getType(), economy);
	}

	public static void updateSigns(String itemName, String economyName) {
		// Bukkit.broadcastMessage("Updating signs!");
		try {
			PreparedStatement stmt = Plugin.db.getConn()
					.prepareStatement("SELECT item_id, price FROM items WHERE name = ? AND econ_name = ?");
			stmt.setString(1, itemName);
			stmt.setString(2, economyName);
			ResultSet rs = stmt.executeQuery();
			int itemID = rs.getInt("item_id");
			double price = rs.getDouble("price");
			rs.close();

			rs = Plugin.db.query(String.format("SELECT xpos, ypos, zpos FROM signs WHERE item_id = %d", itemID));
			while (rs.next()) {
				int xpos, ypos, zpos;
				xpos = rs.getInt("xpos");
				ypos = rs.getInt("ypos");
				zpos = rs.getInt("zpos");

				BlockState state = Bukkit.getWorlds().get(0).getBlockAt(xpos, ypos, zpos).getState();
				if (state instanceof Sign) {
					Sign sign = (Sign) state;
					sign.setLine(3, Plugin.economy.getEconomy().format(price));
					sign.update();
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void updateSigns(Material mat, String economyName) {
		ItemStack item = new ItemStack(mat, 1);
		updateSigns(Plugin.itemdb.serialize(item), economyName);
	}

	public static void updateSigns(ItemStack item, String economyName) {
		updateSigns(item.getType(), economyName);
	}
	
	public static int getItemID(String itemName, String economy) {
		try {
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT item_id FROM items WHERE name = ? AND econ_name = ?");
			stmt.setString(1, itemName);
			stmt.setString(2, economy);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
			int id = rs.getInt("item_id");
			rs.close();
			return id;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
