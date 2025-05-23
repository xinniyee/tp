package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.FilteredPersonList;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final CommandHistory commandHistory;
    private final ObservableList<Person> personList;
    private final SortedList<Person> sortedFilteredPersons;
    private Predicate<Person> currentPredicate;
    private final ArrayList<ModelState> stateHistory;
    private int currentStatePointer;


    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        commandHistory = new CommandHistory();
        this.personList = addressBook.getPersonList();
        sortedFilteredPersons = new SortedList<>(filteredPersons);

        stateHistory = new ArrayList<>();

        // initialise the current predicate by default
        currentPredicate = PREDICATE_SHOW_ALL_PERSONS;

        // create default model state
        ModelState initState = new ModelState(addressBook, currentPredicate);

        stateHistory.add(initState);

        currentStatePointer = 0;
    }

    /**
     * Initializes a ModelManager with an empty address book and user preferences.
     */
    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    /**
     * Returns true if a person with the same identity as person exists in the address book.
     *
     * @param person The person to check for.
     * @return True if the person exists, false otherwise.
     */
    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    /**
     * Deletes the specified target person from the address book.
     *
     * @param target The person to delete.
     */
    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    /**
     * Adds the specified person to the address book.
     *
     * @param person The person to add.
     */
    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    /**
     * Replaces the target person with the edited person.
     *
     * @param target The person to be replaced.
     * @param editedPerson The person to replace with.
     */
    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    @Override
    public void pinPerson(Person person) {
        addressBook.pinPerson(person);
    }

    @Override
    public void unpinPerson(Person person) {
        addressBook.unpinPerson(person);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    /**
     * Updates the filtered person list based on the specified predicate.
     *
     * @param predicate The predicate to filter the persons.
     */
    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);

        currentPredicate = predicate;
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void addPastCommandInput(String rawCommandInput) {
        commandHistory.addInput(rawCommandInput);
    }

    @Override
    public ObservableList<String> getCommandHistoryList() {
        return commandHistory.getPastCommands();
    }

    //=========== Sorted Person List Accessors =============================================================

    /**
     * Updates the sorted person list based on the given prefix.The prefix is used to
     * filter the list of persons in the address book and sort the resulting filtered list.
     *
     * @param prefix The string prefix used to filter and sort the person list.
     * @throws NullPointerException if prefix is null.
     */
    @Override
    public void updateSortedPersonList(String... prefix) {
        requireNonNull(prefix);
        addressBook.updateSortedList(prefix);
    }

    /**
     * Updates the sorted person list based on the given prefix. The prefix is used to
     * filter the list of persons in the address book and sort the resulting filtered list.
     *
     * @param prefix The string prefix used to filter and sort the person list.
     * @throws NullPointerException if prefix is null.
     */
    @Override
    public void updateSortedFilteredPersonList(String... prefix) {
        requireNonNull(prefix);

        filteredPersons.setPredicate(currentPredicate);
        FilteredPersonList filteredPersonList = new FilteredPersonList(filteredPersons);
        filteredPersonList.sortByFilteredList(prefix);
    }

    @Override
    public void commit() {
        ModelState newState = new ModelState(new AddressBook(addressBook), currentPredicate);

        // commit current predicate
        removeAheadCurrent();
        stateHistory.add(newState);
        currentStatePointer += 1;

        logger.fine("Successfully committed the model state");
    }

    @Override
    public void undo() {

        // must have a last state to be undoable
        // this is the responsibility of the person using this function
        // throw unchecked error if not ensured
        if (currentStatePointer - 1 < 0) {
            throw new IndexOutOfBoundsException();
        }
        currentStatePointer -= 1;

        // get state
        ModelState pastState = stateHistory.get(currentStatePointer);
        requireNonNull(pastState);

        // set state
        addressBook.resetData(pastState.getAddressBookState());
        updateFilteredPersonList(pastState.getPredicate());

        logger.fine("Successfully undone the model state");
    }
    @Override
    public void redo() {
        // must have a undone state to be able to be redone
        // this is the responsibility of the person using this function
        // throw unchecked error if not ensured
        if (currentStatePointer + 1 >= stateHistory.size()) {
            throw new IndexOutOfBoundsException();
        }

        currentStatePointer += 1;

        // get state
        ModelState nextState = stateHistory.get(currentStatePointer);
        requireNonNull(nextState);

        // set state
        addressBook.resetData(nextState.getAddressBookState());
        updateFilteredPersonList(nextState.getPredicate());

        logger.fine("Successfully redone the undone model state");
    }

    /**
     * Removes all model states ahead of the current state
     */
    private void removeAheadCurrent() {
        int curSize = stateHistory.size();

        // remove AddressBooks ahead of the current book until none left
        for (int i = currentStatePointer + 1; i < curSize; i++) {
            stateHistory.remove(currentStatePointer + 1);
        }

        assert currentStatePointer == stateHistory.size() - 1;
    }

    @Override
    public boolean hasUndo() {
        return currentStatePointer - 1 >= 0;
    }

    @Override
    public boolean hasRedo() {
        return currentStatePointer + 1 < stateHistory.size();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager)) {
            return false;
        }

        ModelManager otherModelManager = (ModelManager) other;
        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                //&& filteredPersons.equals(otherModelManager.filteredPersons);
                && sortedFilteredPersons.equals(otherModelManager.sortedFilteredPersons)
                && currentPredicate.equals(otherModelManager.currentPredicate)
                && currentStatePointer == otherModelManager.currentStatePointer
                && stateHistory.equals(otherModelManager.stateHistory);
    }
}
