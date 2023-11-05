package transact.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static transact.testutil.Assert.assertThrows;
import static transact.testutil.TypicalTransactions.APPLES;
import static transact.testutil.TypicalTransactions.BANANAS;
import static transact.testutil.TypicalTransactions.getTypicalTransactionBook;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.TransactionId;
import transact.testutil.TransactionBuilder;

public class TransactionBookTest {

    private final TransactionBook transactionBook = new TransactionBook();

    @Test
    public void constructor() {
        assertEquals(Collections.emptyList(), transactionBook.getTransactionList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> transactionBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyTransactionBook_replacesData() {
        TransactionBook newData = getTypicalTransactionBook();
        transactionBook.resetData(newData);
        assertEquals(newData, transactionBook);
    }

    @Test
    public void hasTransaction_nullTransaction_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> transactionBook.hasTransaction(null));
    }

    @Test
    public void hasTransaction_transactionNotInAddressBook_returnsFalse() {
        assertFalse(transactionBook.hasTransaction(APPLES.getTransactionId()));
    }

    @Test
    public void hasTransaction_transactionInAddressBook_returnsTrue() {
        transactionBook.addTransaction(APPLES);
        assertTrue(transactionBook.hasTransaction(APPLES.getTransactionId()));
    }

    @Test
    public void hasTransaction_transactionWithSameIdentityFieldsInAddressBook_returnsTrue() {
        transactionBook.addTransaction(BANANAS);
        Transaction editedTrans = new TransactionBuilder(BANANAS).withDescription("Banana 123").withAmount(123)
                .build();
        assertTrue(transactionBook.hasTransaction(editedTrans.getTransactionId()));
    }

    @Test
    public void toStringMethod() {
        String expected = TransactionBook.class.getCanonicalName() + "{transactions="
                + transactionBook.getTransactionMap() + "}";
        assertEquals(expected, transactionBook.toString());
    }

    /**
     * A stub ReadOnlyTransactionBook whose transactions list can violate interface
     * constraints.
     */
    private static class TransactionBookStub implements ReadOnlyTransactionBook {
        private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
        private final ObservableMap<TransactionId, Transaction> transactionMap = FXCollections.observableHashMap();

        TransactionBookStub(Collection<Transaction> transactions) {
            this.transactionList.setAll(transactions);
        }

        @Override
        public ObservableList<Transaction> getTransactionList() {
            return transactionList;
        }

        @Override
        public ObservableMap<TransactionId, Transaction> getTransactionMap() {
            return transactionMap;
        }
    }
}
