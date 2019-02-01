package net.parinacraft.victorum.events;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.claim.Faction;

public class ChestOpenListener implements Listener {
	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		if (e.getPlayer().isSneaking())
			return;
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;
		if (b == null)
			return;
		if (!b.getType().equals(Material.CHEST) && !b.getType().equals(Material.TRAPPED_CHEST))
			return;

		Player p = e.getPlayer();

		// If not own area
		Claim c = Claim.get(b.getLocation());
		if (c.getFactionID() == 0 || c.getFactionID() != Faction.get(p.getUniqueId()).getID()) {
			e.setCancelled(true);
			p.sendMessage("§eVoit avata chestit vain omalla alueellasi.");
		}
	}

	@EventHandler
	public void onEnderChestOpen(InventoryOpenEvent e) {
		if (e.getInventory().getType().equals(InventoryType.ENDER_CHEST))
			e.setCancelled(true);
	}
}
