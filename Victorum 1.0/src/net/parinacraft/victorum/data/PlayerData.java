package net.parinacraft.victorum.data;

import java.util.UUID;

import org.bukkit.Bukkit;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Faction;
import net.parinacraft.victorum.claim.FactionRole;

public class PlayerData {
	private final Victorum pl;

	private final UUID UUID;
	private int factionID;
	private FactionRole role;

	public PlayerData(Victorum pl, UUID id, int factionID, FactionRole role) {
		this.pl = pl;
		this.UUID = id;
		this.factionID = factionID;
		this.role = role;
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

	public FactionRole getRole() {
		return role;
	}

	public void setFactionID(int id) {
		this.factionID = id;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setFactionID(this.UUID, id);
		});
	}

	public void setRole(FactionRole role) {
		this.role = role;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().setFactionRole(UUID, role);
		});
	}
}
