package me.victorum.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.victorum.claim.Relation;
import me.victorum.data.PlayerData;
import me.victorum.victorum.Victorum;

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
			int position = 1;
			for (PlayerData playerData : top10) {
				Relation rl = pl.getRelationHandler().getRelation(callerData.getFactionID(), playerData.getFactionID());
				ChatColor color = rl.getColor();
				sender.sendMessage("§4" + position++ + ". " + color + playerData.getLastSeenName() + "§e: §a$"
						+ playerData.getBalance());
			}
		} else if (args.length == 1) {
			// TODO: args
		}
		return true;
	}

}
