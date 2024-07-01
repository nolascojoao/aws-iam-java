package com.example.iam.policies;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreatePolicyRequest;
import software.amazon.awssdk.services.iam.model.CreatePolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyRequest;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.IamException;
import software.amazon.awssdk.services.iam.waiters.IamWaiter;

public class CreatePolicy {

	public static final String POLICY_DOCUMENT = "{" +
            "  \"Version\": \"2012-10-17\"," +
            "  \"Statement\": [" +
            "    {" +
            "        \"Effect\": \"Allow\"," +
            "        \"Action\": [" +
            "            \"dynamodb:DeleteItem\"," +
            "            \"dynamodb:GetItem\"," +
            "            \"dynamodb:PutItem\"," +
            "            \"dynamodb:Scan\"," +
            "            \"dynamodb:UpdateItem\"" +
            "       ]," +
            "       \"Resource\": \"*\"" +
            "    }" +
            "   ]" +
            "}";

	public static void main(String[] args) {
		
		 final String usage = """
	                Usage:
	                    CreatePolicy <policyName>\s

	                Where:
	                    policyName - A unique policy name.\s
	                """;

		if (args.length != 1) {
			System.out.println(usage);
			System.exit(1);
		}
		
		String policyName = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		String result = createIAMPolicy(iam, policyName);
		System.out.println("Successfully create a policy with this ARN value: " + result);
		iam.close();
	}
	
	public static String createIAMPolicy(IamClient iam, String policyName) {
		try {
			// Create an IamWaiter object.
			IamWaiter iamWaiter = iam.waiter();
			
			CreatePolicyRequest request = CreatePolicyRequest.builder()
					.policyName(policyName)
					.policyDocument(POLICY_DOCUMENT)
					.build();
			
			CreatePolicyResponse response = iam.createPolicy(request);
			
			// Wait until the policy is created.
			GetPolicyRequest polRequest = GetPolicyRequest.builder()
					.policyArn(response.policy().arn())
					.build();
			
			WaiterResponse<GetPolicyResponse> waitUntilPolicyExists = iamWaiter.waitUntilPolicyExists(polRequest);
			waitUntilPolicyExists.matched().response().ifPresent(System.out::println);
			return response.policy().arn();
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		return "";
	}
}
