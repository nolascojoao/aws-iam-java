package com.example.iam.users;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.UpdateUserRequest;

public class UpdateUser {
	
	public static void main(String[] args) {
		final String usage = """
				
				Usage:
					<curName> <newName>\s
				
				Where:
					curName - The current user name.\s
					newName - An updated user name.\s
				""";
		
		if (args.length !=2) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String curName = args[0];
		String newName = args[1];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		updateIAMUser(iam, curName, newName);
		System.out.println("\nDone");
		iam.close();
	}
	
	public static void updateIAMUser(IamClient iam, String curName, String newName) {
		try {
			UpdateUserRequest request = UpdateUserRequest.builder()
					.userName(curName)
					.newUserName(newName)
					.build();
			iam.updateUser(request);
			System.out.printf("Successfully updated user to username %s\n", newName);
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
