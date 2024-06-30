package com.example.iam.users;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPermissionsBoundary;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListUsersRequest;
import software.amazon.awssdk.services.iam.model.ListUsersResponse;
import software.amazon.awssdk.services.iam.model.User;

public class ListUsers {

	public static void main(String[] args) {
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		listAllUsers(iam);
		System.out.println("\n Done");
		iam.close();
	}
	
	public static void listAllUsers(IamClient iam) {
		try {
			boolean done = false;
			String newMarker = null;
			while (!done) {
				ListUsersResponse response;
				if (newMarker == null) {
					ListUsersRequest request = ListUsersRequest.builder().build();
					response = iam.listUsers(request);
				} else {
					ListUsersRequest request = ListUsersRequest.builder()
							.marker(newMarker)
							.build();
					response = iam.listUsers(request);
				}
				
				for (User user : response.users()) {
					System.out.format("\n Retrieved user %s", user.userName());
					AttachedPermissionsBoundary permissionsBoundary = user.permissionsBoundary();
					if (permissionsBoundary != null) {
						System.out.format("\n Permissions boundary details %s", 
								permissionsBoundary.permissionsBoundaryTypeAsString());
					}
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
		System.out.println();
	}
}
