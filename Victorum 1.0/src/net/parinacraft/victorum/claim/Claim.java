
package net.parinacraft.victorum.claim;

public class Claim {
	private final int chunkX, chunkZ;
	private final int factionID;

	public Claim(int chunkX, int chunkZ, int factionID) {
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

}
