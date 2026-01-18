package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Transaction State Tests")
class TransactionStateTest {

    private TransactionState transactionState;

    @Mock
    private ATMMachineContext mockContext;

    @Mock
    private ATMStateFactory mockFactory;

    @Mock
    private Card mockCard;

    @Mock
    private Account mockAccount;

    @Mock
    private ATMInventory mockInventory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionState = new TransactionState();
    }

    @Test
    @DisplayName("Should return correct state name")
    void testGetStateName() {
        assertEquals("TransactionState", transactionState.getStateName());
    }

    @Test
    @DisplayName("Should transition to IdleState when card is null")
    void testNextStateWithoutCard() {
        IdleState idleState = new IdleState();
        when(mockContext.getCurrentCard()).thenReturn(null);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createIdleState()).thenReturn(idleState);

        ATMState nextState = transactionState.next(mockContext);

        assertInstanceOf(IdleState.class, nextState);
    }

    @Test
    @DisplayName("Should transition to SelectOperationState after transaction")
    void testNextStateAfterTransaction() {
        SelectOperationState selectState = new SelectOperationState();
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createSelectOperationState()).thenReturn(selectState);

        ATMState nextState = transactionState.next(mockContext);

        assertInstanceOf(SelectOperationState.class, nextState);
    }

    @Test
    @DisplayName("Should successfully perform withdrawal with sufficient funds")
    void testSuccessfulWithdrawal() {
        double amount = 500.0;
        Map<CashType, Integer> dispensedCash = new HashMap<>();
        dispensedCash.put(CashType.BILL_100, 5);

        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.WITHDRAW_CASH);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockContext.getATMInventory()).thenReturn(mockInventory);
        when(mockAccount.withdraw(amount)).thenReturn(true);
        when(mockInventory.hasSufficientCash((int) amount)).thenReturn(true);
        when(mockInventory.dispenseCash((int) amount)).thenReturn(dispensedCash);

        transactionState.performTransaction(mockContext, amount);

        verify(mockAccount).withdraw(amount);
        verify(mockInventory).dispenseCash((int) amount);
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should fail withdrawal when insufficient account balance")
    void testWithdrawalInsufficientAccountBalance() {
        double amount = 1000.0;
        SelectOperationState selectState = new SelectOperationState();

        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.WITHDRAW_CASH);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockAccount.withdraw(amount)).thenReturn(false);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createSelectOperationState()).thenReturn(selectState);

        transactionState.performTransaction(mockContext, amount);

        verify(mockAccount).withdraw(amount);
        verify(mockInventory, never()).dispenseCash(anyInt());
        verify(mockContext).setCurrentState(any(SelectOperationState.class));
    }

    @Test
    @DisplayName("Should fail withdrawal and rollback when insufficient ATM cash")
    void testWithdrawalInsufficientATMCash() {
        double amount = 500.0;
        SelectOperationState selectState = new SelectOperationState();

        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.WITHDRAW_CASH);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockContext.getATMInventory()).thenReturn(mockInventory);
        when(mockAccount.withdraw(amount)).thenReturn(true);
        when(mockInventory.hasSufficientCash((int) amount)).thenReturn(false);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createSelectOperationState()).thenReturn(selectState);

        transactionState.performTransaction(mockContext, amount);

        verify(mockAccount).withdraw(amount);
        verify(mockAccount).deposit(amount); // Rollback
        verify(mockInventory, never()).dispenseCash(anyInt());
        verify(mockContext).setCurrentState(any(SelectOperationState.class));
    }

    @Test
    @DisplayName("Should fail withdrawal and rollback when cannot dispense exact amount")
    void testWithdrawalCannotDispenseExactAmount() {
        double amount = 500.0;
        SelectOperationState selectState = new SelectOperationState();

        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.WITHDRAW_CASH);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockContext.getATMInventory()).thenReturn(mockInventory);
        when(mockAccount.withdraw(amount)).thenReturn(true);
        when(mockInventory.hasSufficientCash((int) amount)).thenReturn(true);
        when(mockInventory.dispenseCash((int) amount)).thenReturn(null);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createSelectOperationState()).thenReturn(selectState);

        transactionState.performTransaction(mockContext, amount);

        verify(mockAccount).withdraw(amount);
        verify(mockInventory).dispenseCash((int) amount);
        verify(mockAccount).deposit(amount); // Rollback
        verify(mockContext).setCurrentState(any(SelectOperationState.class));
    }

    @Test
    @DisplayName("Should successfully perform balance check")
    void testSuccessfulBalanceCheck() {
        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.CHECK_BALANCE);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockAccount.getBalance()).thenReturn(1500.0);

        transactionState.performTransaction(mockContext, 0.0);

        verify(mockAccount).getBalance();
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should not allow card insertion during transaction")
    void testInsertCardNotAllowed() {
        assertDoesNotThrow(() -> transactionState.insertCard(mockContext, mockCard));
        verify(mockContext, never()).setCurrentCard(any());
    }

    @Test
    @DisplayName("Should not allow PIN entry during transaction")
    void testEnterPinNotAllowed() {
        assertDoesNotThrow(() -> transactionState.enterPin(mockContext, 1234));
        verify(mockContext, never()).setCurrentAccount(any());
    }

    @Test
    @DisplayName("Should not allow operation selection during transaction")
    void testSelectOperationNotAllowed() {
        assertDoesNotThrow(() -> transactionState.selectOperation(mockContext, TransactionType.WITHDRAW_CASH));
        verify(mockContext, never()).setSelectedOperation(any());
    }

    @Test
    @DisplayName("Should return card and reset ATM")
    void testReturnCard() {
        transactionState.returnCard(mockContext);

        verify(mockContext).resetATM();
    }

    @Test
    @DisplayName("Should cancel transaction and reset ATM")
    void testCancelTransaction() {
        transactionState.cancelTransaction(mockContext);

        verify(mockContext).resetATM();
    }
}
