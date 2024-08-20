package mojo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * This class is finished with a little help of chatGPT and modified manually to make
 * if work correctly.
 */



public class ParseEscape {
    public static String replaceEscapes(String input) {
        /*
          Escape = "\" "a"   | "\" "b"   | "\" "f"   | "\" "n"   | "\" "r"
                 | "\" "t"   | "\" "v"   | "\" "\"   | "\" "'"   | "\" "\""
                 | "\" ( "0" | "1" | "2" | "3" ) OctalDigit OctalDigit
                 | "\x" HexDigit HexDigit
                 | "\\u" HexDigit HexDigit HexDigit HexDigit
                 | "\U" HexDigit HexDigit HexDigit HexDigit HexDigit HexDigit
         */
        String parsedString = input.replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\b", "\b")
                .replaceAll("\\\\f", "\f")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\v", "\u000B")
                .replaceAll("\\\\\\\\", "\\\\")
                .replaceAll("\\\\'", "'")
                .replaceAll("\\\\a", "\u0007");

        // Hex digit sequence \x
        Pattern hexPattern = Pattern.compile("\\\\x([0-9a-fA-F]{2})");
        Matcher hexMatcher = hexPattern.matcher(parsedString);
        StringBuffer hexResult = new StringBuffer();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            char ch = (char) Integer.parseInt(hex, 16);
            hexMatcher.appendReplacement(hexResult, String.valueOf(ch));
        }
        hexMatcher.appendTail(hexResult);

        // unicode sequence \\u
        Pattern unicodePattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher unicodeMatcher = unicodePattern.matcher(hexResult.toString());
        StringBuffer unicodeResult = new StringBuffer();
        while (unicodeMatcher.find()) {
            String hex = unicodeMatcher.group(1);
            char ch = (char) Integer.parseInt(hex, 16);
            unicodeMatcher.appendReplacement(unicodeResult, String.valueOf(ch));
        }
        unicodeMatcher.appendTail(unicodeResult);

        // unicode sequence \U
        Pattern unicodeBigPattern = Pattern.compile("\\\\U([0-9a-fA-F]{6})");
        Matcher unicodeBigMatcher = unicodeBigPattern.matcher(unicodeResult.toString());
        StringBuffer unicodeBigResult = new StringBuffer();
        try {
            while (unicodeBigMatcher.find()) {
                String hex = unicodeBigMatcher.group(1);
                int ch = Integer.parseInt(hex, 16);
                unicodeBigMatcher.appendReplacement(unicodeBigResult, String.valueOf(Character.toChars(ch)));
            }
            unicodeBigMatcher.appendTail(unicodeBigResult);
        } catch (Exception e) {
            unicodeBigMatcher.appendTail(unicodeBigResult);
        }

        // octal digit sequence \[0-3][0-7]{2}
        Pattern octalPattern = Pattern.compile("\\\\([0-3][0-7]{2})");
        Matcher octalMatcher = octalPattern.matcher(unicodeBigResult.toString());
        StringBuffer octalResult = new StringBuffer();
        while (octalMatcher.find()) {
            String octal = octalMatcher.group(1);
            char ch = (char) Integer.parseInt(octal, 8);
            octalMatcher.appendReplacement(octalResult, String.valueOf(ch));
        }
        octalMatcher.appendTail(octalResult);

        return octalResult.toString();
    }
}

