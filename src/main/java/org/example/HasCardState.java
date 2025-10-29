package org.example;

public class HasCardState implements ATMState {
    public HasCardState() {
        System.out.println("ATM is in Has Card State - Please enter your PIN");
    }

    @Override
    public String getStateName() {
        return "HasCardState";
    }

    @Override
    public ATMState next(ATMMachineContext context) {
        if (context.getCurrentCard() == null) {
            return context.getStateFactory().createIdleState();
        }
        if (context.getCurrentAccount() != null) {
            return context.getStateFactory().createSelectOperationState();
        }
        return this;
    }

    @Override
    public void insertCard(ATMMachineContext context, Card card) {
        System.out.println("Card already inserted in " + getStateName());
    }

    @Override
    public void enterPin(ATMMachineContext context, int pin) {
        Card currentCard = context.getCurrentCard();
        if (currentCard.validatePin(pin)) {
            System.out.println("PIN authenticated successfully");
            context.setCurrentAccount(
                context.getAccount(currentCard.getAccountNumber())
            );
            context.advanceState();
        } else {
            System.out.println("Invalid PIN. Please try again");
        }
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
        System.out.println("Card returned to customer");
        context.resetATM();
    }

    @Override
    public void cancelTransaction(ATMMachineContext context) {
        System.out.println("Operation cancelled");
        returnCard(context);
    }
}