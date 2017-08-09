package commlib.cinvesframework.interaction;

import commlib.cinvesframework.messages.ACLPerformative;

import java.util.HashMap;

public class ContractNet {

    private static HashMap<ACLPerformative,ACLPerformative[]> states = new HashMap<>();

    static
    {
        states.put(ACLPerformative.CFP, new ACLPerformative[]{ACLPerformative.PROPOSE,ACLPerformative.REJECT_PROPOSAL});

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
