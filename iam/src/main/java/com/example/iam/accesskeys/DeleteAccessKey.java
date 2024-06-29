package com.example.iam.accesskeys;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeleteAccessKeyRequest;
import software.amazon.awssdk.services.iam.model.IamException;

/**
 * This class is responsible for permanently deleting an access key.
 */
public class DeleteAccessKey {

	public static void main(String[] args) {
		final String usage = """
				
				Usage:
					<username> <accessKey>\s
					
				Where:
					username - The name of the user.\s
					accessKey - The access key ID for the secret access key you want to delete.\s
				""";
		
		if (args.length != 2) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String username = args[0];
		String accessKey = args[1];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		deleteKey(iam, username, accessKey);
		iam.close();
	}
	
	public static void deleteKey(IamClient iam, String username, String accessKey) {
		try {
			DeleteAccessKeyRequest request = DeleteAccessKeyRequest.builder()
					.accessKeyId(accessKey)
					.userName(username)
					.build();
			iam.deleteAccessKey(request);
			System.out.println(String.format("Successfully deleted access key %s from user %s", accessKey, username));
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
