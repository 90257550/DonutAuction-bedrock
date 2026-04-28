# DonutAuctionHouse 1.2.0 Patch Notes

## Minimum Auction Price

A new configurable minimum listing price has been added to reduce auction spam.

### New config options

```yml
auction:
  min-price: 10

messages:
  price-below-min: '&cMinimum auction price is &6%min_price%&c.'
```

### What changed

- Players can no longer list auctions below `auction.min-price`.
- Listings at exactly the minimum price are allowed.
- Existing `auction.max-price` behavior is unchanged.
- The below-minimum message is configurable with `%min_price%` placeholder support.
- Admins can now reload config changes with `/ah reload`.

### Upgrade notes

- Existing configs are safely updated with defaults for new keys on startup/reload.
- If your config does not include the new keys, they are added automatically.
