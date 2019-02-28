package net.parinacraft.victorum.data;

import java.util.HashMap;

import org.bukkit.Bukkit;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;

public class ClaimHandler {
	private final HashMap<Integer, Claim> claims;
	private final Victorum pl;

	public ClaimHandler(Victorum pl) {
		this.pl = pl;
		claims = pl.getSqlManager().loadClaims();
	}

	public Claim getClaim(int chunkX, int chunkZ) {
		// Return default faction with coords if not claimed
		int id = chunkX << 16 | (chunkZ & 0xFFFF);
		if (!claims.containsKey(id))
			return new Claim(pl, chunkX, chunkZ, 0);
		return claims.get(id);
	}

	public void create(int chunkX, int chunkZ, int facID) {
		Claim c = new Claim(pl, chunkX, chunkZ, facID);
		claims.put(c.getID(), c);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createClaim(c);
		});
	}

	public void unclaim(Claim claim) {
		claims.remove(claim.getID());
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeClaim(claim.getChunkX(), claim.getChunkZ());
		});
	}

}
