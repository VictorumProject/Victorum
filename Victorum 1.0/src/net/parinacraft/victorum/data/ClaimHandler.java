package net.parinacraft.victorum.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

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
		return claims.getOrDefault(id, new Claim(pl, chunkX, chunkZ, 0));
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

	public void unclaimAll(int facID) {
		Set<Integer> claimIDs = new HashSet<>();
		for (Entry<Integer, Claim> e : claims.entrySet()) {
			if (e.getValue().getFactionID() == facID)
				claimIDs.add(e.getKey());
		}
		for (Integer id : claimIDs) {
			claims.remove(id);
		}
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().unclaimAll(facID);
		});
	}

	public Set<Claim> getAllClaims(int facID) {
		Set<Claim> claims = new HashSet<Claim>();
		for (Claim claim : this.claims.values()) {
			if (claim.getFactionID() == facID)
				claims.add(claim);
		}
		return claims;
	}

}
