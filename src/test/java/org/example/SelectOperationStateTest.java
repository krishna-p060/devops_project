package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Select Operation State Tests")
class SelectOperationStateTest {

    private SelectOperationState selectOperationState;

    @Mock
    private ATMMachineContext mockContext;

    @Mock
    private ATMStateFactory mockFactory;

    @Mock
    private Card mockCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        selectOperationState = new SelectOperationState();
    }

    @Test
    @DisplayName("Should return correct state name")
    void testGetStateName() {
        assertEquals("SelectOperationState", selectOperationState.getStateName());
    }

    @Test
    @DisplayName("Should transition to IdleState when card is null")
    void testNextStateWithoutCard() {
        IdleState idleState = new IdleState();
        when(mockContext.getCurrentCard()).thenReturn(null);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createIdleState()).thenReturn(idleState);

        ATMState nextState = selectOperationState.next(mockContext);

        assertInstanceOf(IdleState.class, nextState);
    }

    @Test
    @DisplayName("Should transition to TransactionState when operation selected")
    void testNextStateWithOperation() {
        TransactionState transactionState = new TransactionState();
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getSelectedOperation()).thenReturn(TransactionType.WITHDRAW_CASH);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createTransactionState()).thenReturn(transactionState);

        ATMState nextState = selectOperationState.next(mockContext);

        assertInstanceOf(TransactionState.class, nextState);
    }

    @Test
    @DisplayName("Should remain in SelectOperationState when waiting for selection")
    void testNextStateWaitingForSelection() {
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getSelectedOperation()).thenReturn(null);

        ATMState nextState = selectOperationState.next(mockContext);

        assertSame(selectOperationState, nextState);
    }

    @Test
    @DisplayName("Should select WITHDRAW_CASH operation and advance state")
    void testSelectWithdrawOperation() {
        selectOperationState.selectOperation(mockContext, TransactionType.WITHDRAW_CASH);

        verify(mockContext).setSelectedOperation(TransactionType.WITHDRAW_CASH);
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should select CHECK_BALANCE operation and advance state")
    void testSelectBalanceCheckOperation() {
        selectOperationState.selectOperation(mockContext, TransactionType.CHECK_BALANCE);

        verify(mockContext).setSelectedOperation(TransactionType.CHECK_BALANCE);
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should not allow card insertion when already authenticated")
    void testInsertCardNotAllowed() {
        assertDoesNotThrow(() -> selectOperationState.insertCard(mockContext, mockCard));
        verify(mockContext, never()).setCurrentCard(any());
    }

    @Test
    @DisplayName("Should not allow PIN entry when already authenticated")
    void testEnterPinNotAllowed() {
        assertDoesNotThrow(() -> selectOperationState.enterPin(mockContext, 1234));
        verify(mockContext, never()).setCurrentAccount(any());
    }

    @Test
    @DisplayName("Should not allow transaction before selecting operation")
    void testPerformTransactionNotAllowed() {
        assertDoesNotThrow(() -> selectOperationState.performTransaction(mockContext, 100.0));
    }

    @Test
    @DisplayName("Should return card and reset ATM")
    void testReturnCard() {
        selectOperationState.returnCard(mockContext);

        verify(mockContext).resetATM();
    }

    @Test
    @DisplayName("Should cancel transaction and reset ATM")
    void testCancelTransaction() {
        selectOperationState.cancelTransaction(mockContext);

        verify(mockContext).resetATM();
    }
}
