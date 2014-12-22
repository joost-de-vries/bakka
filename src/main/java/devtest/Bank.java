package devtest;

import java.util.*;

/**
 * Control class to apply transactions and interest to Account's
 */
public class Bank {

    /**
     * Manipulate the amount on both Accounts based on the given Transaction
     * 
     * @param transaction
     *            the transaction to make
     * @return true only if the transaction was possible and is executed
     */
    public boolean applyTransaction(Transaction transaction) {
        switch (transaction.getType()) {
        case INTEREST:
        case WITHDRAWL:
        case DEPOSIT:
            return transaction.getAccount().addTransaction(transaction);

        case TRANSFER:
            // TODO: IMPLEMENT
        }
        return false;
    }

    /**
     * Applies the calculated interest to the account by adding an INTEREST transaction on the last day of that year
     * 
     * @param account
     *            the account to apply interest for
     * @param year
     *            the year to apply interest for
     */
    public void applyInterestForYear(Account account, int year) {
        long interest = account.calculateInterestForYear(year);
        Date lastDayOfYear = null; // TODO: IMPLEMENT
        Transaction interestTransaction = Transaction.createInterest(account, interest, lastDayOfYear);
        applyTransaction(interestTransaction);
    }

    /**
     * class to represent an account. <br>
     * <br>
     * State changes are based upon adding transactions and interest rate changes to it. <br>
     * Its main feature is to answer queries about its balance at a current point in time
     * 
     * @see Account#getBalanceAtTime(Date)
     */
    public static class Account {
        long accountNumber;

        public Account(long accountNumber) {
            this.accountNumber = accountNumber;
        }

        public long getAccountNumber() {
            return accountNumber;
        }

        /**
         * Set the rate of interest to apply from the given date onwards until another interestRate applies
         * 
         * @param interestRate
         *            the (new) rate of interest to apply once a year on the account (i.e. 0.011 = 1.1% interest per
         *            year)
         * @param time
         *            from when this rate should be applied
         */
        public void setInterestRateFromTime(double interestRate, Date time) {
            // TODO: IMPLEMENT
        }

        /**
         * Get the amount of balance available on this account at the given point in time
         * 
         * @param time
         *            the point in time (inclusive)
         * @return the amount on the given time
         */
        public long getBalanceAtTime(Date time) {
            // TODO: IMPLEMENT
            return 0;
        }

        /**
         * Tries to add the transaction to the account (given that the account has sufficient balance)
         * 
         * @param transaction
         * @return true only if the transaction was possible and is added
         */
        public boolean addTransaction(Transaction transaction) {
            // TODO: IMPLEMENT
            return false;
        }

        /**
         * Calculates the total interest to be gained for an Account over the given year <br>
         * <br>
         * The total interest is defined as followed:<br>
         * <ul>
         * <li>interest is gained for any balance on the account within the given year</li>
         * <li>interest is applied against the applicable rate for that period in time</li>
         * <li>interest rates are flat and virtual until applied, no interest upon interest applies within the same year
         * </li>
         * <li>proper financial rounding is applied but over the total amount of interest only</li>
         * <li>default (JVM) timezone is used for any date related calculations</li>
         * </ul>
         * 
         * <br>
         * <br>
         * i.e. an account that has<br>
         * <ul>
         * <li>1000 balance for half a year</li>
         * <li>0 balance for the other half of the year</li>
         * <li>and an interest rate of 0.0123 (1.23%) for the entire year</li>
         * </ul>
         * <br>
         * should result in a total interest of 1000 * 0.5 * 0.0123 = 6 <br>
         * (using 0.5 because the 1000 was only for half a year on the account)
         * 
         * <br>
         * <br>
         * or an account that has<br>
         * <ul>
         * <li>1000 balance for the whole year</li>
         * <li>and an interest rate of 0.0123 (1.23%) set from the second half of the year</li>
         * </ul>
         * <br>
         * should result in a total interest of 1000 * 0.5 * 0.0123 = 6 <br>
         * (using 0.5 because the interest only applies for the second half of the year)
         * 
         * @param year
         *            the year over which to calculate interest
         */
        public long calculateInterestForYear(int year) {
            // TODO: IMPLEMENT
            return 0;
        }
    }

    /**
     * POJO for a transaction, has factory methods for creation
     */
    public static class Transaction {
        enum Type {
            TRANSFER, WITHDRAWL, DEPOSIT, INTEREST
        }

        private Account account;
        private Account contraAccount;
        private long amount;
        private Date time;
        private Type type;

        private Transaction(Account account, Account contraAccount, long amount, Date time, Type type) {
            if (amount < 0) {
                throw new IllegalArgumentException("amount should never be a negative number");
            }
            this.account = account;
            this.contraAccount = contraAccount;
            this.amount = amount;
            this.time = time;
            this.type = type;
        }

        public Account getAccount() {
            return account;
        }

        public Account getContraAccount() {
            return contraAccount;
        }

        public long getAmount() {
            return amount;
        }

        public Date getTime() {
            return time;
        }

        public Type getType() {
            return type;
        }

        public static Transaction createTransfer(Account account, Account contraAccount, long amount, Date time) {
            return new Transaction(account, contraAccount, amount, time, Type.TRANSFER);
        }

        public static Transaction createDeposit(Account account, long amount, Date time) {
            return new Transaction(account, null, amount, time, Type.DEPOSIT);
        }

        public static Transaction createWithdrawl(Account account, long amount, Date time) {
            return new Transaction(account, null, amount, time, Type.WITHDRAWL);
        }

        public static Transaction createInterest(Account account, long amount, Date time) {
            return new Transaction(account, null, amount, time, Type.INTEREST);
        }

        @Override
        public String toString() {
            return getAmount() + " @ " + getTime();
        }
    }
}