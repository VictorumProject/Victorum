
package net.parinacraft.victorum.claim;

import net.parinacraft.victorum.Victorum;

public class Claim {
	private final int chunkX, chunkZ;
	private final int factionID;
	
	private final Victorum pl;

	public Claim(Victorum pl, int chunkX, int chunkZ, int factionID) {
		this.pl = pl;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.factionID = factionID;
	}

	public int getFactionID() {
		return factionID;
	}

	public int getChunkX() {
		return chunkX;
	}

	public int getChunkZ() {
		return chunkZ;
	}

	public int getID() {
		return chunkX << 16 | (chunkZ & 0xFFFF);
	}

	public Faction getFaction() {
		return pl.getFactionHandler().getFaction(factionID);
	}
}
