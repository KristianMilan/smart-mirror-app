package com.ineptech.magicmirror.modules;

/**
 * Created by Stefano on 15-Mar-16.
 */
import android.util.Log;

import java.io.*;
import java.util.*;
import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.*;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;


public class FolderFetchIMAP {
    public static void main(String[] args) throws MessagingException, IOException {
        IMAPFolder folder = null;
        Store store = null;
        String subject = null;
        Flag flag = null;
        try
        {
            Properties props = System.getProperties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imap.ssl.enable", "true");

            Session session = Session.getInstance(props);
            session.setDebug(true);

            store = session.getStore("imaps");
            store.connect("imap.googlemail.com","stefano286@gmail.com", "Lhouse2806");

            folder = (IMAPFolder) store.getFolder("inbox");

            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Log.i("inside FolderFetch","No of Messages : " + folder.getMessageCount());
            Log.i("inside FolderFetch", "No of Unread Messages : " + folder.getUnreadMessageCount());
            Log.i("inside FolderFetch","length: " + messages.length);
            for (int i=0; i < messages.length;i++) {

                Log.i("inside FolderFetch","*****************************************************************************");
                Log.i("inside FolderFetch","MESSAGE " + (i + 1) + ":");
                Message msg =  messages[i];

                subject = msg.getSubject();

                Log.i("inside FolderFetch","Subject: " + subject);
                Log.i("inside FolderFetch","From: " + msg.getFrom()[0]);
                Log.i("inside FolderFetch","To: "+msg.getAllRecipients()[0]);
                Log.i("inside FolderFetch","Date: "+msg.getReceivedDate());
                Log.i("inside FolderFetch","Size: "+msg.getSize());
                Log.i("inside FolderFetch","flag"+msg.getFlags());
                Log.i("inside FolderFetch","Body: \n"+ msg.getContent());
                Log.i("inside FolderFetch",msg.getContentType());
            }
        }
        finally
        {
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }

    }
}