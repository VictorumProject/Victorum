package net.parinacraft.victorum.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClaimInvClickCanceller implements Listener {
	@EventHandler
	public void onChestClick(InventoryClickEvent e) {
		if (e.getInventory().getName().equals("§e/claim map")) {
			e.setCancelled(true);
		}
	}
}
