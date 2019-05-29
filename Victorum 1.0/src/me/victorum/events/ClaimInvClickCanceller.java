package me.victorum.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.victorum.victorum.Victorum;

public class ClaimInvClickCanceller implements Listener {
	// private final Victorum pl;

	public ClaimInvClickCanceller(Victorum pl) {
		// this.pl = pl;
	}

	@EventHandler
	public void onChestClick(InventoryClickEvent e) {
		if (e.getInventory().getName().equals("§e/claim map"))
			e.setCancelled(true);
	}
}
