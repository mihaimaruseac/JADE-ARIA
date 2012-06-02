package jade.proto;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.*;
import jade.proto.states.*;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

public class DutchAuctionResponder extends SSResponder {
	public final String CFP_KEY	= INITIATION_KEY;
	public final String SOA_KEY	= "__SOA" + CFP_KEY;

	public static final String RECEIVE_INFORM_START_OF_AUCTION = "Receive-Inform-Start-Of-Auction";
	public static final String RECEIVE_CFP = "Receive-Cfp";
	public static final String HANDLE_CFP = "Handle-Cfp";

	public DutchAuctionResponder(Agent a) {
		this(a, null, new DataStore(), true);
	}

	public DutchAuctionResponder(Agent a, ACLMessage initiation,
			DataStore store, boolean useInitiationKey) {
		super(a, initiation, store, useInitiationKey);

		Behaviour b = null;

		/* receive INFORM_START_OF_AUCTION */
		b = new MsgReceiver(a, /*mt*/null, -1, getDataStore(), SOA_KEY);
		registerFirstState(b, RECEIVE_INFORM_START_OF_AUCTION);
		registerDefaultTransition(RECEIVE_INFORM_START_OF_AUCTION, RECEIVE_CFP);

		/* receive CFP */
		b = new MsgReceiver(a, /*mt*/null, -1, getDataStore(), CFP_KEY);
		registerState(b, RECEIVE_CFP);
		registerDefaultTransition(RECEIVE_CFP, HANDLE_CFP);

		/* handle CFP */
		b = new CfpHandler(myAgent);
		registerFirstState(b, HANDLE_CFP);
		b.setDataStore(getDataStore());
		registerTransition(HANDLE_CFP, SEND_REPLY, CfpHandler.CFP_ACCEPTED);
		registerDefaultTransition(HANDLE_CFP, SEND_REPLY);

		/* handle SEND_REPLY */
	}

	protected boolean handleCfp(ACLMessage cfp) {
		return false;
	}

	protected Object handleAcceptProposal(ACLMessage cfp, ACLMessage accept) throws FailureException {
		return null;
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
	}

	private static class CfpHandler extends OneShotBehaviour {
		public static final int CFP_ACCEPTED = -4242;
		private int ret;

		public CfpHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			DutchAuctionResponder parent = (DutchAuctionResponder) getParent();
			if (parent.handleCfp((ACLMessage) getDataStore().get(parent.CFP_KEY))) {
				ret = CFP_ACCEPTED;
			}
		}

		public int onEnd() {
			return ret;
		}
	}
}
