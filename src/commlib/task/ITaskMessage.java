package commlib.task;

import rescuecore2.worldmodel.EntityID;

public interface ITaskMessage {
	/**
	 * Return EntityID of agent that have to execute this task.
	 * 
	 * @return 
	 *         EntityID of agent
	 */
	public EntityID getAssignedAgentID();
}
