import java.util.regex.Pattern;

public class StringNormalizer {

    private static final Pattern spaces = Pattern.compile("\\s+");

    public String normalizeString(String string) {

        if (string == null || string.trim().isEmpty()) {
            return "";
        }

        return spaces.matcher(string.trim()).replaceAll(" ");
    }
}