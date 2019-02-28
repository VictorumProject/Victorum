package net.parinacraft.victorum.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import net.parinacraft.victorum.Victorum;

public class ConnectionListener implements Listener {
	private final Victorum pl;
	
	public ConnectionListener(Victorum pl) {
		this.pl = pl;
	}
	
	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		pl.getPlayerDataHandler().checkForExistingData(e.getUniqueId());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage("§8[§a+§8] §e" + e.getPlayer().getName());
	}
}
