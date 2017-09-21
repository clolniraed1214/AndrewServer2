package com.creamyrootbeer.andrewserver.event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.creamyrootbeer.andrewserver.Plugin;

public class SignCreateEvent implements Listener {
	
	@EventHandler
	public void onSignCreated(SignChangeEvent e) {
		e.getPlayer().sendMessage(e.getLines());
		
		/*if (!CommandChecker.checkPermission(e.getPlayer(), "andrewserver.sign.create"))
			return;
		if (e.getLine(0).equals("[EconBuy]") || e.getLine(0).equals("[EconSell]")) {
			checkEconExists(e.getLine(1));
			if (checkItemExists(e.getLines(), e.getLine(1))) {
				createSignSQL(e.getLines(), e.getBlock().getLocation());
			} else {
				e.getPlayer().sendMessage(ChatColor.DARK_RED + "Error: Invalid Item!");
			}
		}*/
	}
	
	private void checkEconExists(String name) {
		try {
			System.out.println(name);
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
			
			PreparedStatement stmt = Plugin.db.getConn().prepareStatement("SELECT * FROM items WHERE name = ? and economy = ?");
			stmt.setString(1, Plugin.itemdb.name(Plugin.itemdb.get(lines[2])));
			stmt.setString(2, economy);
			ResultSet rs = stmt.executeQuery();
			stmt.close();
			if (rs.getFetchSize() == 0) {
				stmt = Plugin.db.getConn().prepareStatement("INSERT INTO items (buys, sales, econ, name, price) "
						+ "VALUES (10, 10, ?, ?, 5.00)");
				stmt.setString(1, Plugin.itemdb.serialize(Plugin.itemdb.get(lines[2])));
				stmt.setString(2, economy);
				stmt.execute();
				stmt.close();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
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