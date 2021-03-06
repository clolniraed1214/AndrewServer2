package com.creamyrootbeer.andrewserver.event;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.creamyrootbeer.andrewserver.Constants;
import com.creamyrootbeer.andrewserver.Plugin;
import com.creamyrootbeer.andrewserver.util.EconUtil;

import net.md_5.bungee.api.ChatColor;

public class SignClickEvent implements Listener {

	@EventHandler
	public void onSignClick(PlayerInteractEvent e) {
		if (e.getClickedBlock().getType().equals(Material.SIGN_POST)
				|| e.getClickedBlock().getType().equals(Material.WALL_SIGN)) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Sign sign = (Sign) e.getClickedBlock().getState();

				if (sign.getLine(0).equals(Constants.BUY_SIGN_TEXT))
					playerUseBuySign(sign, e.getPlayer());
				if (sign.getLine(0).equals(Constants.SELL_SIGN_TEXT))
					playerUseSellSign(sign, e.getPlayer());
			}
		}

	}

	
	
	
	
	
	private void playerUseBuySign(Sign sign, Player player) {
		double playerCash = Plugin.economy.getPlayerBalance(player);
		double cost = getSignPrice(sign);
		
		if (playerCash >= cost) {
			Plugin.economy.takePlayer(player, cost);
			try {
				ItemStack item = Plugin.itemdb.get(sign.getLine(2));
				item.setAmount(8);
				
				player.sendMessage(ChatColor.GREEN+ "You purchased that item for " + Plugin.economy.getEconomy().format(cost));
				player.getInventory().addItem(item);
				EconUtil.buy(item, sign.getLine(1));
				EconUtil.updateSigns(item.getType(), sign.getLine(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			player.sendMessage(ChatColor.AQUA + "You don't have the cash to buy that!");
		}
	}

	private void playerUseSellSign(Sign sign, Player player) {
		try {
			if (player.getInventory().contains(Plugin.itemdb.get(sign.getLine(2)).getType(), 8)) {
				double cost = getSignPrice(sign) * Constants.SELLING_MULT;
				Plugin.economy.givePlayer(player, cost);
				player.sendMessage(ChatColor.GREEN + "You sold that item for " + Plugin.economy.getEconomy().format(cost));
				
				Material mat = Plugin.itemdb.get(sign.getLine(2)).getType();
				removeItem(player.getInventory(), mat, 8);
				EconUtil.sell(mat, sign.getLine(1));
				return;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
	
	public void removeItem(Inventory inv, Material mat, int amount) {
		if (inv.contains(mat)) {
			int left = amount;
			ItemStack[] items = inv.getContents();
			for (int i = 0; i < items.length; i++) {
				try {
					if (items[i].getType().equals(mat)) {
						if (items[i].getAmount() >= left) {
							items[i].setAmount(items[i].getAmount() - left);
							break;
						} else {
							left -= items[i].getAmount();
							items[i].setAmount(0);
						}
					}
				} catch (NullPointerException e) {
					
				}
			}
			inv.setContents(items);
		}
	}

}
