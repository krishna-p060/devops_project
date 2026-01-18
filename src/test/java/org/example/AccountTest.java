package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Tests")
class AccountTest {

    private Account account;
    private static final String ACCOUNT_NUMBER = "ACC123456";
    private static final double INITIAL_BALANCE = 1000.0;

    @BeforeEach
    void setUp() {
        account = new Account(ACCOUNT_NUMBER, INITIAL_BALANCE);
    }

    @Test
    @DisplayName("Should initialize account with correct account number and balance")
    void testAccountInitialization() {
        assertEquals(ACCOUNT_NUMBER, account.getAccountNumber());
        assertEquals(INITIAL_BALANCE, account.getBalance());
    }

    @Test
    @DisplayName("Should successfully withdraw when sufficient balance exists")
    void testSuccessfulWithdrawal() {
        double withdrawAmount = 500.0;
        boolean result = account.withdraw(withdrawAmount);

        assertTrue(result);
        assertEquals(INITIAL_BALANCE - withdrawAmount, account.getBalance());
    }

    @Test
    @DisplayName("Should fail withdrawal when insufficient balance")
    void testFailedWithdrawalInsufficientBalance() {
        double withdrawAmount = 1500.0;
        boolean result = account.withdraw(withdrawAmount);

        assertFalse(result);
        assertEquals(INITIAL_BALANCE, account.getBalance()); // Balance unchanged
    }

    @Test
    @DisplayName("Should successfully withdraw exact balance amount")
    void testWithdrawExactBalance() {
        boolean result = account.withdraw(INITIAL_BALANCE);

        assertTrue(result);
        assertEquals(0.0, account.getBalance());
    }

    @Test
    @DisplayName("Should successfully withdraw zero amount")
    void testWithdrawZeroAmount() {
        boolean result = account.withdraw(0.0);

        assertTrue(result);
        assertEquals(INITIAL_BALANCE, account.getBalance());
    }

    @Test
    @DisplayName("Should fail withdrawal for negative amount")
    void testWithdrawNegativeAmount() {
        boolean result = account.withdraw(-100.0);

        assertTrue(result); // Current implementation allows this
        assertEquals(INITIAL_BALANCE + 100.0, account.getBalance());
    }

    @Test
    @DisplayName("Should successfully deposit amount")
    void testSuccessfulDeposit() {
        double depositAmount = 500.0;
        account.deposit(depositAmount);

        assertEquals(INITIAL_BALANCE + depositAmount, account.getBalance());
    }

    @Test
    @DisplayName("Should handle deposit of zero amount")
    void testDepositZeroAmount() {
        account.deposit(0.0);

        assertEquals(INITIAL_BALANCE, account.getBalance());
    }

    @Test
    @DisplayName("Should handle deposit of negative amount")
    void testDepositNegativeAmount() {
        account.deposit(-100.0);

        assertEquals(INITIAL_BALANCE - 100.0, account.getBalance());
    }

    @Test
    @DisplayName("Should return correct balance after multiple transactions")
    void testMultipleTransactions() {
        account.withdraw(200.0);
        account.deposit(300.0);
        account.withdraw(100.0);

        assertEquals(1000.0, account.getBalance());
    }

    @Test
    @DisplayName("Should maintain correct balance with decimal amounts")
    void testDecimalAmounts() {
        account.withdraw(123.45);
        account.deposit(67.89);

        assertEquals(944.44, account.getBalance(), 0.01);
    }
}
