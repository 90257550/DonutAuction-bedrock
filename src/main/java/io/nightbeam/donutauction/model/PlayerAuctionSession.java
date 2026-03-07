package io.nightbeam.donutauction.model;

import java.util.UUID;

public final class PlayerAuctionSession {

    private final UUID playerId;
    private AuctionBrowseRequest request;

    public PlayerAuctionSession(UUID playerId) {
        this.playerId = playerId;
        this.request = new AuctionBrowseRequest(1, AuctionSortMode.RECENTLY_LISTED, AuctionFilterCategory.ALL, "");
    }

    public UUID playerId() {
        return playerId;
    }

    public AuctionBrowseRequest request() {
        return request;
    }

    public void request(AuctionBrowseRequest request) {
        this.request = request;
    }
}