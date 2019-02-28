package net.parinacraft.victorum.claim;

import com.google.common.base.Preconditions;

public class Faction {
	private final int id;
	private String shortName, longName;

	public Faction(int id, String shortName, String longName) {
		this.shortName = Preconditions.checkNotNull(shortName);
		this.longName = Preconditions.checkNotNull(longName);
		this.id = id;
	}

	public int getID() {
		return id;
	}

	public String getLongName() {
		return longName;
	}

	public String getShortName() {
		return shortName;
	}
}
