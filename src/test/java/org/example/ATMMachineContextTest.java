package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ATM Machine Context Tests")
class ATMMachineContextTest {

    private ATMMachineContext atmContext;
    private Account testAccount;
    private Card testCard;

    @BeforeEach
    void setUp() {
        atmContext = new ATMMachineContext();
        testAccount = new Account("ACC123", 1000.0);
        testCard = new Card("CARD123", 1234, "ACC123");
        atmContext.addAccount(testAccount);
    }

    @Test
    @DisplayName("Should initialize ATM in IdleState")
    void testInitialization() {
        assertNotNull(atmContext.getCurrentState());
        assertInstanceOf(IdleState.class, atmContext.getCurrentState());
        assertEquals("IdleState", atmContext.getCurrentState().getStateName());
    }

    @Test
    @DisplayName("Should initialize with ATM inventory")
    void testInventoryInitialization() {
        assertNotNull(atmContext.getATMInventory());
        assertEquals(2350, atmContext.getATMInventory().getTotalCash());
    }

    @Test
    @DisplayName("Should add account successfully")
    void testAddAccount() {
        Account newAccount = new Account("ACC456", 2000.0);
        atmContext.addAccount(newAccount);

        assertEquals(newAccount, atmContext.getAccount("ACC456"));
    }

    @Test
    @DisplayName("Should retrieve account by account number")
    void testGetAccount() {
        Account retrieved = atmContext.getAccount("ACC123");

        assertNotNull(retrieved);
        assertEquals(testAccount, retrieved);
        assertEquals("ACC123", retrieved.getAccountNumber());
    }

    @Test
    @DisplayName("Should return null for non-existent account")
    void testGetNonExistentAccount() {
        Account retrieved = atmContext.getAccount("NONEXISTENT");

        assertNull(retrieved);
    }

    @Test
    @DisplayName("Should delegate insertCard to current state")
    void testInsertCardDelegation() {
        atmContext.insertCard(testCard);

        // Should transition to HasCardState
        assertInstanceOf(HasCardState.class, atmContext.getCurrentState());
        assertEquals(testCard, atmContext.getCurrentCard());
    }

    @Test
    @DisplayName("Should delegate enterPin to current state")
    void testEnterPinDelegation() {
        atmContext.insertCard(testCard);
        atmContext.enterPin(1234);

        // Should transition to SelectOperationState
        assertInstanceOf(SelectOperationState.class, atmContext.getCurrentState());
        assertEquals(testAccount, atmContext.getCurrentAccount());
    }

    @Test
    @DisplayName("Should delegate selectOperation to current state")
    void testSelectOperationDelegation() {
        atmContext.insertCard(testCard);
        atmContext.enterPin(1234);
        atmContext.selectOperation(TransactionType.WITHDRAW_CASH);

        // Should transition to TransactionState
        assertInstanceOf(TransactionState.class, atmContext.getCurrentState());
        assertEquals(TransactionType.WITHDRAW_CASH, atmContext.getSelectedOperation());
    }

    @Test
    @DisplayName("Should delegate performTransaction to current state")
    void testPerformTransactionDelegation() {
        atmContext.insertCard(testCard);
        atmContext.enterPin(1234);
        atmContext.selectOperation(TransactionType.WITHDRAW_CASH);
        atmContext.performTransaction(100.0);

        // Should transition back to SelectOperationState
        assertInstanceOf(SelectOperationState.class, atmContext.getCurrentState());
        assertEquals(900.0, testAccount.getBalance());
    }

    @Test
    @DisplayName("Should delegate returnCard to current state")
    void testReturnCardDelegation() {
        atmContext.insertCard(testCard);
        atmContext.returnCard();

        // Should reset to IdleState
        assertInstanceOf(IdleState.class, atmContext.getCurrentState());
        assertNull(atmContext.getCurrentCard());
    }

    @Test
    @DisplayName("Should delegate cancelTransaction to current state")
    void testCancelTransactionDelegation() {
        atmContext.insertCard(testCard);
        atmContext.enterPin(1234);
        atmContext.cancelTransaction();

        // Should reset to IdleState
        assertInstanceOf(IdleState.class, atmContext.getCurrentState());
        assertNull(atmContext.getCurrentCard());
        assertNull(atmContext.getCurrentAccount());
    }

    @Test
    @DisplayName("Should advance state correctly")
    void testAdvanceState() {
        // Start in IdleState
        assertInstanceOf(IdleState.class, atmContext.getCurrentState());

        // Insert card
        atmContext.insertCard(testCard);
        assertInstanceOf(HasCardState.class, atmContext.getCurrentState());
    }

    @Test
    @DisplayName("Should reset ATM to idle state")
    void testResetATM() {
        atmContext.insertCard(testCard);
        atmContext.enterPin(1234);
        atmContext.selectOperation(TransactionType.CHECK_BALANCE);

        atmContext.resetATM();

        assertInstanceOf(IdleState.class, atmContext.getCurrentState());
        assertNull(atmContext.getCurrentCard());
        assertNull(atmContext.getCurrentAccount());
        assertNull(atmContext.getSelectedOperation());
    }

    @Test
    @DisplayName("Should get state factory instance")
    void testGetStateFactory() {
        assertNotNull(atmContext.getStateFactory());
        assertSame(ATMStateFactory.getInstance(), atmContext.getStateFactory());
    }

    @Test
    @DisplayName("Should set and get current card")
    void testSetAndGetCurrentCard() {
        atmContext.setCurrentCard(testCard);

        assertEquals(testCard, atmContext.getCurrentCard());
    }

    @Test
    @DisplayName("Should set and get current account")
    void testSetAndGetCurrentAccount() {
        atmContext.setCurrentAccount(testAccount);

        assertEquals(testAccount, atmContext.getCurrentAccount());
    }

    @Test
    @DisplayName("Should set and get selected operation")
    void testSetAndGetSelectedOperation() {
        atmContext.setSelectedOperation(TransactionType.WITHDRAW_CASH);

        assertEquals(TransactionType.WITHDRAW_CASH, atmContext.getSelectedOperation());
    }

    @Test
    @DisplayName("Should set current state")
    void testSetCurrentState() {
        HasCardState hasCardState = new HasCardState();
        atmContext.setCurrentState(hasCardState);

        assertEquals(hasCardState, atmContext.getCurrentState());
    }
}
