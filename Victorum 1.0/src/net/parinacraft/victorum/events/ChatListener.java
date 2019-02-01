package net.parinacraft.victorum.events;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.parinacraft.victorum.data.SQLManager;

public class ChatListener implements Listener {
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		String factName = null;
		int id = 0;
		try (PreparedStatement stmt = SQLManager.prepare(
				"SELECT Faction.Short, Faction.ID FROM Faction, PlayerData WHERE PlayerData.UUID = ? AND PlayerData.FactionID = Faction.ID")) {
			stmt.setString(1, p.getUniqueId().toString());
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					factName = rs.getString("Faction.Name");
					id = rs.getInt("Faction.ID");
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		if (id == 0)
			e.setFormat("§8[§a§l" + factName + "§8] §e" + p.getDisplayName() + " §8> §b%2$s");
		else
			e.setFormat("§8[§e§l" + factName + "§8] §e" + p.getDisplayName() + " §8> §b%2$s");
	}
}
