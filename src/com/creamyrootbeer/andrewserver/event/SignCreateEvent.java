package com.creamyrootbeer.andrewserver.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.creamyrootbeer.andrewserver.Constants;
import com.creamyrootbeer.andrewserver.Plugin;
import com.creamyrootbeer.andrewserver.util.CommandChecker;
import com.creamyrootbeer.andrewserver.util.EconUtil;

public class SignCreateEvent implements Listener {
	
	@EventHandler
	public void onSignCreated(SignChangeEvent e) {
		if (!CommandChecker.checkPermission(e.getPlayer(), "andrewserver.sign.create"))
			return;
		if (e.getLine(0).equals("[EconBuy]") || e.getLine(0).equals("[EconSell]")) {
			checkEconExists(e.getLine(1));
			if (checkItemExists(e.getLines(), e.getLine(1))) {
				createSignSQL(e.getLines(), e.getBlock().getLocation());
				
				if (e.getLine(0).equals("[EconBuy]")) e.setLine(0, Constants.BUY_SIGN_TEXT);
				if (e.getLine(0).equals("[EconSell]")) e.setLine(0, Constants.SELL_SIGN_TEXT);
			} else {
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "Error: Invalid Item!");
			}
		}
	}
	
	private void checkEconExists(String name) {
		try {
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT COUNT(*) FROM economies WHERE name = ?");
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			if (rs.getInt("COUNT(*)") == 0) {
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO economies (name) VALUES (?)");
				stmt.setString(1, name);
				stmt.execute();
				stmt.close();
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkItemExists(String[] lines, String economy) {
		try {
			if (Plugin.itemdb.get(lines[2]) == null) return false;
			ItemStack item = Plugin.itemdb.get(lines[2]);
			item.setAmount(1);
			String itemName = Plugin.itemdb.serialize(item);
			
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT COUNT(*) FROM items WHERE name = ? and econ_name = ?");
			stmt.setString(1, itemName);
			stmt.setString(2, economy);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			
			if (rs.getInt("COUNT(*)") == 0) {
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO items (buys, sales, econ_name, name, price) "
						+ "VALUES (10, 10, ?, ?, 5.00)");
				stmt.setString(1, economy);
				stmt.setString(2, itemName);
				stmt.execute();
				stmt.close();
				
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO purchases (time, item_id, bought) VALUES (?, ?, ?)");
				stmt.setLong(1, System.currentTimeMillis());
				stmt.setInt(2, EconUtil.getItemID(itemName, economy));
				stmt.setInt(3, 1);
				stmt.executeUpdate();
				
				stmt.setInt(3, 0);
				stmt.executeUpdate();
				stmt.close();
			}
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void createSignSQL(String[] lines, Location loc) {
		try {
			ItemStack item = Plugin.itemdb.get(lines[2]);
			item.setAmount(1);
			String itemName = Plugin.itemdb.serialize(item);
		
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT item_id FROM items WHERE econ_name = ? and name = ?"); 
			stmt.setString(1, lines[1]);
			stmt.setString(2, itemName);
			ResultSet rs = stmt.executeQuery();
			
			int itemID = rs.getInt("item_id");
			
			stmt = Plugin.db.getConn().prepareStatement("INSERT INTO signs (xpos, ypos, zpos, item_id) VALUES (?, ?, ?, ?)");
			stmt.setInt(1, loc.getBlockX());
			stmt.setInt(2, loc.getBlockY());
			stmt.setInt(3, loc.getBlockZ());
			stmt.setInt(4, itemID);
			stmt.execute();
			stmt.close();
			
			EconUtil.updateSigns(itemName, lines[1]);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}