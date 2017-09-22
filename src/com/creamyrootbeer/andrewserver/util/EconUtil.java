package com.creamyrootbeer.andrewserver.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.creamyrootbeer.andrewserver.Constants;
import com.creamyrootbeer.andrewserver.Plugin;

public class EconUtil {

	public static void buy(String itemName, String economy) {
		transaction(itemName, economy, true);
		recalculatePrices(itemName, economy);
	}

	public static void sell(String itemName, String economy) {
		transaction(itemName, economy, false);
		recalculatePrices(itemName, economy);
	}
	
	public static void recalculatePrices(String itemName, String economy) {
		try {
			int id = getItemID(itemName, economy);
			double sold = getTime(id, false, 30);
			double bought = getTime(id, true, 30);
			double mult = calcMult(Math.log(sold / bought), .25, 20);
			Statement stmt = Plugin.db.getConn().createStatement();
			ResultSet rs = stmt.executeQuery(String.format("SELECT price FROM items WHERE item_id = %d", id));
			double price = rs.getDouble("price");
			rs.close();
			stmt.close();
			price *= mult;
			
			PreparedStatement pstmt = Plugin.db.getConn().prepareStatement("UPDATE items SET price = ? WHERE item_id = ?");
			pstmt.setDouble(1, price);
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
			
			updateSigns(itemName, economy);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
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
							"SELECT purchase_id, time FROM purchases WHERE item_id = ? AND bought = %d ORDER BY time DESC LIMIT 0,%d",
					bought ? 1 : 0, count));
			stmt.setInt(1, itemID);
			ResultSet rs = stmt.executeQuery();
			
			long max;
			int size = 0;
			max = rs.getLong("time");
			while (rs.next()) {
				if (rs.getLong("time") < max) max = rs.getLong("time");
				size++;
			}
			
//			Bukkit.broadcastMessage(String.valueOf(max));
//			Bukkit.broadcastMessage(String.valueOf(System.currentTimeMillis()));
//			Bukkit.broadcastMessage(String.valueOf(System.currentTimeMillis() - max));
			
			double value = (System.currentTimeMillis() - max) / size;
			rs.close();
			stmt.close();
			return value;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

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
					double newPrice;
					if (sign.getLine(0).equals(Constants.SELL_SIGN_TEXT)) {
						newPrice = price * Constants.SELLING_MULT;
					} else {
						newPrice = price;
					}
					sign.setLine(3, Plugin.economy.getEconomy().format(newPrice));
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
			int id = rs.getInt("item_id");
			stmt.close();
			rs.close();
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
