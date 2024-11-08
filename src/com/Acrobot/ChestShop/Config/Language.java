package com.Acrobot.ChestShop.Config;

/**
 * @author Acrobot
 */
public enum Language {
    prefix("&a[Shop] &f"),
    iteminfo("&aItem Information:&f"),

    INVALID_USAGE("Invalid usage: /shophistory [page]"),
    INVALID_USAGE_ADMIN("Invalid usage: /shophistory <username> [page]"),

    ACCESS_DENIED("You don't have permission to do that!"),

    PLAYER_NOT_FOUND("Could not find player %player%"),

    NOT_ENOUGH_MONEY("You don't have enough money!"),
    NOT_ENOUGH_MONEY_SHOP("Shop owner doesn't have enough money!"),

    NO_BUYING_HERE("You can't buy here!"),
    NO_SELLING_HERE("You can't sell here!"),

    NOT_ENOUGH_SPACE_IN_INVENTORY("You haven't got enough space in inventory!"),
    NOT_ENOUGH_SPACE_IN_CHEST("There isn't enough space in chest!"),
    NOT_ENOUGH_ITEMS_TO_SELL("You don't have enough items to sell!"),

    NOT_ENOUGH_STOCK("This shop is out of stock."),
    NOT_ENOUGH_STOCK_IN_YOUR_SHOP("Your %material shop is out of stock!"),

    YOU_BOUGHT_FROM_SHOP("You bought %amount %item from %owner for %price."),
    SOMEBODY_BOUGHT_FROM_YOUR_SHOP("%buyer bought %amount %item for %price from %you."),

    YOU_ACTIVATED_SIGN("You activated %owner's sign for %price."),
    SOMEBODY_ACTIVATED_YOUR_SIGN("%buyer activated %your sign for %price."),
    SIGN_NOT_CONNECTED("This shop is missing a redstone torch to activate."),

    YOU_SOLD_TO_SHOP("You sold %amount %item to %buyer for %price."),
    SOMEBODY_SOLD_TO_YOUR_SHOP("%seller sold %amount %item for %price to %you."),

    YOU_CANNOT_CREATE_SHOP("You can't create this type of shop!"),
    NO_CHEST_DETECTED("Couldn't find a chest!"),
    ANOTHER_SHOP_DETECTED("Another player's shop detected!"),
    CANNOT_ACCESS_THE_CHEST("You don't have permissions to access this chest!"),

    PROTECTED_SHOP("Successfully protected the shop with LWC!"),
    SHOP_CREATED("Shop successfully created!"),

    NO_PERMISSION("You don't have permissions to do that!"),
    INCORRECT_ITEM_ID("You have specified invalid item id!"),

    HISTORY_PAGE_X("Shop History: Page &c%page% &fof &c%pagecount%"),
    HISTORY_DISABLED("This feature is disabled, please enable LOG_TO_CSV"),
    NO_ENTRIES_IN_HISTORY("There are no entries in the transaction history!"),
    INVALID_PAGE_NUMBER("You entered an invalid page number!");

    private final String text;

    private Language(String def) {
        text = def;
    }

    public String toString() {
        return text;
    }
}
