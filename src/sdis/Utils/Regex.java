package sdis.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle regular expressions
 */
public class Regex {
    private Pattern p;
    private Matcher m;

    /**
     * Create a regular expression that accepts a pattern
     * @param pattern
     */
    public Regex(String pattern){
        p = Pattern.compile(pattern);
        m = null;
    }

    /**
     * Create a regular expression that accepts all characters
     * except newlines and line breaks
     */
    public Regex(){
        p = Pattern.compile(".*");
        m = null;
    }

    /**
     * Get this regular expression's pattern
     * @return The pattern
     */
    public String getPattern(){
        return p.pattern();
    }

    /**
     * Sets the regular expression's pattern
     * @param pattern The pattern
     */
    public void setPattern(String pattern){
        p = Pattern.compile(pattern);
    }

    /**
     * Checks if a String sequence matches the regular expression's
     * pattern
     * @param sequence The sequence to test
     * @return
     */
    public boolean check(String sequence){
        m = p.matcher(sequence);
        return m.matches();
    }

    /**
     * Gets the matching groups that match  the pattern given a
     * certain String sequence
     * @param sequence The sequence to test
     * @return Array of matching groups
     */
    public String[] getGroups(String sequence){
        String[] groups = new String[0];
        m = p.matcher(sequence);
        if (m.find()){
            groups = new String[m.groupCount()];
            for (int i = 0; i < m.groupCount(); i++){
                groups[i] = m.group(i+1);
            }
        }
        return groups;
    }
}
