package cyberspacelabs.ru.crosshairmobile;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TextSanitation {
    @Test
    public void textSanitationSucceeded() throws Exception {
        String raw = "^^55exy ^2Chick^";
        String san = "^5exy Chick^";
        Assert.assertEquals(san, sanitizeQuakeColors(raw));
    }

    private String sanitizeQuakeColors(String source){
        StringBuilder result = new StringBuilder();
        char[] chars = source.toCharArray();
        for(int i = 0; i < chars.length; i++){
            char current = chars[i];
            if (current != '^'){ result.append(current); continue;}
            if (i == chars.length -1){result.append(current); continue;}
            if (Character.isDigit(chars[i + 1])){i = i+1; continue;}
            result.append(current);
        }
        return result.toString();
    }
}