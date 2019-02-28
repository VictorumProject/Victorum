package net.parinacraft.victorum.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.claim.Faction;

public class ClaimCommand implements CommandExecutor {
	private final Victorum pl;

	public ClaimCommand(Victorum pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§eDerp");
			return true;
		}
		Player p = (Player) sender;
		final int chunkX = p.getLocation().getChunk().getX(), chunkZ = p.getLocation().getChunk().getZ();
		final Faction playerFac = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFaction();
		if (args.length == 0) {
			sender.sendMessage("§eVoit käyttää /claim komentoa seuraaviin asioihin:");
			sender.sendMessage(
					"§b/claim buy <ID>§e, jossa ID on claimin sijainti, esim. F45. Tämä ostaa plotin ja valtaa alueen factillesi.");
			sender.sendMessage("§b/claim tp <ID>§e, teleporttaa claimillesi, jos se on factisi omistuksessa.");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("buy")) {
				if (pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID() == 0) {
					// Not in a faction
					sender.sendMessage("§eEt ole factionissa.");
					sender.sendMessage("§eVoit luoda factionin komennolla /f create <nimi>");
					return true;
				}
				int claimFaction = pl.getClaimHandler().getClaim(chunkX, chunkZ).getFactionID();
				if (claimFaction != 0) {
					// TODO: Overclaiming
					sender.sendMessage("§eTämä alue on jo claimattu.");
					return true;
				}

				pl.getClaimHandler().create(chunkX, chunkZ, playerFac.getID());
				p.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " claimattu.");
			} else if (args[0].equalsIgnoreCase("map")) {
				openMap(p, p.getLocation().getChunk().getX(), p.getLocation().getChunk().getZ());
			} else if (args[0].equalsIgnoreCase("create")) {
				sender.sendMessage("§c" + lbl + " <nimi>");
			} else {
				sender.sendMessage("§eKomentoa ei prosessoitu.");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("buy")) {

			} else if (args[0].equalsIgnoreCase("create")) {
				// Create a new faction
				String name = args[1].toUpperCase();
				if (name.length() > Opt.MAX_FACTION_NAME_SHORT) {
					sender.sendMessage("§eLiian pitkä nimi factille: " + args[1]);
					return true;
				}

				Faction current = pl.getFactionHandler()
						.getFaction(pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID());
				if (current.getID() != 0) {
					// Already in a fac
					sender.sendMessage("§eFactionisi nimi on " + current.getShortName());
					sender.sendMessage("§eVoit jättää factionisi komennolla /faction leave.");
					return true;
				}

				if (pl.getFactionHandler().exists(name)) {
					sender.sendMessage("§eTämä faction on jo olemassa.");
					return true;
				}
				Faction created = new Faction((int) (Math.random() * Integer.MAX_VALUE), name, name, 0, -1);
				pl.getFactionHandler().create(created);
				pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).setFactionID(created.getID());
				sender.sendMessage("§eFaction luotu! Uusi nimi: " + created.getShortName() + ".");

			} else if (args[0].equalsIgnoreCase("tp")) {

			}
		}
		return true;
	}

	private void openMap(Player p, int centerChunkX, int centerChunkZ) {
		Inventory inv = Bukkit.createInventory(null, 9 * 7, "§e/claim map");
		// 14 enemy, 4 neutral, 5 friend
		for (int i = -4; i < 5; i++) {
			for (int j = -3; j < 4; j++) {
				int inventoryIndex = 31 + i + j * 9;
				Claim c = pl.getClaimHandler().getClaim(centerChunkX + i, centerChunkZ + j);
				int id = c.getFactionID();
				byte relColor = 4;
				if (id == 0)
					inv.setItem(inventoryIndex, new ItemStack(Material.STAINED_CLAY, 1, (byte) 4));
				else {
					// TODO: Colour by relations
					Faction fac = c.getFaction();
					inv.setItem(inventoryIndex, new ItemStack(Material.STAINED_CLAY, fac.getBoardPosition(), relColor));
				}
			}
		}
		p.openInventory(inv);
	}
}