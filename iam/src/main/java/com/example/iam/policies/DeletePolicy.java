package com.example.iam.policies;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.DeletePolicyRequest;
import software.amazon.awssdk.services.iam.model.IamException;

public class DeletePolicy {

	public static void main(String[] args) {
		final String usage = """
				Usage:
					<policyARN>\s
					
				Where:
					policyARN - A policy ARN value to delete.\s
				""";
		
		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String policyARN = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		deleteIAMPolicy(iam, policyARN);
		iam.close();
	}
	
	public static void deleteIAMPolicy(IamClient iam, String policyARN) {
		try {
			DeletePolicyRequest request = DeletePolicyRequest.builder()
					.policyArn(policyARN)
					.build();
			iam.deletePolicy(request);
			System.out.println("Successfully deleted the policy");
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		System.out.println("\nDone");
	}
}
