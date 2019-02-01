package net.parinacraft.victorum.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.data.PlayerData;

public class ClaimCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("ßeDerp");
			return true;
		}
		Player p = (Player) sender;
		if (args.length == 0) {
			sender.sendMessage("ßeVoit k‰ytt‰‰ /claim komentoa seuraaviin asioihin:");
			sender.sendMessage(
					"ßb/claim buy <ID>ße, jossa ID on claimin sijainti, esim. F45. T‰m‰ ostaa plotin ja valtaa alueen factillesi.");
			sender.sendMessage(
					"ßb/claim tp <ID>ße, teleporttaa claimillesi, jos se on factisi omistuksessa.");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("map")) {
				Inventory inv = Bukkit.createInventory(null, 9 * 7, "ße/claim map");
				for (int i = 0; i < 9 * 7; i++) {
					inv.setItem(i, new ItemStack(Material.STAINED_CLAY, 1, (short) 4));
				}
				p.openInventory(inv);
			} else
				sender.sendMessage("ßeidk mate... ei viel valmis");
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("buy")) {
			} else if (args[0].equalsIgnoreCase("create")) {
				// Create a new faction
				String name = args[1];
				if (name.length() >= Opt.MAX_FACTION_LENGTH) {
					sender.sendMessage("ßeLiian pitk‰ nimi factille: " + args[1]);
					return true;
				}

				PlayerData pd = new PlayerData(p.getUniqueId());
				if (pd.getFaction().getID() != 0) {

				}
			} else if (args[0].equalsIgnoreCase("tp")) {

			} else if (args[0].equalsIgnoreCase("map")) {
				Inventory inv = Bukkit.createInventory(null, 9 * 7, "ße/claim map");
				for (int i = 0; i < 9 * 7; i++) {
					inv.setItem(i, new ItemStack(Material.STAINED_CLAY, 1, (short) 4));
				}
				p.openInventory(inv);
			}
		}
		return true;
	}

}
