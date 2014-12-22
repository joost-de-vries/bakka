import java.util.*;

import junit.framework.*;
import devtest.*;
import devtest.Bank.Account;
import devtest.Bank.Transaction;

public class BankTest extends TestCase {


    Account account1;
    Account account2;
    Bank bank;

    public void setUp() {
        account1 = new Account(11111L);
        account2 = new Account(22222L);
        bank = new Bank();
    }

    private boolean makeInitialDeposits() {
        Transaction deposit1 = Transaction.createDeposit(account1, 1000, TestDate.JAN1_2001);
        Transaction deposit2 = Transaction.createDeposit(account2, 2000, TestDate.JAN1_2001);
        boolean result = bank.applyTransaction(deposit1);
        return result && bank.applyTransaction(deposit2);
    }

    private boolean makeTransfer() {
        Transaction transfer = Transaction.createTransfer(account1, account2, 500, TestDate.JAN5_2001);
        return bank.applyTransaction(transfer);
    }

    public void testGetAccountNumber() {
        assertEquals(11111L, account1.getAccountNumber());
        assertEquals(22222L, account2.getAccountNumber());
    }
    
    public void testApplyTransaction_Deposit() {
        assertTrue(makeInitialDeposits());
    }

    public void testApplyTransaction_Transfer() {
        makeInitialDeposits();
        assertTrue(makeTransfer());
    }

    public void testApplyTransaction_Withdrawl() {
        makeInitialDeposits();

        Transaction withdrawl = Transaction.createWithdrawl(account1, 300, TestDate.JAN5_2001);
        assertTrue(bank.applyTransaction(withdrawl));
    }

    public void testApplyTransaction_Transfer_InsufficientBalance() {
        makeInitialDeposits();

        Transaction validTransfer = Transaction.createTransfer(account2, account1, 500, TestDate.JAN5_2001);
        assertTrue(bank.applyTransaction(validTransfer));

        Transaction invalidTransfer = Transaction.createTransfer(account1, account2, 2000, TestDate.JAN8_2001);
        assertFalse(bank.applyTransaction(invalidTransfer));
    }

    public void testApplyTransaction_Withdrawl_InsufficientBalance() {
        makeInitialDeposits();

        Transaction validWithdrawl = Transaction.createWithdrawl(account1, 300, TestDate.JAN5_2001);
        assertTrue(bank.applyTransaction(validWithdrawl));

        Transaction invalidWithdrawl = Transaction.createWithdrawl(account1, 3000, TestDate.JAN8_2001);
        assertFalse(bank.applyTransaction(invalidWithdrawl));
    }

    public void testGetBalanceAtTime() {
        makeInitialDeposits();

        assertEquals(1000, account1.getBalanceAtTime(TestDate.JAN3_2001));
        assertEquals(2000, account2.getBalanceAtTime(TestDate.JAN3_2001));

        makeTransfer();

        assertEquals(500, account1.getBalanceAtTime(TestDate.JAN8_2001));
        assertEquals(2500, account2.getBalanceAtTime(TestDate.JAN8_2001));
    }

    public void testGetBalanceAtTime_ExactDateInclusive() {
        makeInitialDeposits();

        assertEquals(1000, account1.getBalanceAtTime(TestDate.JAN5_2001));
        assertEquals(2000, account2.getBalanceAtTime(TestDate.JAN5_2001));

        makeTransfer();

        assertEquals(500, account1.getBalanceAtTime(TestDate.JAN5_2001));
        assertEquals(2500, account2.getBalanceAtTime(TestDate.JAN5_2001));
    }

    public void testGetBalanceAtTime_MultipleTransactions() {
        makeInitialDeposits();
        makeInitialDeposits();
        makeInitialDeposits();

        assertEquals(3000, account1.getBalanceAtTime(TestDate.JAN3_2001));
        assertEquals(6000, account2.getBalanceAtTime(TestDate.JAN3_2001));

        makeTransfer();

        assertEquals(2500, account1.getBalanceAtTime(TestDate.JAN8_2001));
        assertEquals(6500, account2.getBalanceAtTime(TestDate.JAN8_2001));
    }

    // START OF INTEREST RELATED TESTCASES

    public void testApplyInterestForYear_Basic() {
        Transaction deposit1 = Transaction.createDeposit(account1, 1000, TestDate.JAN1_2001);
        bank.applyTransaction(deposit1);
        account1.setInterestRateFromTime(0.025, TestDate.JAN1_2001);

        assertEquals(1000, account1.getBalanceAtTime(TestDate.JAN1_2002));

        bank.applyInterestForYear(account1, 2001);

        assertEquals(1025, account1.getBalanceAtTime(TestDate.JAN1_2002));
    }

    public void testApplyInterestForYear_MultipleInterests() {
        Transaction deposit1 = Transaction.createDeposit(account1, 1000, TestDate.JAN1_2001);
        bank.applyTransaction(deposit1);

        account1.setInterestRateFromTime(0.2, TestDate.JAN1_2001);
        account1.setInterestRateFromTime(0.015, TestDate.MAR1_2001);

        assertEquals(1000, account1.getBalanceAtTime(TestDate.JAN1_2002));

        bank.applyInterestForYear(account1, 2001);

        assertEquals(1045, account1.getBalanceAtTime(TestDate.JAN1_2002));
    }

    public void testApplyInterestForYear_MultipleTransactions() {
        Transaction deposit1 = Transaction.createDeposit(account1, 1000, TestDate.JAN1_2001);
        Transaction deposit2 = Transaction.createDeposit(account1, 4000, TestDate.MAR1_2001);
        Transaction deposit3 = Transaction.createDeposit(account1, 1630, TestDate.NOV11_2001);
        bank.applyTransaction(deposit1);
        bank.applyTransaction(deposit2);
        bank.applyTransaction(deposit3);

        account1.setInterestRateFromTime(0.08, TestDate.JAN1_2001);

        assertEquals(6630, account1.getBalanceAtTime(TestDate.JAN1_2002));

        bank.applyInterestForYear(account1, 2001);

        assertEquals(7000, account1.getBalanceAtTime(TestDate.JAN1_2002));
    }

    public void testApplyInterestForYear_MultipleYears() {
        Transaction deposit1 = Transaction.createDeposit(account1, 1000, TestDate.JAN1_2001);
        bank.applyTransaction(deposit1);

        account1.setInterestRateFromTime(0.2, TestDate.JAN1_2001);

        assertEquals(1000, account1.getBalanceAtTime(TestDate.JAN1_2002));

        bank.applyInterestForYear(account1, 2001);

        assertEquals(1200, account1.getBalanceAtTime(TestDate.JAN1_2002));

        bank.applyInterestForYear(account1, 2002);

        assertEquals(1440, account1.getBalanceAtTime(TestDate.JAN1_2003));
    }

    public void testApplyInterestForYear_Complex() {
        bank.applyTransaction(Transaction.createDeposit(account1, 1234, TestDate.JAN1_2001));
        bank.applyTransaction(Transaction.createDeposit(account1, 1234, TestDate.JAN8_2001));
        bank.applyTransaction(Transaction.createDeposit(account1, 4321, TestDate.MAR1_2001));
        bank.applyTransaction(Transaction.createDeposit(account1, 10, TestDate.NOV11_2001));
        bank.applyTransaction(Transaction.createDeposit(account1, 5000, TestDate.JAN1_2002));
        bank.applyTransaction(Transaction.createDeposit(account1, 400, TestDate.MAY5_2002));
        bank.applyTransaction(Transaction.createDeposit(account1, 3801, TestDate.JAN1_2003));

        account1.setInterestRateFromTime(0.47, TestDate.JAN1_2000);
        account1.setInterestRateFromTime(0.074, TestDate.JAN8_2001);
        account1.setInterestRateFromTime(0.017, TestDate.JAN1_2002);
        account1.setInterestRateFromTime(0.0188, TestDate.MAY5_2002);

        assertEquals(16000, account1.getBalanceAtTime(TestDate.APR5_2004));

        bank.applyInterestForYear(account1, 2001);
        bank.applyInterestForYear(account1, 2002);
        bank.applyInterestForYear(account1, 2003);
        
        assertEquals(17000, account1.getBalanceAtTime(TestDate.APR5_2004));
    }

    static {
        // the dates below are specified in UTC so force all
        // JVM's to use UTC as their timezone during testing
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    static class TestDate {
        static final Date JAN1_2000 = new Date(946684800000L);

        static final Date JAN1_2001 = new Date(978307200000L);
        static final Date JAN3_2001 = new Date(978480000000L);
        static final Date JAN5_2001 = new Date(978652800000L);
        static final Date JAN8_2001 = new Date(978912000000L);
        static final Date MAR1_2001 = new Date(983404800000L);
        static final Date MAY5_2001 = new Date(988675200000L);
        static final Date NOV11_2001 = new Date(1004572800000L);

        static final Date JAN1_2002 = new Date(1009843200000L);
        static final Date MAY5_2002 = new Date(1020556800000L);

        static final Date JAN1_2003 = new Date(1041379200000L);
        static final Date APR5_2004 = new Date(1081123200000L);
    }
}
