package tech.mayanksoni.safebrowsing.utils;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.Soundex;

public class PhoneticUtils {
    private static final Soundex SOUNDEX = new Soundex();
    private static final Metaphone METAPHONE = new Metaphone();
    private static final DoubleMetaphone DOUBLE_METAPHONE = new DoubleMetaphone();

    public static String encodeSoundex(String inputString) {
        return SOUNDEX.encode(inputString);
    }

    public static String encodeMetaphone(String inputString) {
        return METAPHONE.encode(inputString);
    }

    public static String encodeDoubleMetaphone(String inputString) {
        return DOUBLE_METAPHONE.encode(inputString);
    }

    public static boolean similarSoundingWithSoundex(String input1, String input2) {
        return encodeSoundex(input1).equals(encodeSoundex(input2));
    }

    public static boolean similarSoundingWithMetaphone(String input1, String input2) {
        return encodeMetaphone(input1).equals(encodeMetaphone(input2));
    }

    public static boolean similarSoundingWithDoubleMetaphone(String input1, String input2) {
        return encodeDoubleMetaphone(input1).equals(encodeDoubleMetaphone(input2));
    }
}
