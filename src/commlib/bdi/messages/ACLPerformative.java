package commlib.bdi.messages;

public enum ACLPerformative {

    ACCEPT_PROPOSAL(1),
    AGREE(2),
    CANCEL(3),
    CFP(4),
    CONFIRM(5),
    DISCONFIRM(6),
    FAILURE(7),
    INFORM(8),
    INFORM_IF(9),
    INFORM_REF(10),
    NOT_UNDERSTOOD(11),
    PROPAGATE(12),
    PROPOSE(13),
    PROXY(14),
    QUERY_IF(15),
    QUERY_REF(16),
    REFUSE(17),
    REJECT_PROPOSAL(18),
    REQUEST(19),
    REQUEST_WHEN(20),
    REQUEST_WHENEVER(21),
    SUBSCRIBE(22);

    private int value;

    private ACLPerformative(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ACLPerformative createPerformative(int value) {
        switch (value) {
            case 1:
                return ACLPerformative.ACCEPT_PROPOSAL;
            case 2:
                return ACLPerformative.
                        AGREE;
            case 3:
                return ACLPerformative.
                        CANCEL;
            case 4:
                return ACLPerformative.
                        CFP;
            case 5:
                return ACLPerformative.
                        CONFIRM;
            case 6:
                return ACLPerformative.
                        DISCONFIRM;
            case 7:
                return ACLPerformative.
                        FAILURE;
            case 8:
                return ACLPerformative.
                        INFORM;
            case 9:
                return ACLPerformative.
                        INFORM_IF;
            case 10:
                return ACLPerformative.
                        INFORM_REF;
            case 11:
                return ACLPerformative.
                        NOT_UNDERSTOOD;
            case 12:
                return ACLPerformative.
                        PROPAGATE;
            case 13:
                return ACLPerformative.
                        PROPOSE;
            case 14:
                return ACLPerformative.
                        PROXY;
            case 15:
                return ACLPerformative.
                        QUERY_IF;
            case 16:
                return ACLPerformative.
                        QUERY_REF;
            case 17:
                return ACLPerformative.
                        REFUSE;
            case 18:
                return ACLPerformative.
                        REJECT_PROPOSAL;
            case 19:
                return ACLPerformative.
                        REQUEST;
            case 20:
                return ACLPerformative.
                        REQUEST_WHEN;
            case 21:
                return ACLPerformative.
                        REQUEST_WHENEVER;
            case 22:
                return ACLPerformative.
                        SUBSCRIBE;
        }

        return null;
    }
}
