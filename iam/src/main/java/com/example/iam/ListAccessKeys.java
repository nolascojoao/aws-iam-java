package com.example.iam;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKeyMetadata;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListAccessKeysRequest;
import software.amazon.awssdk.services.iam.model.ListAccessKeysResponse;

public class ListAccessKeys {

	public static void main(String[] args) {
		final String usage = """
				Usage:
					<user>\s	
					
				Description:
                   The <userName> parameter specifies the IAM user for whom you want to retrieve access keys.
                   This IAM user should already exist in your AWS account.

                Example:
                   java com.example.iam.ListAccessKeys <user>
                """;
		
		if (args.length!= 1) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String user = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		listKeys(iam, user);
		System.out.println("\nDone !");
		iam.close();
	}
	
	public static void listKeys(IamClient iam, String user) {
		try {
			boolean done = false;
			String newMarker = null;
			
			while (!done) {
				ListAccessKeysResponse response;
				
				if (newMarker == null) {
					ListAccessKeysRequest request = ListAccessKeysRequest.builder()
							.userName(user)
							.build();
					response = iam.listAccessKeys(request);
				} else {
					ListAccessKeysRequest request = ListAccessKeysRequest.builder()
							.userName(user)
							.marker(newMarker)
							.build();
					response = iam.listAccessKeys(request);
				}
				
				for (AccessKeyMetadata metadata : response.accessKeyMetadata()) {
					System.out.println(String.format("Retrieved Access Key ID: %s", metadata.accessKeyId()));
				}
				
				if (!response.isTruncated()) {
					done = true;
				} else {
					newMarker = response.marker();
				}
			}
			
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
