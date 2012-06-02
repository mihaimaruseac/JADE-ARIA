package jade.proto;

import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.lang.acl.ACLMessage;

import java.util.Vector;

public class DutchAuctionInitiator extends Initiator {

	protected DutchAuctionInitiator(Agent a, ACLMessage initiation,
			DataStore store) {
		super(a, initiation, store);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Vector prepareInitiations(ACLMessage initiation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean checkInSequence(ACLMessage reply) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected int checkSessions(ACLMessage reply) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String[] getToBeReset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}
