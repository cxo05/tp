package transact.testutil;

import transact.model.TransactionBook;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.TransactionId;

/**
 * A utility class for Transaction.
 */
public class TransactionUtil {
    /**
     * Clears the {@code TransactionId} used by {@code transactionBook} to avoid
     * duplicate IDs when testing
     */
    public static void clearTransactionBookTransactionIdUniqueness(TransactionBook transactionBook) {
        for (Transaction transaction : transactionBook.getTransactionList()) {
            TransactionId.freeUsedTransactionIds(transaction.getTransactionId().getValue());
        }
    }
}
