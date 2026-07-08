package com.tradeshop.data;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class Offer {
	public final UUID id;
	public final UUID listingId;
	public final UUID offererId;
	public final String offererName;
	public final List<ItemStack> items;
	public OfferStatus status;

	public Offer(UUID id, UUID listingId, UUID offererId, String offererName, List<ItemStack> items, OfferStatus status) {
		this.id = id;
		this.listingId = listingId;
		this.offererId = offererId;
		this.offererName = offererName;
		this.items = items;
		this.status = status;
	}
}
