package org.example;

public class Main {
    public static void main(String[] args) {
        ATMMachineContext atm = new ATMMachineContext();

        atm.addAccount(new Account("123456", 1000.0));
        atm.addAccount(new Account("654321", 500.0));

        try {

            atm.insertCard(new Card("123456", 1234, "654321"));

            atm.enterPin(1234);

            atm.selectOperation(TransactionType.WITHDRAW_CASH);

            atm.performTransaction(100.0);

            atm.selectOperation(TransactionType.CHECK_BALANCE);

            atm.performTransaction(0.0);

            atm.returnCard();


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}