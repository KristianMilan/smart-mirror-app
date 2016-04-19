package com.ineptech.magicmirror.modules;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.ineptech.magicmirror.MainActivity;
import com.ineptech.magicmirror.MainApplication;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


/**
 * Created by Stefano on 10-Mar-16.
 */
public class EmailMsg extends Module {

    private static final long timeBetweenCalls = 3 * 60 * 1000; // Update every 3 minutes
    long lastRan = 0;
    int consecFails = 0;
    public String mEmailAccount;
    public String mEmailPasswod;
    public int  maxEmailDisplay;
    final String prefsEMsg  = "EmailMsg";
    final String defaultAcc = "";
    final String defaultPsw = "";
    final int    defaultEmD = 3;



    public EmailMsg() {
        super("GMAIL Messenger");
        desc = "This module fetches and displays email data from a gmail account.\n"
                +"Currently, the email Object is displayed. "
                +"With this module, you can send messages to a specified gmail account "
                +"you have access and see it displayed on the mirror (it updates every 3 minutes).\n"
                +"You can set the number of recent emails to be displayed.";
        defaultTextSize = 35;
        mEmailAccount   = "";
        mEmailPasswod   = "";
        maxEmailDisplay = defaultEmD;
        loadConfig();
    }

    private void loadConfig() {
        mEmailAccount   = prefs.get(prefsEMsg+"_emailAccount",  defaultAcc);
        mEmailPasswod   = prefs.get(prefsEMsg+"_emailPassword", defaultPsw);
        maxEmailDisplay = prefs.get(prefsEMsg+"_emailNumDispl", defaultEmD);
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        prefs.set(prefsEMsg+"_emailAccount",  mEmailAccount);
        prefs.set(prefsEMsg+"_emailPassword", mEmailPasswod);
        prefs.set(prefsEMsg+"_emailNumDispl", maxEmailDisplay);
    }

    @Override
    public void makeConfigLayout() {
        super.makeConfigLayout();

        // add a textbox to get user email-account
        if (mEmailAccount.length() > 0) {
            Button remove = new Button(MainApplication.getContext());
            remove.setText("X");
            remove.setOnClickListener // what to do when clicking X
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
            plusEmail.setText("Add G-mail ");
            plusEmail.setOnClickListener // what to do when clicking add
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            mEmailAccount = addemail.getText().toString();
                            if (!isValidEmailAddress(mEmailAccount)){//validate GMAIL account
                                mEmailAccount="";
                            }
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

        // add a textbox to get user password
        if (mEmailPasswod.length() > 0) {
            Button removePsw = new Button(MainApplication.getContext());
            removePsw.setText("X");
            removePsw.setOnClickListener // what to do when clicking X
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
            addholderPsw.addView(removePsw);
            configLayout.addView(addholderPsw);
        } else {
            final EditText addpsw = new EditText(MainApplication.getContext());
            addpsw.setText("trentanni");
            Button plusPsw = new Button(MainApplication.getContext());
            plusPsw.setText("Add password");
            plusPsw.setOnClickListener // what to do when clicking add
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

        // add a textbox to set max number of emails to fetch
        if (maxEmailDisplay > 0) {
            Button btn_change = new Button(MainApplication.getContext());
            btn_change.setText("X");
            btn_change.setOnClickListener // what to do when clicking X
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            Log.i("por","maxE "+maxEmailDisplay);
                            maxEmailDisplay = 0;
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholderNED = new LinearLayout(MainApplication.getContext());
            addholderNED.setOrientation(LinearLayout.HORIZONTAL);
            TextView hdtv = new TextView(MainApplication.getContext());
            hdtv.setText(""+maxEmailDisplay);
            addholderNED.addView(hdtv);
            addholderNED.addView(btn_change);
            configLayout.addView(addholderNED);
        } else {
            Log.i("por","0 maxE "+maxEmailDisplay);
            final EditText addNoEmailDisp = new EditText(MainApplication.getContext());
            addNoEmailDisp.setText(""+defaultEmD);
            Button plusNumE = new Button(MainApplication.getContext());
            plusNumE.setText("Add new value");
            plusNumE.setOnClickListener // what to do when clicking add
                    (new View.OnClickListener() {
                        public void onClick(View v) {
                            if ((addNoEmailDisp.getText().toString()).matches("^[+-]?\\d+$")){//make sure it is integer
                                maxEmailDisplay = Integer.parseInt(addNoEmailDisp.getText().toString().trim());
                                maxEmailDisplay = Math.max(maxEmailDisplay, 1);
                                maxEmailDisplay = Math.min(maxEmailDisplay, 20);
                            }
                            Log.i("por","2b maxE "+maxEmailDisplay);
                            addNoEmailDisp.setText(""+maxEmailDisplay);
                            saveConfig();
                            makeConfigLayout();
                        }
                    });
            LinearLayout addholderNED = new LinearLayout(MainApplication.getContext());
            addholderNED.setOrientation(LinearLayout.HORIZONTAL);
            addholderNED.addView(plusNumE);
            addholderNED.addView(addNoEmailDisp);
            configLayout.addView(addholderNED);
        }


    }

    public static boolean isValidEmailAddress(String email) { //make sure it is a GMAIL, valid email
        boolean result = true;
        //Log.i("em",email+' '+email.contains("gmail")+' '+email.contains("googlemail")+' '+(email.contains("gmail")||email.contains("googlemail")));
        if (email.contains("gmail") || email.contains("googlemail")) {
            try {
                InternetAddress emailAddr = new InternetAddress(email);
                emailAddr.validate();
            } catch (AddressException ex) {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

    public void update() {
        if (consecFails > 9) { // if it fails, make the textview disappear
            tv.setText("");
            tv.setVisibility(TextView.GONE);
        } else if (Calendar.getInstance().getTimeInMillis() > (lastRan + timeBetweenCalls)) {
            tv.setVisibility(TextView.VISIBLE);
            new EmailMsgTask(this).execute();
        }
    }

    public void newText(String s) {
//        Spanned span = Html.fromHtml(s);
//        tv.setText(span);
        tv.setText(s);
    }
}

class EmailMsgTask extends AsyncTask <Void, Void, String>{

    private EmailMsg module;

    public EmailMsgTask(EmailMsg _module) {
        module = _module;
    }



    @Override
    protected String doInBackground(Void... params) {

        String text = "";
        if (module.mEmailAccount.length()>0 & module.mEmailPasswod.length()>0) {

            Properties props = new Properties();
            props.setProperty("mail.imap.ssl.enable", "true");
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", "imap.gmail.com");
            props.setProperty("mail.imaps.port", "993");
            Folder ActiveMailbox;

            try {
                Session session = Session.getInstance(props);
                Store store = session.getStore();
                store.connect("imap.gmail.com", module.mEmailAccount,module.mEmailPasswod);
                ActiveMailbox = store.getFolder("INBOX");
                ActiveMailbox.open(Folder.READ_ONLY);
                javax.mail.Message[] messages = ActiveMailbox.getMessages();
                for (int i = messages.length-1; i > messages.length-module.maxEmailDisplay - 1; i--) { //only fetch last 3 emails
                    javax.mail.Message msg = messages[i];
                    Date date = msg.getReceivedDate();
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM.d");
                    String datestr = formatter.format(date);
                    String SenderName = msg.getFrom()[0].toString();
                    text=text+msg.getSubject()+" \u0040 "+SenderName.split("\\s+")[0]+" on "+datestr+" \n"; // save email Object's in text
                }
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        return text;
    }

    protected void onPostExecute(String results) {
        if (results!=null) {
            module.newText(results);
        } else {
            module.consecFails++;
        }
    }


}



