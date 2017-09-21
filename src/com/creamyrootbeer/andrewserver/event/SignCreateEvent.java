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

import com.creamyrootbeer.andrewserver.Plugin;
import com.creamyrootbeer.andrewserver.util.CommandChecker;

public class SignCreateEvent implements Listener {
	
	@EventHandler
	public void onSignCreated(SignChangeEvent e) {
		if (!CommandChecker.checkPermission(e.getPlayer(), "andrewserver.sign.create"))
			return;
		if (e.getLine(0).equals("[EconBuy]") || e.getLine(0).equals("[EconSell]")) {
			checkEconExists(e.getLine(1));
			if (checkItemExists(e.getLines(), e.getLine(1))) {
				createSignSQL(e.getLines(), e.getBlock().getLocation());
			} else {
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "Error: Invalid Item!");
			}
		}
	}
	
	private void checkEconExists(String name) {
		try {
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT * FROM economies WHERE name = ?");
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
			
			if (rs.getFetchSize() == 0) {
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO economies (name) VALUES (?)");
				stmt.setString(1, name);
				stmt.execute();
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean checkItemExists(String[] lines, String economy) {
		try {
			if (Plugin.itemdb.get(lines[2]) == null) return false;
			
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT * FROM items WHERE name = ? and econ_name = ?");
			stmt.setString(1, Plugin.itemdb.name(Plugin.itemdb.get(lines[2])));
			stmt.setString(2, economy);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
			if (rs.getFetchSize() == 0) {
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO items (buys, sales, econ_name, name, price) "
						+ "VALUES (10, 10, ?, ?, 5.00)");
				ItemStack item = Plugin.itemdb.get(lines[2]);
				item.setAmount(1);
				stmt.setString(1, economy);
				stmt.setString(2, Plugin.itemdb.serialize(item));
				stmt.execute();
				stmt.close();
			}
			
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
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("INSERT INTO signs (xpos, ypos, zpos) VALUES (?, ?, ?)");
			stmt.setInt(1, loc.getBlockX());
			stmt.setInt(2, loc.getBlockY());
			stmt.setInt(3, loc.getBlockZ());
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}