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

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;

public class ChestOpenListener implements Listener {
	private final Victorum pl;

	public ChestOpenListener(Victorum pl) {
		this.pl = pl;
	}

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
		Claim c = pl.getClaimHandler().getClaim(b.getChunk().getX(), b.getChunk().getZ());
		if (c.getFactionID() == 0
				|| c.getFactionID() != pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID()) {
			e.setCancelled(true);
			p.sendMessage("§eVoit avata chestit vain omalla alueellasi.");
		}
	}

	/**
	 * Disable enderchests.
	 */
	@EventHandler
	public void onEnderChestOpen(InventoryOpenEvent e) {
		if (e.getInventory().getType().equals(InventoryType.ENDER_CHEST))
			e.setCancelled(true);
	}
}
