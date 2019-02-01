package net.parinacraft.victorum.claim;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.data.SQLManager;

public class Faction {
	private final int id;

	public Faction(int id) {
		this.id = id;
	}

	public String getName() {
		try (PreparedStatement stmt = SQLManager.prepare("SELECT Name FROM Faction WHERE ID = ?")) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return rs.getString(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<Claim> getClaims() {
		Set<Claim> claims = new HashSet<>();
		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT ChunkX, ChunkZ FROM Claims WHERE FactionID = ?")) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Chunk ch = Bukkit.getWorld(Opt.GAME_WORLD).getChunkAt(rs.getInt("ChunkX"), rs
						.getInt("ChunkZ"));
				claims.add(new Claim(ch, id));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return claims;
	}

	public int getID() {
		return id;
	}

	public static Faction get(UUID playerUUID) {
		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT FactionID FROM PlayerData WHERE UUID = ?")) {
			stmt.setString(1, playerUUID.toString());
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return new Faction(rs.getInt(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
