package examples.protocols;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.proto.DutchAuctionResponder;

public class DutchAuctionResponderAgent extends Agent
{
	private static final long	serialVersionUID	= 2516245721928852073L;
	
	private void log(String msg)
	{
		System.out.println("[" + getLocalName() + "]" + msg);
	}
	
	@Override
	protected void setup()
	{
		super.setup();
		
		log("booting agent; waiting for CFP");
		
		addBehaviour(new DutchAuctionResponder(this) {
			private static final long	serialVersionUID	= -8438328424220521732L;
			
			@Override
			protected boolean handleCfp(ACLMessage cfp)
			{
				log("CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
				
				return shouldPropose(cfp);
			}
			
			@Override
			protected Object handleAcceptProposal(ACLMessage cfp, ACLMessage accept) throws FailureException
			{
				log("Proposal accepted");
				if(performAction(cfp, accept))
				{
					String result = "OK";
					log("Action performed; result: " + result);
					return result;
				}
				else
				{
					log("Action execution failed");
					throw new FailureException("action failed");
				}
			}
			
			@Override
			protected void handleRejectProposal(ACLMessage cfp, ACLMessage reject)
			{
				log("Proposal rejected");
			}
			
		});
	}
	
	/**
	 * To override;
	 */
	protected boolean shouldPropose(ACLMessage cfp)
	{
//		return false;
		return (Math.random() > 0.9);
	}
	
	/**
	 * To override;
	 */
	protected boolean performAction(ACLMessage cfp, ACLMessage accept)
	{
		return (Math.random() > 0.1);
	}
}
