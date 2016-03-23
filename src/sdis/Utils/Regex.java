package sdis.Utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
    private Pattern p;
    private Matcher m;

    public Regex(String pattern){
        p = Pattern.compile(pattern);
        m = null;
    }

    public Regex(){
        p = Pattern.compile(".*");
        m = null;
    }

    public String getPattern(){
        return p.pattern();
    }

    public void setPattern(String pattern){
        p = Pattern.compile(pattern);
    }

    public boolean check(String sequence){
        m = p.matcher(sequence);
        return m.matches();
    }

    public ArrayList<String> getGroups(String sequence){
        ArrayList<String> groups = new ArrayList<>();
        m = p.matcher(sequence);
        if (!m.find()){
            return groups;
        }
        for (int i = 0; i < m.groupCount(); i++){
            groups.add(m.group(i+1));
        }
        return groups;
    }
}
