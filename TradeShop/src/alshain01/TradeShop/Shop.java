package alshain01.TradeShop;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;

public class Shop {

	private ConcurrentHashMap<Integer, Trade> trades = new ConcurrentHashMap<Integer, Trade>();
	private String owner;
	
	public Shop(String ownerName) {
		this.owner = ownerName;
	}
	
	public String getOwner() {
		return this.owner;
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
		return openSlot;
	}
	
	/**
	 * Removes a trade based on its ID number
	 * 
	 * @param tradeID The chest slot ID of the trade item.
	 */
	public void removeTrade(int tradeID) {
		trades.remove(tradeID);
	}
	
	/*
	 * Writes the shop to the data store
	 */
	protected void write(ConfigurationSection data) {
		Iterator<Entry<Integer, Trade>> it = trades.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Trade> trade = it.next();
			((Trade)trade).write(data, trade.getKey().toString());
		}			
	}
}
