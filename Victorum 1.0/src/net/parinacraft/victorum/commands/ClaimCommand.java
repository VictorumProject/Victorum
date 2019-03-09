package net.parinacraft.victorum.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.claim.Faction;
import net.parinacraft.victorum.claim.FactionRole;
import net.parinacraft.victorum.data.PlayerData;

public class ClaimCommand implements CommandExecutor {
	private final Victorum pl;
	private final HashMap<UUID, Long> confirmLeave;

	public ClaimCommand(Victorum pl) {
		this.pl = pl;
		this.confirmLeave = new HashMap<>();
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
			sender.sendMessage("§eVictorum v" + pl.getDescription().getVersion());
			sender.sendMessage("§e    /" + lbl + " create");
			sender.sendMessage("§e    /" + lbl + " claim/buy");
			sender.sendMessage("§e    /" + lbl + " who/show");
			sender.sendMessage("§e    /" + lbl + " invite");
			sender.sendMessage("§e    /" + lbl + " map");
			sender.sendMessage("§e    /" + lbl + " leave");

		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				claim(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("unclaim")) {
				processUnclaiming(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("map")) {
				openMap(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("list")) {
				listFactions(p, 1);
			} else if (args[0].equalsIgnoreCase("create")) {
				sender.sendMessage("§e/" + lbl + " create <lyhenne>");
			} else if (args[0].equalsIgnoreCase("leave")) {
				processFactionLeave(p, playerFac);
			} else if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("who")) {
				showFaction(p, playerFac);
			} else {
				sender.sendMessage("§eKomentoa ei prosessoitu.");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				claimRadius(p, playerFac, p.getLocation().getChunk(), args[0], args[1]);
			} else if (args[0].equalsIgnoreCase("create")) {
				processFactionCreate(p, args[1]);
			} else if (args[0].equalsIgnoreCase("list")) {
				try {
					listFactions(p, Integer.parseInt(args[1]));
				} catch (Exception e) {
					sender.sendMessage("§e/f list <sivu>");
				}
			} else if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("who")) {
				String name = args[1];
				if (pl.getFactionHandler().exists(name)) {
					showFaction(p, pl.getFactionHandler().getFactionWithName(name));
					return true;
				}

			}
		}
		return true;

	}

	private void claim(Player claimer, Faction fac, int chunkX, int chunkZ) {
		if (pl.getPlayerDataHandler().getPlayerData(claimer.getUniqueId()).getFactionID() == pl.getFactionHandler()
				.getDefaultFactionID()) {
			// Not in a faction
			claimer.sendMessage("§eEt ole factionissa.");
			claimer.sendMessage("§eVoit luoda factionin komennolla /f create <lyhenne>");
			return;
		}
		int claimFaction = pl.getClaimHandler().getClaim(chunkX, chunkZ).getFactionID();
		if (claimFaction != pl.getFactionHandler().getDefaultFactionID()) {
			// TODO: Overclaiming
			claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " on jo varattu!");
			return;
		}

		pl.getClaimHandler().create(chunkX, chunkZ, fac.getID());
		claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " claimattu!");

	}

	private void claimRadius(Player p, Faction fac, Chunk ch, String arg0, String arg1) {
		if (fac.getID() == pl.getFactionHandler().getDefaultFactionID()) {
			// Not in a faction
			p.sendMessage("§eEt ole factionissa.");
			p.sendMessage("§eVoit luoda factionin komennolla /f create <lyhenne>");
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
			p.sendMessage("§e/f " + arg0 + " <säde>");
		}

	}

	private void processUnclaiming(Player p, Faction playerFac, int chunkX, int chunkZ) {
		// PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		// if (pd.getRole() != FactionRole.LEADER) {
		// p.sendMessage("§eEt ole factisi johtaja. Raja johtajuuteen on " +
		// playerFac.getLeaderThreshold());
		// return;
		// }
		pl.getClaimHandler().unclaim(pl.getClaimHandler().getClaim(chunkX, chunkZ));
	}

	private void openMap(Player p, Faction playerFac, int centerChunkX, int centerChunkZ) {
		HashMap<Integer, Character> factionSigns = new HashMap<>();
		String possibilities = "+-/&$?ABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ";
		int signIndex = 0;

		// 14 enemy, 4 neutral, 5 friend
		String rows = "";
		for (int i = -8; i < 9; i++) {
			for (int j = -25; j < 24; j++) {
				Claim claim = pl.getClaimHandler().getClaim(centerChunkX + j, centerChunkZ + i);
				if (j == 0 && i == 0)
					rows += "§2X";
				Character c;
				if (!factionSigns.containsKey(claim.getFactionID()))
					factionSigns.put(claim.getFactionID(), possibilities.charAt(signIndex++));
				c = factionSigns.get(claim.getFactionID());

				ChatColor color = pl.getRelationHandler().getRelation(playerFac.getID(), claim.getFactionID())
						.getColor();
				rows += color.toString() + c.toString();
			}
			rows += "\n";
		}
		p.sendMessage("");
		p.sendMessage(rows);
	}

	private void listFactions(Player p, int page) {
		ArrayList<Faction> factionList = new ArrayList<>(pl.getFactionHandler().getAllFactions());
		Faction vict = null;
		for (Faction faction : factionList)
			if (faction.getID() == pl.getFactionHandler().getDefaultFactionID()) {
				vict = faction;
				break;
			}
		factionList.remove(vict);
		factionList.sort(new Comparator<Faction>() {
			@Override
			public int compare(Faction f1, Faction f2) {
				int dValue = (int) (f1.getValue() - f2.getValue());
				if (dValue == 0)
					return f1.getID() - f2.getID();
				return dValue;
			}
		});
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		p.sendMessage("§eListing " + page + "/" + (factionList.size() / 10 + 1) + " page of factions...");
		for (int i = page; i < Math.min((page + 1) * 10, factionList.size()); i++) {
			Faction fac = factionList.get(i);
			ChatColor color = pl.getRelationHandler().getRelation(pd.getFactionID(), factionList.get(i).getID())
					.getColor();
			p.sendMessage("  " + color + " " + fac.getShortName() + ": " + fac.getPlayers().size());
		}
	}

	private void showFaction(Player p, Faction fac) {
		int playerFacID = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFactionID();
		ChatColor color = pl.getRelationHandler().getRelation(fac.getID(), playerFacID).getColor();
		String playerList = "";
		for (UUID uuid : fac.getPlayers()) {
			FactionRole role = pl.getPlayerDataHandler().getPlayerData(uuid).getRole();
			String roleStr = (role == FactionRole.LEADER ? "*" : role == FactionRole.FOUNDER ? "**" : "+");
			OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
			if (op != null)
				playerList += color + roleStr + op.getName() + "§e, ";
			else
				playerList += uuid.toString();
			playerList = playerList.substring(0, playerList.length() - 2);
		}
		OfflinePlayer founder = Bukkit.getOfflinePlayer(fac.getFounder());
		String founderStr = (founder == null ? fac.getFounder().toString() : founder.getName());
		p.sendMessage("");
		p.sendMessage("              §eFaction " + color + fac.getShortName());
		p.sendMessage("§eKoko nimi: " + fac.getLongName());
		p.sendMessage("§ePerustaja: " + founderStr);
		p.sendMessage("§eJäsenet: " + playerList);
		p.sendMessage("§eAlueet: " + pl.getClaimHandler().getAllClaims(fac.getID()).size());
		p.sendMessage("§eArvo: " + fac.getValue());
	}

	private void processFactionCreate(Player p, String arg1) {
		// Create a new faction
		String name = arg1.toUpperCase();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (!Opt.ALLOWED_FACTION_NAME_CHARS.contains(c)) {
				p.sendMessage("§eFactin lyhenne ei voi sisältää '" + c + "' merkkiä.");
				return;
			}
		}
		if (name.length() > Opt.MAX_FACTION_NAME_SHORT) {
			p.sendMessage("§eLiian pitkä lyhenne factille: " + arg1);
			return;
		}

		Faction current = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFaction();
		if (current.getID() != pl.getFactionHandler().getDefaultFactionID()) {
			// Already in a fac
			p.sendMessage("§eFactionisi lyhenne on " + current.getShortName());
			p.sendMessage("§eVoit jättää factionisi komennolla /faction leave.");
			return;
		}

		if (pl.getFactionHandler().exists(name)) {
			p.sendMessage("§eTämä faction on jo olemassa.");
			return;
		}
		// Generate a new ID randomly. If there's more a billion factions, we're
		// screwd;
		Faction created = pl.getFactionHandler().create(p, name);
		pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).setFactionID(created.getID());
		p.sendMessage("§eFaction luotu! Uusi nimi: " + created.getShortName() + ".");
		Bukkit.broadcastMessage(p.getDisplayName() + " §eloi factionin " + created.getShortName());
	}

	private void processFactionLeave(Player p, Faction playerFac) {
		PlayerData data = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		int oldFactionID = data.getFactionID();
		String oldFactionName = data.getFaction().getLongName();
		if (oldFactionID == pl.getFactionHandler().getDefaultFactionID()) {
			p.sendMessage("§eEt ole missään factionissa. /f create <lyhenne>");
			return;
		}
		if (confirmLeave.containsKey(p.getUniqueId())) {
			long lastTime = confirmLeave.get(p.getUniqueId());
			if (lastTime + 10000 < System.currentTimeMillis()) {
				p.sendMessage("§eJos haluat jättää factionisi, tee /f leave uudelleen 10 sekunnin sisällä.");
				confirmLeave.put(p.getUniqueId(), System.currentTimeMillis());
				return;
			}
		} else {
			confirmLeave.put(p.getUniqueId(), System.currentTimeMillis());
			p.sendMessage("§eJos haluat jättää factionisi, tee /f leave uudelleen 10 sekunnin sisällä.");
			return;
		}
		confirmLeave.remove(p.getUniqueId());

		p.sendMessage("§eLähdit factionista " + data.getFaction().getLongName() + ".");
		data.setFactionID(pl.getFactionHandler().getDefaultFactionID());

		if (pl.getFactionHandler().getFaction(oldFactionID).getPlayers().size() == 0) {
			pl.getFactionHandler().delete(oldFactionID);
			p.sendMessage("§eOlit viimeinen factionissasi, joten se lopetettiin.");
		}

		Bukkit.broadcastMessage(p.getDisplayName() + " lopetti factionin " + oldFactionName);
	}
}