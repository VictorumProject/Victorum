package net.parinacraft.victorum.utils;

import net.minecraft.server.v1_8_R3.Tuple;

public class ChunkCode {

	private static final String BASE25_STR = "ABCDEFGHIJKLMNOPQRSTUVXYZ";
	public static final int NUMBER_COUNT = 3;

	public static Tuple<Integer, Integer> getCoordinates(String chunkCode) {
		int chunkX = getIntFromCode(chunkCode.substring(0, 2));
		int chunkY = Integer.parseInt(chunkCode.substring(2, 5));
		return new Tuple<>(chunkX, chunkY);
	}

	private static int getIntFromCode(String code) {
		if (code.length() == 2) {
			int first = BASE25_STR.indexOf(code.charAt(0) - 1) + 1;
			int last = BASE25_STR.indexOf(code.charAt(1) - 1) + 1;
			return last + first * BASE25_STR.length();
		}
		return 0;
	}

	public static void main(String[] args) {
		System.out.println(getCode(35325235));
	}

	private static String getCode(int id) {
		String first = Integer.toString(id, 16);
		return first;
	}
}
