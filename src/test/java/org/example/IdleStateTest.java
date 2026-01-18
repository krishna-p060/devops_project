package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Idle State Tests")
class IdleStateTest {

    private IdleState idleState;

    @Mock
    private ATMMachineContext mockContext;

    @Mock
    private ATMStateFactory mockFactory;

    @Mock
    private Card mockCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        idleState = new IdleState();
    }

    @Test
    @DisplayName("Should return correct state name")
    void testGetStateName() {
        assertEquals("IdleState", idleState.getStateName());
    }

    @Test
    @DisplayName("Should transition to HasCardState when card is inserted")
    void testNextStateWithCard() {
        HasCardState hasCardState = new HasCardState();
        when(mockContext.getCurrentCard()).thenReturn(mockCard);
        when(mockContext.getStateFactory()).thenReturn(mockFactory);
        when(mockFactory.createHasCardState()).thenReturn(hasCardState);

        ATMState nextState = idleState.next(mockContext);

        assertInstanceOf(HasCardState.class, nextState);
    }

    @Test
    @DisplayName("Should remain in IdleState when no card is present")
    void testNextStateWithoutCard() {
        when(mockContext.getCurrentCard()).thenReturn(null);

        ATMState nextState = idleState.next(mockContext);

        assertSame(idleState, nextState);
    }

    @Test
    @DisplayName("Should accept card insertion and advance state")
    void testInsertCard() {
        idleState.insertCard(mockContext, mockCard);

        verify(mockContext).setCurrentCard(mockCard);
        verify(mockContext).advanceState();
    }

    @Test
    @DisplayName("Should not allow PIN entry in idle state")
    void testEnterPinNotAllowed() {
        // Should just print message, no exception
        assertDoesNotThrow(() -> idleState.enterPin(mockContext, 1234));
        verify(mockContext, never()).advanceState();
    }

    @Test
    @DisplayName("Should not allow operation selection in idle state")
    void testSelectOperationNotAllowed() {
        assertDoesNotThrow(() -> idleState.selectOperation(mockContext, TransactionType.WITHDRAW_CASH));
        verify(mockContext, never()).advanceState();
    }

    @Test
    @DisplayName("Should not allow transaction in idle state")
    void testPerformTransactionNotAllowed() {
        assertDoesNotThrow(() -> idleState.performTransaction(mockContext, 100.0));
        verify(mockContext, never()).advanceState();
    }

    @Test
    @DisplayName("Should not allow card return when no card present")
    void testReturnCardNotAllowed() {
        assertDoesNotThrow(() -> idleState.returnCard(mockContext));
        verify(mockContext, never()).resetATM();
    }

    @Test
    @DisplayName("Should not allow transaction cancellation in idle state")
    void testCancelTransactionNotAllowed() {
        assertDoesNotThrow(() -> idleState.cancelTransaction(mockContext));
        verify(mockContext, never()).resetATM();
    }
}
