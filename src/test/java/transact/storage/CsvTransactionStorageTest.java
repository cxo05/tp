package transact.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static transact.testutil.TransactionUtil.clearTransactionBookTransactionIdUniqueness;
import static transact.testutil.TypicalTransactions.TRANSACTION_EXPENSE;
import static transact.testutil.TypicalTransactions.TRANSACTION_REVENUE;
import static transact.testutil.TypicalTransactions.getTypicalTransactionBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import transact.commons.exceptions.DataLoadingException;
import transact.model.ReadOnlyTransactionBook;
import transact.model.TransactionBook;

public class CsvTransactionStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "CsvTransactionStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readTransactionBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readTransactionBook(null));
    }

    private java.util.Optional<ReadOnlyTransactionBook> readTransactionBook(String filePath) throws Exception {
        return new CsvTransactionBookStorage(Paths.get(filePath))
                .readTransactionBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readTransactionBook("NonExistentFile.csv").isPresent());
    }

    @Test
    public void readTransactionBook_invalidTransaction_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readTransactionBook("invalidTransactionStorage.csv"));
    }

    @Test
    public void readTransactionBook_invalidAndValidTransaction_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readTransactionBook("invalidAndValidTransactionStorage.csv"));
    }

    @Test
    public void readAndSaveTransactionBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempTransactionBook.csv");
        TransactionBook original = getTypicalTransactionBook();
        CsvTransactionBookStorage csvTransactionBookStorage = new CsvTransactionBookStorage(filePath);
        clearTransactionBookTransactionIdUniqueness(original);

        // Save in new file and read back
        csvTransactionBookStorage.saveTransactionBook(original, filePath);
        ReadOnlyTransactionBook readBack = csvTransactionBookStorage.readTransactionBook(filePath).get();
        assertEquals(original, new TransactionBook(readBack));

        // Modify data, overwrite exiting file, and read back
        original.addTransaction(TRANSACTION_EXPENSE);
        clearTransactionBookTransactionIdUniqueness(original);
        csvTransactionBookStorage.saveTransactionBook(original, filePath);
        readBack = csvTransactionBookStorage.readTransactionBook(filePath).get();
        assertEquals(original, new TransactionBook(readBack));

        // Save and read without specifying file path
        original.addTransaction(TRANSACTION_REVENUE);
        clearTransactionBookTransactionIdUniqueness(original);
        csvTransactionBookStorage.saveTransactionBook(original); // file path not specified
        readBack = csvTransactionBookStorage.readTransactionBook().get(); // file path not specified
        assertEquals(original, new TransactionBook(readBack));

    }

    @Test
    public void saveTransactionBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTransactionBook(null, "SomeFile.csv"));
    }

    /**
     * Saves {@code transactionBook} at the specified {@code filePath}.
     */
    private void saveTransactionBook(ReadOnlyTransactionBook transactionBook, String filePath) {
        try {
            new CsvTransactionBookStorage(Paths.get(filePath))
                    .saveTransactionBook(transactionBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveTransactionBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTransactionBook(new TransactionBook(), null));
    }
}
