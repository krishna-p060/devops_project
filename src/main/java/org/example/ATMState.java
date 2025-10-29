package org.example;

public interface ATMState {
    String getStateName();
    ATMState next(ATMMachineContext context);
    
    void insertCard(ATMMachineContext context, Card card);
    void enterPin(ATMMachineContext context, int pin);
    void selectOperation(ATMMachineContext context, TransactionType type);
    void performTransaction(ATMMachineContext context, double amount);
    void returnCard(ATMMachineContext context);
    void cancelTransaction(ATMMachineContext context);
}