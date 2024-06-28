package com.example.iam;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.StatusType;
import software.amazon.awssdk.services.iam.model.UpdateAccessKeyRequest;

/**
 * This class is for activating or deactivating IAM access keys.
 */
public class UpdateAccessKey {

	private static StatusType statusType;
	
	public static void main(String[] args) {
		final String usage = """

                Usage:
                    <username> <accessId> <status>\s

                Where:
                    username - The name of the user whose key you want to update.\s
                    accessId - The access key ID of the secret access key you want to update.\s
                    status - The status you want to assign to the secret access key.\s
                """;
		
		if (args.length != 3) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String username = args[0];
		String accessId = args[1];
		String status = args[2];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		updateKey(iam, username, accessId, status);
		System.out.println("\nDone !");
		iam.close();
	}
	
	public static void updateKey(IamClient iam, String username, String accessId, String status) {
		try {
			if (status.equalsIgnoreCase("active")) {
				statusType = StatusType.ACTIVE;
			} else if (status.equalsIgnoreCase("inactive")) {
				statusType = StatusType.INACTIVE;
			} else {
				statusType = StatusType.UNKNOWN_TO_SDK_VERSION;
			}
			
			UpdateAccessKeyRequest request = UpdateAccessKeyRequest.builder()
					.accessKeyId(accessId)
					.userName(username)
					.status(statusType)
					.build();
			
			iam.updateAccessKey(request);
			System.out.printf("Successfully updated the status of access key %s to " + "status %s for user %s\n", 
					accessId, status, username);
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
