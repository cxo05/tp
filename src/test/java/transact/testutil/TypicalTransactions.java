package transact.testutil;

import static transact.testutil.TypicalPersons.ALICE;

import transact.model.TransactionBook;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.Amount;
import transact.model.transaction.info.Date;
import transact.model.transaction.info.Description;
import transact.model.transaction.info.TransactionType;

/**
 * A utility class containing a list of {@code Transaction} objects to be used
 * in tests.
 */
public class TypicalTransactions {
    public static final Transaction APPLES = new Transaction(TransactionType.EXPENSE,
            new Description("Apples"),
            new Amount(1.50), new Date("15/11/18"), ALICE.getPersonId().getValue());
    public static final Transaction BANANAS = new Transaction(TransactionType.EXPENSE,
            new Description("Bananas"),
            new Amount(1.50), new Date("15/11/18"));
    public static final Transaction CARROTS = new Transaction(TransactionType.EXPENSE,
            new Description("Carrots"),
            new Amount(1.50), new Date("15/11/18"));
    public static final Transaction DATES = new Transaction(TransactionType.EXPENSE,
            new Description("Dates"),
            new Amount(4.50), new Date("15/11/18"));
    public static final Transaction EGGS = new Transaction(TransactionType.EXPENSE,
            new Description("Eggs"),
            new Amount(5.00), new Date("15/11/18"));
    public static final Transaction FISH = new Transaction(TransactionType.REVENUE,
            new Description("Fish"),
            new Amount(6.50), new Date("15/11/18"));

    // Manually Added
    public static final Transaction TRANSACTION_REVENUE = new Transaction(TransactionType.REVENUE,
            new Description("Revenue"),
            new Amount(10.50), new Date("15/11/18"));
    public static final Transaction TRANSACTION_EXPENSE = new Transaction(TransactionType.EXPENSE,
            new Description("Expense"),
            new Amount(5.50), new Date("15/11/18"));

    private TypicalTransactions() {
    } // prevents instantiation

    /**
     * Returns a {@code TransactionBook} with all the typical transactions.
     */
    public static TransactionBook getTypicalTransactionBook() {
        TransactionBook tb = new TransactionBook();
        for (Transaction transaction : getTypicalTransactions()) {
            tb.addTransaction(transaction);
        }
        return tb;
    }

    public static Transaction[] getTypicalTransactions() {
        return new Transaction[] { APPLES, BANANAS, CARROTS, DATES, EGGS, FISH };
    }
}
