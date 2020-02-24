package me.victorum.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.victorum.claim.Faction;
import me.victorum.claim.FactionRole;
import me.victorum.data.PlayerData;
import me.victorum.victorum.Victorum;

public class ChatListener implements Listener {
	private final Victorum pl;

	public ChatListener(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		Faction fac = pd.getFaction();
		String roleStr = (fac.getFounder().equals(p.getUniqueId()) ? "**"
				: pd.getRole() == FactionRole.LEADER ? "*" : "+");
		e.setFormat("§8[§e§l" + roleStr + fac.getShortName() + "§8] §e" + p.getDisplayName() + " §8> §b%2$s");
	}
}