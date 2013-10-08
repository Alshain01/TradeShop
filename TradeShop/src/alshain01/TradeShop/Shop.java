package alshain01.TradeShop;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

public class Shop {
	private ConcurrentHashMap<Integer, Trade> trades = new ConcurrentHashMap<Integer, Trade>();
	private Location location;
	
	public Shop(Location location) {
		this.location = location;
	}
	
	public String getOwner() {
		return TradeShop.dataStore.getConfig().getString("Shops." + location.toString() + ".Owner");
	}
	
	public boolean setOwner(String playerName) {
		if(exists()) { return false; }
		TradeShop.dataStore.getConfig().set("Shops." + location.toString() + ".Owner", playerName);
		return true;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public boolean exists() {
		return !(TradeShop.dataStore.getConfig().getString("Shops." + location.toString() + ".Owner") == null);
	}
	
	public boolean remove() {
		if(!exists()) { return false; }
		TradeShop.dataStore.getConfig().set(location.toString(), null);
		return true;
	}
	
	/**
	 * Adds a new trade to the shop.
	 * 
	 * @param newTrade The trade to add to the shop.
	 * @return The slot number the trade has been assigned. -1 if the shop is full 
	 */
	public int addTrade(Trade newTrade) {
		int openSlot = -1;

		// Find the first empty slot
		for(int i = 0; i < 9; i++) {
			if(trades.containsKey(String.valueOf(i))) { continue; }
			openSlot = i;
			break;
		}
			
		if(openSlot != -1) { trades.put(openSlot, newTrade); }
		//this.write();
		return openSlot;
	}
	
	/**
	 * Removes a trade based on its ID number
	 * 
	 * @param tradeID The chest slot ID of the trade item.
	 */
	public void removeTrade(int tradeID) {
		trades.remove(tradeID);
		//this.write();
	}
	
/*	
	 * Writes the shop to the data store
	 
	private void write() {
		ConfigurationSection data = TradeShop.dataStore.getConfig().getConfigurationSection("Shops." + location.toString());
		data.set("Owner", owner);
		Iterator<Entry<Integer, Trade>> it = trades.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Trade> trade = it.next();
			((Trade)trade).write(data, trade.getKey().toString());
		}			
	}*/
}
