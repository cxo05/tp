package transact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import transact.commons.core.Config;
import transact.commons.core.LogsCenter;
import transact.commons.core.Version;
import transact.commons.exceptions.DataLoadingException;
import transact.commons.util.ConfigUtil;
import transact.commons.util.StringUtil;
import transact.logic.Logic;
import transact.logic.LogicManager;
import transact.model.AddressBook;
import transact.model.Model;
import transact.model.ModelManager;
import transact.model.ReadOnlyAddressBook;
import transact.model.ReadOnlyTransactionBook;
import transact.model.ReadOnlyUserPrefs;
import transact.model.TransactionBook;
import transact.model.UserPrefs;
import transact.model.util.SampleDataUtil;
import transact.storage.AddressBookStorage;
import transact.storage.CsvTransactionBookStorage;
import transact.storage.JsonAddressBookStorage;
import transact.storage.JsonUserPrefsStorage;
import transact.storage.Storage;
import transact.storage.StorageManager;
import transact.storage.TransactionBookStorage;
import transact.storage.UserPrefsStorage;
import transact.ui.Ui;
import transact.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 3, 0, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static HostServices hostServices;

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing AddressBook ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());
        initLogging(config);

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        AddressBookStorage addressBookStorage = new JsonAddressBookStorage(userPrefs.getAddressBookFilePath());
        TransactionBookStorage transactionBookStorage = new CsvTransactionBookStorage(
                userPrefs.getTransactionBookFilePath());
        storage = new StorageManager(addressBookStorage, transactionBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic);

        hostServices = getHostServices();
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address
     * book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if
     * {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading
     * {@code storage}'s address book.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file for address book: " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialAddressData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (!addressBookOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample AddressBook.");
            }
            initialAddressData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getAddressBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty AddressBook.");
            initialAddressData = new AddressBook();
        }

        logger.info("Using data file for transaction book: " + storage.getTransactionBookFilePath());

        Optional<ReadOnlyTransactionBook> transactionBookOptional;
        ReadOnlyTransactionBook initialTransactionData;
        try {
            transactionBookOptional = storage.readTransactionBook();
            if (!transactionBookOptional.isPresent()) {
                logger.info("Creating a new data file " + storage.getTransactionBookFilePath()
                        + " populated with a sample TransactionBook.");
            }
            initialTransactionData = transactionBookOptional.orElseGet(SampleDataUtil::getSampleTransactionBook);
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getTransactionBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty TransactionBook.");
            initialTransactionData = new TransactionBook();
        }

        return new ModelManager(initialAddressData, initialTransactionData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            if (!configOptional.isPresent()) {
                logger.info("Creating new config file " + configFilePathUsed);
            }
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataLoadingException e) {
            logger.warning("Config file at " + configFilePathUsed + " could not be loaded."
                    + " Using default config properties.");
            initializedConfig = new Config();
        }

        // Update config file in case it was missing to begin with or there are
        // new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs
     * file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (!prefsOptional.isPresent()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        // Update prefs file in case it was missing to begin with or there are
        // new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting AddressBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Address Book ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }

    public static HostServices getLocalHostServices() {
        return hostServices;
    }
}
