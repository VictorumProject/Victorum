package net.parinacraft.victorum.test;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.parinacraft.victorum.Victorum;

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
		case GRASS:
			price = 50;
			break;
		case DIRT:
			price = 30;
			break;
		case DIAMOND_ORE:
			price = 500;
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
