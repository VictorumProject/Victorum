package net.parinacraft.victorum.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

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
		final PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		final Faction playerFac = pd.getFaction();
		if (args.length == 0) {
			printUsage(sender, lbl);
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				claim(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("join")) {
				sender.sendMessage("§e/" + lbl + " join <facti>");
			} else if (args[0].equalsIgnoreCase("unclaim")) {
				unclaiming(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("map")) {
				openMap(p, playerFac, chunkX, chunkZ);
			} else if (args[0].equalsIgnoreCase("list")) {
				listFactions(p, 1);
			} else if (args[0].equalsIgnoreCase("sethome")) {
				setFactionHome(p, pd, p.getLocation());
			} else if (args[0].equalsIgnoreCase("home")) {
				sendToHome(p, playerFac);
			} else if (args[0].equalsIgnoreCase("create")) {
				sender.sendMessage("§e/" + lbl + " create <lyhenne>");
			} else if (args[0].equalsIgnoreCase("setname") || args[0].equalsIgnoreCase("name") || args[0]
					.equalsIgnoreCase("desc")) {
				sender.sendMessage("§e/" + lbl + " setname <short|long> <uusi nimi>");
			} else if (args[0].equalsIgnoreCase("invite")) {
				p.sendMessage("§e/f invite <nimi>");
			} else if (args[0].equalsIgnoreCase("invites")) {
				showInvites(p, pd, playerFac);
			} else if (args[0].equalsIgnoreCase("leave")) {
				factionLeave(p, playerFac);
			} else if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("who")) {
				showFaction(p, playerFac);
			} else if (args[0].equalsIgnoreCase("bal") || args[0].equalsIgnoreCase("balance") || args[0]
					.equalsIgnoreCase("money")) {
				showFactionBalance(p, playerFac);
			} else {
				printUsage(sender, lbl);
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("claim")) {
				claimRadius(p, playerFac, p.getLocation().getChunk(), args[0], args[1]);
			} else if (args[0].equalsIgnoreCase("create")) {
				createFaction(p, args[1]);
			} else if (args[0].equalsIgnoreCase("setname") || args[0].equalsIgnoreCase("name") || args[0]
					.equalsIgnoreCase("desc")) {
				if (args[1].equalsIgnoreCase("short") || args[1].equalsIgnoreCase("long"))
					sender.sendMessage("§e/" + lbl + " setname " + args[1] + " <uusi nimi>");
				else
					sender.sendMessage("§e/" + lbl + " setname <short|long> <uusi nimi>");
			} else if (args[0].equalsIgnoreCase("join")) {
				joinFaction(p, args[1].toUpperCase());
			} else if (args[0].equalsIgnoreCase("list")) {
				try {
					listFactions(p, Integer.parseInt(args[1]));
				} catch (Exception e) {
					sender.sendMessage("§e/f list <sivu>");
				}
			} else if (args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("who")) {
				String arg1 = args[1].toUpperCase();
				if (!pl.getFactionHandler().exists(arg1)) {
					PlayerData data = pl.getPlayerDataHandler().getPlayerData(arg1);
					if (data != null) {
						showFaction(p, data.getFaction());
					} else {
						sender.sendMessage("§eFactionia " + arg1 + " ei ole olemassa.");
					}
					return true;
				}
				showFaction(p, pl.getFactionHandler().getFactionWithName(arg1));
			} else if (args[0].equalsIgnoreCase("invite")) {
				invite(p, args[1]);
			} else if (args[0].equalsIgnoreCase("bal") || args[0].equalsIgnoreCase("balance") || args[0]
					.equalsIgnoreCase("money")) {
				String name = args[1].toUpperCase();
				if (!pl.getFactionHandler().exists(name)) {
					PlayerData data = pl.getPlayerDataHandler().getPlayerData(name);
					if (data != null) {
						showFactionBalance(p, data.getFaction());
					} else {
						sender.sendMessage("§eFactionia " + name + " ei ole olemassa.");
					}
					return true;
				}
				showFactionBalance(p, pl.getFactionHandler().getFactionWithName(name));
			} else {
				printUsage(sender, lbl);
			}
		} else {
			if (args[0].equalsIgnoreCase("setname") || args[0].equalsIgnoreCase("name") || args[0].equalsIgnoreCase(
					"desc")) {
				setFactionName(p, playerFac, args[1], Joiner.on(' ').join(args).substring(args[0].length() + args[1]
						.length() + 2).trim());
			}
		}
		return true;
	}

	private void setFactionName(Player p, Faction playerFac, String arg1, String args) {
		if (playerFac.isDefaultFaction()) {
			p.sendMessage("§eEt ole missään factionissa. /f create <lyhenne>");
			return;
		}
		if (!playerFac.getFounder().equals(p.getUniqueId())) {
			p.sendMessage("§eVain factionisi perustaja voi vaihtaa sen nimen.");
			return;
		}
		if (arg1.equalsIgnoreCase("short")) {
			String shortName = args;
			for (int i = 0; i < shortName.length(); i++) {
				char c = shortName.charAt(i);
				if (!Opt.ALLOWED_FACTION_NAME_CHARS.contains(c)) {
					p.sendMessage("§eFactin lyhenne ei voi sisältää '" + c + "' merkkiä.");
					return;
				}
			}
			if (shortName.length() > Opt.MAX_FACTION_NAME_SHORT) {
				p.sendMessage("§eLiian pitkä lyhenne factille: " + shortName + ".");
				return;
			}

			if (pl.getFactionHandler().exists(shortName)) {
				p.sendMessage("§eTämä faction on jo olemassa.");
				return;
			}

			playerFac.setShortName(shortName);
			p.sendMessage("§eFactionisi uusi nimi on nyt " + playerFac.getShortName() + ".");
			return;
		} else if (arg1.equalsIgnoreCase("long")) {
			String longName = args.toUpperCase();
			if (longName.length() > Opt.MAX_FACTION_NAME_LONG) {
				p.sendMessage("§eLiian pitkä nimi factille: " + longName + ".");
				return;
			}
			playerFac.setLongName(longName);
			p.sendMessage("§eFactionisi uusi nimi on nyt " + playerFac.getLongName() + ".");
			return;
		}
		printUsage(p, "f");
	}

	private void printUsage(CommandSender sender, String lbl) {
		sender.sendMessage("§eVictorum v" + pl.getDescription().getVersion());
		sender.sendMessage("§e    /" + lbl + " create");
		sender.sendMessage("§e    /" + lbl + " join");
		sender.sendMessage("§e    /" + lbl + " setname");
		sender.sendMessage("§e    /" + lbl + " claim/buy");
		sender.sendMessage("§e    /" + lbl + " who/show");
		sender.sendMessage("§e    /" + lbl + " map");
		sender.sendMessage("§e    /" + lbl + " home");
		sender.sendMessage("§e    /" + lbl + " sethome");
		sender.sendMessage("§e    /" + lbl + " invite");
		sender.sendMessage("§e    /" + lbl + " invites");
		sender.sendMessage("§e    /" + lbl + " leave");
	}

	private void joinFaction(Player p, String name) {
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		Faction fac = pd.getFaction();
		if (fac.getID() != Opt.DEFAULT_FACTION_ID) {
			p.sendMessage("§eOlet jo factionissa " + fac.getShortName());
			return;
		}

		Faction joining = pl.getFactionHandler().getFactionWithName(name);
		if (joining == null) {
			p.sendMessage("§eFactionia " + name + " ei ole olemassa");
			return;
		}

		if (joining.isDefaultFaction()) {
			p.sendMessage("§eTämä on oletusfactioni. Ei mitään erikoista.");
			return;
		}

		// Test for 33% of invites
		int inviteCount = pl.getInviteHandler().getIncomingInvites(p.getUniqueId()).size();
		if ((inviteCount / joining.getPlayers().size()) < 0.33) {
			p.sendMessage("§eAinakin 33% factionin jäsenistä täytyy kutsua sinut, jotta voit liittyä.");
			return;
		}

		pd.setFactionID(joining.getID());
		p.sendMessage("§eLiityit factioniin " + joining.getLongName() + ".");
		for (Player broadcastTarget : Bukkit.getOnlinePlayers()) {
			if (pl.getPlayerDataHandler().getPlayerData(broadcastTarget.getUniqueId()).getFactionID() == joining
					.getID()) {
				broadcastTarget.sendMessage("§e" + p.getName() + " liittyi factioniisi.");
			}
		}
	}

	private void invite(Player inviter, String invitedName) {
		Faction inviterFac = pl.getPlayerDataHandler().getPlayerData(inviter.getUniqueId()).getFaction();
		if (inviterFac.getID() == Opt.DEFAULT_FACTION_ID) {
			// Not in a faction
			inviter.sendMessage("§eEt ole factionissa.");
			inviter.sendMessage("§eVoit luoda factionin komennolla /f create <lyhenne>");
			return;
		}

		if (inviter.getName().equals(invitedName)) {
			inviter.sendMessage("§eUGFHHHhh... Häh?? Mitä sä koitat tehä? /f");
			return;
		}

		UUID invitedUuid = pl.getPlayerDataHandler().getUUIDWithName(invitedName);
		if (invitedUuid == null) {
			inviter.sendMessage("§ePelaajaa " + invitedName + " ei löydetty.");
			return;
		}

		if (pl.getInviteHandler().getOutgoingInvites(inviter.getUniqueId()).contains(invitedUuid)) {
			inviter.sendMessage("§eOlet jo kutsunut pelaajan " + invitedName + ".");
			return;
		}

		pl.getInviteHandler().createInvite(inviter.getUniqueId(), invitedUuid);
		inviter.sendMessage("§e" + invitedName
				+ " kutsuttu factiin. Voit katsoa kutsujen tilanteen komennolla /f invites");

		Player target = Bukkit.getPlayer(invitedName);
		if (target != null) {
			Faction fac = pl.getPlayerDataHandler().getPlayerData(inviter.getUniqueId()).getFaction();
			target.sendMessage("§eSinut on kutsuttu factiin " + fac.getLongName());
		}
	}

	private void showInvites(Player p, PlayerData pd, Faction playerFac) {
		if (pd.getFaction().isDefaultFaction()) {
			Set<UUID> invites = pl.getInviteHandler().getIncomingInvites(pd.getUUID());
			HashMap<Integer, Integer> invitesForFaction = new HashMap<>();
			for (UUID uuid : invites) {
				int factionID = pl.getPlayerDataHandler().getPlayerData(uuid).getFactionID();
				int count = invitesForFaction.getOrDefault(factionID, 0);
				invitesForFaction.put(factionID, count + 1);
			}
			p.sendMessage("");
			p.sendMessage("§eKutsusi:");
			for (Entry<Integer, Integer> e : invitesForFaction.entrySet()) {
				Faction fac = pl.getFactionHandler().getFaction(e.getKey());
				ChatColor color = pl.getRelationHandler().getRelation(fac.getID(), playerFac.getID()).getColor();
				int percentage = 100 * (e.getValue() / fac.getPlayers().size());
				p.sendMessage(color + "§l" + fac.getShortName() + "§e: " + percentage + "%");
			}
		} else {

		}
	}

	private void setFactionHome(Player p, PlayerData pd, Location location) {
		// TODO: Possible faction flags? Flag LEADER_CAN_SET_HOME?
		Faction fac = pd.getFaction();
		if (!fac.getFounder().equals(p.getUniqueId())) {
			p.sendMessage("§eVain perustaja voi asettaa factionin kodin.");
			return;
		}
		pl.getSqlManager().setHome(fac, location);
		fac.setHome(location);
		p.sendMessage("§eFactionisi koti asetettu.");
	}

	private void sendToHome(Player p, Faction f) {
		if (f.getHome() == null) {
			p.sendMessage("§eFactionillasi ei ole kotia.");
			return;
		}
		p.teleport(f.getHome());
		p.sendMessage("§eTeleportattu factionisi kotiin.");
	}

	private void claim(Player claimer, Faction playerFac, int chunkX, int chunkZ) {
		if (playerFac.getID() == Opt.DEFAULT_FACTION_ID) {
			// Not in a faction
			claimer.sendMessage("§eEt ole factionissa.");
			claimer.sendMessage("§eVoit luoda factionin komennolla /f create <lyhenne>");
			return;
		}
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(claimer.getUniqueId());
		if (pd.getRole() != FactionRole.LEADER) {
			claimer.sendMessage("§eEt ole factionisi johtaja. Raja johtajuuteen on $" + playerFac.getLeaderThreshold()
					+ ".");
			return;
		}
		int claimFaction = pl.getClaimHandler().getClaim(chunkX, chunkZ).getFactionID();
		if (claimFaction != Opt.DEFAULT_FACTION_ID) {
			// TODO: Overclaiming
			claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " on jo varattu!");
			return;
		}

		long rent = Claim.getWeeklyRent(chunkX, chunkZ);
		if (playerFac.getBalance() < rent) {
			claimer.sendMessage("§eFactillasi ei ole varaa vuokrata enempää alueita. Alueen hinta on " + rent
					+ "/viikko.");
			return;
		}
		playerFac.withdraw(rent);
		pl.getClaimHandler().create(chunkX, chunkZ, playerFac.getID());
		claimer.sendMessage("§eAlue " + chunkX + ":" + chunkZ + " claimattu! Vuokra: $" + rent + "/viikko.");
	}

	private void claimRadius(Player p, Faction fac, Chunk ch, String arg0, String arg1) {
		if (fac.getID() == Opt.DEFAULT_FACTION_ID) {
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

			int taken = 0;
			int created = 0;
			for (int i = -rad + 1; i < rad; i++) {
				for (int j = -rad + 1; j < rad; j++) {
					int claimFaction = pl.getClaimHandler().getClaim(ch.getX() + i, ch.getZ() + j).getFactionID();
					if (claimFaction != Opt.DEFAULT_FACTION_ID) {
						taken++;
						continue;
					}

					pl.getClaimHandler().create(ch.getX() + i, ch.getZ() + j, fac.getID());
					created++;
				}
			}
			p.sendMessage("§eClaimattu " + created + " aluetta.");
			if (taken != 0)
				p.sendMessage("§e" + taken + " aluetta on jo varattu, ja ne täytyy ostaa manuaalisesti.");
		} catch (Exception e) {
			p.sendMessage("§e/f " + arg0 + " <säde>");
		}

	}

	private void unclaiming(Player p, Faction playerFac, int chunkX, int chunkZ) {
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		if (pd.getRole() != FactionRole.LEADER) {
			p.sendMessage("§eEt ole factisi johtaja. Raja johtajuuteen on $" + playerFac.getLeaderThreshold() + ".");
			return;
		}
		pl.getClaimHandler().unclaim(pl.getClaimHandler().getClaim(chunkX, chunkZ));
	}

	private void openMap(Player p, Faction playerFac, int centerChunkX, int centerChunkZ) {
		HashMap<Integer, Character> factionSigns = new HashMap<>();
		String possibilities = "+-/&$?ABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ";
		int signIndex = 0;

		// 14 enemy, 4 neutral, 5 friend
		String rows = "";
		for (int i = -8; i < 9; i++) {
			for (int j = -24; j < 25; j++) {
				Claim claim = pl.getClaimHandler().getClaim(centerChunkX + j, centerChunkZ + i);
				if (j == 0 && i == 0) {
					float yaw = (p.getLocation().getYaw() + 360) % 360;
					if (yaw > 180 - 45 && yaw <= 180 + 45)
						rows += "§a^";
					else if (yaw > 270 - 45 && yaw <= 270 + 45)
						rows += "§a§l>";
					else if (yaw > 90 - 45 && yaw <= 90 + 45)
						rows += "§a§l<";
					else if ((yaw >= 360 - 45 && yaw < 360) || (yaw < 45 && yaw > 0))
						rows += "§av";
					else
						rows += "§4+";
					continue;
				}
				Character c;
				if (!factionSigns.containsKey(claim.getFactionID()))
					factionSigns.put(claim.getFactionID(), possibilities.charAt(signIndex++));
				c = factionSigns.get(claim.getFactionID());

				if (claim.getFactionID() == Opt.DEFAULT_FACTION_ID) {
					rows += "§7+";
				} else {
					ChatColor color = pl.getRelationHandler().getRelation(playerFac.getID(), claim.getFactionID())
							.getColor();
					rows += color.toString() + c.toString();
				}
			}
			rows += "\n";
		}
		p.sendMessage("");
		p.sendMessage(rows);
	}

	private void listFactions(Player p, int pageNumber) {
		ArrayList<Faction> factionList = new ArrayList<>(pl.getFactionHandler().getAllFactions());

		// // Remove default faction from list
		// Faction vict = null;
		// for (Faction faction : factionList) {
		// if (faction.isDefaultFaction()) {
		// vict = faction;
		// break;
		// }
		// }
		// factionList.remove(vict);

		// Sort the list by value
		factionList.sort(new Comparator<Faction>() {
			@Override
			public int compare(Faction f1, Faction f2) {
				int dValue = (int) (f2.getValue() - f1.getValue());
				if (dValue == 0)
					return f1.getID() - f2.getID();
				return dValue;
			}
		});

		// Print in chat
		PlayerData pd = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		p.sendMessage("§eListataan " + pageNumber + "/" + (factionList.size() / 10 + 1) + " sivu factioneista...");
		int size = Math.min((pageNumber) * 10, factionList.size());
		for (int i = pageNumber; i < size; i++) {
			Faction fac = factionList.get(i);
			ChatColor color = pl.getRelationHandler().getRelation(pd.getFactionID(), factionList.get(i).getID())
					.getColor();
			p.sendMessage("  " + color + " " + fac.getShortName() + "§e: $" + fac.getValue() + "/" + fac.getPlayers()
					.size() + "p/" + fac.getClaims().size() + "c");
		}
	}

	private void showFaction(Player caller, Faction fac) {
		int playerFacID = pl.getPlayerDataHandler().getPlayerData(caller.getUniqueId()).getFactionID();
		ChatColor color = pl.getRelationHandler().getRelation(fac.getID(), playerFacID).getColor();
		String playerList = "";
		Set<UUID> players = fac.getPlayers();
		for (UUID uuid : players) {
			PlayerData targetData = pl.getPlayerDataHandler().getPlayerData(uuid);
			String roleStr = (fac.getFounder().equals(uuid) ? "**"
					: targetData.getRole() == FactionRole.LEADER ? "*" : "+");
			playerList += color + roleStr + targetData.getLastSeenName() + ", ";
			playerList = playerList.substring(0, playerList.length());
		}
		OfflinePlayer founder = Bukkit.getOfflinePlayer(fac.getFounder());
		String founderStr = (founder == null ? fac.getFounder().toString() : founder.getName());
		caller.sendMessage("");
		caller.sendMessage("              §eFaction " + color + fac.getShortName());
		caller.sendMessage("§eKoko nimi: " + fac.getLongName());
		caller.sendMessage("§ePerustaja: " + color + founderStr);
		caller.sendMessage("§eJäsenet (" + players.size() + "): " + color + playerList.substring(2, playerList.length()
				- 2));
		caller.sendMessage("§eAlueet: " + pl.getClaimHandler().getAllClaims(fac.getID()).size());
		caller.sendMessage("§eRahaa: $" + fac.getBalance());
		caller.sendMessage("§eArvo: $" + fac.getValue());
	}

	private void showFactionBalance(Player caller, Faction faction) {
		caller.sendMessage("§eFactionin " + faction.getLongName() + " rahatilanne on $" + faction.getBalance());
	}

	private void createFaction(Player p, String arg1) {
		// Create a new faction
		Faction current = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).getFaction();
		if (current.getID() != Opt.DEFAULT_FACTION_ID) {
			// Already in a fac
			p.sendMessage("§eFactionisi lyhenne on " + current.getShortName());
			p.sendMessage("§eVoit jättää factionisi komennolla /faction leave.");
			return;
		}

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

		if (pl.getFactionHandler().exists(name)) {
			p.sendMessage("§eTämä faction on jo olemassa.");
			return;
		}
		Faction created = pl.getFactionHandler().create(p, name);
		pl.getPlayerDataHandler().getPlayerData(p.getUniqueId()).setFactionID(created.getID());
		p.sendMessage("§eFaction luotu! Uusi nimi: " + created.getShortName() + ".");
		Bukkit.broadcastMessage(p.getDisplayName() + " §eloi factionin " + created.getShortName() + ".");
	}

	private void factionLeave(Player p, Faction playerFac) {
		PlayerData data = pl.getPlayerDataHandler().getPlayerData(p.getUniqueId());
		int oldFactionID = data.getFactionID();
		String oldFactionName = data.getFaction().getLongName();
		if (oldFactionID == Opt.DEFAULT_FACTION_ID) {
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
		data.setFactionID(Opt.DEFAULT_FACTION_ID);
		data.setRole(FactionRole.MEMBER);

		if (pl.getFactionHandler().getFaction(oldFactionID).getPlayers().size() == 0) {
			pl.getFactionHandler().delete(oldFactionID);
			p.sendMessage("§eOlit viimeinen factionissasi, joten se lopetettiin.");
			Bukkit.broadcastMessage(p.getDisplayName() + " lopetti factionin " + oldFactionName + ".");
		}
	}
}