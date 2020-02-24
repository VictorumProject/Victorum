package me.victorum.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

import me.victorum.claim.Faction;
import me.victorum.claim.FactionRole;
import me.victorum.victorum.Opt;
import me.victorum.victorum.Victorum;

public class FactionHandler {
	private final Victorum pl;
	private final HashMap<Integer, Faction> factions;

	public FactionHandler(Victorum pl) {
		this.pl = pl;
		this.factions = pl.getSqlManager().loadFactions();

		// Remove factions with no members
		List<Faction> removing = new ArrayList<>();
		for (Faction fac : factions.values()) {
			if (fac.getPlayers().size() == 0 && !fac.isDefaultFaction())
				removing.add(fac);
		}
		removing.forEach((Faction fac) -> {
			this.delete(fac.getID());
			System.out.println("Removed faction " + fac.getLongName() + " because they had no members.");
		});

	}

	public Faction getFaction(int id) {
		return Preconditions.checkNotNull(factions.get(id));
	}

	public void delete(int facID) {

		Faction fac = factions.get(facID);

		for (UUID uuid : fac.getPlayers()) {
			pl.getPlayerDataHandler().getPlayerData(uuid).setFactionID(Opt.DEFAULT_FACTION_ID);
		}
		pl.getClaimHandler().unclaimAll(facID);
		factions.remove(facID);

		// TODO: Joku kusi tässä

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
			if (fac.getShortName().equalsIgnoreCase(shortName))
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
