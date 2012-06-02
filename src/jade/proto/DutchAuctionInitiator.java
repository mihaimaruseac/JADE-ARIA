package jade.proto;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


import java.util.Vector;

public class DutchAuctionInitiator extends Initiator {


    private static final String HANDLE_PROPOSE = "Handle-propose";
    private static final String HANDLE_ALL_RESPONSES = "Handle-all-responses";

    private static final int DROP_PRICE = 42;
    private static final int SOLD = 43;

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
        registerDefaultTransition(HANDLE_PROPOSE, CHECK_SESSIONS);
        registerDefaultTransition(CHECK_SESSIONS, HANDLE_ALL_RESPONSES);
        registerTransition(HANDLE_ALL_RESPONSES, SEND_INITIATIONS, DROP_PRICE);
        registerTransition(HANDLE_ALL_RESPONSES, DUMMY_FINAL, SOLD);

        // Create and register the states specific to the protocol
        Behaviour b = null;
        // HANDLE_PROPOSE
        b = new OneShotBehaviour(myAgent) {
            private static final long     serialVersionUID = 3487495895819003L;

            public void action() {
                Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
                ACLMessage propose = (ACLMessage) getDataStore().get(REPLY_K);
                handlePropose(propose, acceptances);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_PROPOSE);

        // HANDLE_ALL_RESPONSES
        b = new OneShotBehaviour(myAgent) {

            public void action() {
                Vector responses = (Vector) getDataStore().get(ALL_RESPONSES_KEY);
                Vector acceptances = (Vector) getDataStore().get(ALL_ACCEPTANCES_KEY);
                handleAllResponses(responses, acceptances);
            }
        };
        b.setDataStore(getDataStore());
        registerState(b, HANDLE_ALL_RESPONSES);
	}

	@Override
	protected Vector prepareInitiations(ACLMessage initiation) {
        return prepareCfps(initiation);
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

    /**
     * This method is called every time a <code>propose</code>
     * message is received, which is not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event.
     * @param propose the received propose message
     * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
     * This list can be filled step by step redefining this method, or
     * it can be filled at once
     * redefining the handleAllResponses method.
     **/
    protected void handlePropose(ACLMessage propose, Vector acceptances) {
    }

    /**
     * This method is called when all the responses have been
     * collected or when the timeout is expired.
     * The used timeout is the minimum value of the slot <code>replyBy</code>
     * of all the sent messages.
     * By response message we intend here all the <code>propose, not-understood,
     * refuse</code> received messages, which are not
     * not out-of-sequence according
     * to the protocol rules.
     * This default implementation does nothing; programmers might
     * wish to override the method in case they need to react to this event
     * by analysing all the messages in just one call.
     * @param responses the Vector of ACLMessage objects that have been received
     * @param acceptances the list of ACCEPT/REJECT_PROPOSAL to be sent back.
     * This list can be filled at once redefining this method, or step by step
     * redefining the handlePropose method.
     **/
    protected void handleAllResponses(Vector responses, Vector acceptances) {
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
        Vector v = new Vector(1);
        v.addElement(cfp);
        return v;
    }
}
