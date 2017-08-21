package implementation.agents.policeforce;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.Belief;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desires;
import commlib.cinvesframework.intention.AbstractPlan;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.interaction.ContractNet;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.messages.ACLPerformative;
import implementation.agents.ActionConstants;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;

public class LeaderElectionPlan extends AbstractPlan{

    private SearchPlan sp;

    private ArrayList<Integer> knownEntities;
    private ArrayList<int[]> nextQuadrantLeaders;

    private int leaderID = 0;

    private int time;
    private int lastConversationID = 0;
    private boolean imLeader = true;
    private boolean leaderElected = false;


    public LeaderElectionPlan(CinvesAgent agent){

        super(agent);

        knownEntities = new ArrayList<>();
        nextQuadrantLeaders = new ArrayList<>();

        leaderID = agent.getID().getValue();

        sp = new SearchPlan(getAgent());
    }

    public void setTime(int time) {
        this.time = time;
    }

    private void sendPropose(int receiver,int conversationId,int action){

        ACLMessage propose = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.PROPOSE,
                new EntityID(receiver),
                conversationId,
                action,
                getAgent().getID().getValue());

        getAgent().addACLMessage(propose);
    }



    private void sendReject(int receiver,int conversationId,int action){

        ACLMessage propose = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.REJECT_PROPOSAL,
                new EntityID(receiver),
                conversationId,
                action,0);

        getAgent().addACLMessage(propose);
    }


    private void sendAccept(int receiver,int conversationId,int action){

        ACLMessage propose = new ACLMessage(time,
                getAgent().getID(),
                ACLPerformative.ACCEPT_PROPOSAL,
                new EntityID(receiver),
                conversationId,
                action,0);

        getAgent().addACLMessage(propose);
    }

    private void sendInform(int receiver,int conversationId){

        EntityID position = ((Human) getAgent().me()).getPosition();

        ACLMessage propose = new ACLMessage(time,getAgent().getID(),
                ACLPerformative.INFORM,
                new EntityID(receiver),
                conversationId,
                ActionConstants.LEADER_ELECTION,
                getAgent().getCurrentQuadrant());
                //position.getValue());

        getAgent().addACLMessage(propose);
    }

    private void sendCFP(int quadrant){

        int conversationId = getAgent().nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time, getAgent().getID(), ACLPerformative.CFP, new EntityID(0), conversationId, ActionConstants.LEADER_ELECTION, quadrant);

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);

        getAgent().addACLMessage(leaderCFP);


    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

        if(leaderElected)return new Boolean(true);

        stateControl(beliefs,desires);


       if(getAgent().getQueuedMessages().size() == 0){

            if(!leaderElected){

                int quadrant = beliefs.getBelief(BeliefType.QUADRANT).getDataInt();

                sendCFP(quadrant);

            }

       }else {

          ACLMessage lastMessage = getAgent().getACLMessageFromQueue(lastConversationID);

          if(lastMessage != null) {

              if (lastMessage.getPerformative() == ACLPerformative.REJECT_PROPOSAL || lastMessage.getPerformative() == ACLPerformative.ACCEPT_PROPOSAL) {

                  if(imLeader()){

                    leaderElected = true;

                    leaderID = getAgent().getID().getValue();

                    Belief lb = new Belief();
                    lb.setDataBoolean(true);
                    lb.setDataInt(leaderID);

                    beliefs.addBelief(BeliefType.IM_LEADER,lb);

                    sendInform(0,getAgent().nextConversationId()); //Notifica a todos quien es el lider la escuadra X

                    /*
                    for(Integer ent:knownEntities){
                        sendInform(ent,getAgent().nextConversationId());
                    }*/


                 }
              }

              getAgent().getQueuedMessages().clear();

          }

       }

        return null;
    }


    /**
     * ContractNet se revisan los mensajes de solicitud antes de hacer algun movimiento
     */

    private void stateControl(Beliefs beliefs, Desires desires){

        int quadrant = beliefs.getBelief(BeliefType.QUADRANT).getDataInt();

        ArrayList<ACLMessage> aclMessages = getAgent().getAclMessages();

        for(ACLMessage msg: aclMessages){

            switch (msg.getPerformative()){

                case CFP:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION && msg.getExtra(0) == quadrant){

                        if(!knownEntities.contains(msg.getSender())){
                            knownEntities.add(msg.getSender());
                        }

                        sendPropose(msg.getSender(),msg.getConversationId(),ActionConstants.LEADER_ELECTION);
                    }

                    break;

                case PROPOSE:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){

                        ACLMessage previous = getAgent().getACLMessageFromQueue(msg.getConversationId());

                        if(previous != null) {

                            if(ContractNet.isValidState(previous.getPerformative(),msg.getPerformative())){

                                int proposedValue = msg.getExtra(0);

                                if (proposedValue < getAgent().getID().getValue()){
                                    sendReject(msg.getSender(),msg.getConversationId(),ActionConstants.LEADER_ELECTION);
                                }else {
                                    sendAccept(msg.getSender(), msg.getConversationId(),ActionConstants.LEADER_ELECTION);
                                }
                            }

                        }

                    }

                    break;

                case REJECT_PROPOSAL:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){

                        ACLMessage previous = getAgent().getACLMessageFromQueue(msg.getConversationId());

                        if(previous != null) {
                            if(ContractNet.isValidState(previous.getPerformative(),msg.getPerformative())){
                                imLeader = imLeader() & false;
                                lastConversationID = msg.getConversationId();
                                getAgent().getQueuedMessages().put(msg.getConversationId(),msg);
                            }
                        }
                    }

                    break;

                case ACCEPT_PROPOSAL:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){
                        ACLMessage previous = getAgent().getACLMessageFromQueue(msg.getConversationId());

                        if(previous != null) {
                            if(ContractNet.isValidState(previous.getPerformative(),msg.getPerformative())){
                                lastConversationID = msg.getConversationId();
                                getAgent().getQueuedMessages().put(msg.getConversationId(),msg);
                            }
                        }
                    }

                    break;

                case INFORM:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){

                        if(msg.getExtra(0) == getAgent().getCurrentQuadrant()){
                            leaderElected = true;

                            leaderID = msg.getSender();

                            getAgent().getQueuedMessages().clear();

                            Belief lb = new Belief();
                            lb.setDataBoolean(false);
                            lb.setDataInt(leaderID);

                            beliefs.addBelief(BeliefType.IM_LEADER,lb);
                        }else {
                            getNextQuadrantLeaders().add(new int[]{msg.getExtra(0),msg.getSender()});
                        }



                    }

                    break;
            }


        }

    }

    public int getLeaderID() {
        return leaderID;
    }

    public boolean imLeader() {
        return imLeader;
    }

    public ArrayList<int[]> getNextQuadrantLeaders() {
        return nextQuadrantLeaders;
    }
}
