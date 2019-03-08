package net.parinacraft.victorum.commands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
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
import net.parinacraft.victorum.data.PlayerData;

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
		final int chunkX = p.getLocation().getChunk().getX(), chunkZ = p.getLocation().getChunk()
				.getZ();
		final Faction playerFac = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId())
				.getFaction();
		if (args.length == 0) {
			sender.sendMessage("§eVictorum v" + pl.getDescription().getVersion());
			sender.sendMessage("§e    /" + lbl + " create");
			sender.sendMessage("§e    /" + lbl + " claim|buy");
			sender.sendMessage("§e    /" + lbl + " invite");
			sender.sendMessage("§e    /" + lbl + " map");
			sender.sendMessage("§e    /" + lbl + " leave");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				if (pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID() == 0) {
					// Not in a faction
					p.sendMessage("§eEt ole factionissa.");
					p.sendMessage("§eVoit luoda factionin komennolla /" + lbl + " create <nimi>");
					return true;
				}
				claim(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("map")) {
				openMap(p, p.getLocation().getChunk().getX(), p.getLocation().getChunk().getZ());
			} else if (args[0].equalsIgnoreCase("create")) {
				sender.sendMessage("§c/" + lbl + " create <nimi>");
			} else if (args[0].equalsIgnoreCase("leave")) {
				leaveFaction(p, playerFac);
			} else {
				sender.sendMessage("§eKomentoa ei prosessoitu.");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				processBuyClaim(p, playerFac, p.getLocation().getChunk(), args[1]);
			} else if (args[0].equalsIgnoreCase("create")) {
				processFactionCreate(p, args[1]);
			}
		}
		return true;
	}

	private void processBuyClaim(Player p, Faction fac, Chunk ch, String arg1) {
		if (pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID() == 0) {
			// Not in a faction
			p.sendMessage("§eEt ole factionissa.");
			p.sendMessage("§eVoit luoda factionin komennolla /f create <nimi>");
			return;
		}
		try {
			int rad = Integer.parseInt(arg1);
			if (rad < 1) {
				p.sendMessage("§eLiian pieni säde: " + rad);
				return;
			}
			if (rad > 3) {
				p.sendMessage("§eLiian iso säde: " + rad);
				return;
			}

			for (int i = -rad + 1; i < rad; i++) {
				for (int j = -rad + 1; j < rad; j++) {
					claim(p, fac, ch.getX() + i, ch.getZ() + j);
				}
			}
		} catch (Exception e) {
			p.sendMessage("§c/f claim <säde>");
		}

	}

	private void leaveFaction(Player p, Faction playerFac) {
		PlayerData data = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		int oldFactionID = data.getFactionID();
		if (oldFactionID == 0) {
			p.sendMessage("§eEt ole missään factionissa. /f create <nimi>");
			return;
		}
		p.sendMessage("§eLähdit factionista " + data.getFaction().getLongName() + ".");
		data.setFactionID(0);

		if (pl.getFactionHandler().getFaction(oldFactionID).getPlayers().size() == 0) {
			pl.getFactionHandler().delete(oldFactionID);
			p.sendMessage("§eOlit viimeinen factionissasi, joten se lopetettiin.");
		}
	}

	private void claim(Player claimer, Faction fac, int chunkX, int chunkZ) {
		int claimFaction = pl.getClaimHandler().getClaim(chunkX, chunkZ).getFactionID();
		if (claimFaction != 0) {
			// TODO: Overclaiming
			claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " on jo varattu!");
			return;
		}

		pl.getClaimHandler().create(chunkX, chunkZ, fac.getID());
		claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " claimattu!");

	}

	private void openMap(Player p, int centerChunkX, int centerChunkZ) {
		Inventory inv = Bukkit.createInventory(null, 9 * 7, "§e/claim map");
		// 14 enemy, 4 neutral, 5 friend
		for (int i = -4; i < 5; i++) {
			for (int j = -3; j < 4; j++) {
				int inventoryIndex = 31 + i + j * 9;
				Claim c = pl.getClaimHandler().getClaim(centerChunkX + i, centerChunkZ + j);
				int id = c.getFactionID();
				byte relColor = 5;
				if (id == 0)
					inv.setItem(inventoryIndex, new ItemStack(Material.AIR, 1, (byte) 4));
				else {
					// TODO: Colour by relations
					Faction fac = c.getFaction();
					inv.setItem(inventoryIndex, new ItemStack(Material.STAINED_CLAY, fac
							.getBoardPosition(), relColor));
				}
			}
		}
		p.openInventory(inv);
	}

	private void processFactionCreate(Player p, String arg1) {
		// Create a new faction
		String name = arg1.toUpperCase();
		if (name.length() > Opt.MAX_FACTION_NAME_SHORT) {
			p.sendMessage("§eLiian pitkä nimi factille: " + arg1);
			return;
		}

		Faction current = pl.getFactionHandler().getFaction(pl.getPlayerDataHandler().getPlayerData(
				p.getUniqueId()).getFactionID());
		if (current.getID() != 0) {
			// Already in a fac
			p.sendMessage("§eFactionisi nimi on " + current.getShortName());
			p.sendMessage("§eVoit jättää factionisi komennolla /faction leave.");
			return;
		}

		if (pl.getFactionHandler().exists(name)) {
			p.sendMessage("§eTämä faction on jo olemassa.");
			return;
		}
		// Generate a new ID randomly. If there's more than 2 billion factions, we're
		// screwd;
		int newID;
		do {
			newID = (int) (Math.random() * Integer.MAX_VALUE);
		} while (pl.getFactionHandler().exists(newID));
		Faction created = new Faction(pl, newID, name, name, 0, -1);
		pl.getFactionHandler().create(created);
		pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).setFactionID(created.getID());
		p.sendMessage("§eFaction luotu! Uusi nimi: " + created.getShortName() + ".");
	}
}