
package net.parinacraft.victorum.claim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Chunk;
import org.bukkit.Location;

import net.parinacraft.victorum.data.SQLManager;
import net.parinacraft.victorum.utils.FactionUtils;

public class Claim {
	public static double NEUTRAL_PRICE = 5000;

	private Chunk ch;
	private int factionID;

	public Claim(Chunk ch, int factionID) {
		this.ch = ch;
		this.factionID = factionID;
	}

	public String getChunkCode() {
		return Integer.toHexString(ch.getX() * 625 + ch.getZ());
	}

	public Chunk getChunk() {
		return ch;
	}

	public int getFactionID() {
		return factionID;
	}

	public static Claim get(Chunk ch) {
		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT FactionID FROM Claim WHERE Claim.X = ? AND Claim.Z = ? LIMIT 1")) {
			stmt.setInt(1, ch.getX());
			stmt.setInt(2, ch.getZ());
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				return new Claim(ch, rs.getInt(0));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return new Claim(ch, FactionUtils.getNeutral().getID());
	}

	public static Claim get(Location loc) {
		return get(loc.getChunk());
	}
}
