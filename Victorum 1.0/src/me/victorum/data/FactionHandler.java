package me.victorum.data;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import me.victorum.claim.Faction;
import me.victorum.claim.FactionRole;
import me.victorum.victorum.Victorum;

public class FactionHandler {
	private final Victorum pl;
	private final HashMap<Integer, Faction> factions;

	public FactionHandler(Victorum pl) {
		this.pl = pl;
		this.factions = pl.getSqlManager().loadFactions();
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
		pl.getPlayerDataHandler().getPlayerData(creator.getUniqueId()).setRole(FactionRole.LEADER);
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

	public Collection<Faction> getAllFactions() {
		return factions.values();
	}

	/**
	 * @return null if faction doesn't exist
	 */
	public Faction getFactionWithName(String name) {
		for (Faction fac : factions.values()) {
			if (fac.getShortName().contentEquals(name))
				return fac;
		}
		return null;
	}
}
