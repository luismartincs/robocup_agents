package commlib.components;

import java.util.Collection;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.ChangeSet;

/**
 * The AbstractCSAgent show the Agent using this communication library.
 * 
 * @author takefumi
 * 
 * @param <E>
 */
public abstract class AbstractCSAgent<E extends StandardEntity> extends
		RCRSCSAgent<E> {
	/**
	 * Constructor
	 */
	public AbstractCSAgent() {
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
