package devtest.scala

import java.util.Date

/**
 * Control class to apply transactions and interest to Account's
 */
object Bank {

  /**
   * class to represent an account. <br>
   * <br>
   * State changes are based upon adding transactions and interest rate changes to it. <br>
   * Its main feature is to answer queries about its balance at a current point in time
   *
   * @see Account#getBalanceAtTime(Date)
   */
  class Account {
    private[devtest] var accountNumber: Long = 0L

    def this(accountNumber: Long) {
      this()
      this.accountNumber = accountNumber
    }

    def getAccountNumber: Long = {
      return accountNumber
    }

    /**
     * Set the rate of interest to apply from the given date onwards until another interestRate applies
     *
     * @param interestRate
         * the (new) rate of interest to apply once a year on the account (i.e. 0.011 = 1.1% interest per
     *     year)
     * @param time
         * from when this rate should be applied
     */
    def setInterestRateFromTime(interestRate: Double, time: Date) {
    }

    /**
     * Get the amount of balance available on this account at the given point in time
     *
     * @param time
         * the point in time (inclusive)
     * @return the amount on the given time
     */
    def getBalanceAtTime(time: Date): Long = {
      return 0
    }

    /**
     * Tries to add the transaction to the account (given that the account has sufficient balance)
     *
     * @param transaction
     * @return true only if the transaction was possible and is added
     */
    def addTransaction(transaction: Bank.Transaction): Boolean = {
      return false
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
         * the year over which to calculate interest
     */
    def calculateInterestForYear(year: Int): Long = {
      return 0
    }
  }

  /**
   * POJO for a transaction, has factory methods for creation
   */
  object Transaction {

    private[devtest] object Type extends Enumeration {
      type Type = Value
      val TRANSFER, WITHDRAWL, DEPOSIT, INTEREST = Value
    }

    def createTransfer(account: Bank.Account, contraAccount: Bank.Account, amount: Long, time: Date): Bank.Transaction = {
      return new Bank.Transaction(account, contraAccount, amount, time, Type.TRANSFER)
    }

    def createDeposit(account: Bank.Account, amount: Long, time: Date): Bank.Transaction = {
      return new Bank.Transaction(account, null, amount, time, Type.DEPOSIT)
    }

    def createWithdrawl(account: Bank.Account, amount: Long, time: Date): Bank.Transaction = {
      return new Bank.Transaction(account, null, amount, time, Type.WITHDRAWL)
    }

    def createInterest(account: Bank.Account, amount: Long, time: Date): Bank.Transaction = {
      return new Bank.Transaction(account, null, amount, time, Type.INTEREST)
    }
  }

  class Transaction {
    private var account: Bank.Account = null
    private var contraAccount: Bank.Account = null
    private var amount: Long = 0L
    private var time: Date = null
    private var `type`: Bank.Transaction#Type = null

    private def this(account: Bank.Account, contraAccount: Bank.Account, amount: Long, time: Date, `type`: Bank.Transaction#Type) {
      this()
      if (amount < 0) {
        throw new IllegalArgumentException("amount should never be a negative number")
      }
      this.account = account
      this.contraAccount = contraAccount
      this.amount = amount
      this.time = time
      this.`type` = `type`
    }

    def getAccount: Bank.Account = {
      return account
    }

    def getContraAccount: Bank.Account = {
      return contraAccount
    }

    def getAmount: Long = {
      return amount
    }

    def getTime: Date = {
      return time
    }

    def getType: Bank.Transaction#Type = {
      return `type`
    }

    override def toString: String = {
      return getAmount + " @ " + getTime
    }
  }

}

class Bank {
  /**
   * Manipulate the amount on both Accounts based on the given Transaction
   *
   * @param transaction
     * the transaction to make
   * @return true only if the transaction was possible and is executed
   */
  def applyTransaction(transaction: Bank.Transaction): Boolean = {
    transaction.getType match {
      case INTEREST =>
      case WITHDRAWL =>
      case DEPOSIT =>
        return transaction.getAccount.addTransaction(transaction)
      case TRANSFER =>
    }
    return false
  }

  /**
   * Applies the calculated interest to the account by adding an INTEREST transaction on the last day of that year
   *
   * @param account
     * the account to apply interest for
   * @param year
     * the year to apply interest for
   */
  def applyInterestForYear(account: Bank.Account, year: Int) {
    val interest: Long = account.calculateInterestForYear(year)
    val lastDayOfYear: Date = null
    val interestTransaction: Bank.Transaction = Bank.Transaction.createInterest(account, interest, lastDayOfYear)
    applyTransaction(interestTransaction)
  }
}