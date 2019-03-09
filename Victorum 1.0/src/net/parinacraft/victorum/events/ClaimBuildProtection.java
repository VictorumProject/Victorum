package net.parinacraft.victorum.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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
		int playerFactionID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		if (claim.getFactionID() != playerFactionID) {
			p.sendMessage("§eTämän alueen omistaa " + claim.getFaction().getLongName() + ".");
			e.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBuild(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Chunk ch = e.getBlock().getChunk();
		Claim claim = pl.getClaimHandler().getClaim(ch.getX(), ch.getZ());
		int playerFactionID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		if (claim.getFactionID() != playerFactionID) {
			p.sendMessage("§eTämän alueen omistaa " + claim.getFaction().getLongName() + ".");
			e.setCancelled(true);
			return;
		}
	}
}
