package examples.protocols;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import jade.lang.acl.ACLMessage;
import jade.proto.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;

public class DutchAuctionInitiatorAgent extends Agent {

	private int numBidders;
	private int crtPrice = 10;
	private int minPrice = 5;
	Object[] args; 
			
	private ACLMessage getCfp(Object[] args){
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		for (int i = 0; i < args.length; ++i) {
			cfp.addReceiver(new AID((String) args[i], AID.ISLOCALNAME));
		}
		cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
		cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		cfp.setContent(crtPrice  + "");
		return cfp;
	}
	
	protected void setup() {
		
		args = getArguments();
		if (args != null && args.length > 0) {
			numBidders = args.length;
			System.out.println("["+getLocalName()+"] There are: " + numBidders + " responders.");

			ACLMessage cfp = getCfp(args);

			addBehaviour(new DutchAuctionInitiator(this, cfp) {				

				protected void handleBid(ACLMessage bid, ACLMessage accept) {
					accept = bid.createReply();
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					System.out.println("Sold to: " + bid.getSender().getName());
				}
				
				protected void handleTimeout(Vector responses, Vector acceptances) {
					if (endAuction()){
						 System.out.println("end");
					} else {
						acceptances.add(prepareCFP());
					}
						
				}

				@Override
				protected boolean endAuction() {
					return crtPrice < minPrice;
				}

				@Override
				protected ACLMessage prepareCFP() {					
					crtPrice--;					
					return getCfp(args);
				}
				
			});
		}
	}
}
