package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ATM State Factory Tests")
class ATMStateFactoryTest {

    @Test
    @DisplayName("Should return singleton instance")
    void testGetInstanceReturnsSingleton() {
        ATMStateFactory instance1 = ATMStateFactory.getInstance();
        ATMStateFactory instance2 = ATMStateFactory.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    @DisplayName("Should create IdleState instance")
    void testCreateIdleState() {
        ATMStateFactory factory = ATMStateFactory.getInstance();
        ATMState state = factory.createIdleState();

        assertNotNull(state);
        assertInstanceOf(IdleState.class, state);
        assertEquals("IdleState", state.getStateName());
    }

    @Test
    @DisplayName("Should create HasCardState instance")
    void testCreateHasCardState() {
        ATMStateFactory factory = ATMStateFactory.getInstance();
        ATMState state = factory.createHasCardState();

        assertNotNull(state);
        assertInstanceOf(HasCardState.class, state);
        assertEquals("HasCardState", state.getStateName());
    }

    @Test
    @DisplayName("Should create SelectOperationState instance")
    void testCreateSelectOperationState() {
        ATMStateFactory factory = ATMStateFactory.getInstance();
        ATMState state = factory.createSelectOperationState();

        assertNotNull(state);
        assertInstanceOf(SelectOperationState.class, state);
        assertEquals("SelectOperationState", state.getStateName());
    }

    @Test
    @DisplayName("Should create TransactionState instance")
    void testCreateTransactionState() {
        ATMStateFactory factory = ATMStateFactory.getInstance();
        ATMState state = factory.createTransactionState();

        assertNotNull(state);
        assertInstanceOf(TransactionState.class, state);
        assertEquals("TransactionState", state.getStateName());
    }

    @Test
    @DisplayName("Should create new state instances on each call")
    void testCreateNewInstances() {
        ATMStateFactory factory = ATMStateFactory.getInstance();

        ATMState idle1 = factory.createIdleState();
        ATMState idle2 = factory.createIdleState();

        // States should be different instances (not singleton)
        assertNotSame(idle1, idle2);
    }
}
