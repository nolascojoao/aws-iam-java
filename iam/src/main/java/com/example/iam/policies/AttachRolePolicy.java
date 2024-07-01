package com.example.iam.policies;

import java.util.List;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachRolePolicyRequest;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesResponse;

public class AttachRolePolicy {

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
		
		attachIAMRolePolicy(iam, roleName, policyArn);
		iam.close();
	}
	
	public static void attachIAMRolePolicy(IamClient iam, String roleName, String policyArn) {
		try {
			ListAttachedRolePoliciesRequest request = ListAttachedRolePoliciesRequest.builder()
					.roleName(roleName)
					.build();
			
			ListAttachedRolePoliciesResponse response = iam.listAttachedRolePolicies(request);
			List<AttachedPolicy> attachedPolicies = response.attachedPolicies();
			
			// Ensure that the policy is not attached to this role
			String polArn = "";
			for (AttachedPolicy policy : attachedPolicies) {
				polArn = policy.policyArn();
				if (polArn.compareTo(policyArn) == 0) {
					System.out.println(roleName + " policy is already attached to this role.");
					return;
				}
			}
			
			AttachRolePolicyRequest attachRequest = AttachRolePolicyRequest.builder()
					.roleName(roleName)
					.policyArn(policyArn)
					.build();
			
			iam.attachRolePolicy(attachRequest);
			
			System.out.println("Successfully attached policy " + policyArn + " to role " + roleName);
			
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		System.out.println("\nDone");
	}
}
