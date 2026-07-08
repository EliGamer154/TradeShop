# TradeShop

A Fabric server-side mod for Minecraft **26.1.2 - 26.2** that adds a `/shop` player-to-player item trading system. No client-side mod is required — players join with a vanilla client.

## How it works

Run `/shop` to open the trade menu:

- **Add Listing** — pick items from your inventory (up to 9 distinct item types) that you're willing to trade away. Items stay in your inventory; nothing is taken from you yet.
- **Browse Listings** — see other players' open listings and make an offer (also built from your own inventory).
- **My Listings** — see offers made on your listings and **accept** one. Accepting a specific offer automatically cancels the other pending offers on that listing.
- **My Offers** — once the seller accepts your offer, come back here and **confirm** to complete the trade.

The trade only actually happens when both sides have confirmed, and only if both players still have the required items in their inventory at that moment. If either side is missing an item, the trade fails, both players are notified, and the listing reopens so the seller can accept a different offer.

## Requirements

- Minecraft **26.1.2 - 26.2** (Fabric)
- Fabric Loader `>= 0.19.3`
- Fabric API (matching game version)
- Java **25** on the server

## Building

```
./gradlew build
```

The output jar is written to `build/libs/`.

## Known limitations (v1)

- A listing/offer can contain at most 9 distinct item types.
- Both players must be online at the moment the trade is confirmed — there's no offline queue.
- Item matching for "still has it" checks compares item type + full component/NBT data (so enchanted items must match exactly), but counts are summed across stacks.
- No in-game currency — this is purely item-for-item barter.
