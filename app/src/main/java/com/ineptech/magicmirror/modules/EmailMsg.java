package com.ineptech.magicmirror.modules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import android.util.Log;

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

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;


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
    final String defaultAcc = "";
    final String defaultPsw = "";



    public EmailMsg() {
        super("EmailMsg Module");
        desc = "This module fetches and displays data from gmail account";
        defaultTextSize = 40;
        mEmailAccount = "";//"trentanniepassa@gmail.com";
        mEmailPasswod = "";//"trentanni";
        loadConfig();
    }

    private void loadConfig() {
        mEmailAccount = prefs.get(prefsUrl+"_emailAccount", defaultAcc);
        mEmailPasswod = prefs.get(prefsUrl+"_emailPassword", defaultPsw);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        prefs.set(prefsUrl+"_emailAccount", mEmailAccount);
        prefs.set(prefsUrl+"_emailPassword", mEmailPasswod);
    }

    @Override
    public void makeConfigLayout() {
        super.makeConfigLayout();

        // add a textbox for email and another for psw
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

    static final String TAG = "TestApp";



    @Override
    protected String doInBackground(Void... params) {

        if (module.mEmailAccount.length()>0 & module.mEmailPasswod.length()>0) { try {  } catch (Exception e) {	} }
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.host", "imap.gmail.com");
        props.setProperty("mail.imaps.port", "993");
        List<String> FromAddressArrList = new ArrayList<String>();
        Folder ActiveMailbox;

        try {
            Session session = Session.getInstance(props);
            session.setDebug(true);
            Store store = session.getStore();
            store.connect("imap.gmail.com", module.mEmailAccount,module.mEmailPasswod);// "stefano286@gmail.com", "Lhouse2806");
            ActiveMailbox = store.getFolder("INBOX");
            ActiveMailbox.open(Folder.READ_ONLY);
            javax.mail.Message[] messages = ActiveMailbox.getMessages();
            for (int i = messages.length-1; i > messages.length-3; i--) { //messages.length
                javax.mail.Message msg = messages[i];
                javax.mail.Address[] from = msg.getFrom();
                FromAddressArrList.add(from[0].toString());

                Log.i("Read Email","Subject: " + msg.getSubject());
                Log.i("Read Email","From: "    + msg.getFrom()[0]);
                Log.i("Read Email","To: "      + msg.getAllRecipients()[0]);
                Log.i("Read Email","Date: "    + msg.getReceivedDate());
                Log.i("Read Email","Size: "    + msg.getSize());
                //Log.i("Read Email", "flag"     + msg.getFlags());
                // Log.i("Read Email","Body: \n"   + msg.getContent());
                Log.i("Read Email", msg.getContentType());
            }
            //ActiveMailbox.close(true);
            store.close();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        String[] FromAddressArr = new String[FromAddressArrList.size()];
        FromAddressArrList.toArray(FromAddressArr);


        String text = FromAddressArrList.toString();//"";
        return text;
    }

    protected void onPostExecute(String results) {
        if (results!=null) {
            module.newText(results);
        } else {
            module.consecFails++;
        }
    }

//    private String[] ReadMailbox(String MailboxName) throws IOException {
//        Properties props = new Properties();
//        props.setProperty("mail.imap.ssl.enable", "true");
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.host", "imap.gmail.com");
//        props.setProperty("mail.imaps.port", "993");
//        List<String> FromAddressArrList = new ArrayList<String>();
//
//        try {
//            Session session = Session.getInstance(props);
//            Store store = session.getStore();
//            store.connect("imap.gmail.com", "stefano286@gmail.com", "Lhouse2806");
//            Folder ActiveMailbox = store.getDefaultFolder();
//            ActiveMailbox.open(Folder.READ_ONLY);
//            javax.mail.Message[] messages = ActiveMailbox.getMessages();
//            //System.out.println("Number of mails = " + messages.length);
//            for (int i = 0; i < 10; i++) { //messages.length
//                javax.mail.Message msg = messages[i];
//                javax.mail.Address[] from = msg.getFrom();
//                FromAddressArrList.add(from[0].toString());
//
//                Log.i("Read Email","Subject: " + msg.getSubject());
//                Log.i("Read Email","From: " + msg.getFrom()[0]);
//                Log.i("Read Email","To: "+msg.getAllRecipients()[0]);
//                Log.i("Read Email","Date: "+msg.getReceivedDate());
//                Log.i("Read Email","Size: "+msg.getSize());
//                Log.i("Read Email","flag"+msg.getFlags());
//                Log.i("Read Email","Body: \n"+ msg.getContent());
//                Log.i("Read Email", msg.getContentType());
//            }
//            //ActiveMailbox.close(true);
//            store.close();
////        } catch (NoSuchProviderException e) {
////            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//
//        String[] FromAddressArr = new String[FromAddressArrList.size()];
//        FromAddressArrList.toArray(FromAddressArr);
//        return FromAddressArr;
//    }

}




