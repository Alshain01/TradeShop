package alshain01.TradeShop;

import org.bukkit.inventory.ItemStack;

public class Trade {
	private ItemStack[] tradeItems = new ItemStack[3];
	
	public Trade(ItemStack buyItem1, ItemStack buyItem2) {
		this.tradeItems[0] = buyItem1;
		this.tradeItems[1] = buyItem2;
	}
	
	public ItemStack getSellItem() {
		return this.tradeItems[2];
	}
	
	protected void setSellItem(ItemStack sellItem) {
		this.tradeItems[2] = sellItem;
	}
	
	public ItemStack getBuyItem(int index) {
		if(index < 0 || index > 1) { return null; }
		return this.tradeItems[index];
	}
}
