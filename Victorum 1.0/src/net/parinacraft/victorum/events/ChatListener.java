package net.parinacraft.victorum.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Faction;

public class ChatListener implements Listener {
	private final Victorum pl;

	public ChatListener(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		int facID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		Faction fac = pl.getFactionHandler().getFaction(facID);
		e.setFormat("§8[§e§l" + fac.getShortName() + "§8] §e" + p.getDisplayName() + " §8> §b%2$s");
	}
}