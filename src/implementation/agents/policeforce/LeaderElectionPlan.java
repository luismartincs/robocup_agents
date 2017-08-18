package implementation.agents.policeforce;

import commlib.cinvesframework.agent.CinvesAgent;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
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
import java.util.List;

public class LeaderElectionPlan extends AbstractPlan{

    private SearchPlan sp;

    private ArrayList<Integer> knownEntities;
    private int leaderID = 0;

    private int time;
    private int lastConversationID = 0;
    private boolean imLeader = true;
    private boolean leaderElected = false;

    public LeaderElectionPlan(CinvesAgent agent){
        super(agent);

        knownEntities = new ArrayList<>();
        leaderID = agent.getID().getValue();

        sp = new SearchPlan(getAgent());
    }

    public void setTime(int time) {
        this.time = time;
    }

    private void sendPropose(int receiver,int conversationId,int action){

        System.out.println("send propose");

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
                position.getValue());

        getAgent().addACLMessage(propose);
    }

    private void sendCFP(int quadrant){

        System.out.println("send cfp");

        int conversationId = getAgent().nextConversationId();

        ACLMessage leaderCFP = new ACLMessage(time, getAgent().getID(), ACLPerformative.CFP, new EntityID(0), conversationId, ActionConstants.LEADER_ELECTION, quadrant);

        getAgent().addACLMessageToQueue(conversationId, leaderCFP);

        getAgent().addACLMessage(leaderCFP);


    }

    @Override
    public Object createPlan(Beliefs beliefs, Desires desires) {

        /**
         * ContractNet se revisan los mensajes de solicitud antes de hacer algun movimiento
         */

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
                        System.out.println("reject "+previous.getPerformative()+" - "+msg.getPerformative());

                        if(previous != null) {
                            if(ContractNet.isValidState(previous.getPerformative(),msg.getPerformative())){
                                imLeader &= false;
                                lastConversationID = msg.getConversationId();
                                getAgent().getQueuedMessages().put(msg.getConversationId(),msg);
                            }
                        }
                        System.out.println(getAgent().getQueuedMessages().size());

                    }

                    break;

                case ACCEPT_PROPOSAL:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){
                        ACLMessage previous = getAgent().getACLMessageFromQueue(msg.getConversationId());

                        if(previous != null) {
                            System.out.println("accept "+previous.getPerformative()+" - "+msg.getPerformative());
                            if(ContractNet.isValidState(previous.getPerformative(),msg.getPerformative())){
                                imLeader&=true;
                                lastConversationID = msg.getConversationId();
                                getAgent().getQueuedMessages().put(msg.getConversationId(),msg);
                            }
                        }

                        System.out.println(getAgent().getQueuedMessages().size());
                    }

                    break;

                case INFORM:

                    if (msg.getContent() == ActionConstants.LEADER_ELECTION){

                        leaderElected = true;

                        getAgent().getQueuedMessages().clear();

                        desires.addDesire(DesireType.GOAL_LOCATION, new Desire(new EntityID(msg.getExtra(0))));

                    }

                    break;
            }


        }





       if(getAgent().getQueuedMessages().size() == 0){

            if(!leaderElected){
                sendCFP(quadrant);
            }else{
                System.out.println("Me voy a mover con el lider");

                Desire goalLocation = desires.getDesire(DesireType.GOAL_LOCATION);
                EntityID position = ((Human) getAgent().me()).getPosition();

                if (goalLocation.getEntityID().getValue() == position.getValue()) {
                    getAgent().sendRest(time);
                }else {

                    List<EntityID> path = sp.createPlan(beliefs, desires);
                    getAgent().sendMove(time, path);
                    return path;
                }

            }

       }else {

          ACLMessage lastMessage = getAgent().getACLMessageFromQueue(lastConversationID);

          if(lastMessage != null) {

              if (lastMessage.getPerformative() == ACLPerformative.REJECT_PROPOSAL || lastMessage.getPerformative() == ACLPerformative.ACCEPT_PROPOSAL) {
                 if(imLeader){
                    for(Integer ent:knownEntities){
                        sendInform(ent,getAgent().nextConversationId());
                    }
                 }
              }

              getAgent().getQueuedMessages().clear();

          }

       }

        return null;
    }
}
