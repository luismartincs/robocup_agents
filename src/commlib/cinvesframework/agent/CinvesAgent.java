package commlib.cinvesframework.agent;

import com.sun.org.apache.xpath.internal.SourceTree;
import commlib.cinvesframework.belief.BeliefType;
import commlib.cinvesframework.belief.EntityListBelief;
import commlib.cinvesframework.belief.PathBelief;
import commlib.cinvesframework.desire.Desire;
import commlib.cinvesframework.desire.DesireType;
import commlib.cinvesframework.intention.ReportFirePlan;
import commlib.cinvesframework.intention.SearchPlan;
import commlib.cinvesframework.messages.ACLMessage;
import commlib.cinvesframework.belief.Beliefs;
import commlib.cinvesframework.desire.Desires;
import commlib.components.AbstractCSAgent;
import commlib.message.RCRSCSMessage;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.messages.Command;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
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

    private Beliefs beliefs;
    private Desires desires;

    private boolean filterACLMessages = true;
    protected ArrayList<ACLMessage> aclMessages;

    private boolean isHuman = false;

    /**
    * Para ContractNet
    */

    private int conversationId;
    protected HashMap<Integer,ACLMessage> queuedMessages;

    private ReportFirePlan reportFirePlan;

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

        beliefs = new Beliefs(this);
        desires = new Desires(this);

        channel = 1;
        listenChannels = new int[]{1};

        aclMessages = new ArrayList<>();

        queuedMessages = new HashMap<>();
        conversationId = 1;

    }

    protected CinvesAgent(int channel,int listenChannels[]){

        reportFirePlan = new ReportFirePlan(this);

        beliefs = new Beliefs(this);
        this.channel = channel;
        this.listenChannels = listenChannels;

        aclMessages = new ArrayList<>();

        queuedMessages = new HashMap<>();
        conversationId = 1;
    }

    /**
     * Make some methods public
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
        return conversationId++;
    }

    public void addACLMessage(ACLMessage message){
        addMessage(message);
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




        /**
         * Default General Human Behaviour
         */

        if(me() instanceof Human){

            senseBuildingsOnFire();
            clean(time);

            if(canHelp()){
                System.out.println("Puedo ayudar");
                clean(time);
            }else if(canMove()){
                System.out.println("Puedo moverme pero no ayudar =(");

            }else {
                System.out.println("Im sorry guys");
            }


        }


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

    protected void senseGoToClosestRefuge(int time){

        SearchPlan sp = new SearchPlan(this);

        Desire goalLocation = getDesires().getDesire(DesireType.GOAL_LOCATION);

        if(goalLocation == null){

            EntityListBelief refugesList = (EntityListBelief)getBeliefs().getBelief(BeliefType.REFUGE);
            ArrayList<StandardEntity> refuges = refugesList.getEntities();

            int minSteps = Integer.MAX_VALUE;
            int pathSize = 0;
            StandardEntity closestRefuge = null;
            List<EntityID> closestPath = null;
            List<EntityID> path = null;

            for (StandardEntity entity:refuges){

                Refuge refuge = (Refuge)entity;
                getDesires().addDesire(DesireType.GOAL_LOCATION,new Desire(refuge.getID()));
                path = sp.createPlan(getBeliefs(),getDesires());

                if(path != null) {

                    pathSize = path.size();

                    if (pathSize < minSteps) {
                        minSteps = pathSize;
                        closestRefuge = refuge;
                        closestPath = path;
                    }

                }

            }

            getDesires().addDesire(DesireType.GOAL_LOCATION,new Desire(closestRefuge.getID()));
            getBeliefs().addBelief(BeliefType.GOAL_PATH,new PathBelief(closestPath));

            sendMove(time,path);

        }else {

            EntityID position = ((Human)me()).getPosition();

            if(goalLocation.getEntityID().getValue() == position.getValue()){
                System.out.println("Llegue al refugio");
            }else {

                List<EntityID> path = sp.createPlan(getBeliefs(), getDesires());
                sendMove(time, path);

            }
        }

    }

    protected void clean(int time){
        Blockade target = getTargetBlockade();
        if(target != null){
            System.out.println("TARGET "+target);
            //sendSpeak(time, 1, ("Clearing " + target).getBytes());
            sendClear(time, target.getID());
            return;
        }else {
            senseGoToClosestRefuge(time);
        }
    }

    private Blockade getTargetBlockade(){
        int distance = config.getIntValue("clear.repair.distance");
        Area location = (Area) location();
        Blockade result = getTargetBlockade(location, distance);
        if(result != null){
            return result;
        }

        for(EntityID next : location.getNeighbours()){
            location = (Area) model.getEntity(next);
            result = getTargetBlockade(location, distance);
            if(result != null){
                return result;
            }
        }
        return null;
    }

    private Blockade getTargetBlockade(Area area, int maxDistance){
        // Logger.debug("Looking for nearest blockade in " + area);
        Human human = ((Human)me());

        if(area == null || !area.isBlockadesDefined()){
            // Logger.debug("Blockades undefined");
            return null;
        }
        List<EntityID> ids = area.getBlockades();
        // Find the first blockade that is in range.
        int x = human.getX();
        int y = human.getY();
        for(EntityID next : ids){
            Blockade b = (Blockade) model.getEntity(next);
            double d = findDistanceTo(b, x, y);
            // Logger.debug("Distance to " + b + " = " + d);
            if(maxDistance < 0 || d < maxDistance){
                // Logger.debug("In range");
                return b;
            }
        }
        // Logger.debug("No blockades in range");
        return null;
    }


    private int findDistanceTo(Blockade b, int x, int y){
        // Logger.debug("Finding distance to " + b + " from " + x + ", " + y);
        List<Line2D> lines = GeometryTools2D.pointsToLines(
                GeometryTools2D.vertexArrayToPoints(b.getApexes()), true);
        double best = Double.MAX_VALUE;
        Point2D origin = new Point2D(x, y);
        for(Line2D next : lines){
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(next, origin);
            double d = GeometryTools2D.getDistance(origin, closest);
            // Logger.debug("Next line: " + next + ", closest point: " + closest +
            // ", distance: " + d);
            if(d < best){
                best = d;
                // Logger.debug("New best distance");
            }

        }
        return (int) best;
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
