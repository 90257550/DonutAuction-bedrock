package io.nightbeam.donutauction.model;

import java.util.List;

public record AuctionPage(
        List<AuctionListing> listings,
        int currentPage,
        int totalPages,
        long totalResults
) {

    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }
}