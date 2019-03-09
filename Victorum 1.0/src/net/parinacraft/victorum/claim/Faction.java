package net.parinacraft.victorum.claim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.data.PlayerData;

public class Faction {
	private final int factionID;
	private long value;
	private String shortName, longName;
	private final UUID founder;
	private long leaderThreshold;

	private final Victorum pl;

	public Faction(Victorum pl, int factionID, String shortName, String longName, UUID founder, long value,
			long leaderThreshold) {
		this.pl = pl;
		this.factionID = factionID;
		this.shortName = Preconditions.checkNotNull(shortName);
		this.longName = Preconditions.checkNotNull(longName);
		this.founder = Preconditions.checkNotNull(founder);
		this.value = value;
		this.leaderThreshold = leaderThreshold;
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

	public UUID getFounder() {
		return founder;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public Set<UUID> getPlayers() {
		Set<UUID> players = new HashSet<>();
		Set<PlayerData> data = pl.getPlayerDataHandler().getAllData();
		for (PlayerData pd : data) {
			if (pd.getFactionID() == this.factionID)
				players.add(pd.getUUID());
		}
		return players;
	}

	public long getLeaderThreshold() {
		return leaderThreshold;
	}

}
