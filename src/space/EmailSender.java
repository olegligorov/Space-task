package space;
import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailSender {
    public void sendMail(String senderEmail, String senderPassword, String recipientEmail) {
        // SMTP server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a mail session with the SMTP server
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Space shuttle launch, weather report");

            // Create a multipart message to hold the email body and attachment
            Multipart multipart = new MimeMultipart();

            // Add the email body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("This is the weather report.");
            multipart.addBodyPart(messageBodyPart);

            // Add the CSV file attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File csvFile = new File("WeatherReport.csv");
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(csvFile)));
            attachmentPart.setFileName(csvFile.getName());
            multipart.addBodyPart(attachmentPart);

            // Set the message content to the multipart message
            message.setContent(multipart);

            // Send the email message
            Transport.send(message);

            System.out.println("Email sent successfully.");
        } catch (Exception e) {
            System.out.println("Failed to send email.");
            e.printStackTrace();
        }
    }
}
