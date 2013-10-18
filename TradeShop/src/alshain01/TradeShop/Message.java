package alshain01.TradeShop;

import org.bukkit.ChatColor;

/**
* Class for retrieving localized messages.
*/
public enum Message {
	// Errors
	NoConsoleError, InvalidMaterialError, ShopExistsError, TradeExistsError, RemoveShopError, PermError,
	CommandTimeout, TradeRemoved,
	TradeShopRemoved, CreateShopMode, CreateRepoMode, CreateSalesmanMode, AddMode, ShopCreated, EnterAdminMode, ExitAdminMode,
	FlagsCreateDescription, FlagsCreateArea, FlagsCreateWorld, FlagsTradeDescription, FlagsTradeWorld, FlagsTradeArea;
	
	/**
	* @return A localized message for the enumeration.
	*/
	public final String get() {
		String message = TradeShop.messageReader.getConfig().getString("Message." + this.toString());
		if (message == null) {
			TradeShop.instance.getLogger().warning("ERROR: Invalid message.yml Message for " + this.toString());
			return "ERROR: Invalid message.yml Message. Please contact your server administrator.";
		}
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}