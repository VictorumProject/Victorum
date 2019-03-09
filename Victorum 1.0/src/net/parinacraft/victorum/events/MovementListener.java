package net.parinacraft.victorum.events;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;

public class MovementListener implements Listener {

	private final Victorum pl;

	public MovementListener(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onDisplayNewTerratory(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		// Faction fac =
		// pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFaction();
		Chunk toCh = e.getTo().getChunk();
		Chunk fromCh = e.getFrom().getChunk();
		Claim toClaim = pl.getClaimHandler().getClaim(toCh.getX(), toCh.getZ());
		Claim fromClaim = pl.getClaimHandler().getClaim(fromCh.getX(), fromCh.getZ());

		if (fromClaim.getFactionID() == toClaim.getFactionID())
			return;

		p.sendMessage("§e~ " + toClaim.getFaction().getLongName());
	}
}
