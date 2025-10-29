package org.example;


public class ATMStateFactory {
    private static ATMStateFactory instance = null;

    private ATMStateFactory() {}

    public static ATMStateFactory getInstance() {
        if (instance == null) {
            instance = new ATMStateFactory();
        }
        return instance;
    }

    public ATMState createIdleState() {
        return (ATMState) new IdleState();
    }

    public ATMState createHasCardState() {
        return (ATMState) new HasCardState();
    }

    public ATMState createSelectOperationState() {
        return (ATMState) new SelectOperationState();
    }

    public ATMState createTransactionState() {
        return (ATMState) new TransactionState();
    }
}
