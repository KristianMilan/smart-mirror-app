package com.ineptech.magicmirror.modules;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

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
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
//import android.support.v4.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ineptech.magicmirror.MainApplication;
import com.ineptech.magicmirror.Utils;
import com.sun.mail.imap.IMAPFolder;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.*;

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
    final String defaultAcc = "";
    final String defaultPsw = "";



    public EmailMsg() {
        super("EmailMsg Module");
        desc = "This module would, one day, fetch and display data from gmail account";
        defaultTextSize = 40;
        sampleString = "Arbitrary email account";
        mUrl          = "";
        mEmailAccount = "";//"trentanniepassa@gmail.com";
        mEmailPasswod = "";//"trentanni";
        loadConfig();
    }

    private void loadConfig() {
        mUrl          = prefs.get(prefsUrl, defaultUrl);
        mEmailAccount = prefs.get(prefsUrl+"_emailAccount", defaultAcc);
        mEmailPasswod = prefs.get(prefsUrl+"_emailPassword", defaultPsw);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        prefs.set(prefsUrl, mUrl);
        prefs.set(prefsUrl+"_emailAccount", mEmailAccount);
        prefs.set(prefsUrl+"_emailPassword", mEmailPasswod);
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
            addurl.setText("http://guhu.website/mirrordisplay.html");//http://ineptech.com/test.html");
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
            hdtv.setText(mEmailPasswod);
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

//    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
//    private static final String[] FEATURES_MAIL = {"service_mail"};
    static final String TAG = "TestApp";



    @Override
    protected String doInBackground(Void... params) {

        FolderFetchIMAP newm = new FolderFetchIMAP();
        Log.i(TAG, "doin stuff here with newm " + newm);


        // Get the account list, and pick the first one
        Log.i(TAG, "doin stuff here 2"+MainApplication.getContext());
//        AccountManager.get(MainApplication.getContext()).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL,
//                new AccountManagerCallback<Account[]>() {
//                    @Override
//                    public void run(AccountManagerFuture<Account[]> future) {
//                        Mail m = new Mail("stefano286@gmail.com", "Lhouse2806");
//
//                        String[] toArr = {"stefano286@gmail.com"};
//                        m.setTo(toArr);
//                        m.setFrom("stefano286@gmail.com");
//                        m.setSubject("This is an email sent using my Mail JavaMail wrapper from an Android device.");
//                        m.setBody("Email body.");
//                        try {
//                            if(m.send()) { Log.i("MailApp", "Email was sent successfully"); }
//                            else {         Log.i("MailApp", "Email was not sent"); }
//                        } catch(Exception e) { Log.i("MailApp", "Could not send email", e); }


//                        Account[] accounts = null;
//                        try { accounts = future.getResult(); }
//                        catch (OperationCanceledException oce) { Log.e(TAG, "Got OperationCanceledException", oce);}
//                        catch (IOException ioe)                { Log.e(TAG, "Got OperationCanceledException", ioe);}
//                        catch (AuthenticatorException ae)      { Log.e(TAG, "Got OperationCanceledException", ae); }
//                        onAccountResults(accounts);
//                    }
//                }, null /* handler */);

        String text = "";
        if (module.mUrl.length() > 0) { try {  } catch (Exception e) {	} }
        return text;
    }

    protected void onPostExecute(String results) {
        if (results!=null) {
            module.newText(results);
        } else {
            module.consecFails++;
        }
    }

    private String[] ReadMailbox(String MailboxName) throws IOException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", "imap.gmail.com");
        props.setProperty("mail.imaps.port", "993");
        IMAPFolder ActiveMailbox = null;
        List<String> FromAddressArrList = new ArrayList<String>();

        try {
            Session session = Session.getInstance(props);
            Store store = session.getStore();
            store.connect("imap.gmail.com", "stefano286@gmail.com", "Lhouse2806");
            ActiveMailbox = (IMAPFolder) store.getFolder(MailboxName);
            ActiveMailbox.open(Folder.READ_ONLY);
            Message[] messages = ActiveMailbox.getMessages();
            //System.out.println("Number of mails = " + messages.length);
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                Address[] from = message.getFrom();
                FromAddressArrList.add(from[0].toString());
            }
            //ActiveMailbox.close(true);
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        String[] FromAddressArr = new String[FromAddressArrList.size()];
        FromAddressArrList.toArray(FromAddressArr);
        return FromAddressArr;
    }

}




