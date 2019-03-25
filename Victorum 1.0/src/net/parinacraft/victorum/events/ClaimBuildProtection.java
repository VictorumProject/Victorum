package net.parinacraft.victorum.events;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;

public class ClaimBuildProtection implements Listener {
	private final Victorum pl;

	public ClaimBuildProtection(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onBuild(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Chunk ch = e.getBlock().getChunk();
		Claim claim = pl.getClaimHandler().getClaim(ch.getX(), ch.getZ());

		if (claim.getFactionID() == Opt.DEFAULT_FACTION_ID)
			return;

		int playerFactionID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		if (claim.getFactionID() != playerFactionID) {
			p.sendMessage("§eTämän alueen omistaa " + claim.getFaction().getLongName() + ".");
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onPlace(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Chunk ch = e.getBlock().getChunk();
		Claim claim = pl.getClaimHandler().getClaim(ch.getX(), ch.getZ());

		if (claim.getFactionID() == Opt.DEFAULT_FACTION_ID)
			return;

		int playerFactionID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		if (claim.getFactionID() != playerFactionID) {
			p.sendMessage("§eTämän alueen omistaa " + claim.getFaction().getLongName() + ".");
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onWater(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		Block b = e.getClickedBlock();
		if (b == null)
			return;
		Chunk ch = b.getRelative(e.getBlockFace()).getChunk();
		Claim c = pl.getClaimHandler().getClaim(ch.getX(), ch.getZ());
		if (item == null)
			return;
		switch (e.getItem().getType()) {
		case LAVA_BUCKET:
		case WATER_BUCKET:
		case BUCKET:
			p.sendMessage("§eEt voi tehdä tätä factionin " + c.getFaction().getShortName() + " alueella.");
			e.setCancelled(true);
			return;
		default:
			return;
		}
	}
}
