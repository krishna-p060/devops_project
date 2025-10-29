package org.example;

import java.util.Map;

public class TransactionState implements ATMState {
    public TransactionState() {
        System.out.println("ATM is in Transaction State");
    }

    @Override
    public String getStateName() {
        return "TransactionState";
    }

    @Override
    public ATMState next(ATMMachineContext context) {
        if (context.getCurrentCard() == null) {
            return context.getStateFactory().createIdleState();
        }
        return context.getStateFactory().createSelectOperationState();
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
        System.out.println("Transaction already in progress in " + getStateName());
    }

    @Override
    public void performTransaction(ATMMachineContext context, double amount) {
        try {
            TransactionType selectedOperation = context.getSelectedOperation();
            if (selectedOperation == TransactionType.WITHDRAW_CASH) {
                performWithdrawal(context, amount);
            } else if (selectedOperation == TransactionType.CHECK_BALANCE) {
                checkBalance(context);
            }
            context.advanceState();
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
            context.setCurrentState(
                context.getStateFactory().createSelectOperationState()
            );
        }
    }

    @Override
    public void returnCard(ATMMachineContext context) {
        System.out.println("Card returned to customer");
        context.resetATM();
    }

    @Override
    public void cancelTransaction(ATMMachineContext context) {
        System.out.println("Transaction cancelled");
        returnCard(context);
    }

    private void performWithdrawal(ATMMachineContext context, double amount) 
            throws Exception {
        Account currentAccount = context.getCurrentAccount();
        ATMInventory atmInventory = context.getATMInventory();

        if (!currentAccount.withdraw(amount)) {
            throw new Exception("Insufficient funds in account");
        }
        if (!atmInventory.hasSufficientCash((int) amount)) {
            currentAccount.deposit(amount);
            throw new Exception("Insufficient cash in ATM");
        }
        Map<CashType, Integer> dispensedCash = 
            atmInventory.dispenseCash((int) amount);
        if (dispensedCash == null) {
            currentAccount.deposit(amount);
            throw new Exception("Unable to dispense exact amount");
        }
        System.out.println("Transaction successful. Please collect your cash:");
        for (Map.Entry<CashType, Integer> entry : dispensedCash.entrySet()) {
            System.out.println(entry.getValue() + " x $" + entry.getKey().value);
        }
    }

    private void checkBalance(ATMMachineContext context) {
        Account currentAccount = context.getCurrentAccount();
        System.out.println(
            "Your current balance is: $" + currentAccount.getBalance()
        );
    }
}