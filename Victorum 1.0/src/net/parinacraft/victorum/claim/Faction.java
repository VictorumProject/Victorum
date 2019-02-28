package net.parinacraft.victorum.claim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.data.PlayerData;

public class Faction {
	private final int factionID;
	private String shortName, longName;

	public Faction(int factionID, String shortName, String longName) {
		this.shortName = Preconditions.checkNotNull(shortName);
		this.longName = Preconditions.checkNotNull(longName);
		this.factionID = factionID;
	}

	public int getID() {
		return factionID;
	}

	public String getLongName() {
		return longName;
	}

	public String getShortName() {
		return shortName;
	}

	public Set<UUID> getPlayers() {
		Set<UUID> players = new HashSet<UUID>();
		Set<PlayerData> data = Victorum.get().getPlayerDataHandler().getAllData();
		for (PlayerData pd : data) {
			if (pd.getFactionID() == this.factionID)
				players.add(pd.getUUID());
		}
		return players;
	}
}
