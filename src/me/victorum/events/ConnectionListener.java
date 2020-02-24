package me.victorum.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.victorum.victorum.Victorum;

public class ConnectionListener implements Listener {
	private final Victorum pl;

	public ConnectionListener(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage("§8[§a+§8] §e" + e.getPlayer().getName());
		Player p = e.getPlayer();
		pl.getPlayerDataHandler().checkForExistingData(p);
		p.setDisplayName("§e" + p.getName());

		// Update last seen name
		pl.getSqlManager().setLastSeenName(p.getUniqueId(), p.getName());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		e.setQuitMessage("§8[§c-§8] §e" + e.getPlayer().getName());
	}
}
