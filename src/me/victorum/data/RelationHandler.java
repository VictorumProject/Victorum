package me.victorum.data;

import me.victorum.claim.Relation;
import me.victorum.victorum.Victorum;

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
