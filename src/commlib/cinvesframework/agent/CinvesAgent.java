package commlib.cinvesframework.agent;

import commlib.cinvesframework.belief.*;
import commlib.cinvesframework.intention.GoToRefugePlan;
import commlib.cinvesframework.intention.Intentions;
import commlib.cinvesframework.intention.ReportFirePlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.desire.Desires;
import commlib.components.AbstractCSAgent;
import commlib.message.RCRSCSMessage;
import implementation.agents.Quadrant;
import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import java.io.PrintWriter;
import java.util.*;


public abstract class CinvesAgent <E extends StandardEntity>  extends AbstractCFAgent<E>{
    public HashSet<EntityID> targetRoads;
    public HashSet<EntityID> informedCivilians;
    private boolean usingChannel;
    private int channel;
    private int listenChannels[];
    public int quadrant;

    protected Beliefs beliefs;
    protected Desires desires;
    protected Intentions intentions;

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

    private static PrintWriter logFile;
    private boolean logMessages = true;


    protected CinvesAgent(){

        if(logMessages){
            try {
                if(logFile == null) {
                    logFile = new PrintWriter("message_log.txt");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        targetRoads=new HashSet<>();
        informedCivilians=new HashSet<>();
        reportFirePlan = new ReportFirePlan(this);
        refugePlan = new GoToRefugePlan(this);

        beliefs = new Beliefs(this);
        desires = new Desires(this);
        intentions = new Intentions(this);

        channel = 1;
        //listenChannels = new int[]{1};
        this.subscribedChannels = new int[]{1};
        setAclMessages(new ArrayList<>());

        setQueuedMessages(new HashMap<>());
        conversationId = 1;


    }

    protected CinvesAgent(int channel,int listenChannels[]){

        if(logMessages){
            try {
                if(logFile == null) {
                    logFile = new PrintWriter("message_log.txt");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        targetRoads=new HashSet<>();
        informedCivilians=new HashSet<>();
        reportFirePlan = new ReportFirePlan(this);
        refugePlan = new GoToRefugePlan(this);

        beliefs = new Beliefs(this);
        desires = new Desires(this);
        intentions = new Intentions(this);

        this.channel = channel;
        //this.listenChannels = listenChannels;
        this.subscribedChannels = listenChannels;

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

        if(logMessages){
            logFile.println(message.toString());
        }

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
    public void sendExtinguish(int time, EntityID target, int water) {
        /* By default, the Core prevents any non-fireBrigade from executing this action, so we make no extra checks here. */
        super.sendExtinguish(time, target, water);
    }

    @Override
    public void sendClear(int time, EntityID target) {
        super.sendClear(time, target);
    }

    @Override
    public void sendLoad(int time, EntityID target) {
        super.sendLoad(time, target);
    }

    @Override
    public void sendRescue(int time, EntityID target) {
        super.sendRescue(time, target);
    }

    @Override
    public void sendUnload(int time) {
        super.sendUnload(time);
    }

    public void addACLMessageToQueue(int conversationId, ACLMessage message){
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

        Belief quadrantBelief = new Belief();

        getQuadrant();

        quadrantBelief.setDataInt(quadrant);

        beliefs.loadDefaultBeliefs();

        distance = config.getIntValue("clear.repair.distance");

        beliefs.addBelief(BeliefType.REPAIR_DISTANCE,new LocationBelief(new EntityID(distance)));

        beliefs.addBelief(BeliefType.QUADRANT,quadrantBelief);


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
                sendSubscribe(time, this.subscribedChannels);
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

            if(time >= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {

                if (canHelp()) {
                    onFullHealthBehaviour(time, changed, heard);
                } else if (canMove()) {
                    onRegularHealthBehaviour(time, changed, heard);
                } else {
                    onLowHealthBehaviour(time, changed, heard);
                }
            }

        }else{

            if(time >= config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
                doCentreAction(time, changed, heard);
            }

        }


    }

    /**
     * get the quadrant of the agent
     */
    public void getQuadrant(){

        Pair<Integer,Integer> point = me().getLocation(getWorldModel());

        //StandardEntity entity=getWorldModel().getEntity(this.getID());

        int px=point.first();//Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:x").getValue().toString());
        int py=point.second();//Integer.parseInt(entity.getProperty("urn:rescuecore2.standard:property:y").getValue().toString());


        System.out.println(px+"      "+py);
        quadrant= Quadrant.getQuadrant(getWorldModel(),px,py);

    }

    /**
     * Agent Methods to override for a custom behaviour
     */

    public void onBlockadeDetected(int time,EntityID entityID){
        //sendClear(time,entityID);
    }

    protected void doCentreAction(int time, ChangeSet changed, Collection<Command> heard){

    }

    protected void onFullHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

    }

    protected void onRegularHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

       // System.out.println("Puedo moverme pero no ayudar =(");

        refugePlan.setTime(time);
        refugePlan.setVolunteer(false);
        refugePlan.createPlan(getBeliefs(),getDesires(),intentions);

    }

    protected void onLowHealthBehaviour(int time, ChangeSet changed, Collection<Command> heard){

        //System.out.println("Help me!");
        sendRest(time);

    }


    protected void senseBuildingsOnFire(){
        reportFirePlan.createPlan(getBeliefs(),getDesires(),intentions);
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

    public Intentions getIntentions() {
        return intentions;
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

    public void setQuadrant(int quadrant){
        this.quadrant = quadrant;
    }

    public int getCurrentQuadrant(){
        return this.quadrant;
    }


}
