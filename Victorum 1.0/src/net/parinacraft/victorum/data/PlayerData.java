package net.parinacraft.victorum.data;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Faction;

public class PlayerData {
	private final Victorum pl;

	private final UUID UUID;
	private int factionID;

	public PlayerData(Victorum pl, UUID id, int factionID) {
		this.pl = pl;
		this.UUID = id;
		this.factionID = factionID;
	}

	public int getFactionID() {
		return factionID;
	}

	public Faction getFaction() {
		return pl.getFactionHandler().getFaction(factionID);
	}

	public UUID getUUID() {
		return UUID;
	}

	public void setFactionID(int id) {
		this.factionID = id;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setFactionID(this.UUID, id);
		});
	}
}
