package transact.storage;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import transact.commons.exceptions.DataLoadingException;
import transact.model.ReadOnlyTransactionBook;
import transact.model.TransactionBook;
import transact.model.person.Address;
import transact.model.person.Email;
import transact.model.person.Name;
import transact.model.person.Person;
import transact.model.person.Phone;
import transact.model.transaction.Transaction;
import transact.model.transaction.info.Amount;
import transact.model.transaction.info.Date;
import transact.model.transaction.info.Description;
import transact.model.transaction.info.TransactionId;
import transact.model.transaction.info.TransactionType;

public class CsvAdaptedTransactionStorageManualTest {
    public static void main(String[] args) {
        Path filePath = Path.of("transactions.csv");

        ReadOnlyTransactionBook transactionBook = createTestTransactionBook();

        CsvTransactionBookStorage storage = new CsvTransactionBookStorage(filePath);

        try {
            storage.saveTransactionBook(transactionBook);
            System.out.println("Transaction log saved successfully.");

            Optional<ReadOnlyTransactionBook> readTransactionBook = storage.readTransactionBook();
            if (readTransactionBook.isPresent()) {
                System.out.println("Transaction log loaded successfully.");
                System.out.println("Loaded Transaction Book:");
                System.out.println(readTransactionBook.get());
            } else {
                System.out.println("No transaction log found.");
            }
            storage.saveTransactionBook(transactionBook);
            System.out.println("Transaction log saved successfully.");
        } catch (IOException | DataLoadingException e) {
            e.printStackTrace();
        }
    }

    private static ReadOnlyTransactionBook createTestTransactionBook() {
        Person person1 = new Person(new Name("John"), new Phone("43124312"), new Email("SSS@gmail.com"),
                new Address("PGP"), Collections.emptySet());
        Transaction transaction1 = new Transaction(new TransactionId(100), TransactionType.REVENUE,
                new Description("Revenue 1"),
                new Amount(new BigDecimal("10.50")), new Date("01/05/23"), person1.getPersonId().getValue());
        Transaction transaction2 = new Transaction(new TransactionId(101), TransactionType.EXPENSE,
                new Description("Expense 1"),
                new Amount(new BigDecimal("21.50")), new Date("01/05/23"));
        Transaction transaction3 = new Transaction(new TransactionId(102), TransactionType.REVENUE,
                new Description("Revenue 2"),
                new Amount(new BigDecimal("10.50")), new Date("01/05/23"));
        Transaction transaction4 = new Transaction(new TransactionId(103), TransactionType.EXPENSE,
                new Description("Expense 2"),
                new Amount(new BigDecimal("25.21")), new Date("01/05/23"));
        Transaction transaction5 = new Transaction(new TransactionId(104), TransactionType.REVENUE,
                new Description("Revenue 3"),
                new Amount(new BigDecimal("31.1")), new Date("01/05/23"));
        Transaction transaction6 = new Transaction(new TransactionId(105), TransactionType.EXPENSE,
                new Description("Expense 3"),
                new Amount(new BigDecimal("21.02")), new Date("01/05/23"));

        TransactionBook transactionBook = new TransactionBook();
        transactionBook.addTransaction(transaction1);
        transactionBook.addTransaction(transaction2);
        transactionBook.addTransaction(transaction3);
        transactionBook.addTransaction(transaction4);
        transactionBook.addTransaction(transaction5);
        transactionBook.addTransaction(transaction6);

        return transactionBook;
    }
}
