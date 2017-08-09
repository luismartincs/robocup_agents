package commlib.cinvesframework.agent;

import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desires;
import commlib.components.AbstractCSAgent;
import commlib.message.RCRSCSMessage;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public abstract class CinvesAgent <E extends StandardEntity>  extends AbstractCSAgent<E>{

    private boolean usingChannel;
    private int channel;
    private int listenChannels[];

    private Beliefs beliefs;
    private Desires desires;

    private boolean filterACLMessages = true;
    protected ArrayList<ACLMessage> aclMessages;

    /*
    * Para ContractNet
    */

    private int conversationId;
    protected HashMap<Integer,ACLMessage> queuedMessages;


    protected CinvesAgent(){

        beliefs = new Beliefs(this);
        desires = new Desires(this);

        channel = 1;
        listenChannels = new int[]{1};

        aclMessages = new ArrayList<>();

        queuedMessages = new HashMap<>();
        conversationId = 1;

    }

    protected CinvesAgent(int channel,int listenChannels[]){

        beliefs = new Beliefs(this);
        this.channel = channel;
        this.listenChannels = listenChannels;

        aclMessages = new ArrayList<>();

        queuedMessages = new HashMap<>();
        conversationId = 1;
    }

    /**
     * Make getModel public
     */

    public StandardWorldModel getWorldModel(){
        return this.model;
    }

    public E me(){
        return super.me();
    }

    public boolean isUsingChannel(){
        return usingChannel;
    }

    public int nextConversationId(){
        return ++conversationId;
    }


    @Override
    protected void postConnect(){

        super.postConnect();

        beliefs.loadDefaultBeliefs();

        //----

        model.indexClass(StandardEntityURN.CIVILIAN,
                StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.REFUGE,
                StandardEntityURN.BUILDING); //<-- Mejora el desempeño de acceso a las clases mas utilizadas

        boolean speakComm = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(ChannelCommunicationModel.class.getName());

        int numChannels = this.config.getIntValue("comms.channels.count");
        if ((speakComm) && (numChannels > 1)) {
            this.usingChannel = true;
        } else {
            this.usingChannel = false;
        }
    }

    @Override
    protected void thinking(int time, ChangeSet changed, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            if (isUsingChannel()) {
                setMessageChannel(channel);
                sendSubscribe(time, listenChannels);
            }
        }

        if(isFilterACLMessages()){

            this.aclMessages.clear();

            for(RCRSCSMessage msg : this.receivedMessageList){
                if(msg instanceof ACLMessage){
                    ACLMessage aclMessage = (ACLMessage)msg;
                    if(aclMessage.getSender() != getID().getValue()) { //Evita que recibas tus propios mensajes

                        if(aclMessage.getReceiver() == getID().getValue() || aclMessage.getReceiver() == 0) { //Si es para mi o para todos

                            aclMessages.add((ACLMessage) msg);

                        }
                    }
                }
            }
        }
    }

    public Beliefs getBeliefs() {
        return beliefs;
    }

    public Desires getDesires(){
        return desires;
    }

    public boolean isFilterACLMessages() {
        return filterACLMessages;
    }

    public void setFilterACLMessages(boolean filterACLMessages) {
        this.filterACLMessages = filterACLMessages;
    }
}
