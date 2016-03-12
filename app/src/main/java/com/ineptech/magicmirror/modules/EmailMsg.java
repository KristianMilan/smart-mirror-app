package com.ineptech.magicmirror.modules;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;

import android.app.LoaderManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
//import android.support.v4.app.LoaderManager;

import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.Utils;

/**
 * Created by Stefano on 10-Mar-16.
 */
public class EmailMsg extends Module {

    private static final long timeBetweenCalls = 1 * 60 * 1000; // Only update every 1 minute
    long lastRan = 0;
    int consecFails = 0;
    public String mUrl; // list of stock tickers currently configured to be displayed
    public String mEmailAccount;
    public String mEmailPasswod;
    final String prefsUrl = "EmailMsg";
    final String defaultUrl = "";



    public EmailMsg() {
        super("EmailMsg Module");
        desc = "This module would, one day, fetch and display data from gmail account";
        defaultTextSize = 40;
        sampleString = "Arbitrary email account";
        mUrl = "";
        mEmailAccount = "";//"trentanniepassa@gmail.com";
        mEmailPasswod = "";//"trentanni";
        loadConfig();
    }

    private void loadConfig() {
        mUrl = prefs.get(prefsUrl, defaultUrl);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        prefs.set(prefsUrl, mUrl);
        prefs.set(prefsUrl+"_emailAccount", mEmailAccount);
        prefs.set(prefsUrl+"_emailPasswod", mEmailPasswod);
    }

    @Override
    public void makeConfigLayout() {
        super.makeConfigLayout();

        // add a display of each item in the map
        if (mUrl.length() > 0) {
            Button remove = new Button(MainApplication.getContext());
            remove.setText("X");
            remove.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mUrl = "";
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout holder = new LinearLayout(MainApplication.getContext());
            holder.setOrientation(LinearLayout.HORIZONTAL);
            TextView hdtv = new TextView(MainApplication.getContext());
            hdtv.setText(mUrl);
            holder.addView(hdtv);
            holder.addView(remove);
            configLayout.addView(holder);
        } else {        // widgets for adding a new Url
//        if (mUrl.length() == 0) {
            final EditText addurl = new EditText(MainApplication.getContext());
            addurl.setText("http://yoursite.com/page.txt");
            Button plus = new Button(MainApplication.getContext());
            plus.setText("+");
            plus.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mUrl = addurl.getText().toString();
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout holder = new LinearLayout(MainApplication.getContext());
            holder.setOrientation(LinearLayout.HORIZONTAL);
            holder.addView(plus);
            holder.addView(addurl);
            configLayout.addView(holder);
        }

        if (mEmailAccount.length() > 0) {
            Button remove = new Button(MainApplication.getContext());
            remove.setText("X");
            remove.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mEmailAccount = "";
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholder = new LinearLayout(MainApplication.getContext());
            addholder.setOrientation(LinearLayout.HORIZONTAL);
            TextView hdtv = new TextView(MainApplication.getContext());
            hdtv.setText(mEmailAccount);
            addholder.addView(hdtv);
            addholder.addView(remove);
            configLayout.addView(addholder);
        } else {
            final EditText addemail = new EditText(MainApplication.getContext());
            addemail.setText("trentanniepassa@gmail.com");
            Button plusEmail = new Button(MainApplication.getContext());
            plusEmail.setText("Add email");
            plusEmail.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mEmailAccount = addemail.getText().toString();
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholder = new LinearLayout(MainApplication.getContext());
            addholder.setOrientation(LinearLayout.HORIZONTAL);
            addholder.addView(plusEmail);
            addholder.addView(addemail);
            configLayout.addView(addholder);
        }

        if (mEmailPasswod.length() > 0) {
            Button remove = new Button(MainApplication.getContext());
            remove.setText("X");
            remove.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mEmailPasswod = "";
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholderPsw = new LinearLayout(MainApplication.getContext());
            addholderPsw.setOrientation(LinearLayout.HORIZONTAL);
            TextView hdtv = new TextView(MainApplication.getContext());
            hdtv.setText(mEmailAccount);
            addholderPsw.addView(hdtv);
            addholderPsw.addView(remove);
            configLayout.addView(addholderPsw);
        } else {
            final EditText addpsw = new EditText(MainApplication.getContext());
            addpsw.setText("trentanni");
            Button plusPsw = new Button(MainApplication.getContext());
            plusPsw.setText("Add password");
            plusPsw.setOnClickListener
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mEmailPasswod = addpsw.getText().toString();
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholderPsw = new LinearLayout(MainApplication.getContext());
            addholderPsw.setOrientation(LinearLayout.HORIZONTAL);
            addholderPsw.addView(plusPsw);
            addholderPsw.addView(addpsw);
            configLayout.addView(addholderPsw);
        }


    }

    public void update() {
        if (consecFails > 9) {
            tv.setText("");
            tv.setVisibility(TextView.GONE);
        } else if (Calendar.getInstance().getTimeInMillis() > (lastRan + timeBetweenCalls)) {
            Log.i("TestApp", "doin stuff here 00");
            tv.setVisibility(TextView.VISIBLE);
            new EmailMsgTask(this).execute();
        }
    }

    public void newText(String s) {
        // for now, just overwrite every time this is called
        Spanned span = Html.fromHtml(s);
        tv.setText(span);
        tv.setText(s);
    }
}

class EmailMsgTask extends AsyncTask <Void, Void, String>{

    private EmailMsg module;

    public EmailMsgTask(EmailMsg _module) {
        module = _module;
    }

    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String[] FEATURES_MAIL = {"service_mail"};
    static final String TAG = "TestApp";


    @Override
    protected String doInBackground(Void... params) {

        Log.i(TAG, "doin stuff here 1");
        // Get the account list, and pick the first one
        Log.i(TAG, "doin stuff here 2"+MainApplication.getContext());
        AccountManager.get(MainApplication.getContext()).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL,
                new AccountManagerCallback<Account[]>() {
                    @Override
                    public void run(AccountManagerFuture<Account[]> future) {
                        Log.i(TAG, "doin stuff here 3"+future);
                        Account[] accounts = null;
                        try {
                            accounts = future.getResult();
                            Log.i(TAG, "doin stuff here 4"+accounts);
                        } catch (OperationCanceledException oce) {
                            Log.e(TAG, "Got OperationCanceledException", oce);
                        } catch (IOException ioe) {
                            Log.e(TAG, "Got OperationCanceledException", ioe);
                        } catch (AuthenticatorException ae) {
                            Log.e(TAG, "Got OperationCanceledException", ae);
                        }
                        onAccountResults(accounts);
                    }
                }, null /* handler */);


        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();

        String text = "";

        if (module.mUrl.length() > 0) {
            try {
                String urlStr = module.mUrl;
                URL url = new URL(urlStr);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
                url = uri.toURL();
                // the above looks convoluted, but is necessary to get the urlencoding correct
                HttpGet httpGet = new HttpGet(uri);
                HttpResponse response = httpClient.execute(httpGet, localContext);
                HttpEntity entity = response.getEntity();
                text += "\n" + Utils.getASCIIContentFromEntity(entity);

            } catch (Exception e) {	}
        }
        return text;
    }

    private void onAccountResults(Account[] accounts) {
        Log.i(TAG, "received accounts: " + Arrays.toString(accounts));
        Log.i(TAG, "accounts is null: "+ accounts);
        Log.i(TAG, "accoun lenth: " + accounts.length);
        if (accounts != null && accounts.length > 0) {
            // Pick the first one, and display a list of labels
            final String account = accounts[0].name;
            Log.i(TAG, "Starting loader for labels of account: " + account);
            final Bundle args = new Bundle();
            args.putString("account", account);
            //getSupportLoaderManager().restartLoader(0, args, this);
        }
    }


    protected void onPostExecute(String results) {
        if (results!=null) {
            module.newText(results);
        } else {
            module.consecFails++;
        }
    }
}
