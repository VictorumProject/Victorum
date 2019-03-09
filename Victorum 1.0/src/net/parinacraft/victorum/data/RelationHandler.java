package net.parinacraft.victorum.data;

import net.parinacraft.victorum.Victorum;
import net.parinacraft.victorum.claim.Relation;

public class RelationHandler {
	final Victorum pl;

	public RelationHandler(Victorum pl) {
		this.pl = pl;
	}

	public Relation getRelation(int facID, int otherFacID) {
		if (facID == otherFacID)
			return Relation.OWN;
		return Relation.NEUTRAL;
	}

}
