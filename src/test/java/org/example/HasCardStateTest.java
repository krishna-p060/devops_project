package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Has Card State Tests")
class HasCardStateTest {

    private HasCardState hasCardState;

    @Mock
    private ATMMachineContext mockContext;

    @Mock
    private ATMStateFactory mockFactory;

    @Mock
    private Card mockCard;

    @Mock
    private Account mockAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        hasCardState = new HasCardState();
    }

    @Test
    @DisplayName("Should return correct state name")
    void testGetStateName() {
        assertEquals("HasCardState", hasCardState.getStateName());
    }

    @Test
    @DisplayName("Should transition to IdleState when card is null")
    void testNextStateWithoutCard() {
        IdleState idleState = new IdleState();
        when(mockContext.getCurrentCard()).thenReturn(null);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createIdleState()).thenReturn(idleState);

        ATMState nextState = hasCardState.next(mockContext);

        assertInstanceOf(IdleState.class, nextState);
    }

    @Test
    @DisplayName("Should transition to SelectOperationState when authenticated")
    void testNextStateWithAccount() {
        SelectOperationState selectState = new SelectOperationState();
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getCurrentAccount()).thenReturn(mockAccount);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createSelectOperationState()).thenReturn(selectState);

        ATMState nextState = hasCardState.next(mockContext);

        assertInstanceOf(SelectOperationState.class, nextState);
    }

    @Test
    @DisplayName("Should remain in HasCardState when waiting for PIN")
    void testNextStateWaitingForPin() {
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getCurrentAccount()).thenReturn(null);

        ATMState nextState = hasCardState.next(mockContext);

        assertSame(hasCardState, nextState);
    }

    @Test
    @DisplayName("Should authenticate with correct PIN and advance state")
    void testEnterCorrectPin() {
        int correctPin = 1234;
        String accountNumber = "ACC123";

        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockCard.validatePin(correctPin)).thenReturn(true);
        when(mockCard.getAccountNumber()).thenReturn(accountNumber);
        when(mockContext.getAccount(accountNumber)).thenReturn(mockAccount);

        hasCardState.enterPin(mockContext, correctPin);

        verify(mockContext).setCurrentAccount(mockAccount);
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should reject incorrect PIN and remain in same state")
    void testEnterIncorrectPin() {
        int incorrectPin = 9999;

        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockCard.validatePin(incorrectPin)).thenReturn(false);

        hasCardState.enterPin(mockContext, incorrectPin);

        verify(mockContext, never()).setCurrentAccount(any());
        verify(mockContext, never()).advanceState();
    }

    @Test
    @DisplayName("Should not allow card insertion when card already present")
    void testInsertCardNotAllowed() {
        assertDoesNotThrow(() -> hasCardState.insertCard(mockContext, mockCard));
        verify(mockContext, never()).setCurrentCard(any());
    }

    @Test
    @DisplayName("Should not allow operation selection before authentication")
    void testSelectOperationNotAllowed() {
        assertDoesNotThrow(() -> hasCardState.selectOperation(mockContext, TransactionType.WITHDRAW_CASH));
        verify(mockContext, never()).setSelectedOperation(any());
    }

    @Test
    @DisplayName("Should not allow transaction before authentication")
    void testPerformTransactionNotAllowed() {
        assertDoesNotThrow(() -> hasCardState.performTransaction(mockContext, 100.0));
    }

    @Test
    @DisplayName("Should return card and reset ATM")
    void testReturnCard() {
        hasCardState.returnCard(mockContext);

        verify(mockContext).resetATM();
    }

    @Test
    @DisplayName("Should cancel transaction and reset ATM")
    void testCancelTransaction() {
        hasCardState.cancelTransaction(mockContext);

        verify(mockContext).resetATM();
    }
}
