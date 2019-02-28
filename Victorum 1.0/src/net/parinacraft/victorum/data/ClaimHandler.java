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
		claims = SQLManager.loadClaims();
	}

	public Claim getClaim(int chunkX, int chunkZ) {
		return claims.get(toID(chunkX, chunkZ));
	}

	public void create(int chunkX, int chunkZ, Claim claim) {
		claims.put(toID(chunkX, chunkZ), claim);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			SQLManager.createClaim(claim);
		});
	}

	public void unclaim(int chunkX, int chunkZ) {
		claims.remove(toID(chunkX, chunkZ));
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			SQLManager.removeClaim(chunkX, chunkZ);
		});
	}

	public static int toID(int chunkX, int chunkZ) {
		return chunkX << 16 | (chunkZ & 0xFFFF);
	}
}
