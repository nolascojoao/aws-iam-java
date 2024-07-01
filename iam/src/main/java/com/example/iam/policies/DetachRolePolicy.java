package com.example.iam.policies;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DetachRolePolicyRequest;
import software.amazon.awssdk.services.iam.model.IamException;

public class DetachRolePolicy {

	public static void main(String[] args) {
		final String usage = """
				Usage:
					<roleName> <policyArn>\s
					
				Where:
					roleName - A role name that you can obtain from the AWS Management Console.\s
					policyArn - A policy ARN that you can obtain from the AWS Management Console.\s
				""";
		
		if (args.length != 2) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String roleName = args[0];
		String policyArn = args[1];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		detachPolicy(iam, roleName, policyArn);
		System.out.println("\nDone");
		iam.close();
	}
	
	public static void detachPolicy(IamClient iam, String roleName, String policyArn) {
		try {
			DetachRolePolicyRequest request = DetachRolePolicyRequest.builder()
					.roleName(roleName)
					.policyArn(policyArn)
					.build();
			
			iam.detachRolePolicy(request);
			System.out.println("Successfully detached policy " + policyArn + " from role" + roleName);
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
	}
}
