/*
 *  http://docs.aws.amazon.com/sns/latest/dg/using-awssdkjava.html
 */

/* display name - enter how you want your messages to be delivered 
 * Sample - Do you want to receive messages from "Display Name Here"
 */
/*
 * Command Line Arguments:
 * Parameter 1 : Topic Name
 * Parameter 2 : Display Name
 * Parameter 3 : File for subscription end points
 * 
 * 
 * Note: In case you want to add to an existing topic, use the same Topic Name and Display Name.
 * List of your topics: https://console.aws.amazon.com/sns/v2/home?region=us-east-1#/topics
 */
import com.amazonaws.services.sns.AmazonSNSClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
public class SNS {

	private AmazonSNSClient snsClient;
	private String topicArn;
	public SNS()
	{
		//create a new SNS client
	    AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAIIVCSDMHC2QWB3BA", "PhN/18CEzuv0PD7/jioPwpyGKtYyliYeurUWJL7X");
	    //new ClasspathPropertiesFileCredentialsProvider()
		snsClient = new AmazonSNSClient(awsCredentials);	
		topicArn = "";
	}
	public void createTopic(String topicName, String displayNameValue)
	{	                           
		snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		
		CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
		
		CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
		
		topicArn = createTopicResult.toString();
		topicArn = topicArn.substring(11, topicArn.length()-1);
		
		// setting the display name required for sms
		
		snsClient.setTopicAttributes(topicArn, "DisplayName", displayNameValue);
		//System.out.println(topicArn);
		System.out.println(createTopicResult);
		//get request id for CreateTopicRequest from SNS metadata		
		System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));
	}
	
	public void subscribeToTopic(String phonenumber)
	{
		//subscribe to an SNS topic
		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "sms", phonenumber);
		snsClient.subscribe(subRequest);
		
		//get request id for SubscribeRequest from SNS metadata
		System.out.println("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subRequest));
		System.out.println("Check your phone and reply with 'YES' to confirm subscription.");
	}
	
	public void list(){
		System.out.println(snsClient.listSubscriptions());
	}
	public void publishMessage(String msg)
	{
		//publish to an SNS topic
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		
		PublishResult publishResult = snsClient.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
	}
	public void deleteTopic()
	{
		//delete a SNS topic
		DeleteTopicRequest deleteTopicRequest = new DeleteTopicRequest(topicArn);
		
		snsClient.deleteTopic(deleteTopicRequest);
		//get request id for DeleteTopicRequest from SNS metadata
		System.out.println("DeleteTopicRequest - " + snsClient.getCachedResponseMetadata(deleteTopicRequest));
	}
	
	public static void main(String[] args) throws IOException
	{
		SNS newMessage = new SNS();
		
		ArrayList<String> phoneNumbers = new ArrayList<String>();
		String line = null;
		try{
			FileReader fin = new FileReader(args[2]);
			BufferedReader input = new BufferedReader(fin);
			while((line = input.readLine())!=null)
			{
				line = line.replace("\n", "").replace("\r", "").replace("-", "");
				if(line.charAt(0)!='1')
					line = '1' + line;
				if(line.length()==11)
					phoneNumbers.add(line);
				else
					System.out.println("Invalid Phone number " + line);
			}
			input.close();
		}
		catch(FileNotFoundException ex){
			System.out.println("Unable to open the given file");
			return;
		}
		newMessage.createTopic(args[0], args[1]);
		newMessage.list();
		if(phoneNumbers.size()>0)
		{
			newMessage.createTopic(args[0], args[1]);
			for(int i=0; i<phoneNumbers.size(); i++)
			{
				System.out.println(phoneNumbers.get(i));
				newMessage.subscribeToTopic(phoneNumbers.get(i));
			}
		}
		else 
			System.out.println("No valid phone numbers found");
	}
	
}
