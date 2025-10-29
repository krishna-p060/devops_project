package org.example;

public class IdleState implements ATMState {
    public IdleState() {
        System.out.println("ATM is in Idle State - Please insert your card");
    }

    @Override
    public String getStateName() {
        return "IdleState";
    }

    @Override
    public ATMState next(ATMMachineContext context) {
        if (context.getCurrentCard() != null) {
            return context.getStateFactory().createHasCardState();
        }
        return this;
    }

    @Override
    public void insertCard(ATMMachineContext context, Card card) {
        System.out.println("Card inserted");
        context.setCurrentCard(card);
        context.advanceState();
    }

    @Override
    public void enterPin(ATMMachineContext context, int pin) {
        System.out.println("Cannot enter PIN in " + getStateName());
    }

    @Override
    public void selectOperation(ATMMachineContext context, TransactionType type) {
        System.out.println("Cannot select operation in " + getStateName());
    }

    @Override
    public void performTransaction(ATMMachineContext context, double amount) {
        System.out.println("Cannot perform transaction in " + getStateName());
    }

    @Override
    public void returnCard(ATMMachineContext context) {
        System.out.println("No card to return in " + getStateName());
    }

    @Override
    public void cancelTransaction(ATMMachineContext context) {
        System.out.println("No transaction to cancel in " + getStateName());
    }
}