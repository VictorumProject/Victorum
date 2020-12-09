package me.victorum.temp;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.victorum.victorum.Victorum;

public class GrassForMoney implements Listener {
	private final Victorum pl;

	public GrassForMoney(Victorum pl) {
		this.pl = pl;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();

		int price;
		switch (b.getType()) {
		case DIAMOND_ORE:
			price = 5000;
			break;
		default:
			return;
		}
		e.setCancelled(true);
		b.setType(Material.AIR);
		pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).addBalance(price);
		p.sendMessage("§a$" + price);
	}
}
