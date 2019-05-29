package me.victorum.victorum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.victorum.data.PlayerData;

public class EconomyHandler {
	private final Victorum pl;

	public EconomyHandler(Victorum pl) {
		this.pl = pl;
	}

	public List<PlayerData> getTopBalances(int count) {
		List<PlayerData> players = new ArrayList<>(pl.getPlayerDataHandler().getAllData());
		count = Math.max(count, players.size());

		// TODO: This can be optimized, we only need count number of entries
		players.sort(new Comparator<PlayerData>() {
			@Override
			public int compare(PlayerData o1, PlayerData o2) {
				return o2.getBalance() > o1.getBalance() ? 1 : -1;
			}
		});
		return players.subList(0, count);
	}
}
