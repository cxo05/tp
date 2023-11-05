package transact.storage;

import static java.util.Objects.requireNonNull;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import transact.commons.exceptions.DataLoadingException;
import transact.commons.util.FileUtil;
import transact.logic.parser.ParserUtil;
import transact.model.ReadOnlyTransactionBook;
import transact.model.TransactionBook;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.Amount;
import transact.model.transaction.info.Date;
import transact.model.transaction.info.Description;
import transact.model.transaction.info.TransactionId;
import transact.model.transaction.info.TransactionType;

/**
 * A class to access TransactionBook data stored as a csv file on the hard disk.
 */
public class CsvTransactionBookStorage implements TransactionBookStorage {

    private Path filePath;

    /**
     * Creates CsvAdaptedTransactionStorage Object
     *
     * @param filePath
     *            for storing data
     */
    public CsvTransactionBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Path getTransactionBookFilePath() {
        return filePath;
    }

    @Override
    public Optional<ReadOnlyTransactionBook> readTransactionBook() throws DataLoadingException {
        return readTransactionBook(filePath);
    }

    @Override
    public Optional<ReadOnlyTransactionBook> readTransactionBook(Path path) throws DataLoadingException {
        requireNonNull(filePath);

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try {
            TransactionBook transactions = new TransactionBook();

            CSVReader reader = new CSVReader(new FileReader(path.toFile()));

            reader.readNext(); // Skip the header

            String[] row;
            while ((row = reader.readNext()) != null) {
                try {
                    TransactionId transactionId = new TransactionId(row[0]);
                    TransactionType transactionType = ParserUtil.parseType(row[1]);
                    Description description = ParserUtil.parseDescription(row[2]);
                    Amount amount = ParserUtil.parseAmount(row[3]);
                    Date date = ParserUtil.parseDate(row[4]);
                    Integer personId = ParserUtil.parsePersonId(row[5]);

                    Transaction t = new Transaction(transactionId, transactionType, description, amount, date,
                            personId);

                    transactions.addTransaction(t);

                } catch (Exception e) {
                    // Skip if error with a line
                    // TODO Warn user of malformed data
                    throw new DataLoadingException(e);
                }
            }

            return Optional.of(new TransactionBook(transactions));
        } catch (IOException e) {
            throw new DataLoadingException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveTransactionBook(ReadOnlyTransactionBook transactionBook) throws IOException {
        saveTransactionBook(transactionBook, filePath);
    }

    /**
     * Similar to {@link #saveTransactionBook(ReadOnlyTransactionBook)}
     * (ReadOnlyTransactionBook)}.
     *
     * @param filePath
     *            location of the data. Cannot be null.
     */
    public void saveTransactionBook(ReadOnlyTransactionBook transactionBook, Path filePath) throws IOException {
        requireNonNull(transactionBook);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        Map<TransactionId, Transaction> transactions = transactionBook.getTransactionMap();

        FileWriter fileWriter = new FileWriter(filePath.toFile());
        CSVWriter writer = new CSVWriter(fileWriter);

        String[] header = { "TransactionId", "Type", "Description", "Amount", "Date", "Person" };
        writer.writeNext(header);

        for (Transaction transaction : transactions.values()) {
            String transactionId = transaction.getTransactionId().toString();
            String transactionType = transaction.getTransactionType().toString();
            String description = transaction.getDescription().toString();
            String amount = transaction.getAmount().toString();
            String date = transaction.getDate().toString();
            String personId = transaction.getPersonId().toString();

            String[] row = { transactionId, transactionType, description, amount, date, personId };

            writer.writeNext(row);
        }
        writer.close();
        fileWriter.close();
    }
}
