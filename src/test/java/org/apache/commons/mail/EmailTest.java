package org.apache.commons.mail;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.Session;
import java.util.Date;
import java.util.Properties;

public class EmailTest {

	private static final String[] TEST_EMAILS = {"ab@bc.com", "a.b@c.org",
    		"abcdefghijklmnopqrst@abcdefghijklmnopqrst.com.bd" 
    };
    
    /* Concrete Email Class for testing */
    private EmailConcrete email;
    
    @Before
    public void setUp() {
        email = new EmailConcrete();
    }
    
    @After
    public void tearDownEmailTest() throws Exception {
    	
    }

    @Test
    public void testAddBcc() throws Exception {
        email.addBcc(TEST_EMAILS);
        assertEquals(3, email.getBccAddresses().size());
    }

    
    @Test
    public void testAddCc() throws Exception {
        email.addCc("cc@gmail.com");
        assertEquals(1, email.getCcAddresses().size());
    }

    
    @Test
    public void testAddHeader() {
        // Valid case
        email.addHeader("ATestHeader", "HeaderValue");
        if (!"HeaderValue".equals(email.headers.get("ATestHeader"))) {
            fail("Failed to add a valid header.");
        }

        // Null name case
        try {
            email.addHeader(null, "ValidValue");
            fail("Expected IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("name can not be null or empty")) {
                fail("Incorrect exception message for null name.");
            }
        }
       
    }


    @Test
    public void testAddReplyTo() throws Exception {
        email.addReplyTo("reply@gmail.com", "Reply Name");
        assertEquals(1, email.getReplyToAddresses().size());
    }

    @Test
    public void testBuildMimeMessage() {
        try {
            // Case 1: Valid email with messageset we use setMsg()
            email.setFrom("ahmad@gmail.com");
            email.addTo("hussein@gmail.com");
            email.setSubject("Subject line");
            email.setHostName("Test Host Name");
            email.setMsg("This is the email message");
            email.buildMimeMessage();
            assertNotNull(email.getMimeMessage().getContent());
            assertTrue(email.getMimeMessage().getContentType().contains("text/plain"));

            // Case 2:  content and email body are not set (should be empty )
            EmailConcrete emailEmpty = new EmailConcrete();
            emailEmpty.setFrom("ahmad@gmail.com");
            emailEmpty.addTo("hussein@gmail.com");
            emailEmpty.setSubject("Subject line");
            emailEmpty.setHostName("Test Host Name");
            emailEmpty.buildMimeMessage();
            assertEquals("", emailEmpty.getMimeMessage().getContent().toString());

            // Case 3: we call buildMimeMessage twice should make an exception
            try {
                email.buildMimeMessage();
                fail("Expected IllegalStateException for duplicate buildMimeMessage call");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("The MimeMessage is already built."));
            }

            // Case 4: Missing sender should show EmailException
            EmailConcrete emailNoSender = new EmailConcrete();
            emailNoSender.addTo("hussein@gmail.com");
            emailNoSender.setSubject("Subject line");
            emailNoSender.setMsg("Test Message");
            emailNoSender.setHostName("Test Host Name");
            try {
                emailNoSender.buildMimeMessage();
                fail("Expected EmailException for missing sender");
            } catch (EmailException e) {
                assertTrue(e.getMessage().contains("From address required"));
            }

            // Case 5: No recipients should throw EmailException
            EmailConcrete emailNoRecipient = new EmailConcrete();
            emailNoRecipient.setFrom("ahmad@gmail.com");
            emailNoRecipient.setSubject("Subject line");
            emailNoRecipient.setMsg("Test Message");
            emailNoRecipient.setHostName("Test Host Name");
            try {
                emailNoRecipient.buildMimeMessage();
                fail("Expected EmailException for missing recipients");
            } catch (EmailException e) {
                assertTrue(e.getMessage().contains("At least one receiver address required"));
            }

            // Case 6: Ensure CC and BCC recipients are handled good
            EmailConcrete emailWithCcBcc = new EmailConcrete();
            emailWithCcBcc.setFrom("ahmad@gmail.com");
            emailWithCcBcc.addTo("hussein@gmail.com");
            emailWithCcBcc.setSubject("Subject line");
            emailWithCcBcc.setHostName("Test Host Name");
            emailWithCcBcc.setMsg("Test Message");
            emailWithCcBcc.addCc("cc@gmail.com");
            emailWithCcBcc.addBcc("bcc@gmail.com");
            emailWithCcBcc.buildMimeMessage();
            assertEquals(1, emailWithCcBcc.getMimeMessage().getRecipients(Message.RecipientType.CC).length);
            assertEquals(1, emailWithCcBcc.getMimeMessage().getRecipients(Message.RecipientType.BCC).length);

            // Case 7: make sure headers are added properly
            EmailConcrete emailWithHeader = new EmailConcrete();
            emailWithHeader.setFrom("ahmad@gmail.com");
            emailWithHeader.addTo("hussein@gmail.com");
            emailWithHeader.setSubject("Subject line");
            emailWithHeader.setHostName("Test Host Name");
            emailWithHeader.setMsg("Test Message");
            emailWithHeader.addHeader("ATestHeader", "HeaderValue");
            emailWithHeader.buildMimeMessage();
            assertNotNull(emailWithHeader.getMimeMessage().getHeader("ATestHeader"));

            // Case 8: make sure sentDate is set
            assertNotNull(emailWithHeader.getMimeMessage().getSentDate());

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }


    @Test
    public void testGetHostName() {
        // Test setting and retrieving a valid host name
        email.setHostName("smtp.example.com");
        assertEquals("smtp.example.com", email.getHostName());
        
        // Test default case where host name is not set
        EmailConcrete emailNoHost = new EmailConcrete();
        assertNull(emailNoHost.getHostName());
    }

    @Test
    public void testGetMailSession() throws Exception {
        // Case 1: Valid case - when host name is set
        email.setHostName("smtp.example.com");
        Session session = email.getMailSession();
        
        // Ensure session is created successfully
        assertNotNull(session);

        // Check that the correct host name is set in the session properties
        assertEquals("smtp.example.com", session.getProperty("mail.smtp.host"));

        // Case 2: No host name set - should throw an EmailException
        EmailConcrete emailNoHost = new EmailConcrete();
        try {
            emailNoHost.getMailSession();
            fail("Expected EmailException due to missing hostname");
        } catch (EmailException e) {
            // Verify the exception message contains the expected text
            assertTrue(e.getMessage().toLowerCase().contains("cannot find valid hostname"));
        }
    }



    @Test
    public void testGetSentDate() {
        // First case: Default sent date which shouldn't be null and should be recent
        Date defaultDate = email.getSentDate();
        assertNotNull(defaultDate);
        assertTrue(Math.abs(defaultDate.getTime() - System.currentTimeMillis()) < 5000); // Allow slight time difference

        // Second case: Set custom sent date and verify
        Date customDate = new Date(1672531200000L); 
        email.setSentDate(customDate);
        assertEquals(customDate, email.getSentDate());
    }


    @Test
    public void testGetSocketConnectionTimeout() {
        // Set socket connection timeout to 4000 ms
        email.setSocketConnectionTimeout(4000);
        // Verify that timeout value was set and retrieved correctly
        assertEquals(4000, email.getSocketConnectionTimeout());
    }

    @Test
    public void testSetFrom() throws Exception {
        // Set the "From" email address
        email.setFrom("from@gmail.com");
        // Verify that the email address was correctly stored in the fromAddress field
        assertEquals("from@gmail.com", email.getFromAddress().getAddress());
    }
}
