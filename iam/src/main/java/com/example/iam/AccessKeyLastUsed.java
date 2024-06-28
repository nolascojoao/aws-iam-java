package com.example.iam;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetAccessKeyLastUsedRequest;
import software.amazon.awssdk.services.iam.model.GetAccessKeyLastUsedResponse;
import software.amazon.awssdk.services.iam.model.IamException;

/**
 * This class retrieves the last used date of an AWS IAM access key.
 * The date returned by this class is formatted to the system's local time.
 */
public class AccessKeyLastUsed {
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	public static void main(String[] args) {
		final String usage = """

				Usage:
					<accessId>\s

				Where:
					accessId - An access key id that you can obtain from the AWS Management Console.\s
				""";

		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}

		String accessId = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		getAccessKeyLastUsed(iam, accessId);
		iam.close();
	}
	
	public static void getAccessKeyLastUsed(IamClient iam, String accessId) {
		try {
			GetAccessKeyLastUsedRequest request = GetAccessKeyLastUsedRequest.builder()
					.accessKeyId(accessId)
					.build();
			
			GetAccessKeyLastUsedResponse response = iam.getAccessKeyLastUsed(request);
			
			ZonedDateTime lastUsedDate = response.accessKeyLastUsed()
					.lastUsedDate()
					.atZone(ZoneId.systemDefault()); // Convert the last used date to the system's local time zone 		
			
			String formattedDate = lastUsedDate.format(dateTimeFormatter);	
			
			System.out.println(String.format("The Access key %s was last used at: %s ", accessId, formattedDate));
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		System.out.println("\nDone !");
	}
}
