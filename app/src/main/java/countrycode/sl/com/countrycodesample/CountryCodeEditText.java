/*
 * No Copyright Free to modify and reuse :)
 */

package countrycode.sl.com.countrycodesample;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountryCodeEditText extends EditText {

    private static final String LOG_TAG = "CountryCodeEditText";

    /**
     * JSON files constants
     */
    private static final String JSON_KEY_COUNTRIES     = "countries";
    private static final String JSON_KEY_NAME          = "fullName";
    private static final String KEY_SHORTNAME          = "shortName";
    private static final String JSON_KEY_LOCALE        = "locale";
    private static final String JSON_KEY_TELEPHONYCODE = "telephonyCode";
    private static final String COUNTRIES_JSON_FILE    = "countries.json";

    private static final String CHARSET_NAME         = "UTF-8";
    private static final String EMPTY_TELEPHONY_CODE = "NA";
    private static final String PLUS                 = "+";

    private AlertDialog       countryDialog;
    private List<CountryData> allCountries, displayList;
    private String             mCountryTelphonyCode;
    private CountryListAdapter countryListAdapter;

    public CountryCodeEditText(Context context) {
        super(context);
        init();
    }

    public CountryCodeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CountryCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        this.setFocusableInTouchMode(false);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        allCountries = readMobileDataCountriesJSONFile();
        displayList = new ArrayList<CountryData>();
        displayList.addAll(allCountries);
        //countryDialog = initDialog();
        this.setOnClickListener(clickListener);
        setUserCurrentSimCountry();
    }

    private AlertDialog initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        countryListAdapter = new CountryListAdapter(this.getContext(), displayList);
        View inflate = LayoutInflater.from(this.getContext()).inflate(R.layout.country_code_picker_dialog_layout, null);
        ((EditText) inflate.findViewById(R.id.search_country_name)).addTextChangedListener(new textChangeListener(this));
        ((ListView) inflate.findViewById(R.id.county_list)).setAdapter(countryListAdapter);
        inflate.findViewById(R.id.cancel_button).setOnClickListener(cancelListener);
        builder.setView(inflate);
        return builder.create();
    }

    private void setUserCurrentSimCountry() {
        TelephonyManager manager = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String countryTelephonyCode = null;
        if (manager != null && allCountries != null && allCountries.size() > 0) {
            if (manager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
                String countryIso = manager.getSimCountryIso().toLowerCase();
                if (!TextUtils.isEmpty(countryIso)) {
                    for (CountryData country : allCountries) {
                        if (countryIso.equals(country.getLocale().toLowerCase())) {
                            countryTelephonyCode = country.getCountryTelephonyCode();
                            break;
                        }
                    }
                }
            }
        }

        if (TextUtils.isEmpty(countryTelephonyCode)) {
            countryTelephonyCode = PLUS;
        } else {
            countryTelephonyCode = PLUS + countryTelephonyCode;
        }

        mCountryTelphonyCode = countryTelephonyCode;
        CountryCodeEditText.this.setText(mCountryTelphonyCode);
    }

    private DialogInterface.OnDismissListener dismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            Log.i(LOG_TAG, "Set " + mCountryTelphonyCode);
            CountryCodeEditText.this.setText(PLUS + mCountryTelphonyCode);
        }
    };

    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            displayList.clear();
            displayList.addAll(allCountries);
            countryDialog = initDialog();
            countryDialog.setOnDismissListener(dismissListener);
            countryDialog.show();
        }
    };

    private OnClickListener cancelListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (countryDialog != null && countryDialog.isShowing()) {
                countryDialog.dismiss();
            }
        }
    };

    private class textChangeListener implements TextWatcher {
        final CountryCodeEditText editText;

        textChangeListener(CountryCodeEditText mobileEditText) {
            this.editText = mobileEditText;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                String searchText = editable.toString();
                displayList.clear();
                if (!searchText.isEmpty() /*|| this.editText.allCountries == null*/) {
                    CharSequence toLowerCase = searchText.toLowerCase();
                    if (allCountries != null) {
                        for (CountryData mobileDataCountry : allCountries) {
                            if (mobileDataCountry.getCountryFullName().toLowerCase().contains(toLowerCase) || mobileDataCountry.getCountryShortName().toLowerCase().contains(toLowerCase)) {
                                displayList.add(mobileDataCountry);
                            }
                        }
                    }
                } else {
                    this.editText.displayList.addAll(allCountries);
                }
                this.editText.countryListAdapter.notifyDataSetChanged();
            }
        }
    }

    public void setCountryList(List<CountryData> countryList) {
        allCountries = countryList;
        //displayList = new ArrayList<CountryData>();
        //displayList.addAll(allCountries);
        //countryDialog = initDialog();
        //countryDialog.setOnDismissListener(dismissListener);
    }

    private List<CountryData> readMobileDataCountriesJSONFile() {
        List<CountryData> countryList = new ArrayList<>();
        try {
            JSONArray jSONArray = new JSONObject(loadJSONFromAsset()).getJSONArray(JSON_KEY_COUNTRIES);
            int i = 0;
            while (jSONArray != null && i < jSONArray.length()) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                countryList.add(new CountryData(jSONObject.getString(JSON_KEY_NAME), jSONObject.getString(KEY_SHORTNAME), jSONObject.getString(JSON_KEY_LOCALE), jSONObject.getString(JSON_KEY_TELEPHONYCODE)));
                i++;
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing Countries.json");
        }
        Log.i(LOG_TAG, " " + Arrays.toString(countryList.toArray()));
        return countryList;
    }

    private String loadJSONFromAsset() {
        try {
            InputStream open = CountryCodeEditText.this.getContext().getAssets().open(COUNTRIES_JSON_FILE);
            byte[] bArr = new byte[open.available()];
            open.read(bArr);
            open.close();
            return new String(bArr, CHARSET_NAME);
        } catch (IOException e) {
            return null;
        }
    }

    private class CountryListAdapter extends BaseAdapter {
        private final List<CountryData> displayList;
        private       Context           context;

        public CountryListAdapter(Context context, List<CountryData> countries) {
            this.context = context;
            this.displayList = countries;
        }

        public int getCount() {
            return this.displayList.size();
        }

        public CountryData getItem(int i) {
            return this.displayList.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.country_list_item_row, null);
            }
            CountryData item = getItem(i);
            ((TextView) view.findViewById(R.id.country_row_item_full_name)).setText(item.getCountryFullName() + " (" + item.getCountryShortName() + ")");
            TextView textView = (TextView) view.findViewById(R.id.country_row_item_telephony_code);
            String countryTelephonyCode = item.getCountryTelephonyCode();
            textView.setVisibility(View.VISIBLE);
            textView.setText("+" + countryTelephonyCode);
            view.setOnClickListener(new rowClickListener(this, item, countryTelephonyCode));
            return view;
        }

        private class rowClickListener implements OnClickListener {
            private String code;

            public rowClickListener(CountryListAdapter adapter, CountryData item, String code) {
                this.code = code;
            }

            @Override
            public void onClick(View view) {
                mCountryTelphonyCode = this.code;
                if (countryDialog != null && countryDialog.isShowing()) {
                    countryDialog.dismiss();
                }
                Log.i(LOG_TAG, " " + mCountryTelphonyCode);
                CountryCodeEditText.this.setText("+" + mCountryTelphonyCode);
            }
        }
    }
}
