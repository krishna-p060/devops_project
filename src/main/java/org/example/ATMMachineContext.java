package org.example;

import java.util.HashMap;
import java.util.Map;

public class ATMMachineContext {
    private ATMState currentState;
    private Card currentCard;
    private Account currentAccount;
    private ATMInventory atmInventory;
    private Map<String, Account> accounts;
    private ATMStateFactory stateFactory;
    private TransactionType selectedOperation;

    public ATMMachineContext() {
        this.stateFactory = ATMStateFactory.getInstance();
        this.currentState = stateFactory.createIdleState();
        this.atmInventory = new ATMInventory();
        this.accounts = new HashMap<>();
        System.out.println("ATM initialized in: " + currentState.getStateName());
    }

    public void advanceState() {
        ATMState nextState = currentState.next(this);
        currentState = nextState;
        System.out.println("Current state: " + currentState.getStateName());
    }

    // Delegate to current state
    public void insertCard(Card card) {
        currentState.insertCard(this, card);
    }

    // Delegate to current state
    public void enterPin(int pin) {
        currentState.enterPin(this, pin);
    }

    // Delegate to current state
    public void selectOperation(TransactionType transactionType) {
        currentState.selectOperation(this, transactionType);
    }

    // Delegate to current state
    public void performTransaction(double amount) {
        currentState.performTransaction(this, amount);
    }

    // Delegate to current state
    public void returnCard() {
        currentState.returnCard(this);
    }

    // Delegate to current state
    public void cancelTransaction() {
        currentState.cancelTransaction(this);
    }

    // Package-private method for states to reset ATM
    void resetATM() {
        this.currentCard = null;
        this.currentAccount = null;
        this.selectedOperation = null;
        this.currentState = stateFactory.createIdleState();
    }

    // Getters
    public ATMState getCurrentState() {
        return currentState;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public ATMInventory getATMInventory() {
        return atmInventory;
    }

    public TransactionType getSelectedOperation() {
        return selectedOperation;
    }

    public ATMStateFactory getStateFactory() {
        return stateFactory;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    // Setters - package-private so only states can modify
    void setCurrentState(ATMState state) {
        this.currentState = state;
    }

    void setCurrentCard(Card card) {
        this.currentCard = card;
    }

    void setCurrentAccount(Account account) {
        this.currentAccount = account;
    }

    void setSelectedOperation(TransactionType operation) {
        this.selectedOperation = operation;
    }

    // Public method to add accounts
    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }
}