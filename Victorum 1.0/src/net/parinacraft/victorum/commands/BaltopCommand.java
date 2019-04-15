package net.parinacraft.victorum.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Relation;
import net.parinacraft.victorum.data.PlayerData;

public class BaltopCommand implements CommandExecutor {
	private final Victorum pl;

	public BaltopCommand(Victorum pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§eTämän komennon voi suorittaa vain pelaaja.");
			return true;
		}
		PlayerData callerData = pl.getPlayerDataHandler().getPlayerData(((Player) sender).getUniqueId());
		if (args.length == 0) {
			List<PlayerData> top10 = pl.getEconomyHandler().getTopBalances(10);
			sender.sendMessage("");
			sender.sendMessage("§eRikkaimmat pelaajat:");
			for (PlayerData playerData : top10) {
				Relation rl = pl.getRelationHandler().getRelation(callerData.getFactionID(), playerData.getFactionID());
				ChatColor color = rl.getColor();
				sender.sendMessage(color + playerData.getLastSeenName() + "§e: " + playerData.getBalance());
			}
		} else if (args.length == 1) {

		}
		return true;
	}

}
