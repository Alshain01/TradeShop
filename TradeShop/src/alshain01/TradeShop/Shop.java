package alshain01.TradeShop;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Shop {
	private ConfigurationSection shopData;
	private Location location;
	
	/**
	 * Creates a new shop at the given block location.
	 * 
	 * @param location The location of the chest that will be a shop
	 */
	public Shop(Location location) {
		this.location = location;
		shopData = TradeShop.dataStore.getConfig().getConfigurationSection("Shops." + location.toString());
	}
	
	/**
	 * Retrieves the owner of the current shop.
	 * 
	 * @return The owner name of the shop.
	 */
	public String getOwner() {
		return shopData.getString("Owner");
	}
	
	/**
	 * Retrieves the location of the current shop 
	 * 
	 * @return The shop location.
	 */
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Retrieves whether or not the shop has been configured.
	 * A shop is considered non existent if it has no assigned owner.
	 * 
	 * @return True if the shop exists
	 */
	public boolean exists() {
		return !(shopData.getString("Owner") == null);
	}

	/**
	 * Sets the permanent owner of the shop.  This can not be changed.
	 * 
	 * @param playerName The owner name to assign to the shop
	 * @return False if this shop location has an owner and could not be set.
	 */
	public boolean setOwner(String playerName) {
		if(exists()) { return false; }
		shopData.set("Owner", playerName);
		return true;
	}
	
	/**
	 * Removes a shop from the data storage.
	 * 
	 * @return False if the shop did not exist.
	 */
	public boolean remove() {
		if(!exists()) { return false; }
		shopData.getParent().set(location.toString(), null);
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	/**
	 * Adds a new trade to the shop.
	 * 
	 * @param slot The chest slot number where the sell item is located.
	 * @param newTrade The trade that occupies this slot number.
	 * @return False if a trade already existed in that slot.
	 */
	public boolean addTrade(int slot, Trade newTrade) {
		if(shopData.getString(String.valueOf(slot)) != null) {
			return false;
		}
		
		ConfigurationSection trade = shopData.getConfigurationSection(String.valueOf(slot));
		
		trade.set("Buy1", newTrade.getBuyItem(0));
		trade.set("Buy2", newTrade.getBuyItem(1));
		trade.set("Sell", newTrade.getSellItem());
		TradeShop.dataStore.saveConfig();
		return true;
	}
	
	/**
	 * Adds a new trade to the shop
	 * 
	 * @param newTrade The trade to be added to the shop
	 * @return The slot number the trade was assigned.
	 */
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
	
	/**
	 * Retrieves a trade.
	 * 
	 * @param slot The slot for which to retrieve a trade.
	 * @return The trade requested. 
	 */
	public Trade getTrade(int slot) {
		ConfigurationSection trade = shopData.getConfigurationSection(String.valueOf(slot)); 
		return new Trade(trade.getItemStack("Buy1"), trade.getItemStack("Buy2"), trade.getItemStack("Sell"));
	}
	
	/**
	 * Retrieves all trades in the shop
	 * 
	 * @return An ordered list of trades in the shop by slot number. Size will always be 9, trade will be null if it does not exist.
	 */
	public Trade[] getTrades() {
		Trade[] allTrades = new Trade[9];
		
		for(int trade = 0; trade < 9; trade++) {
			if(shopData.getKeys(false).contains(String.valueOf(trade))) {
				ConfigurationSection tradeData = shopData.getConfigurationSection(String.valueOf(trade));
				allTrades[trade] =new Trade(tradeData.getItemStack("Buy1"), tradeData.getItemStack("Buy2"), tradeData.getItemStack("Sell"));
			} else {
				//Make sure we fill every slot.
				Trade newTrade = null;
				allTrades[trade] = (newTrade);
			}
		}
		return allTrades;
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
