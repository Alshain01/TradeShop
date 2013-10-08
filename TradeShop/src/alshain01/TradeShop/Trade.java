package alshain01.TradeShop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

/**
 * Class for storing a trade transaction.
 */
public class Trade {
	private ItemStack[] tradeItems = new ItemStack[3];

	/**
	 * Creates a new trade to be finalized by chest drop.
	 * 
	 * @param buyItem1 The required item for trade.
	 * @param buyItem2 The optional item for trade.
	 */
	public Trade(ItemStack buyItem1, ItemStack buyItem2) {
		this.tradeItems[0] = buyItem1;
		this.tradeItems[1] = buyItem2;
	}
	
	/**
	 * Creates a new trade from a serialized map list
	 * 
	 * @param items Map list to deserialize
	 */
	@SuppressWarnings("unchecked")
	//TODO Remove suppress warnings.  
	// Should be ok for now, we are writing the database so we know what is coming back.
	protected Trade(List<Map<?, ?>> items) {
		int i = 0;
		for(Map<?, ?> m : items) {
			tradeItems[i] = ItemStack.deserialize((Map<String, Object>) m);
			i++;
		}
	}
	
	/**
	 * Creates an existing trade.
	 * 
	 * @param buyItem1 The required item for trade.
	 * @param buyItem2 The optional item for trade.
	 */
	public Trade(ItemStack buyItem1, ItemStack buyItem2, ItemStack sellItem) {
		this.tradeItems[0] = buyItem1;
		this.tradeItems[1] = buyItem2;
		this.tradeItems[2] = sellItem;
	}
	
	/**
	 * The item to be sold.
	 * 
	 * @return The item to be sold.
	 */
	public ItemStack getSellItem() {
		return this.tradeItems[2];
	}
	
	/*
	 * Sets the item to be sold
	 */
	protected void setSellItem(ItemStack sellItem) {
		this.tradeItems[2] = sellItem;
	}

	/**
	 * Returns the items to be bought, may be null.
	 * @param index The index of the item (0 or 1).
	 * @return The item to be bought in this trade.
	 */
	public ItemStack getBuyItem(int index) {
		if(index < 0 || index > 1) { return null; }
		return this.tradeItems[index];
	}
	
	/**
	 * Returns true if the configured trade can be a valid transaction
	 * 
	 * @return True if the trade is valid
	 */
	public boolean isValid() {
		if (tradeItems[0] != null && tradeItems[2] != null) { return true; }
		return false;
	}
	
	/**
	 * Returns a serialized map list of item stacks in order by their slot ID in the trade window.
	 *
	 * @return The list of items stacks involved with the trade
	 */
	public List<Map<String, Object>> getItems() {
		List<Map<String, Object>> tradeList = new ArrayList<Map<String, Object>>();
		
		for(ItemStack i : tradeItems) {
			tradeList.add(i.serialize());
		}
		
		return tradeList;
	}
}