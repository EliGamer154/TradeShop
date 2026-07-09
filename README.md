# TradeShop

A Fabric server-side mod for Minecraft **26.1.2 - 26.2** that adds a `/shop` player-to-player item trading system. No client-side mod is required — players join with a vanilla client.

## How it works

Run `/shop` to open the trade menu:

- **Add Listing** — pick exactly one item type from your inventory that you're willing to trade away; click it again to stack more of it, up to that item's normal max stack size (so a sword stays at 1, but dirt can go up to 64). You can have up to 15 active listings at a time by default (configurable) - each one is still just a single item type. Confirming **removes it from your inventory immediately** and holds it in escrow inside the listing. You get it back in full if you cancel the listing; it's only handed to the buyer once a trade actually completes.
- **Browse Listings** — see other players' open listings and make an offer (up to 9 distinct item types from your own inventory, and you can click the same item again to offer more of it; offer items are *not* taken from you until the trade completes).
- **My Listings** — see offers made on your listings and **accept** one, or **cancel the listing** entirely (which returns the escrowed items to you and cancels any pending/accepted offers against it). Accepting a specific offer automatically cancels the other pending offers on that listing.
- **My Offers** — click an offer to open its detail screen. Once the seller accepts, **Confirm Trade** completes it; at any point before that you can **Withdraw Offer**.

The trade only actually happens when both sides have confirmed. Since the listing's items are already held in escrow, only the buyer's offered items are checked at that point — if the buyer no longer has them, the trade fails, both players are notified, and the listing reopens so the seller can accept a different offer.

Anywhere a listed or offered item is shown as an icon, **right-click it to peek inside** if it's a shulker box (or anything else carrying container contents).

**Server operators** (OP) see an extra **Admin: Manage Listings** button in the main menu, listing every open listing from every player with a force-delete action. Deleting one returns the escrowed item to its owner if they're online; if not, the item is forfeited.

## Configuration

Settings live in `config/tradeshop.json`, created automatically on first run:

```json
{
  "maxActiveListingsPerPlayer": 15,
  "maxOfferItemTypes": 9
}
```

Edit the file and run `/shop reload` (OP only) to apply changes without restarting the server.

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

- A listing is 1 item type, stackable up to that item's normal max stack size; an offer can contain up to 9 distinct item types (configurable), each stackable to any quantity.
- Both players must be online at the moment the trade is confirmed — there's no offline queue.
- Item matching for "still has it" checks compares item type + full component/NBT data (so enchanted items must match exactly), but counts are summed across stacks.
- No in-game currency — this is purely item-for-item barter.
