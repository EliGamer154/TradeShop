package com.tradeshop.data;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class Listing {
	public final UUID id;
	public final UUID ownerId;
	public final String ownerName;
	public final List<ItemStack> items;
	public ListingStatus status;

	public Listing(UUID id, UUID ownerId, String ownerName, List<ItemStack> items, ListingStatus status) {
		this.id = id;
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.items = items;
		this.status = status;
	}
}
