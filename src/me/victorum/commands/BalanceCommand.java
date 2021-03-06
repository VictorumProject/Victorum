package me.victorum.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.victorum.data.PlayerData;
import me.victorum.victorum.Victorum;

public class BalanceCommand implements CommandExecutor {
	private final Victorum pl;

	public BalanceCommand(Victorum victorum) {
		this.pl = victorum;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("┬žeVain pelaaja voi katsoa oman rahatilanteensa.");
				return true;
			}
			PlayerData pd = pl.getPlayerDataHandler().getPlayerData(((Player) sender).getUniqueId());
			long balance = pd.getBalance();
			sender.sendMessage("┬žeRahatilanteesi: $" + balance);
		} else {
			String name = args[0];
			PlayerData data = pl.getPlayerDataHandler().getPlayerData(name);
			sender.sendMessage("┬žePelaajan " + data.getLastSeenName() + " rahatilanne: $" + data.getBalance());
		}
		return true;
	}

}
