package com.example.iam.users;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeleteUserRequest;
import software.amazon.awssdk.services.iam.model.IamException;

public class DeleteUser {

	public static void main(String[] args) {
		final String usage = """
				
				Usage:
					<userName>\s
					
				Where:
					userName - The name of the user to delete.\s
				""";
		
		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String userName = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		deleteIAMUser(iam, userName);
		System.out.println("\nDone");
		iam.close();
	}
	
	public static void deleteIAMUser(IamClient iam, String userName) {
		try {
			DeleteUserRequest request = DeleteUserRequest.builder()
					.userName(userName)
					.build();
			iam.deleteUser(request);
			System.out.println("Successfully deleted IAM user " + userName);
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
