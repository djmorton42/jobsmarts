package ca.quadrilateral.jobsmarts.mail;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;

import ca.quadrilateral.jobsmarts.api.Job;
import ca.quadrilateral.jobsmarts.data.api.IUserDataService;

public class NewJobMailHandler {
    private static final Logger logger = LoggerFactory.getLogger(NewJobMailHandler.class);

    private static final String templateText = 
            "subject(job) ::= \"New Job Posted: $job.jobSummary.jobTitle$ - $job.jobSummary.companyName$\" " +
            "message(job) ::= \"<html><head></head><body>" +
            "<table border='1'>" +
            "    <tr><td>Job Title</td><td>$job.jobSummary.jobTitle$</td></tr>" +
            "    <tr><td>Date Posted</td><td>$job.jobDetails.datePosted$</td></tr>" +
            "    $job.jobDetails.fields.keys:{k | <tr><td>$k$</td><td>$job.jobDetails.fields.(k)$</td></tr>}$" +
            "</table>" +
            "$job.jobDetails.descriptionHtml$" +
            "</body></html>\" ";
        ;

    private static final STGroup stGroup = new STGroupString("<string", templateText, '$', '$'); 
    
    @Inject 
    private IUserDataService userDataService;
    
    @Inject
    private MailSender mailSender;
   
    public void handleNewJob(@Observes final Job newJob) {
        final ST messageSubjectTemplate = stGroup.getInstanceOf("subject");
        final ST messageBodyTemplate = stGroup.getInstanceOf("message");
        
        messageSubjectTemplate.add("job", newJob);
        messageBodyTemplate.add("job", newJob);
        
        final String messageSubject = messageSubjectTemplate.render(); 
        final String messageBody = messageBodyTemplate.render();
        
        try {
            mailSender.sendMailMessage(getEmailAddressesToSendTo(), messageSubject, messageBody);
        } catch (final MessagingException e) {
            logger.warn("Error sending new job mail message with subject: {}", messageSubject, e);
        }
    }
    
    private Collection<String> getEmailAddressesToSendTo() {
        return userDataService
                    .getAllUsers()
                    .stream()
                    .map(user -> user.getEmail())
                    .collect(Collectors.toList());
    }
    
}
