package commlib.cinvesframework.agent;

import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.intention.ReportFirePlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.desire.Desires;
import commlib.components.AbstractCSAgent;
import commlib.message.RCRSCSMessage;
import implementation.agents.Quadrant;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public abstract class CinvesAgent <E extends StandardEntity>  extends AbstractCSAgent<E>{

    private boolean usingChannel;
    private int channel;
    private int listenChannels[];
    public int quadrant;

    private Beliefs beliefs;
    private Desires desires;

    private boolean filterACLMessages = true;
    private ArrayList<ACLMessage> aclMessages;

    private boolean isHuman = false;

    /**
    * Para ContractNet
    */

    private int conversationId;
    private HashMap<Integer,ACLMessage> queuedMessages;

    private ReportFirePlan reportFirePlan;
    private GoToRefugePlan refugePlan;

    private int distance = 0;

    /**
     * Default Health Info
     */

    private static final int MAX_HEALTH = 10000;
    private static final int MAX_STAMINA = 10000;
    private static final int MAX_DAMAGE = 10000;

    private int canHelpHealthRequired = (int)(0.70*MAX_HEALTH);
    private int canHelpStaminaRequired = (int)(0.70*MAX_STAMINA);
    private int canHelpDamageRequired = (int)(0.50*MAX_DAMAGE);

    private int canMoveHealthRequired = (int)(0.40*MAX_HEALTH);
    private int canMoveStaminaRequired = (int)(0.70*MAX_STAMINA);
    private int canMoveDamageRequired = (int)(0.60*MAX_DAMAGE);


    protected CinvesAgent(){

        reportFirePlan = new ReportFirePlan(this);
        refugePlan = new GoToRefugePlan(this);

        beliefs = new Beliefs(this);
        desires = new Desires(this);

        channel = 1;
        listenChannels = new int[]{1};

        setAclMessages(new ArrayList<>());

        setQueuedMessages(new HashMap<>());
        conversationId = 1;



    }

    protected CinvesAgent(int channel,int listenChannels[]){

        reportFirePlan = new ReportFirePlan(this);
        refugePlan = new GoToRefugePlan(this);

        beliefs = new Beliefs(this);
        this.channel = channel;
        this.listenChannels = listenChannels;

        setAclMessages(new ArrayList<>());

        setQueuedMessages(new HashMap<>());
        conversationId = 1;

    }



    /**
     * Make some methods public
     */

    @Override
    public StandardEntity location() {
        return super.location();
    }

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
        return conversationId++;
    }

    public void addACLMessage(ACLMessage message){
        addMessage(message);
    }

    @Override
    public void sendMove(int time, List<EntityID> path) {
        super.sendMove(time, path);
    }

    @Override
    public void sendRest(int time) {
        super.sendRest(time);
    }

    @Override
    public void sendClear(int time, EntityID target) {
        super.sendClear(time, target);
    }

    public void addACLMessageToQueue(int conversationId,ACLMessage message){
        getQueuedMessages().put(conversationId,message);
    }

    public ACLMessage getACLMessageFromQueue(int conversationId){
        return getQueuedMessages().get(conversationId);
    }

    public ACLMessage removeACLMessageFromQueue(int conversationId){
        return getQueuedMessages().remove(conversationId);
    }

    @Override
    protected void postConnect(){

        super.postConnect();
        getQuadrant();
        beliefs.loadDefaultBeliefs();

        distance = config.getIntValue("clear.repair.distance");

        beliefs.addBelief(BeliefType.REPAIR_DISTANCE,new LocationBelief(new EntityID(distance)));

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

            this.getAclMessages().clear();

            for(RCRSCSMessage msg : this.receivedMessageList){
                if(msg instanceof ACLMessage){

                    ACLMessage aclMessage = (ACLMessage)msg;
                    if(aclMessage.getSender() != getID().getValue()) { //Evita que recibas tus propios mensajes

                        if(aclMessage.getReceiver() == getID().getValue() || aclMessage.getReceiver() == 0) { //Si es para mi o para todos

                            getAclMessages().add((ACLMessage) msg);

                        }
                    }
                }
            }
        }






        /**
         * Default General Human Behaviour
         */

        if(me() instanceof Human){

            senseBuildingsOnFire();

            if(canHelp()){
                onFullHealthBehaviour(time,changed,heard);
            }else if(canMove()){
                onRegularHealthBehaviour(time,changed,heard);;
            }else {
                onLowHealthBehaviour(time,changed,heard);
            }

        }


    }

    /**
     * get the quadrant of the agent
     */
    public void getQuadrant(){
        StandardEntity entity=getWorldModel().getEntity(this.getID());
        int px=Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:x").getValue().toString());
        int py=Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:y").getValue().toString());
        System.out.println(px+"      "+py);
        quadrant= Quadrant.getQuadrant(px,py);
    }

    /**
     * Agent Methods to override for a custom behaviour
     */

    public void onBlockadeDetected(int time,EntityID entityID){
        //sendClear(time,entityID);
    }

    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

    }

    protected void onRegularHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

        System.out.println("Puedo moverme pero no ayudar =(");
        refugePlan.setTime(time);
        refugePlan.setVolunteer(false);
        refugePlan.createPlan(getBeliefs(),getDesires());

    }

    protected void onLowHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

        System.out.println("Help me bitches!");
        sendRest(time);

    }


    protected void senseBuildingsOnFire(){
        reportFirePlan.createPlan(getBeliefs(),getDesires());
    }


    protected boolean canHelp(){

        Human human = ((Human)me());

        if(human.getHP() >= canHelpHealthRequired && human.getDamage() <= canHelpDamageRequired){
            return true;
        }
        return false;
    }

    protected boolean canMove(){

        Human human = ((Human)me());

        if(human.getHP() >= canMoveHealthRequired && human.getDamage() <= canMoveDamageRequired){
            return true;
        }
        return false;
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

    public HashMap<Integer, ACLMessage> getQueuedMessages() {
        return queuedMessages;
    }

    public void setQueuedMessages(HashMap<Integer, ACLMessage> queuedMessages) {
        this.queuedMessages = queuedMessages;
    }

    public ArrayList<ACLMessage> getAclMessages() {
        return this.aclMessages;
    }

    public void setAclMessages(ArrayList<ACLMessage> aclMessages) {
        this.aclMessages = aclMessages;
    }
}
