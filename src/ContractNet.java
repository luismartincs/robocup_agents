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
    int serviceID;


    //error in the communication probability
    double error=0;

    /**
     * Constructor
     * @param entity
     */
    public ContractNet(Entity entity, int serviceID){
        this.entity=entity;
        this.serviceID=serviceID;
        Request=new ArrayList<>();
        ack=new ArrayList<>();
        Response=new ArrayList<>();
    }

    /**--------------------CLIENT SIDE-----------------------------------------------------*/

    /**
     * First, a message when the inform performative is generated
     * @param time
     * @param entity
     * @return
     */
    public ACLMessage msgRequest(int time, EntityID entity){
        ACLMessage message=new ACLMessage(time,this.entity.getID(), ACLPerformative.REQUEST,entity);//falta el service id
            if (existRequest(entity)) {
                return null;
            } else {
                Request.add(entity);
                return message;
            }
    }


    /**
     * second, the acks are received
     * @param msg
     * @return
     */
    public boolean addACK(ACLMessage msg){
        if(existRequest(msg.getEntityID())) {
            if (msg.getPerformative().equals(ACLPerformative.CONFIRM)) {
                removeRequest(msg.getEntityID());
                ack.add(msg.getEntityID());
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
            if(existACK(entity)){
                removeACK(entity);
                Request.add(entity);
                return message;
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

    /**---------------------------------------SERVER SIDE-------------------------------------*/

    /**
     * the server receive the request
     * @param msg
     */
    public void addRequest(ACLMessage msg){
        if(msg.getTargetAgentID().equals(entity.getID())&&msg.getPerformative().equals(ACLPerformative.REQUEST)){
            if(!existRequest(msg.getEntityID())){
                Request.add(msg.getEntityID());
                ack.add(msg.getEntityID());
            }
        }
    }

    /**
     * generate ack one by one
     * @param time
     * @return
     */
    public ACLMessage generateACK(int time){
        if(ack.size()!=0){
            EntityID id=ack.get(0);
            removeRequest(id);
            removeACK(id);
            return new ACLMessage(time, this.entity.getID(), ACLPerformative.INFORM,id);
        }
        return null;
    }

    /**
     * add the response to the list
     * @param msg
     */
    public void addResponse(ACLMessage msg){
        if(msg.getTargetAgentID().equals(entity.getID())&&msg.getPerformative().equals(ACLPerformative.INFORM)){
            Response.add(msg.getEntityID());
        }
    }
}
