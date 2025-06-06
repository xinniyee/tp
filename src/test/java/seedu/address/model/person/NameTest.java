package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
        // different valid names with special characters
        assertTrue(Name.isValidName("Sinéad O'Connor"));
        assertTrue(Name.isValidName("Nagaratnam s/o Suppiah"));
        assertTrue(Name.isValidName("Tan Cheng Bok @ Adrian Tan"));
        assertTrue(Name.isValidName("Anne-Marie"));
        assertTrue(Name.isValidName("Beyoncé"));
        assertTrue(Name.isValidName("J.R. Smith"));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }

    @Test
    public void constructor_validNameWithExtraSpaces_normalizesName() {
        // leading and trailing spaces
        Name nameWithExtraSpaces = new Name("   John Doe   ");
        assertTrue(nameWithExtraSpaces.toString().equals("John Doe"));

        // multiple spaces between words
        nameWithExtraSpaces = new Name("John   Doe");
        assertTrue(nameWithExtraSpaces.toString().equals("John Doe"));

        // multiple spaces between words and leading/trailing spaces
        nameWithExtraSpaces = new Name("   John   Doe   ");
        assertTrue(nameWithExtraSpaces.toString().equals("John Doe"));

        // multiple consecutive spaces
        nameWithExtraSpaces = new Name("John           Doe");
        assertTrue(nameWithExtraSpaces.toString().equals("John Doe"));
    }
}
