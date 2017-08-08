import commlib.bdi.messages.ACLMessage;
import commlib.bdi.messages.ACLPerformative;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;

public class ContractNet {

    /**
     * the first part is sending the request message
     */
    ArrayList<IDandTime> Request;
    ArrayList<EntityID> ack;
    ArrayList<EntityID> Response;
    EntityID entity;
    int serviceID;


    //error in the communication probability
    double error=0;

    /**
     * Constructor
     * @param entity
     */
    public ContractNet(EntityID entity, int serviceID){
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
        ACLMessage message=new ACLMessage(time,this.entity, ACLPerformative.REQUEST,entity);//falta el service id
            if (!existRequest(entity)) {
                Request.add(new IDandTime(entity));
                return message;
            } else {
                addTime();
                removeRequests(7);
                return null;
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
        ACLMessage message=new ACLMessage(time,this.entity, ACLPerformative.INFORM,entity);
            if(existACK(entity)){
                removeACK(entity);
                Request.add(new IDandTime(entity));
                return message;
            }
            else{
                return null;
            }
    }

    public boolean existRequest(EntityID ids){

        for(IDandTime idt:Request){
            if(idt.getId().equals(ids)){
                return true;
            }
        }
        return false;
    }

    public void removeRequest(EntityID id){

        for(int i=0;i<Request.size();i++){
            if(Request.get(i).getId().equals(id)){
                Request.remove(i);
                break;
            }
        }
    }

    public void addTime(){
        for(IDandTime idt:Request){
            idt.addTime();
        }
    }

    public void removeRequests(int limit){
        ArrayList <IDandTime> newRequest=new ArrayList<>();
        for(IDandTime idt:Request){
            if(idt.getTime()<=limit){
                newRequest.add(idt);
            }
        }
        Request.clear();
        Request.addAll(newRequest);
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
        if(msg.getTargetAgentID().equals(entity)&&msg.getPerformative().equals(ACLPerformative.REQUEST)){
            if(!existRequest(msg.getEntityID())){
                Request.add(new IDandTime(msg.getEntityID()));
                ack.add(msg.getEntityID());
                System.out.println("se añadió el request de "+msg.getTargetAgentID()+" por "+entity);
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
            return new ACLMessage(time, this.entity, ACLPerformative.INFORM,id);
        }
        return null;
    }

    /**
     * add the response to the list
     * @param msg
     */
    public void addResponse(ACLMessage msg){
        if(msg.getTargetAgentID().equals(entity)&&msg.getPerformative().equals(ACLPerformative.INFORM)){
            Response.add(msg.getEntityID());
        }
    }
}


class IDandTime{
    EntityID  id;
    int time=0;

    public IDandTime(EntityID id) {
        this.id = id;
    }

    public EntityID getId() {
        return id;
    }

    public void setId(EntityID id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void addTime(){
        this.time++;
    }
}
