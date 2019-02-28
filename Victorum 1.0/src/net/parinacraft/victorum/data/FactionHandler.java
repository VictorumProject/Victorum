package net.parinacraft.victorum.data;

import java.util.HashMap;

import org.bukkit.Bukkit;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Faction;

public class FactionHandler {
	private final Victorum pl;
	private final HashMap<Integer, Faction> factions;

	public FactionHandler(Victorum pl) {
		this.pl = pl;
		factions = pl.getSqlManager().loadFactions();
	}

	public Faction getFaction(int id) {
		return Preconditions.checkNotNull(factions.get(id));
	}

	public void delete(int id) {
		factions.remove(id);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeFaction(id);
		});
	}

	public void create(Faction fac) {
		factions.put(fac.getID(), fac);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createFaction(factions.get(fac.getID()));
		});
	}

	public boolean exists(String shortName) {
		for (Faction fac : factions.values()) {
			if (fac.getShortName().contentEquals(shortName))
				return true;
		}
		return false;
	}
}
