/*
 * No Copyright Free to modify and reuse :)
 */

package countrycode.sl.com.countrycodesample;

public class CountryData {
    private final String countryFullName;
    private final String countryShortName;
    private final String locale;
    private final String countryTelephonyCode;

    public CountryData(String str, String str2, String str3, String str4) {
        this.countryFullName = str;
        this.countryShortName = str2;
        this.locale = str3;
        this.countryTelephonyCode = str4;
    }

    public String getCountryShortName() {
        return this.countryShortName;
    }

    public String getCountryFullName() {
        return this.countryFullName;
    }

    public String getCountryTelephonyCode() {
        return this.countryTelephonyCode;
    }

    public String getLocale() {
        return this.locale;
    }

    public String toString() {
        return "{countryFullName:" + this.countryFullName + ", countryShortName:" + this.countryShortName + ", locale:" + this.locale + ", countryTelephonyCode:" + this.countryTelephonyCode + "}";
    }
}
