package com.creamyrootbeer.andrewserver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.creamyrootbeer.andrewserver.event.SignBreakEvent;
import com.creamyrootbeer.andrewserver.event.SignClickEvent;
import com.creamyrootbeer.andrewserver.event.SignCreateEvent;
import com.creamyrootbeer.andrewserver.util.EconHook;
import com.creamyrootbeer.andrewserver.util.SQLiteDBConnection;
import com.earth2me.essentials.ItemDb;

import net.ess3.api.IEssentials;

public class Plugin extends JavaPlugin {

	public static Plugin plugin;
	public static Logger logger;
	public static SQLiteDBConnection db;
	private static final Listener[] EVENT_HANDLERS = new Listener[] {
			new SignClickEvent(),
			new SignBreakEvent(),
			new SignCreateEvent()
	};
	
	public static EconHook economy;
	public static ItemDb itemdb;

	public Plugin() throws Exception {
		plugin = this;
		logger = Bukkit.getLogger();
	}

	@Override
	public void onEnable() {
		registerEvents();
		initializeDB();
		itemdb = new ItemDb((IEssentials) Bukkit.getPluginManager().getPlugin("Essentials"));
		economy = new EconHook();
	}

	private void initializeDB() {
		db = new SQLiteDBConnection("plugins/AndrewServer/economy.db");

		if (!db.getInitialized()) {
			logger.log(Level.SEVERE, "Connection to Database not established!");
		}
		
		db.createTable("signs", "sign_id INT AUTO_INCREMENT, xpos INT, ypos INT, zpos INT");
		db.createTable("economies", "econ_id INT AUTO_INCREMENT, name VARCHAR(32)");
		db.createTable("items", "entry_id INT AUTO_INCREMENT, buys INT, sales INT, econ INT, name TEXT, price DOUBLE, times LONG");
		db.createTable("purchases", "id INT AUTO_INCREMENT, time BIGINT, item INT");
	}

	private void registerEvents() {
		for (Listener eventHandler : EVENT_HANDLERS) {
			Bukkit.getPluginManager().registerEvents(eventHandler, this);
		}
	}
}
