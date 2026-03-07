package io.nightbeam.donutauction.model;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public record AuctionListing(
        UUID auctionId,
        ItemStack item,
        UUID seller,
        double price,
        long listingTime,
        long expirationTime,
        AuctionStatus status,
        UUID buyer,
        long soldTime,
        boolean sellerClaimed
) {

    public AuctionListing {
        Objects.requireNonNull(auctionId, "auctionId");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(seller, "seller");
        Objects.requireNonNull(status, "status");
        item = item.clone();
    }

    public boolean isExpired(long now) {
        return expirationTime <= now || status == AuctionStatus.EXPIRED;
    }

    public boolean isActive(long now) {
        return status == AuctionStatus.ACTIVE && expirationTime > now;
    }

    public AuctionListing withStatus(AuctionStatus newStatus) {
        return new AuctionListing(auctionId, item, seller, price, listingTime, expirationTime, newStatus, buyer, soldTime, sellerClaimed);
    }

    public AuctionListing asSold(UUID buyerId, long purchaseTime) {
        return new AuctionListing(auctionId, item, seller, price, listingTime, expirationTime, AuctionStatus.SOLD, buyerId, purchaseTime, false);
    }

    public AuctionListing asExpired() {
        return new AuctionListing(auctionId, item, seller, price, listingTime, expirationTime, AuctionStatus.EXPIRED, buyer, soldTime, sellerClaimed);
    }

    public AuctionListing markSellerClaimed() {
        return new AuctionListing(auctionId, item, seller, price, listingTime, expirationTime, status, buyer, soldTime, true);
    }
}