/**
 * 
 */
package com.rr.email;

/**
 * @author Sankar.Ramasamy
 *
 */
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * @author Sankar
 * 
 */
public class BulkEmailMain {

	/**
	 * @param args
	 */
	private static final int MYTHREADS = 100;
	private static String SMPT_HOSTNAME = "mail.onlinecustomcase.com";
	private static String SMPT_PORT = "26";
	private static String USERNAME = "onlinecu";
	private static String PASSWORD = "GuruOm$123";
	private static String FROMEMAILID = "support@onlinecustomcase.com";

	public static void main(String[] args) {
		try {
			ReadEmailIdFromFile(FROMEMAILID, collectingEmailID());
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\nFinished all threads");
	}

	public static class MyRunnable implements Runnable {
		private String toEmailID;
		private String fromEmailID;
		private String emailBody;

		MyRunnable(String toTempEmailID, String fromTempEmailID, String emailTempBody) {
			this.toEmailID = toTempEmailID;
			this.fromEmailID = fromTempEmailID;
			this.emailBody = emailTempBody;
		}

		@Override
		public void run() {
			try {
				proceedTOSendEmail(toEmailID,fromEmailID,emailBody);
				//System.out.println(toEmailID +" "+ fromEmailID + " " + emailBody);
			} catch (Exception e) {

			}
		}
	}
	
	private static ArrayList collectingEmailID()
	{
		ArrayList emaildId = new ArrayList();

		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			// DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(
					"http://www.onlinecustomcase.com/emailbank/test.php?name=Y");
			getRequest.addHeader("accept", "application/json");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			String json = EntityUtils.toString(response.getEntity(), "UTF-8");

			JSONParser parser = new JSONParser();
			Object resultObject = parser.parse(json);

			if (resultObject instanceof JSONArray) {
				JSONArray array = (JSONArray) resultObject;
				for (Object object : array) {
					JSONObject obj = (JSONObject) object;
					emaildId.add(obj.get("emailid"));
					//System.out.println(obj.get("isactive"));
				}

			} else if (resultObject instanceof JSONObject) {
				JSONObject obj = (JSONObject) resultObject;
				JSONArray array = (JSONArray) obj.get("data");
				for (Object object : array) {
					JSONObject innerObject = (JSONObject) object;
					emaildId.add(innerObject.get("emailid"));
					//System.out.println(innerObject.get("isactive"));
				}
			}
			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return emaildId;
	}
	/**
	 * read the email id from the file.
	 * @param fromEmailID
	 * @param emailBody
	 * @throws Exception 
	 * @throws ParseErrorException 
	 * @throws ResourceNotFoundException 
	 */
	public static void ReadEmailIdFromFile(String fromEmailID, ArrayList emailArray) throws ResourceNotFoundException, ParseErrorException, Exception {
		// TODO Auto-generated method stub
		// Step 1: Read the email id from emailbank text file.
		//String emailArray[] = {"csesankar@gmail.com","onlinecustomcase@gmail.com","case2sasi@gmail.com","seosasikumar@gmail.com"};
		// Step 2: Send the email to customer list
		ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
		
		for (int i = 0; i < emailArray.size(); i++) {
			Runnable run = new MyRunnable(emailArray.get(i).toString(),fromEmailID, PrepareEmailBody(emailArray.get(i).toString()));
			executor.execute(run);
		}
		System.out.println("Total Email was Sent::"+emailArray.size());
		executor.shutdown();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {

		}
	}

	/**
	 * proceed to send email
	 * @param toEmailID
	 * @param fromEmailID
	 * @param emailBody
	 */
	public static void proceedTOSendEmail(String toEmailID, String fromEmailID,
			String emailBody) {

		// Assuming you are sending email from localhost
		// String host = "localhost";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.port", SMPT_PORT);
		properties.setProperty("mail.smtp.host", SMPT_HOSTNAME);
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		
		// Get the default Session object.
		// Session session = Session.getDefaultInstance(properties);

		// create a session with an Authenticator
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(fromEmailID));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toEmailID));

			// Set Subject: header field
			message.setSubject("Pink Bluetooth Headset $15.89 - Black Bluetooth Headset $15.89 - White Bluetooth Headset $15.89 - iPhone6 Case - Modern Art cover - $27.99 - Black Selfie - $14.99 & Many More!!");

			// Now set the actual message
			//message.setText(emailBody);
			message.setContent(emailBody, "text/html");
			// Send message
			Transport.send(message);
			//System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
	/**
	 * prepare the Emailid body template to send the email.
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws ParseErrorException
	 * @throws Exception
	 */
	public static String PrepareEmailBody(String emailId) throws ResourceNotFoundException,
			ParseErrorException, Exception {
		/* first, get and initialize an engine */
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "template/");
		ve.init();
		/* organize our data */

		/* add that list to a VelocityContext */
		VelocityContext context = new VelocityContext();
		context.put("unsubscribe", "http://www.onlinecustomcase.com/emailbank/update.php?id="+emailId);
		/* get the Template */
		Template t = ve.getTemplate("occemail.vm");

		/* now render the template into a Writer */
		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		/* use the output in your email body */

		//System.out.println(writer.toString());
		return writer.toString();
	}
}

