package me.victorum.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.victorum.claim.Claim;
import me.victorum.victorum.Opt;
import me.victorum.victorum.Victorum;

public class ClaimHandler {
	private final HashMap<Integer, Claim> claims;
	private final Victorum pl;

	public ClaimHandler(Victorum pl) {
		this.pl = pl;
		claims = pl.getSqlManager().loadClaims();

		// Check rent expiration dates every minute
		Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, () -> {
			Set<Claim> expired = new HashSet<>();
			for (Claim c : claims.values()) {
				if (System.currentTimeMillis() > c.getExpirationDate()) {
					// Completely expired
					expired.add(c);
					for (Player p : Bukkit.getOnlinePlayers()) {
						PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
						if (pd.getFactionID() == c.getFactionID()) {
							p.sendMessage("§eAlue kohteessa " + c.getChunkX() + ":" + c.getChunkZ()
									+ " on vanhentunut. Kuka vain voi nyt vuokrata sen.");
						}
					}
				}
			}
			for (Claim claim : expired) {
				unclaim(claim);
			}
		}, 0, 20 * 60);
	}

	public Claim getClaim(int chunkX, int chunkZ) {
		// Return default faction with coords if not claimed
		int id = chunkX << 16 | (chunkZ & 0xFFFF);
		return claims.getOrDefault(id, new Claim(pl, chunkX, chunkZ, Opt.DEFAULT_FACTION_ID, -1));
	}

	public Claim create(int chunkX, int chunkZ, int facID) {
		Claim c = new Claim(pl, chunkX, chunkZ, facID, System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7);
		claims.put(c.getID(), c);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createClaim(c);
		});
		return c;
	}

	public void unclaim(int chunkX, int chunkZ) {
		unclaim(getClaim(chunkX, chunkZ));
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
		Set<Claim> claims = new HashSet<>();
		for (Claim claim : this.claims.values()) {
			if (claim.getFactionID() == facID)
				claims.add(claim);
		}
		return claims;
	}

}
