import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ECE 325 - Fall 2020 <br/>
 * Assignment 2: Java regular expressions <br/>
 * Test cookies using regular expressions
 * <p>
 * @author <replace with your name>
 */
class CookieTest {

    private static String cookieString;
    private static String editedString;


    public static boolean checkCookieHttpAv(String av){
        String httpOnly = "HttpOnly";
        return Pattern.compile(httpOnly).matcher(av).matches();
    }
    public static boolean checkCookieSecureAv(String av){
        String secureAv = "Secure";
        return Pattern.compile(secureAv).matcher(av).matches();
    }
    public static boolean checkCookiePathAv(String av){
        String pathAv = "Path=" + "[^\\p{Cntrl}|^;]";
        return Pattern.compile(pathAv).matcher(av).matches();
    }
    public static boolean checkCookieDomainAv(String av){
        String digit = "[0-9]";
        String letter = "[A-Za-z]";
        String letDig = letter + "|" + digit;
        String ldhStr = "(" + letDig + "|" + "-" + ")" + "+";
        String label = letter + "(" + ldhStr + letDig + "|" + letDig + ")" + "?";
        String subDomain = label + "|" + "(" + label + "." + label + ")" +"*";
        String domain = subDomain + "|." + subDomain + "|" + "";
        String domainAV = "Domain=" + domain;

        return Pattern.compile(domainAV).matcher(av).matches();
    }

    public static boolean checkCookieAgeAv(String av){
        String ageAv = "Max-Age=" + "[1-9]" + "\\d*";
        return Pattern.compile(ageAv).matcher(av).matches();
    }
    public static boolean checkCookieExpiresAv(String av){
        String month = "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
        String wkDay = "(Mon|Tue|Wed|Thu|Fri|Sat|Sun)";
        String time = "\\d{2}:\\d{2}:\\d{2}";
        String date = "\\d{2} " + month + " " + "\\d{4}";
        String rfc1123Date = wkDay + ", " + date + " " + time + " GMT";
        String expireAv = "Expires=" + rfc1123Date;

        return Pattern.compile(expireAv).matcher(av).matches();
    }
    public static boolean checkCookieAv(){
        Matcher avSeprator = Pattern.compile("; ").matcher(editedString);
        ArrayList<String> avArray = new ArrayList<>();
        int startIndex;
        if (avSeprator.find()){
            avArray.add(editedString.substring(0, avSeprator.start()));
            startIndex = avSeprator.end();
            while (avSeprator.find()){
                avArray.add(editedString.substring(startIndex, avSeprator.start()));
                startIndex = avSeprator.end();
            }
            avArray.add(editedString.substring(startIndex));
        }else{
            avArray.add(editedString);
        }
        boolean result;
        for (String av : avArray){
            result = checkCookieAgeAv(av);
            if (!result) result = checkCookieDomainAv(av);
            if (!result) result = checkCookieExpiresAv(av);
            if (!result) result = checkCookieHttpAv(av);
            if (!result) result = checkCookiePathAv(av);
            if (!result) result = checkCookieSecureAv(av);
            if (!result) return false;
        }

        return true;
    }

    public static boolean checkCookieOctet(String octet){
        Matcher validOctet = Pattern.compile("[^\\p{ASCII}]|\\p{Cntrl}| \"|,|;|\\\\").matcher(octet);

        return !validOctet.find();
    }

    public static boolean checkCookieValue(){
        boolean result;
        String octet;
        Matcher octetFinder = Pattern.compile("; ").matcher(editedString);
        if (octetFinder.find()){
            octet = editedString.substring(0, octetFinder.start());
            editedString = editedString.substring(octetFinder.end());
        }else{
            octet = editedString;
            editedString = "";
        }

        if (!octet.isEmpty() && octet.charAt(0) == '\"'){
            if (octet.charAt(octet.length()-1) == '\"'){
                octet = octet.substring(1, octet.length()-2);
                result = checkCookieOctet(octet);
            }else{
                result = false;
            }
        }else{
            result = checkCookieOctet(octet);
        }

        return result;
    }
    public static boolean verifyToken(String token){
        boolean result = false;
        if (!token.isEmpty()){
            Matcher invalidChars = Pattern.compile("\\p{Cntrl}\\\\\\?\\(\\)<>@,;:/\\\"\\[\\]=\\{\\}\\t\\s").
                    matcher(token);
            if (!invalidChars.find()){
                result = true;
            }

        }
        return result;
    }
    public static boolean checkCookiePair(){
        Matcher equalSign = Pattern.compile("=").matcher(editedString);
        boolean result = false;
        if (equalSign.find()){
            result = verifyToken(editedString.substring(0, equalSign.end()));
            editedString = editedString.substring(equalSign.end());
        }
        if (result){
            result = checkCookieValue();
        }

        return result;
    }
    //useless
    public static boolean checkCookieString(){
        return true;
    }

    public static boolean checkCookieHeader(){
        //Pattern p = Pattern.compile("Set-Cookie: \\w+=((\\w|\\s|\"|,|;|\\\\)|\"(\\w|\\s|\"|,|;|\\\\)\")*");
        //Matcher m = p.matcher(cookie);
        Pattern header = Pattern.compile("Set-Cookie: ");
        Matcher matcher = header.matcher(cookieString);

        if (matcher.find(0)){
            editedString = cookieString.substring(matcher.end());
            System.out.println(editedString);
            return true;
        }else{
            return false;
        }
    }


    /**
     * Verify a cookie and return the verification result
     * @param cookie  The cookie string
     * @return        True for a legal cookie; false for an illegal one
     */
    public static boolean verifyCookie(String cookie) {
        cookieString = cookie;
        boolean legal;

        //todo: check cookie header
        legal = checkCookieHeader();

        //todo: check cookie pair
        if (legal){
            legal = checkCookiePair();
        }
        //todo: check *( ";" SP cookie-av )
        if (legal){
            legal = checkCookieAv();
        }
        //todo: check for cookie-av

        return legal;
    }

    /**
     * Main entry
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        String [] cookies = {
            // Legal cookies:
            "Set-Cookie: ns1=\"alss/0.foobar^\"",                                           // 01 name=value
            "Set-Cookie: ns1=",                                                             // 02 empty value
            "Set-Cookie: ns1=\"alss/0.foobar^\"; Expires=Tue, 18 Nov 2008 16:35:39 GMT",    // 03 Expires=time_stamp
            "Set-Cookie: ns1=; Domain=",                                                    // 04 empty domain
            "Set-Cookie: ns1=; Domain=.srv.a.com-0",                                        // 05 Domain=host_name
            "Set-Cookie: lu=Rg3v; Expires=Tue, 18 Nov 2008 16:35:39 GMT; Path=/; Domain=.example.com; HttpOnly", // 06
            // Illegal cookies:
            "Set-Cookie:",                                              // 07 empty cookie-pair
            "Set-Cookie: sd",                                           // 08 illegal cookie-pair: no "="
            "Set-Cookie: =alss/0.foobar^",                              // 09 illegal cookie-pair: empty name
            "Set-Cookie: ns@1=alss/0.foobar^",                          // 10 illegal cookie-pair: illegal name
            "Set-Cookie: ns1=alss/0.foobar^;",                          // 11 trailing ";"
            "Set-Cookie: ns1=; Expires=Tue 18 Nov 2008 16:35:39 GMT",   // 12 illegal Expires value
            "Set-Cookie: ns1=alss/0.foobar^; Max-Age=01",               // 13 illegal Max-Age: starting 0
            "Set-Cookie: ns1=alss/0.foobar^; Domain=.0com",             // 14 illegal Domain: starting 0
            "Set-Cookie: ns1=alss/0.foobar^; Domain=.com-",             // 15 illegal Domain: trailing non-letter-digit
            "Set-Cookie: ns1=alss/0.foobar^; Path=",                    // 16 illegal Path: empty
            "Set-Cookie: ns1=alss/0.foobar^; httponly",                 // 17 lower case
        };


        for (int i = 0; i < cookies.length; i++) {
            System.out.println(String.format("Cookie %2d: %s", i + 1, verifyCookie(cookies[i]) ? "Legal" : "Illegal"));
        }

    }

}
