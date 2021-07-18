package ro.jmind.photos;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static List<String> parseDescription(String description) {
        List<String> result;
        description = description.replace("\n\n", ", ");
        Pattern pattern = Pattern.compile("\n");
        Matcher matcher = pattern.matcher(description);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        if (description.contains("•")) {
            description = description.replace("•", "");
            result = splitDescriptionBy(description, "\n");
        } else if (count > 2) {
            result = splitDescriptionBy(description, "\n");
        } else {
            result = splitDescriptionBy(description, ", ");
        }
        return result;
    }

    public static List<String> splitDescriptionBy(String value, String splitString) {
        return Stream.of(value.split(splitString, -1))
                .map(s -> s.trim())
                .map(s -> {
                    if (s.length() > 1 && s.substring(0, 1).equals("-")) {
                        return s.substring(1);
                    }
                    return s;
                })
                .map(s -> {
                    if (s.length() < 1) {
                        return s;
                    }
                    String lastChar = s.substring(s.length() - 1);
                    if (lastChar.equals(";") || lastChar.equals(".")) {
                        return s.substring(0, s.length() - 1);
                    }
                    return s;
                })
                .map(s -> StringUtils.capitalize(s))
                .collect(Collectors.toList());
    }
}