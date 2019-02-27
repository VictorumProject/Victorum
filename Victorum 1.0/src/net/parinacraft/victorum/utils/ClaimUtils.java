package net.parinacraft.victorum.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.data.SQLManager;

public class ClaimUtils {

	public static Chunk getChunkByID(String id) {
		id = id.toUpperCase();
		Tuple<Integer, Integer> t = ChunkCode.getCoordinates(id);
		return Bukkit.getWorld(Opt.GAME_WORLD).getChunkAt(t.a(), t.b());
	}

	public static Set<Claim> getClaimsInRadius(int x, int z, int radX, int radZ) {
		Set<Claim> claims = new HashSet<>();

		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT FactionID FROM Claim WHERE Claim.X < ? AND Claim.X > ? AND Claim.Z < ? AND Claim.Z > ?")) {
			stmt.setInt(1, x + radX);
			stmt.setInt(2, x - radX);
			stmt.setInt(3, z + radZ);
			stmt.setInt(4, z - radZ);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Chunk ch = Bukkit.getWorld(Opt.GAME_WORLD).getChunkAt(rs.getInt("Claim.X"), rs
							.getInt("Claim.Z"));
					claims.add(new Claim(ch, rs.getInt("FactionID")));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return claims;
	}
}
