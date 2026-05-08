package io.nightbeam.donutauction.floodgate;

import io.nightbeam.donutauction.AuctionHousePlugin;
import io.nightbeam.donutauction.gui.GuiManager;
import io.nightbeam.donutauction.hook.DonutCoreHook;
import io.nightbeam.donutauction.model.AuctionBrowseRequest;
import io.nightbeam.donutauction.model.AuctionFilterCategory;
import io.nightbeam.donutauction.model.AuctionListing;
import io.nightbeam.donutauction.model.AuctionPage;
import io.nightbeam.donutauction.model.AuctionStatus;
import io.nightbeam.donutauction.model.ListingPriceValidationResult;
import io.nightbeam.donutauction.model.PlayerAuctionSession;
import io.nightbeam.donutauction.service.ActionResult;
import io.nightbeam.donutauction.service.AuctionService;
import io.nightbeam.donutauction.util.TimeUtil;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;

public final class FloodgateFormHandler {

    private static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.plainText();
    private static final int FORM_PAGE_SIZE = 18;

    private final AuctionHousePlugin plugin;
    private final FloodgateHook floodgateHook;
    private final GuiManager guiManager;
    private final AuctionService auctionService;
    private final DonutCoreHook donutCoreHook;

    public FloodgateFormHandler(
            AuctionHousePlugin plugin,
            FloodgateHook floodgateHook,
            GuiManager guiManager,
            AuctionService auctionService,
            DonutCoreHook donutCoreHook
    ) {
        this.plugin = plugin;
        this.floodgateHook = floodgateHook;
        this.guiManager = guiManager;
        this.auctionService = auctionService;
        this.donutCoreHook = donutCoreHook;
    }

    public boolean isFloodgatePlayer(Player player) {
        return floodgateHook.isAvailable() && floodgateHook.isFloodgatePlayer(player);
    }

    // -----------------------------------------------------------------------
    // Browse Form (SimpleForm)
    // -----------------------------------------------------------------------

    public void openBrowseForm(Player player, PlayerAuctionSession session) {
        AuctionBrowseRequest request = session.request();
        AuctionPage page = auctionService.browse(request);
        List<AuctionListing> listings = page.listings();
        long now = System.currentTimeMillis();
        int n = Math.min(FORM_PAGE_SIZE, listings.size());

        StringBuilder content = new StringBuilder();
        content.append("Page ").append(page.currentPage()).append("/").append(page.totalPages());
        content.append("\nSort: ").append(request.sortMode().displayName());
        content.append(" | Filter: ").append(request.filterCategory().displayName());
        if (!request.searchTerm().isBlank()) {
            content.append("\nSearch: \"").append(request.searchTerm()).append("\"");
        }

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Auction House")
                .content(content.toString());

        for (int i = 0; i < n; i++) {
            AuctionListing listing = listings.get(i);
            builder.button(formatListingButton(listing, now));
        }

        // Navigation row
        builder.button(page.hasPreviousPage() ? "<< Previous Page" : "------");
        builder.button(page.hasNextPage() ? "Next Page >>" : "------");

        // Control row
        builder.button("Sort: " + request.sortMode().displayName());
        builder.button("Filter: " + request.filterCategory().displayName());
        builder.button(request.searchTerm().isBlank() ? "Search" : "Search: \"" + request.searchTerm() + "\"");
        builder.button("Your Items");
        builder.button("Refresh");
        builder.button("Sell an Item");

        builder.validResultHandler((form, response) ->
                handleBrowseResponse(player, session, response.clickedButtonId(), n, page));

        builder.closedResultHandler(form -> {});

        sendFormSafe(player, builder.build());
    }

    private void handleBrowseResponse(Player player, PlayerAuctionSession session,
                                       int buttonId, int listingCount, AuctionPage page) {
        if (buttonId < listingCount) {
            // Clicked a listing → purchase
            List<AuctionListing> listings = page.listings();
            if (buttonId >= listings.size()) {
                openBrowseForm(player, session);
                return;
            }
            UUID auctionId = listings.get(buttonId).auctionId();
            auctionService.purchaseAuction(player, auctionId).thenAccept(result ->
                    sendAndRefreshBrowse(player, session, result));
            return;
        }

        int navOffset = buttonId - listingCount;
        AuctionBrowseRequest request = session.request();
        switch (navOffset) {
            case 0: // Previous page
                if (page.hasPreviousPage()) {
                    session.request(request.withPage(request.page() - 1));
                }
                openBrowseForm(player, session);
                break;
            case 1: // Next page
                if (page.hasNextPage()) {
                    session.request(request.withPage(request.page() + 1));
                }
                openBrowseForm(player, session);
                break;
            case 2: // Sort
                session.request(request.withSortMode(request.sortMode().next()));
                openBrowseForm(player, session);
                break;
            case 3: // Filter
                openFilterForm(player, session);
                break;
            case 4: // Search
                openSearchForm(player, session);
                break;
            case 5: // Your Items
                openPlayerItemsForm(player);
                break;
            case 6: // Refresh
                openBrowseForm(player, session);
                break;
            case 7: // Sell
                openSellForm(player);
                break;
            default:
                openBrowseForm(player, session);
                break;
        }
    }

    // -----------------------------------------------------------------------
    // Player Items Form (SimpleForm)
    // -----------------------------------------------------------------------

    public void openPlayerItemsForm(Player player) {
        List<AuctionListing> listings = auctionService.getPlayerAuctions(player.getUniqueId());
        int page = guiManager.playerItemsPage(player.getUniqueId());
        int totalPages = Math.max(1, (int) Math.ceil(listings.size() / (double) FORM_PAGE_SIZE));
        page = Math.min(page, totalPages);
        guiManager.setPlayerItemsPage(player.getUniqueId(), page);

        int from = (page - 1) * FORM_PAGE_SIZE;
        int to = Math.min(listings.size(), from + FORM_PAGE_SIZE);
        List<AuctionListing> pageListings = listings.subList(from, to);
        long now = System.currentTimeMillis();

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Your Items")
                .content("Page " + page + "/" + totalPages);

        for (AuctionListing listing : pageListings) {
            builder.button(formatPlayerItemButton(listing, now));
        }

        builder.button(page > 1 ? "<< Previous" : "------");
        builder.button(page < totalPages ? "Next >>" : "------");
        builder.button("Back to Auction House");

        int finalPage = page;
        int finalTotalPages = totalPages;
        builder.validResultHandler((form, response) ->
                handlePlayerItemsResponse(player, response.clickedButtonId(),
                        pageListings, finalPage, finalTotalPages));

        builder.closedResultHandler(form -> {});

        sendFormSafe(player, builder.build());
    }

    private void handlePlayerItemsResponse(Player player, int buttonId,
                                            List<AuctionListing> pageListings,
                                            int page, int totalPages) {
        int n = pageListings.size();
        if (buttonId < n) {
            AuctionListing listing = pageListings.get(buttonId);
            if (listing.status() == AuctionStatus.ACTIVE) {
                auctionService.cancelAuction(player, listing.auctionId())
                        .thenAccept(result -> sendAndRefreshPlayerItems(player, result));
            } else {
                auctionService.collectSellerProceeds(player, listing.auctionId())
                        .thenAccept(result -> sendAndRefreshPlayerItems(player, result));
            }
            return;
        }

        int nav = buttonId - n;
        switch (nav) {
            case 0: // Previous
                if (page > 1) {
                    guiManager.setPlayerItemsPage(player.getUniqueId(), page - 1);
                }
                openPlayerItemsForm(player);
                break;
            case 1: // Next
                if (page < totalPages) {
                    guiManager.setPlayerItemsPage(player.getUniqueId(), page + 1);
                }
                openPlayerItemsForm(player);
                break;
            case 2: // Back
                guiManager.openAuctionHouse(player);
                break;
            default:
                openPlayerItemsForm(player);
                break;
        }
    }

    // -----------------------------------------------------------------------
    // Filter Form (SimpleForm)
    // -----------------------------------------------------------------------

    public void openFilterForm(Player player, PlayerAuctionSession session) {
        AuctionFilterCategory[] categories = {
                AuctionFilterCategory.BLOCKS,
                AuctionFilterCategory.TOOLS,
                AuctionFilterCategory.FOOD,
                AuctionFilterCategory.COMBAT,
                AuctionFilterCategory.POTIONS,
                AuctionFilterCategory.BOOKS,
                AuctionFilterCategory.INGREDIENTS,
                AuctionFilterCategory.UTILITIES
        };

        SimpleForm.Builder builder = SimpleForm.builder()
                .title("Filter Auctions")
                .content("Select a category");

        for (AuctionFilterCategory category : categories) {
            builder.button(category.displayName());
        }
        builder.button("Show All");
        builder.button("Back");

        builder.validResultHandler((form, response) -> {
            int id = response.clickedButtonId();
            if (id >= 0 && id < categories.length) {
                session.request(session.request().withFilter(categories[id]));
            } else if (id == categories.length) {
                session.request(session.request().withFilter(AuctionFilterCategory.ALL));
            }
            guiManager.openAuctionHouse(player, session);
        });

        builder.closedResultHandler(form ->
                guiManager.openAuctionHouse(player, session));

        sendFormSafe(player, builder.build());
    }

    // -----------------------------------------------------------------------
    // Search Form (CustomForm)
    // -----------------------------------------------------------------------

    public void openSearchForm(Player player, PlayerAuctionSession session) {
        String current = session.request().searchTerm();

        CustomForm.Builder builder = CustomForm.builder()
                .title("Search Auctions")
                .label("Enter an item name or keyword:")
                .input("query", "item name...", current);

        builder.validResultHandler((form, response) -> {
            String query = response.asInput(0);
            if (query == null) query = "";
            query = query.trim();
            session.request(session.request().withSearch(query));
            guiManager.openAuctionHouse(player, session);
        });

        builder.closedResultHandler(form ->
                guiManager.openAuctionHouse(player, session));

        sendFormSafe(player, builder.build());
    }

    // -----------------------------------------------------------------------
    // Sell Form (CustomForm)
    // -----------------------------------------------------------------------

    public void openSellForm(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        String itemLabel;
        if (itemInHand.getType() == Material.AIR) {
            itemLabel = "You must hold an item to sell!";
        } else {
            itemLabel = "Listing: " + itemName(itemInHand);
        }

        CustomForm.Builder builder = CustomForm.builder()
                .title("Sell Item")
                .label(itemLabel)
                .input("price", "Enter price...", "");

        builder.validResultHandler((form, response) -> {
            String priceStr = response.asInput(0);
            handleSellResponse(player, itemInHand, priceStr);
        });

        builder.closedResultHandler(form -> {});

        sendFormSafe(player, builder.build());
    }

    private void handleSellResponse(Player player, ItemStack heldItem, String priceStr) {
        if (heldItem.getType() == Material.AIR) {
            plugin.schedulerAdapter().runEntity(player, () -> {
                plugin.messages().send(player, "&cHold the item you want to list.");
                guiManager.openAuctionHouse(player);
            });
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            plugin.schedulerAdapter().runEntity(player, () -> {
                plugin.messages().send(player, "&cInvalid price.");
                openSellForm(player);
            });
            return;
        }

        double minPrice = plugin.getConfig().getDouble("auction.min-price", 10.0D);
        double maxPrice = plugin.getConfig().getDouble("auction.max-price", 1.0E9);
        ListingPriceValidationResult validation = ListingPriceValidationResult.validate(price, minPrice, maxPrice);

        if (validation == ListingPriceValidationResult.BELOW_MINIMUM) {
            String message = plugin.getConfig().getString("messages.price-below-min",
                    "&cMinimum auction price is &6%min_price%&c.");
            message = message.replace("%min_price%", auctionService.formatPrice(Math.max(0.0D, minPrice)));
            String finalMessage = message;
            plugin.schedulerAdapter().runEntity(player, () -> {
                plugin.messages().send(player, finalMessage);
                openSellForm(player);
            });
            return;
        }
        if (validation == ListingPriceValidationResult.INVALID_OR_ABOVE_MAX) {
            plugin.schedulerAdapter().runEntity(player, () -> {
                plugin.messages().send(player, "&cPrice must be greater than 0 and below the configured limit.");
                openSellForm(player);
            });
            return;
        }

        auctionService.createAuction(player, heldItem, price).thenAccept(result ->
                plugin.schedulerAdapter().runEntity(player, () -> {
                    plugin.messages().send(player, result.message());
                    if (result.success()) {
                        guiManager.openPlayerItems(player);
                    } else {
                        guiManager.openAuctionHouse(player);
                    }
                }));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void sendAndRefreshBrowse(Player player, PlayerAuctionSession session, ActionResult result) {
        plugin.schedulerAdapter().runEntity(player, () -> {
            plugin.messages().send(player, result.message());
            if (player.isOnline()) {
                openBrowseForm(player, session);
            }
        });
    }

    private void sendAndRefreshPlayerItems(Player player, ActionResult result) {
        plugin.schedulerAdapter().runEntity(player, () -> {
            plugin.messages().send(player, result.message());
            if (player.isOnline()) {
                openPlayerItemsForm(player);
            }
        });
    }

    private void sendFormSafe(Player player, org.geysermc.cumulus.form.Form form) {
        try {
            floodgateHook.sendForm(player, form);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send form to " + player.getName() + ": " + e.getMessage());
            plugin.schedulerAdapter().runEntity(player, () ->
                    plugin.messages().send(player, "&cCould not open the auction house UI. Please try again."));
        }
    }

    private String formatListingButton(AuctionListing listing, long now) {
        String sellerName = resolveSellerName(listing);
        String itemName = itemName(listing.item());
        String price = auctionService.formatPrice(listing.price());
        String remaining = TimeUtil.formatDuration(listing.expirationTime() - now);
        return itemName + "\n" + price + " | " + sellerName + " | " + remaining;
    }

    private String formatPlayerItemButton(AuctionListing listing, long now) {
        String itemName = itemName(listing.item());
        String price = auctionService.formatPrice(listing.price());
        String remaining = TimeUtil.formatDuration(listing.expirationTime() - now);
        String status = humanStatus(listing.status());
        String action = actionLine(listing);
        return itemName + "\n" + price + " | " + status + " | " + action;
    }

    private String resolveSellerName(AuctionListing listing) {
        OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.seller());
        if (seller.isOnline() && seller.getPlayer() != null) {
            return donutCoreHook.resolveDisplayName(seller.getPlayer());
        }
        return seller.getName() == null ? "Unknown" : seller.getName();
    }

    private String itemName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return PLAIN_TEXT.serialize(itemStack.getItemMeta().displayName());
        }
        return itemStack.getType().name().replace('_', ' ');
    }

    private String humanStatus(AuctionStatus status) {
        return switch (status) {
            case ACTIVE -> "Active";
            case SOLD -> "Sold";
            case EXPIRED -> "Expired";
            case CANCELLED -> "Cancelled";
        };
    }

    private String actionLine(AuctionListing listing) {
        return switch (listing.status()) {
            case ACTIVE -> "Tap to cancel";
            case SOLD -> listing.sellerClaimed() ? "Collected" : "Tap to collect payment";
            case EXPIRED, CANCELLED -> listing.sellerClaimed() ? "Collected" : "Tap to reclaim item";
        };
    }
}
