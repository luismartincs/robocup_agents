package commlib.cinvesframework.interaction;

import commlib.cinvesframework.messages.ACLPerformative;

import java.util.HashMap;

public class ContractNet {

    private static HashMap<ACLPerformative,ACLPerformative[]> states = new HashMap<>();

    static
    {
        states.put(ACLPerformative.CFP, new ACLPerformative[]{ACLPerformative.PROPOSE,ACLPerformative.REJECT_PROPOSAL,ACLPerformative.ACCEPT_PROPOSAL});
        states.put(ACLPerformative.PROPOSE, new ACLPerformative[]{ACLPerformative.REJECT_PROPOSAL,ACLPerformative.ACCEPT_PROPOSAL});
        states.put(ACLPerformative.REQUEST, new ACLPerformative[]{ACLPerformative.INFORM});
        states.put(ACLPerformative.REJECT_PROPOSAL, new ACLPerformative[]{ACLPerformative.REJECT_PROPOSAL,ACLPerformative.ACCEPT_PROPOSAL});
        states.put(ACLPerformative.ACCEPT_PROPOSAL, new ACLPerformative[]{ACLPerformative.REJECT_PROPOSAL,ACLPerformative.ACCEPT_PROPOSAL});


    }

    public static boolean isValidState(ACLPerformative current,ACLPerformative next){

        ACLPerformative validStates[] = states.get(current); // Los posibles estados proximos validos

        for(int i=0; i < validStates.length; i++){
            if (validStates[i] == next){
                return true;
            }
        }

        return false;
    }
}
