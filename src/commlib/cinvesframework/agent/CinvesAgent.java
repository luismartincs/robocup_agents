package commlib.cinvesframework.agent;

import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.desire.Desires;
import commlib.components.AbstractCSAgent;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;


public abstract class CinvesAgent <E extends StandardEntity>  extends AbstractCSAgent<E>{

    private boolean usingChannel;
    private int channel;
    private int listenChannels[];

    private Beliefs beliefs;
    private Desires desires;


    protected CinvesAgent(){

        beliefs = new Beliefs(this);
        desires = new Desires(this);

        channel = 1;
        listenChannels = new int[]{1};
    }

    protected CinvesAgent(int channel,int listenChannels[]){

        beliefs = new Beliefs(this);
        this.channel = channel;
        this.listenChannels = listenChannels;
    }

    /**
     * Make getModel public
     */

    public StandardWorldModel getWorldModel(){
        return this.model;
    }

    public boolean isUsingChannel(){
        return usingChannel;
    }


    @Override
    protected void postConnect(){

        super.postConnect();

        beliefs.loadDefaultBeliefs();

        //----

        model.indexClass(StandardEntityURN.ROAD); //<-- Investigar para que sirve

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
    }

    public Beliefs getBeliefs() {
        return beliefs;
    }

    public Desires getDesires(){
        return desires;
    }

}
