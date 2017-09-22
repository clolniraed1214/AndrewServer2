package com.creamyrootbeer.andrewserver.event;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.creamyrootbeer.andrewserver.Constants;
import com.creamyrootbeer.andrewserver.Plugin;

public class SignBreakEvent implements Listener {

	@EventHandler
	public void onSignBreak(BlockBreakEvent e) {
		if (e.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) e.getBlock().getState();
			if (sign.getLine(0).equals(Constants.BUY_SIGN_TEXT) || sign.getLine(0).equals(Constants.SELL_SIGN_TEXT)) {
				signDestroy(sign);
			}
		}
	}
	
	private void signDestroy(Sign sign) {
		Location loc = sign.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		
		Plugin.db.runCommand("DELETE FROM signs WHERE xpos = " + String.valueOf(x)
				+ " AND ypos = " + String.valueOf(y)
				+ " AND zpos = " + String.valueOf(z));
	}
	
}
