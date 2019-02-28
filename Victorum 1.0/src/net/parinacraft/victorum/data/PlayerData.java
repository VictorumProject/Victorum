package net.parinacraft.victorum.data;

import java.util.UUID;

public class PlayerData {
	private final UUID UUID;
	private final int factionID;

	public PlayerData(UUID id, int factionID) {
		this.UUID = id;
		this.factionID = factionID;
	}

	public int getFactionID() {
		return factionID;
	}
	
	
	public UUID getUUID() {
		return UUID;
	}
}
