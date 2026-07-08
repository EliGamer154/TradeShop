# TradeShop

A Fabric server-side mod for Minecraft **26.1.2 - 26.2** that adds a `/shop` player-to-player item trading system. No client-side mod is required — players join with a vanilla client.

## How it works

Run `/shop` to open the trade menu:

- **Add Listing** — pick items from your inventory (up to 9 distinct item types) that you're willing to trade away. Confirming **removes those items from your inventory immediately** and holds them in escrow inside the listing. You get them back in full if you cancel the listing; they're only handed to the buyer once a trade actually completes.
- **Browse Listings** — see other players' open listings and make an offer (built from your own inventory, but offer items are *not* taken from you until the trade completes).
- **My Listings** — see offers made on your listings and **accept** one, or **cancel the listing** entirely (which returns the escrowed items to you and cancels any pending/accepted offers against it). Accepting a specific offer automatically cancels the other pending offers on that listing.
- **My Offers** — click an offer to open its detail screen. Once the seller accepts, **Confirm Trade** completes it; at any point before that you can **Withdraw Offer**.

The trade only actually happens when both sides have confirmed. Since the listing's items are already held in escrow, only the buyer's offered items are checked at that point — if the buyer no longer has them, the trade fails, both players are notified, and the listing reopens so the seller can accept a different offer.

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
