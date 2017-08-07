import commlib.bdi.messages.ACLMessage;
import commlib.bdi.messages.ACLPerformative;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;

public class ContractNet {

    /**
     * the first part is sending the request message
     */
    ArrayList<EntityID> Request;
    ArrayList<EntityID> ack;
    ArrayList<EntityID> Response;
    Entity entity;


    //error in the communication probability
    double error=0.1;

    /**
     * Constructor
     * @param entity
     */
    public ContractNet(Entity entity){
        this.entity=entity;
        Request=new ArrayList<>();
        ack=new ArrayList<>();
        Response=new ArrayList<>();
    }

    /**
     * First, a message when the inform performative is generated
     * @param time
     * @param entity
     * @return
     */
    public ACLMessage msgRequest(int time, EntityID entity){
        ACLMessage message=new ACLMessage(time,this.entity.getID(), ACLPerformative.REQUEST,entity);
        if(noError()) {
            if (existRequest(entity)) {
                return null;
            } else {
                Request.add(entity);
                return message;
            }
        }
        else{
            return null;
        }
    }

    /**
     * second, the acks are received
     * @param msg
     * @return
     */
    public boolean addACK(ACLMessage msg){
        if(existRequest(msg.getAgentID())) {
            if (msg.getPerformative().equals(ACLPerformative.CONFIRM)) {
                removeRequest(msg.getAgentID());
                ack.add(msg.getAgentID());
                return true;
            }
        }
        return false;
    }

    /**
     * third, a inform message is generated
     * @param time
     * @param entity
     * @return
     */
    public ACLMessage msgInform(int time, EntityID entity){
        ACLMessage message=new ACLMessage(time,this.entity.getID(), ACLPerformative.INFORM,entity);
        if(noError()){
            if(existACK(entity)){
                removeACK(entity);
                Request.add(entity);
                return message;
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

    public boolean existRequest(EntityID ids){
        return Request.indexOf(ids)!=-1;
    }

    public void removeRequest(EntityID id){
        Request.remove(Request.indexOf(id));
    }

    public boolean existACK(EntityID id){
        return ack.indexOf(id)!=-1;
    }

    public void removeACK(EntityID id){
        ack.remove(ack.indexOf(id));
    }

    /**
     * return true if the random number is higher than the error probability
     * @return
     */
    public boolean noError(){
        double rnd=Math.random();
        if(rnd >= error){
            return true;
        }
        else{
            return false;
        }
    }
}
