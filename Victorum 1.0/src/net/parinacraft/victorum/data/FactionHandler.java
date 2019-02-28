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
		factions = SQLManager.loadFactions();
	}

	public Faction getFaction(int id) {
		return Preconditions.checkNotNull(factions.get(id));
	}

	public void delete(int id) {
		// TOOD: Update db
		factions.remove(id);
	}

	public void create(Faction fac) {
		// TOOD: Update db
		factions.put(fac.getID(), fac);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			SQLManager.createFaction(factions.get(fac.getID()));
		});
	}
}
