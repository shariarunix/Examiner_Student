package com.shariarunix.examiner.OTPSender;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class OTPSenderClass {

    String receiverMail, receiverName, mailSubject;
    int otpMessage;

    public OTPSenderClass() {
        // Default Empty Constructor
    }

    public OTPSenderClass(String receiverMail, String receiverName, String mailSubject, int otpMessage) {
        this.receiverMail = receiverMail;
        this.receiverName = receiverName;
        this.mailSubject = mailSubject;
        this.otpMessage = otpMessage;
    }

    public void sendOtp() {
        try {
            String stringSenderEmail = "examiner.itbd@gmail.com";
            String stringPasswordSenderEmail = "jpwtqjvoiqcqzbrb";

            String stringHost = "smtp.gmail.com";

            Properties properties = System.getProperties();

            properties.put("mail.smtp.host", stringHost);
            properties.put("mail.smtp.port", "465");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.auth", "true");

            javax.mail.Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMail));

            mimeMessage.setSubject(mailSubject);
            mimeMessage.setText("Hello " + receiverName + "!!!" + "\n\nYour OTP is - " + otpMessage + "\n\nCheers!\nExaminer Team");

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
