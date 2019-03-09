package net.parinacraft.victorum.data;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Faction;
import net.parinacraft.victorum.claim.FactionRole;

public class FactionHandler {
	private final Victorum pl;
	private final HashMap<Integer, Faction> factions;
	private final int defaultFactionID;

	public FactionHandler(Victorum pl, int defaultFactionID) {
		this.pl = pl;
		this.factions = pl.getSqlManager().loadFactions();
		this.defaultFactionID = defaultFactionID;

		// Asynchronously update faction value and leaderboard placement

	}

	public Faction getFaction(int id) {
		return Preconditions.checkNotNull(factions.get(id));
	}

	public void delete(int facID) {
		factions.remove(facID);
		pl.getClaimHandler().unclaimAll(facID);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeFaction(facID);
		});
	}

	public Faction create(Player creator, String name) {
		Faction created = pl.getSqlManager().createFaction(name, creator.getUniqueId());
		factions.put(created.getID(), created);
		pl.getPlayerDataHandler().getPlayerData(creator.getUniqueId()).setRole(FactionRole.FOUNDER);
		return created;
	}

	public boolean exists(String shortName) {
		for (Faction fac : factions.values()) {
			if (fac.getShortName().contentEquals(shortName))
				return true;
		}
		return false;
	}

	public boolean exists(int newID) {
		return factions.containsKey(newID);
	}

	public int getDefaultFactionID() {
		return defaultFactionID;
	}
}
