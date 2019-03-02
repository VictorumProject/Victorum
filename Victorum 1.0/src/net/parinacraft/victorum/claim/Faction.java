package net.parinacraft.victorum.claim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.data.PlayerData;

public class Faction {
	private final int factionID;
	private int boardPosition;
	private long value;
	private String shortName, longName;

	private final Victorum pl;

	public Faction(Victorum pl, int factionID, String shortName, String longName, long value, int boardPosition) {
		this.pl = pl;
		this.shortName = Preconditions.checkNotNull(shortName);
		this.longName = Preconditions.checkNotNull(longName);
		this.factionID = factionID;
		this.value = value;
		this.boardPosition = boardPosition;
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
		Set<PlayerData> data = pl.getPlayerDataHandler().getAllData();
		for (PlayerData pd : data) {
			if (pd.getFactionID() == this.factionID)
				players.add(pd.getUUID());
		}
		return players;
	}

	public long getValue() {
		return value;
	}

	public int getBoardPosition() {
		return boardPosition;
	}

	public void setValue(long value) {
		this.value = value;
	}
}
