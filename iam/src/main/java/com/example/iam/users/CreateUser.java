package com.example.iam.users;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateUserRequest;
import software.amazon.awssdk.services.iam.model.CreateUserResponse;
import software.amazon.awssdk.services.iam.model.GetUserRequest;
import software.amazon.awssdk.services.iam.model.GetUserResponse;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.waiters.IamWaiter;

public class CreateUser {

	public static void main(String[] args) {
		final String usage = """

				Usage:
					<username>\s

				Where:
					username - The name of the user to create.\s
				""";

		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}

		String username = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();

		String result = createIAMUser(iam, username);
		System.out.println("Successfully created user: " + result);
		iam.close();
	}
	
	public static String createIAMUser(IamClient iam, String username) {
		try {
			// Create an IamWaiter object.
			IamWaiter iamWaiter = iam.waiter();
			
			CreateUserRequest request = CreateUserRequest.builder()
					.userName(username)
					.build();			
			
			CreateUserResponse response = iam.createUser(request);
			
			// Wait until the user is created.
			GetUserRequest userRequest = GetUserRequest.builder()
					.userName(response.user().userName())
					.build();
			
			WaiterResponse<GetUserResponse> waitUntilUserExists = iamWaiter.waitUntilUserExists(userRequest);
			waitUntilUserExists.matched().response().ifPresent(System.out::println);
			return response.user().userName();
			
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		return "";
	}
}
