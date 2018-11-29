package ru.frccsc.powermon;

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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static android.os.BatteryManager.BATTERY_PLUGGED_AC;

public class MainActivity extends Activity {

    LinkedList<String> myLog = new LinkedList<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy ' ' HH:mm:ss");
    TextView textView;
    int chargePlug;
    boolean acCharge, acChargeOldStatus;

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

    public class TalkThread extends Thread {
        @Override
        public void run() {
            String chargeStatus, status, smsBody, emailBody;
            String textViewBody = "";
            SmsManager smsManager = SmsManager.getDefault();
            long timeUSB, timeAC;
            timeAC = new Date().getTime();

            myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' +"Проверка запущена" + '\n' + '\n');
            Wait(5);

            while (true) {

                // uncomment next string for test app at AVD
                if (acCharge) { acCharge=false; } else{ acCharge=true; }

                if (acCharge!=acChargeOldStatus) {
                    if (acCharge) {
                        chargeStatus = "Электричеcтво включено ";
                        timeUSB = new Date().getTime();
                        long diff = timeUSB - timeAC;
                        String diffTime = diffTimeString(diff);

                        smsBody = status = chargeStatus +'\n' + "Время отключения " + diffTime;
                        textViewBody = status +'\n' +'\n';
                    } else {
                        chargeStatus = "Электричеcтво отлючено ";
                        smsBody = status = chargeStatus;
                        textViewBody = status +'\n' +'\n';
                        timeAC = new Date().getTime();
                    }
                    myLogStringPrint(dateFormat.format(new Date().getTime()) + '\n' + textViewBody);
                    showToast(status);
                    //smsManager.sendTextMessage("+79680236830", null, smsBody, null, null);
                    //SendEmail(emailBody);
                    acChargeOldStatus=acCharge;
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
        if (diffDays!=0) {sb.append(diffDays + " дней ");}
        if (diffHours!=0) {sb.append(diffHours + " часов ");}
        if (diffMinutes!=0) {sb.append(diffMinutes + " минут ");}
        sb.append(diffSeconds + " секунд");

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
        new mytextView().execute(myLog.toString());
    }

    private class mytextView extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... parametrs) {
            return parametrs[0];
        }

        protected void onPostExecute(String str){
            textView.setText(str);
        }
    }

    public void SendEmail(String messageBody) {
        String to = "power@ipi.ac.ru";
        String from = "PowerMon@ipi.ac.ru";
        Properties properties = System.getProperties();
        //properties.setProperty("mail.smtp.host", "83.149.227.82");
        properties.setProperty("mail.smtp.host", "www.ipi.ac.ru");
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

    private class SendMailTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(javax.mail.Message...  messages)
        {
            try
            {
                Transport.send(messages[0]);
                showToast("Message Sent");
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
    }

    private BroadcastReceiver BatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            acCharge = chargePlug == BATTERY_PLUGGED_AC;
        }
    };

    private void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Wait(int a) {
        try {
            Thread.sleep(a*1000);
        } catch (InterruptedException ignored) {}
    }
}



















