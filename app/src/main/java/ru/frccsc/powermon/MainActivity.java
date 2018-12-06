/*
 * Created by Sergey Denisov (serden85@mail.ru)
 * Copyright (c) 2018 . All rights reserved.
 * Last modified 06.12.18 11:53
 */

package ru.frccsc.powermon;

// if you intend to use showToast or SendEmail uncomment some imports
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
//import android.widget.Toast;
/*import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;*/

import static android.os.BatteryManager.BATTERY_PLUGGED_AC;

public class MainActivity extends Activity {

    private final LinkedList<String> myLog = new LinkedList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy ' ' HH:mm:ss", Locale.US);
    private TextView textView;
    private int bataryLevel;
    private boolean acCharge, acChargeOldStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView= findViewById(R.id.textView);
        this.registerReceiver(this.BatReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        acChargeOldStatus = false;

        myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' +"Программа запущена"+'\n' +'\n');
        Wait(5);
        TalkThread talk = new TalkThread();
        talk.start();
    }

    private class TalkThread extends Thread {
        @Override
        public void run() {
            String chargeStatus, smsBody, emailBody;
            String textViewBody;
            boolean entryPoint=false;
            SmsManager smsManager = SmsManager.getDefault();
            long timeAC;
            timeAC = new Date().getTime();

            myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' +"Проверка запущена" + '\n' + '\n');
            Wait(5);

            while (true) {

                // uncomment next string for test app at AVD
                //acCharge = !acCharge;

                if (acCharge!=acChargeOldStatus) {
                    if (acCharge) {
                        chargeStatus = "Электричеcтво включено ";
                        long diff = new Date().getTime() - timeAC;
                        String diffTime = diffTimeString(diff);

                        smsBody = emailBody = chargeStatus +'\n' + "Время отключения " + diffTime;
                        textViewBody = chargeStatus +'\n' + "Время отключения " + diffTime +'\n' +'\n';
                    }  else {
                            entryPoint = true;
                        chargeStatus = "Электричеcтво отлючено ";
                        smsBody = emailBody = chargeStatus;
                        textViewBody = chargeStatus + '\n' + '\n';
                            timeAC = new Date().getTime();
                    }
                    acChargeOldStatus=acCharge;
                    myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' + textViewBody);

                    //don't forget to setup right telephone number
                    //smsManager.sendTextMessage("+telnum", null, smsBody, null, null);
                    //SendEmail(emailBody);
                }
                if (entryPoint && bataryLevel <= 10) {
                    entryPoint = false;
                        chargeStatus = "Электричеcтво отлючено ";
                        smsBody = emailBody = chargeStatus +'\n' + "Заряд < " + bataryLevel + "%";
                        textViewBody = chargeStatus +'\n' + "Заряд < " + bataryLevel + "%" + '\n' + '\n';
                    timeAC = new Date().getTime();

                    myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' + textViewBody);

                    //don't forget to setup right telephone number
                    //smsManager.sendTextMessage("+telnum", null, smsBody, null, null);
                    //SendEmail(emailBody);
                }
                    Wait(10);
            }
        }
    }

    private String diffTimeString (long diff) {
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();
        if (diffDays!=0) {
            sb.append(diffDays).append(" дней ");}
        if (diffHours!=0) {
            sb.append(diffHours).append(" часов ");}
        if (diffMinutes!=0) {
            sb.append(diffMinutes).append(" минут ");}
        sb.append(diffSeconds).append(" секунд");

    return sb.toString();
    }

    private void myLogStringPrint (String element) {

        if (myLog.size()<9) {
            myLog.addLast(element);
        }
        else {
        myLog.removeFirst();
        myLog.addLast(element);
        }
        new mytextView().execute(myLog);
    }

    private class mytextView extends AsyncTask<LinkedList, Void, String> {

        protected String doInBackground(LinkedList... parametrs) {
            Integer i=0;
            StringBuilder sb1 = new StringBuilder();
            while (i < myLog.size()) {
                sb1.append(parametrs[0].get(i));
                i++;
            }
            return sb1.toString();
        }

        protected void onPostExecute(String str){
            textView.setText(str);
        }
    }

    private final BroadcastReceiver BatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            acCharge = chargePlug == BATTERY_PLUGGED_AC;

            int level =  intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale =  intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level/(float) scale;
            bataryLevel = (int) (batteryPct * 100);
        }
    };

    private void Wait(int a) {
        try {
            Thread.sleep(a*1000);
        } catch (InterruptedException ignored) {}
    }

    /*private void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
    }*/

    /*private void SendEmail(String messageBody) {
        String to = "power@ipi.ac.ru";
        String from = "PowerMon@ipi.ac.ru";
        Properties properties = System.getProperties();

        //don't forget to setup right host address
        properties.setProperty("mail.smtp.host", "host");
        properties.setProperty("mail.smtp.port", "25");
        Session session = Session.getDefaultInstance(properties);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("PowerMon Message");
            message.setText(messageBody);
            new SendMailTask().execute(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class SendMailTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(javax.mail.Message...  messages)
        {
            try
            {
                Transport.send(messages[0]);
            }
            catch (MessagingException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
        }
    }*/
}



















