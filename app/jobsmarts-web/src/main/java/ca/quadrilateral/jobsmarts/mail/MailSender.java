package ca.quadrilateral.jobsmarts.mail;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {
    @Resource(mappedName="java:jboss/mail/Default")
    private Session mailSession;
    
    public void sendMailMessage(
            final Collection<String> addresses, 
            final String messageSubject, 
            final String messageBody) 
                    throws MessagingException {
        
        final MimeMessage message = new MimeMessage(mailSession);
        
        final Address from = new InternetAddress("noreply@quadrilateral.ca");
        
        final Address[] to = convertAddressCollectionToArray(addresses);
        
        message.setFrom(from);
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSubject(messageSubject);
        message.setContent(messageBody, "text/html");
        
        Transport.send(message);
    }
    
    private Address[] convertAddressCollectionToArray(final Collection<String> addresses) {
        return addresses
            .stream()
            .map(address -> {
                try {
                    return new InternetAddress(address);
                } catch (final AddressException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList())
            .toArray(new Address[]{});
    }
}
