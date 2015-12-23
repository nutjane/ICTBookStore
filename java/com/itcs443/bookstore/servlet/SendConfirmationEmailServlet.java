package com.itcs443.bookstore.servlet;

import com.google.appengine.api.utils.SystemProperty;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet for sending a notification e-mail.
 */
public class SendConfirmationEmailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(
            SendConfirmationEmailServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String cartInfo = request.getParameter("cartInfo");
        String name = request.getParameter("name");
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String body = "Hi "+name+",\n You have purchased the following order.\n\n" + cartInfo;
        body +="\n\nThis is an automatically generated message to confirm receipt of your order via the Internet. You do not need to reply to this e-mail, but you may wish to save it for your records.";
        body +="\n\nYour order should arrive in four to six weeks.";
        body +="\n\nShould you have any questions about your order, feel free to call customer services at 555-5555. Again, thank you for your purchase.";
        try {
            Message message = new MimeMessage(session);
            InternetAddress from = new InternetAddress(
                    String.format("noreply@%s.appspotmail.com",
                            SystemProperty.applicationId.get()), "ICT Bookstore");
            message.setFrom(from);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email, ""));
            message.setSubject("Thank you for ordering at our ICT Bookstore!");
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            LOG.log(Level.WARNING, String.format("Failed to send an mail to %s", email), e);
            throw new RuntimeException(e);
        }
    }
}
