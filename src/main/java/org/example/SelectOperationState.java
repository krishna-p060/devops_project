package org.example;

public class SelectOperationState implements ATMState {
    public SelectOperationState() {
        System.out.println("ATM is in Select Operation State - Please select an operation");
        System.out.println("1. Withdraw Cash");
        System.out.println("2. Check Balance");
    }

    @Override
    public String getStateName() {
        return "SelectOperationState";
    }

    @Override
    public ATMState next(ATMMachineContext context) {
        if (context.getCurrentCard() == null) {
            return context.getStateFactory().createIdleState();
        }
        if (context.getSelectedOperation() != null) {
            return context.getStateFactory().createTransactionState();
        }
        return this;
    }

    @Override
    public void insertCard(ATMMachineContext context, Card card) {
        System.out.println("Card already inserted in " + getStateName());
    }

    @Override
    public void enterPin(ATMMachineContext context, int pin) {
        System.out.println("PIN already authenticated in " + getStateName());
    }

    @Override
    public void selectOperation(ATMMachineContext context, TransactionType type) {
        System.out.println("Selected operation: " + type);
        context.setSelectedOperation(type);
        context.advanceState();
    }

    @Override
    public void performTransaction(ATMMachineContext context, double amount) {
        System.out.println("Cannot perform transaction in " + getStateName());
    }

    @Override
    public void returnCard(ATMMachineContext context) {
        System.out.println("Card returned to customer");
        context.resetATM();
    }

    @Override
    public void cancelTransaction(ATMMachineContext context) {
        System.out.println("Operation cancelled");
        returnCard(context);
    }
}