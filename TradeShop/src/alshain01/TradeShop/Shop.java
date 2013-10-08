package alshain01.TradeShop;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Shop {
	private ConfigurationSection shopData;
	private Location location;
	
	public Shop(Location location) {
		this.location = location;
		shopData = TradeShop.dataStore.getConfig().getConfigurationSection("Shops." + location.toString());
	}
	
	public String getOwner() {
		return shopData.getString("Owner");
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public boolean exists() {
		return !(shopData.getString("Owner") == null);
	}

	public boolean setOwner(String playerName) {
		if(exists()) { return false; }
		shopData.set("Owner", playerName);
		return true;
	}
	
	public boolean remove() {
		if(!exists()) { return false; }
		shopData.getParent().set(location.toString(), null);
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	public boolean addTrade(int slot, Trade newTrade) {
		if(shopData.getString(String.valueOf(slot)) != null) {
			return false;
		}
		
		shopData.set(String.valueOf(slot), newTrade.getItems());
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	public int addTrade(Trade newTrade) {
		Set<String> keys = shopData.getKeys(false);
		int openSlot = -1;
		
		// Find the first empty slot
		for(int i = 0; i < 9; i++) {
			if(keys.contains(String.valueOf(i))) { continue; }
			openSlot = i;
			break;
		}
			
		if(openSlot != -1) { addTrade(openSlot, newTrade); }
		return openSlot;
	}
	
	public Trade getTrade(int slot) {
		return new Trade((List<Map<String, Object>>)shopData.getMapList(String.valueOf(slot)));
	}
	
	/**
	 * Removes a trade based on its slot number
	 * 
	 * @param slot The chest slot ID of the trade item.
	 */
	public void removeTrade(int slot) {
		shopData.set(String.valueOf(slot), null);
		TradeShop.dataStore.saveConfig();
	}
}
