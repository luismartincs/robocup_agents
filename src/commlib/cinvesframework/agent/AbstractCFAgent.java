package commlib.cinvesframework.agent;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.ChangeSet;

import java.util.Collection;

public abstract class AbstractCFAgent <E extends StandardEntity> extends BDIBaseAgent<E> {
    /**
     * Constructor
     */
    public AbstractCFAgent() {
        super();
    }

    @Override
    /**
     * Represent each step thinking.<br>
     * (1.Receive message,2.think,3.send new messages)
     */
    protected final void think(int time, ChangeSet changed,
                               Collection<Command> heard) {
        super.receiveMessage(heard);
        this.thinking(time, changed, heard);
        super.sendMessage(time);
    }

    /**
     * Development part that have to be created by users.<br>
     * At this point,we already receive messages from other Agents. Received
     * messages are in 'receivedMessageList'.
     */
    protected abstract void thinking(int time, ChangeSet changed,
                                     Collection<Command> heard);
}
