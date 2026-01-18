package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Card Tests")
class CardTest {

    private Card card;
    private static final String CARD_NUMBER = "1234-5678-9012-3456";
    private static final int CORRECT_PIN = 1234;
    private static final String ACCOUNT_NUMBER = "ACC123456";

    @BeforeEach
    void setUp() {
        card = new Card(CARD_NUMBER, CORRECT_PIN, ACCOUNT_NUMBER);
    }

    @Test
    @DisplayName("Should initialize card with correct card number, PIN, and account number")
    void testCardInitialization() {
        assertEquals(CARD_NUMBER, card.getCardNumber());
        assertEquals(ACCOUNT_NUMBER, card.getAccountNumber());
    }

    @Test
    @DisplayName("Should validate correct PIN successfully")
    void testValidateCorrectPin() {
        assertTrue(card.validatePin(CORRECT_PIN));
    }

    @Test
    @DisplayName("Should reject incorrect PIN")
    void testValidateIncorrectPin() {
        assertFalse(card.validatePin(9999));
    }

    @Test
    @DisplayName("Should reject PIN with different value")
    void testValidateDifferentPin() {
        assertFalse(card.validatePin(4321));
    }

    @Test
    @DisplayName("Should handle zero PIN validation")
    void testValidateZeroPin() {
        Card cardWithZeroPin = new Card(CARD_NUMBER, 0, ACCOUNT_NUMBER);
        assertTrue(cardWithZeroPin.validatePin(0));
        assertFalse(cardWithZeroPin.validatePin(1234));
    }

    @Test
    @DisplayName("Should return correct card number")
    void testGetCardNumber() {
        assertEquals(CARD_NUMBER, card.getCardNumber());
    }

    @Test
    @DisplayName("Should return correct account number")
    void testGetAccountNumber() {
        assertEquals(ACCOUNT_NUMBER, card.getAccountNumber());
    }

    @Test
    @DisplayName("Should handle multiple PIN validation attempts")
    void testMultiplePinValidations() {
        assertFalse(card.validatePin(1111));
        assertFalse(card.validatePin(2222));
        assertTrue(card.validatePin(CORRECT_PIN));
        assertTrue(card.validatePin(CORRECT_PIN)); // Can validate multiple times
    }
}
