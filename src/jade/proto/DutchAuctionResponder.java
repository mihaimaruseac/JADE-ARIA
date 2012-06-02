package jade.proto;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.proto.*;
import jade.proto.states.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;

public class DutchAuctionResponder extends SSResponder {
	public final String CFP_KEY	= INITIATION_KEY;
	public final String SOA_KEY	= "__SOA" + CFP_KEY;
	public final String ACCEPT_PROPOSAL_KEY = RECEIVED_KEY;
	public final String REJECT_PROPOSAL_KEY = RECEIVED_KEY;

	public static final String RECEIVE_INFORM_START_OF_AUCTION = "Receive-Inform-Start-Of-Auction";
	public static final String RECEIVE_CFP = "Receive-Cfp";
	public static final String HANDLE_CFP = "Handle-Cfp";
	public static final String HANDLE_ACCEPT_PROPOSAL = "Handle-Accept-Proposal";
	public static final String HANDLE_REJECT_PROPOSAL = "Handle-Reject-Proposal";

	public DutchAuctionResponder(Agent a) {
		this(a, null, new DataStore(), true);
	}

	public DutchAuctionResponder(Agent a, ACLMessage initiation,
			DataStore store, boolean useInitiationKey) {
		super(a, initiation, store, useInitiationKey);

		Behaviour b = null;

		/* receive INFORM_START_OF_AUCTION */
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION),MessageTemplate.MatchPerformative(ACLMessage.CFP));
		b = new MsgReceiver(a, mt, -1, getDataStore(), SOA_KEY);
		registerFirstState(b, RECEIVE_INFORM_START_OF_AUCTION);
		registerDefaultTransition(RECEIVE_INFORM_START_OF_AUCTION, RECEIVE_CFP);

		/* receive CFP */
		b = new MsgReceiver(a, mt, -1, getDataStore(), CFP_KEY);
		registerState(b, RECEIVE_CFP);
		registerDefaultTransition(RECEIVE_CFP, HANDLE_CFP);

		/* handle CFP */
		b = new CfpHandler(myAgent);
		registerFirstState(b, HANDLE_CFP);
		b.setDataStore(getDataStore());
		registerTransition(HANDLE_CFP, SEND_REPLY, CfpHandler.CFP_ACCEPTED);
		registerDefaultTransition(HANDLE_CFP, RECEIVE_CFP);

		/* handle SEND_REPLY */
		deregisterDefaultTransition(SEND_REPLY);
		registerTransition(SEND_REPLY, RECEIVE_NEXT, ACLMessage.PROPOSE);

		/* handle RECEIVE_NEXT & CHECK_IN_SEQ */
		registerTransition(RECEIVE_NEXT, HANDLE_REJECT_PROPOSAL, MsgReceiver.TIMEOUT_EXPIRED); 
		registerTransition(CHECK_IN_SEQ, HANDLE_ACCEPT_PROPOSAL, ACLMessage.ACCEPT_PROPOSAL, new String[]{SEND_REPLY}); 
		registerTransition(CHECK_IN_SEQ, HANDLE_REJECT_PROPOSAL, ACLMessage.REJECT_PROPOSAL); 
		registerDefaultTransition(HANDLE_ACCEPT_PROPOSAL, SEND_REPLY);
		registerDefaultTransition(HANDLE_REJECT_PROPOSAL, DUMMY_FINAL);

		/* handle HANDLE_ACCEPT_PROPOSAL */
		b = new AcceptHandler(myAgent);
		registerDSState(b, HANDLE_ACCEPT_PROPOSAL);
		
		/* handle HANDLE_REJECT_PROPOSAL */
		b = new RejectHandler(myAgent);
		registerDSState(b, HANDLE_REJECT_PROPOSAL);
	}

	protected boolean handleCfp(ACLMessage cfp) {
		return false;
	}

	protected Object handleAcceptProposal(ACLMessage cfp, ACLMessage accept) throws FailureException {
		return null;
	}

	protected void handleRejectProposal(ACLMessage cfp, ACLMessage reject) {
	}

	private static class CfpHandler extends OneShotBehaviour {
		public static final int CFP_ACCEPTED = -4242;
		public static final int CFP_DENIED = -4243;
		private int ret;

		public CfpHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			DutchAuctionResponder parent = (DutchAuctionResponder) getParent();
			if (parent.handleCfp((ACLMessage) getDataStore().get(parent.CFP_KEY))) {
				ret = CFP_ACCEPTED;
			} else {
				ret = CFP_DENIED;
			}
		}

		public int onEnd() {
			return ret;
		}
	}

	private static class AcceptHandler extends OneShotBehaviour {
		public AcceptHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			DutchAuctionResponder parent = (DutchAuctionResponder) getParent();
			try {
				ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
				ACLMessage accept = (ACLMessage) getDataStore().get(parent.ACCEPT_PROPOSAL_KEY);
				parent.handleAcceptProposal(cfp, accept);
			}
			catch (FIPAException fe) {
				fe.getACLMessage();
			}
		}
	}
	
	
	/**
	 Inner class RejectHandler
	 */
	private static class RejectHandler extends OneShotBehaviour {
		public RejectHandler(Agent a) {
			super(a);
		}
		
		public void action() {
			DutchAuctionResponder parent = (DutchAuctionResponder) getParent();
			ACLMessage cfp = (ACLMessage) getDataStore().get(parent.CFP_KEY);
			ACLMessage reject = (ACLMessage) getDataStore().get(parent.REJECT_PROPOSAL_KEY);
			parent.handleRejectProposal(cfp, reject);
		}
	}
}
