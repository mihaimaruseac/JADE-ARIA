package jade.proto;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.Initiator.ProtocolSession;
import jade.util.leap.Serializable;


import java.util.Vector;

public abstract class DutchAuctionInitiator extends Initiator {

    private static final String HANDLE_PROPOSE = "Handle-propose";
    private static final String TIMEOUT = "timeout";
    private static final String CHECK_CONTINUE = "Check-continue";
    private static final String END_AUCTION = "End-auction";

    private static final int CONTINUE = 42;
    private static final int ABORT = 43;

    // When step == 1 we deal with CFP 
 	// When step == 2 we deal with ACCEPT_PROPOSAL
 	private int step = 1;
    /**
     * key to retrieve from the DataStore of the behaviour the vector of
     * ACCEPT/REJECT_PROPOSAL ACLMessage objects that have to be sent
     **/
    public final String ALL_ACCEPTANCES_KEY = "__all-acceptances" +hashCode();
    /**
     * key to retrieve from the DataStore of the behaviour the vector of
     * ACLMessage objects that have been received as response.
     **/
    public final String ALL_RESPONSES_KEY = "__all-responses" + hashCode();

    public DutchAuctionInitiator(Agent a, ACLMessage cfp){
        this(a, cfp, new DataStore());
    }

    public DutchAuctionInitiator(Agent a, ACLMessage initiation, DataStore store) {
		super(a, initiation, store);

        // FSM
        registerTransition(CHECK_IN_SEQ, HANDLE_PROPOSE, ACLMessage.PROPOSE);
        registerDefaultTransition(HANDLE_PROPOSE, END_AUCTION);
        registerDefaultTransition(CHECK_SESSIONS, TIMEOUT);
        registerDefaultTransition(TIMEOUT, CHECK_CONTINUE);
        registerTransition(CHECK_CONTINUE, PREPARE_INITIATIONS, CONTINUE);
        registerTransition(CHECK_CONTINUE, END_AUCTION, ABORT);
        

        // Create and register the states specific to the protocol
        Behaviour b = null;
        // HANDLE_PROPOSE
        b = new OneShotBehaviour(myAgent) {
            public void action() {
                Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
                ACLMessage propose = (ACLMessage) getDataStore().get(REPLY_K);
                handleBid(propose, acceptances);
            }
        };
        
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_TIMEOUT
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
                Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
                handleTimeout(responses, acceptances);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, TIMEOUT);
        
        b = new OneShotBehaviour(myAgent) {
			
			@Override
			public void action() {
				
			}
			
			@Override
			public int onEnd() {				
				return endAuction() ? CONTINUE : ABORT;  
			}
		};
		
		b.setDataStore(getDataStore());
		registerState(b, END_AUCTION);
		
		b = new OneShotBehaviour() {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				
			}
		};
	}

    protected void handleTimeout(Vector responses, Vector acceptances) {		
	}

	protected abstract boolean endAuction();
    protected abstract ACLMessage prepareCFP();

	@Override
	protected Vector prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
    }
	
	protected void sendInitiations(Vector initiations) {
		// By default the initiations parameter points to the Vector of the CFPs. 
		// However at step 2 we need to deal with the acceptances
/*		if (step == 1) {
			initiations = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
		}*/

		super.sendInitiations(initiations);
	}

    /**
     Check whether a reply is in-sequence and update the appropriate Session
     */
    protected boolean checkInSequence(ACLMessage reply) {
        return true;
    }

    protected int checkSessions(ACLMessage reply) {
        return 1;
    }

    private String[] toBeReset = null;

    /**
     */
    protected String[] getToBeReset() {
        if (toBeReset == null) {
            toBeReset = new String[] {
                    HANDLE_PROPOSE,
                    HANDLE_NOT_UNDERSTOOD,
                    HANDLE_FAILURE,
                    HANDLE_OUT_OF_SEQ
            };
        }
        return toBeReset;
    }

    protected ProtocolSession getSession(ACLMessage msg, int sessionIndex) {
        return null;
    }


    protected void handleBid(ACLMessage propose, Vector accept) {
    }

    /**
    Initialize the data store. 
	 **/
	protected void initializeDataStore(ACLMessage msg){
		super.initializeDataStore(msg);
		DataStore ds = getDataStore();
		Vector l = new Vector();
		l = new Vector();
		ds.put(ALL_ACCEPTANCES_KEY, l);
	}

    /**
     * This method must return the vector of ACLMessage objects to be
     * sent. It is called in the first state of this protocol.
     * This default implementation just returns the ACLMessage object (a CFP)
     * passed in the constructor. Programmers might prefer to override
     * this method in order to return a vector of CFP objects for 1:N conversations
     * or also to prepare the messages during the execution of the behaviour.
     * @param cfp the ACLMessage object passed in the constructor
     * @return a Vector of ACLMessage objects. The value of the slot
     * <code>reply-with</code> is ignored and regenerated automatically
     * by this class.
     **/
    protected Vector prepareCfps(ACLMessage cfp) {
    	System.out.println(cfp.toString());
        Vector v = new Vector(1);
        v.addElement(cfp);	
        return v;
    }   
    
    
    /**
    Inner class Session
	 */
	class Session implements ProtocolSession, Serializable {
		// Session states
		static final int INIT = 0;
		static final int REPLY_RECEIVED = 1;

		private int state = INIT;
		private int step;

		public Session(int step) {
			this.step = step;
		}

		public String getId() {
			return null;
		}

		/** Return true if received ACLMessage is consistent with the protocol */
		public boolean update(int perf) {
			if (state == INIT) {
				if (step == 1) {
					switch (perf) {
					case ACLMessage.PROPOSE:
					case ACLMessage.NOT_UNDERSTOOD:
					case ACLMessage.FAILURE:
						state = REPLY_RECEIVED;
						return true;
					default:
						return false;
					}
				}
				else {
					switch (perf) {
					case ACLMessage.INFORM:
					case ACLMessage.NOT_UNDERSTOOD:
					case ACLMessage.FAILURE:
						state = REPLY_RECEIVED;
						return true;
					default:
						return false;
					}
				}
			}
			else {
				return false;
			}
		}


		public int getState() {
			return state;
		}

		public boolean isCompleted() {
			return (state == REPLY_RECEIVED);
		}
	} // End of inner class Session
}
