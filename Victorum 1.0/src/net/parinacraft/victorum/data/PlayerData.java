package net.parinacraft.victorum.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.parinacraft.victorum.claim.Faction;

public class PlayerData {
	private final UUID id;

	public PlayerData(UUID id) {
		this.id = id;
	}

	public Faction getFaction() {
		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT PD.UUID FROM PlayerData PD WHERE PD.UUID = ? LIMIT 1")) {
			stmt.setString(1, id.toString());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return new Faction(rs.getInt(0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new NullPointerException();
	}

	public double getBalance() {

		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT Balance FROM PlayerData WHERE UUID = ?")) {
			stmt.setString(1, id.toString());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next())
					return rs.getDouble(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new NullPointerException();
	}
}
