//package com.ineptech.magicmirror.modules;
//
//import android.util.Log;
//
//import java.io.*;
//import java.util.*;
//import javax.mail.*;
//import javax.mail.Flags.Flag;
//import javax.mail.internet.*;
//
//import com.sun.mail.imap.IMAPFolder;
//import com.sun.mail.imap.IMAPMessage;
///**
// * Created by Stefano on 15-Mar-16.
// */
//public class ReadMailbox {
//    private String[] ReadMailbox(String MailboxName) throws IOException {
//        Properties props = new Properties();
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.host", "imap.gmail.com");
//        props.setProperty("mail.imaps.port", "993");
//        List<String> FromAddressArrList = new ArrayList<String>();
//
//        try {
//            Session session = Session.getInstance(props, null);
//            Store store = session.getStore();
//            store.connect("imap.gmail.com", "myusername", "mypassword");
//            ActiveMailbox = store.getFolder(MailboxName);
//            ActiveMailbox.open(Folder.READ_ONLY);
//            Message[] messages = ActiveMailbox.getMessages();
//            //System.out.println("Number of mails = " + messages.length);
//            for (int i = 0; i < messages.length; i++) {
//                Message message = messages[i];
//                Address[] from = message.getFrom();
//                FromAddressArrList.add(from[0].toString());
//            }
//            //ActiveMailbox.close(true);
//            store.close();
//        } catch (NoSuchProviderException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        String[] FromAddressArr = new String[FromAddressArrList.size()];
//        FromAddressArrList.toArray(FromAddressArr);
//        return FromAddressArr;
//    }
//}