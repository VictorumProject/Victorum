package net.parinacraft.victorum.data;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;

import com.google.common.base.Preconditions;

import net.parinacraft.victorum.Opt;
import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Claim;
import net.parinacraft.victorum.claim.Faction;

public class FactionHandler {
	private final Victorum pl;
	private final HashMap<Integer, Faction> factions;

	public FactionHandler(Victorum pl) {
		this.pl = pl;
		factions = pl.getSqlManager().loadFactions();

		// Asynchronously update faction value and leaderboard placement
		Bukkit.getScheduler().runTaskTimer(pl, () -> {
			final int[] factIds = new int[factions.size()];
			{
				// Add ids
				int i = 0;
				for (int id : factions.keySet())
					factIds[i++] = id;
			}

			for (int facID : factIds) {
				Faction fac = pl.getFactionHandler().getFaction(facID);
				long value = 0;
				Set<Claim> claims = pl.getClaimHandler().getAllClaims(facID);
				// Count spawners for a test
				for (Claim claim : claims) {
					Chunk ch = Opt.GAME_WORLD.getChunkAt(claim.getChunkX(), claim.getChunkZ());
					for (int rangeX = 0; rangeX < 16; rangeX++) {
						for (int rangeY = 0; rangeY < 256; rangeY++) {
							for (int rangeZ = 0; rangeZ < 16; rangeZ++) {
								Block b = ch.getBlock(rangeX, rangeY, rangeZ);
								switch (b.getType()) {
								case OBSIDIAN:
									value += 50;
									break;
								case MOB_SPAWNER: {
									BlockState bs = b.getState();
									CreatureSpawner cs = (CreatureSpawner) bs;
									if (cs.getCreatureType() == CreatureType.fromEntityType(EntityType.IRON_GOLEM)) {

									}
								}
								default:
									break;
								}
							}
						}
					}
				}

				fac.setValue(value);
				Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
					pl.getSqlManager().setValue(facID, fac.getValue());
				});
			}
		}, 5 * 20, (long) (20 * 60 * 0.25));
	}

	public Faction getFaction(int id) {
		return Preconditions.checkNotNull(factions.get(id));
	}

	public void delete(int facID) {
		factions.remove(facID);
		pl.getClaimHandler().unclaimAll(facID);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().removeFaction(facID);
		});
	}

	public void create(Faction fac) {
		factions.put(fac.getID(), fac);
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			pl.getSqlManager().createFaction(factions.get(fac.getID()));
		});
	}

	public boolean exists(String shortName) {
		for (Faction fac : factions.values()) {
			if (fac.getShortName().contentEquals(shortName))
				return true;
		}
		return false;
	}
}
