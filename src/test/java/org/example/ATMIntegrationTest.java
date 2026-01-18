package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ATM Integration Tests")
class ATMIntegrationTest {

    private ATMMachineContext atm;
    private Account account1;
    private Account account2;
    private Card card1;
    private Card card2;

    @BeforeEach
    void setUp() {
        atm = new ATMMachineContext();

        // Setup test accounts
        account1 = new Account("ACC001", 1500.0);
        account2 = new Account("ACC002", 500.0);

        // Setup test cards
        card1 = new Card("CARD001", 1234, "ACC001");
        card2 = new Card("CARD002", 5678, "ACC002");

        // Add accounts to ATM
        atm.addAccount(account1);
        atm.addAccount(account2);
    }

    @Test
    @DisplayName("Complete withdrawal flow - successful transaction")
    void testCompleteWithdrawalFlow() {
        // 1. Insert card
        atm.insertCard(card1);
        assertInstanceOf(HasCardState.class, atm.getCurrentState());
        assertEquals(card1, atm.getCurrentCard());

        // 2. Enter correct PIN
        atm.enterPin(1234);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());
        assertEquals(account1, atm.getCurrentAccount());

        // 3. Select withdraw operation
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        assertInstanceOf(TransactionState.class, atm.getCurrentState());
        assertEquals(TransactionType.WITHDRAW_CASH, atm.getSelectedOperation());

        // 4. Perform withdrawal
        double initialBalance = account1.getBalance();
        double withdrawAmount = 500.0;
        atm.performTransaction(withdrawAmount);

        // 5. Verify state and balance
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());
        assertEquals(initialBalance - withdrawAmount, account1.getBalance());
    }

    @Test
    @DisplayName("Complete balance check flow")
    void testCompleteBalanceCheckFlow() {
        // 1. Insert card
        atm.insertCard(card1);

        // 2. Enter correct PIN
        atm.enterPin(1234);

        // 3. Select balance check operation
        atm.selectOperation(TransactionType.CHECK_BALANCE);
        assertInstanceOf(TransactionState.class, atm.getCurrentState());

        // 4. Perform balance check
        double initialBalance = account1.getBalance();
        atm.performTransaction(0.0);

        // 5. Verify state and balance unchanged
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());
        assertEquals(initialBalance, account1.getBalance());
    }

    @Test
    @DisplayName("Failed PIN flow - retry with correct PIN")
    void testFailedPinFlow() {
        // 1. Insert card
        atm.insertCard(card1);
        assertInstanceOf(HasCardState.class, atm.getCurrentState());

        // 2. Enter incorrect PIN
        atm.enterPin(9999);
        assertInstanceOf(HasCardState.class, atm.getCurrentState());
        assertNull(atm.getCurrentAccount());

        // 3. Enter correct PIN
        atm.enterPin(1234);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());
        assertNotNull(atm.getCurrentAccount());
    }

    @Test
    @DisplayName("Card return flow before PIN entry")
    void testCardReturnBeforePinEntry() {
        // 1. Insert card
        atm.insertCard(card1);
        assertInstanceOf(HasCardState.class, atm.getCurrentState());

        // 2. Return card
        atm.returnCard();

        // 3. Verify ATM reset
        assertInstanceOf(IdleState.class, atm.getCurrentState());
        assertNull(atm.getCurrentCard());
        assertNull(atm.getCurrentAccount());
    }

    @Test
    @DisplayName("Transaction cancellation flow")
    void testTransactionCancellationFlow() {
        // 1. Insert card
        atm.insertCard(card1);

        // 2. Enter PIN
        atm.enterPin(1234);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // 3. Cancel transaction
        atm.cancelTransaction();

        // 4. Verify ATM reset
        assertInstanceOf(IdleState.class, atm.getCurrentState());
        assertNull(atm.getCurrentCard());
        assertNull(atm.getCurrentAccount());
        assertNull(atm.getSelectedOperation());
    }

    @Test
    @DisplayName("Multiple sequential transactions flow")
    void testMultipleTransactionsFlow() {
        double initialBalance = account1.getBalance();

        // 1. First transaction - withdraw $200
        atm.insertCard(card1);
        atm.enterPin(1234);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(200.0);

        assertEquals(initialBalance - 200.0, account1.getBalance());
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // 2. Second transaction - check balance
        atm.selectOperation(TransactionType.CHECK_BALANCE);
        atm.performTransaction(0.0);

        assertEquals(initialBalance - 200.0, account1.getBalance());
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // 3. Third transaction - withdraw $300
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(300.0);

        assertEquals(initialBalance - 500.0, account1.getBalance());
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // 4. Return card
        atm.returnCard();
        assertInstanceOf(IdleState.class, atm.getCurrentState());
    }

    @Test
    @DisplayName("Failed withdrawal - insufficient account balance")
    void testFailedWithdrawalInsufficientBalance() {
        // 1. Setup - use account2 with only $500
        atm.insertCard(card2);
        atm.enterPin(5678);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);

        // 2. Try to withdraw more than balance
        double initialBalance = account2.getBalance();
        atm.performTransaction(1000.0);

        // 3. Verify transaction failed and balance unchanged
        assertEquals(initialBalance, account2.getBalance());
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());
    }

    @Test
    @DisplayName("Failed withdrawal - insufficient ATM cash")
    void testFailedWithdrawalInsufficientATMCash() {
        // 1. Deplete ATM cash first - withdraw $2300 (leaves $50 in ATM)
        atm.insertCard(card1);
        atm.enterPin(1234);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(1400.0); // Account has $1500, withdraw $1400, leaves $100

        // 2. Return card and start new transaction
        atm.returnCard();

        // 3. Try to withdraw more than ATM has (ATM now has $950, account has $100)
        // We need to deplete more ATM cash first
        // Create a new account with more money
        Account richAccount = new Account("ACC003", 5000.0);
        Card richCard = new Card("CARD003", 9999, "ACC003");
        atm.addAccount(richAccount);

        atm.insertCard(richCard);
        atm.enterPin(9999);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(900.0); // Now ATM has only $50
        atm.returnCard();

        // 4. Now try to withdraw $100 when ATM only has $50
        atm.insertCard(richCard);
        atm.enterPin(9999);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);

        double balanceBeforeFailedWithdrawal = richAccount.getBalance();
        atm.performTransaction(100.0);

        // 5. Verify transaction failed and balance unchanged (rollback)
        assertEquals(balanceBeforeFailedWithdrawal, richAccount.getBalance());
    }

    @Test
    @DisplayName("Multiple users sequential flow")
    void testMultipleUsersFlow() {
        // User 1 transaction
        atm.insertCard(card1);
        atm.enterPin(1234);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(100.0);
        atm.returnCard();

        assertEquals(1400.0, account1.getBalance());
        assertInstanceOf(IdleState.class, atm.getCurrentState());

        // User 2 transaction
        atm.insertCard(card2);
        atm.enterPin(5678);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(50.0);
        atm.returnCard();

        assertEquals(450.0, account2.getBalance());
        assertInstanceOf(IdleState.class, atm.getCurrentState());
    }

    @Test
    @DisplayName("Complete flow with all operations")
    void testCompleteFlowAllOperations() {
        // Start in IdleState
        assertInstanceOf(IdleState.class, atm.getCurrentState());

        // Insert card -> HasCardState
        atm.insertCard(card1);
        assertInstanceOf(HasCardState.class, atm.getCurrentState());

        // Enter PIN -> SelectOperationState
        atm.enterPin(1234);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // Select operation -> TransactionState
        atm.selectOperation(TransactionType.CHECK_BALANCE);
        assertInstanceOf(TransactionState.class, atm.getCurrentState());

        // Perform transaction -> SelectOperationState
        atm.performTransaction(0.0);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // Select another operation -> TransactionState
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        assertInstanceOf(TransactionState.class, atm.getCurrentState());

        // Perform transaction -> SelectOperationState
        atm.performTransaction(250.0);
        assertInstanceOf(SelectOperationState.class, atm.getCurrentState());

        // Return card -> IdleState
        atm.returnCard();
        assertInstanceOf(IdleState.class, atm.getCurrentState());

        // Verify final balance
        assertEquals(1250.0, account1.getBalance());
    }

    @Test
    @DisplayName("ATM inventory depletion across multiple withdrawals")
    void testATMInventoryDepletion() {
        int initialATMCash = atm.getATMInventory().getTotalCash();
        assertEquals(2350, initialATMCash);

        // First withdrawal
        atm.insertCard(card1);
        atm.enterPin(1234);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(500.0);
        atm.returnCard();

        assertEquals(1850, atm.getATMInventory().getTotalCash());

        // Second withdrawal
        atm.insertCard(card1);
        atm.enterPin(1234);
        atm.selectOperation(TransactionType.WITHDRAW_CASH);
        atm.performTransaction(1000.0);
        atm.returnCard();

        assertEquals(850, atm.getATMInventory().getTotalCash());
    }
}
