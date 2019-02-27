package net.parinacraft.victorum.events;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.parinacraft.victorum.data.SQLManager;

public class ConnectionListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage("§e" + p.getName() + " §atuli servulle");

		// Make sure there is data
		try (PreparedStatement stmt = SQLManager.prepare(
				"INSERT INTO PlayerData VALUES (?, 0, 0) ON DUPLICATE KEY UPDATE UUID = UUID")) {
			stmt.setString(1, p.getUniqueId().toString());
			stmt.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
