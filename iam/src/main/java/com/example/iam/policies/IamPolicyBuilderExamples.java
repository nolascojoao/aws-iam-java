package com.example.iam.policies;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.policybuilder.iam.IamConditionOperator;
import software.amazon.awssdk.policybuilder.iam.IamEffect;
import software.amazon.awssdk.policybuilder.iam.IamPolicy;
import software.amazon.awssdk.policybuilder.iam.IamPolicyWriter;
import software.amazon.awssdk.policybuilder.iam.IamPrincipal;
import software.amazon.awssdk.policybuilder.iam.IamPrincipalType;
import software.amazon.awssdk.policybuilder.iam.IamResource;
import software.amazon.awssdk.policybuilder.iam.IamStatement;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetPolicyResponse;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionResponse;
import software.amazon.awssdk.services.sts.StsClient;

public class IamPolicyBuilderExamples {
	private static final Logger logger = LoggerFactory.getLogger(IamPolicyBuilderExamples.class);
	
	public static void main(String[] args) {
		IamPolicyBuilderExamples iamPolicyBuilderExamples = new IamPolicyBuilderExamples();
		IamClient iam = IamClient.builder().region(Region.AWS_GLOBAL).build();
		
		iamPolicyBuilderExamples.runCreateAndUploadPolicyExample(iam);
		iamPolicyBuilderExamples.runCreateNewBasedOnExistingPolicyExample(iam);
		
		if (iam != null) {
			iam.close();
		}
		
		iamPolicyBuilderExamples.runTimesBasedPolicyExample();
		iamPolicyBuilderExamples.runMultiplesConditionsExample();
		iamPolicyBuilderExamples.runSpecifyPrincipalsExample();
		iamPolicyBuilderExamples.runAllowCrossAccountAccessExample();
	}
	
	public String createAndUploadPolicyExample(IamClient iam, String accountID, String policyName) {
		// Build the policy.
		IamPolicy policy = IamPolicy.builder()
				.addStatement(IamStatement.builder()
						.effect(IamEffect.ALLOW)
						.addAction("dynamodb:PutItem")
						.addResource("arn:aws:dynamodb:usd-east-1:" + accountID + ":table/exampleTableName")
						.build())
				.build();
		// Upload the policy.
		iam.createPolicy(r -> r.policyName(policyName).policyDocument(policy.toJson()));
		return policy.toJson(IamPolicyWriter.builder().prettyPrint(true).build());
	}
	
	private void runCreateAndUploadPolicyExample(IamClient iam) {
		String accountId = getAccountID();
		String policyName = "AllowPutItemToExampleTable";
		
		String jsonPolicy = createAndUploadPolicyExample(iam, accountId, policyName);
		logger.info(jsonPolicy);
		
		GetPolicyResponse putItemPolicy = iam
				.getPolicy(b -> b.policyArn("arn:aws:iam::" + accountId + ":policy/" + policyName));	
		iam.deletePolicy(b -> b.policyArn(putItemPolicy.policy().arn()));
		logger.info("Policy [{}] deleted", putItemPolicy.policy().arn());
	}
	
	public String createNewBasedOnExistingPolicyExample(IamClient iam, String accountID, String policyName,
			String newPolicyName) {
		
		String policyArn = "arn:aws:iam::" + accountID + ":policy/" + policyName;
		GetPolicyResponse getPolicyResponse = iam.getPolicy(r -> r.policyArn(policyArn));
		
		String policyVersion = getPolicyResponse.policy().defaultVersionId();
		GetPolicyVersionResponse getPolicyVersionResponse = iam
				.getPolicyVersion(r -> r.policyArn(policyArn).versionId(policyVersion));
		
		// Create an IamPolicy instance from the JSON string returned from IAM.
		String decodePolicy = URLDecoder.decode(getPolicyVersionResponse.policyVersion().document(),
				StandardCharsets.UTF_8);
		IamPolicy policy = IamPolicy.fromJson(decodePolicy);
		
		/*
		 * All IamPolicy components are immutable, so use the copy method that creates a new instance that
		 * can be altered in the same method call.
		 * 
		 * Add the ability to get an item from DynamoDB as an additional action.
		 */
		IamStatement newStatement = policy.statements().get(0).copy(s -> s.addAction("dynamodb:GetItem"));
		
		// Create a new statement that replaces the original statement.
		IamPolicy newPolicy = policy.copy(p -> p.statements(Arrays.asList(newStatement)));
		
		// Upload the new policy. IAM now has both policies.
		iam.createPolicy(r -> r.policyName(newPolicyName)
				.policyDocument(newPolicy.toJson()));
		
		return newPolicy.toJson(IamPolicyWriter.builder().prettyPrint(true).build());
	}
	
	private void runCreateNewBasedOnExistingPolicyExample(IamClient iam) {
		String accountID = getAccountID();
		String policyName = "AllowPutItemToExampleTable";
		String newPolicyName = "AllowGetAndPutItemToExampleTable";
		// First part of the example is to create the policy.
		String jsonPolicy = createAndUploadPolicyExample(iam, accountID, policyName);
		logger.info(jsonPolicy);
		
		String jsonNewPolicy = createNewBasedOnExistingPolicyExample(iam, accountID, policyName, newPolicyName);
		logger.info(jsonNewPolicy);
		
		// Delete the two policies
		GetPolicyResponse putItemPolicy = iam
				.getPolicy(b -> b.policyArn("arn:aws:iam::" + accountID + ":policy/" + policyName));
		iam.deletePolicy(b -> b.policyArn(putItemPolicy.policy().arn()));
		logger.info("Policy [{}] deleted", putItemPolicy.policy().arn());
		
		GetPolicyResponse getAndPutItemPolicy = iam
				.getPolicy(b -> b.policyArn("arn:aws:iam::" + accountID + ":policy/" + newPolicyName));
		iam.deletePolicy(b -> b.policyArn(getAndPutItemPolicy.policy().arn()));
		logger.info("Policy [{}] deleted", getAndPutItemPolicy.policy().arn());
	}
	
	public String multipleConditionsExample() {
		IamPolicy policy = IamPolicy.builder()
				.addStatement(b -> b
						.effect(IamEffect.ALLOW)
						.addAction("dynamodb:GetItem")
						.addAction("dynamodb:BatchGetItem")
						.addAction("dynamodb:Query")
						.addAction("dynamodb:PutItem")
						.addAction("dynamodb:UpdateItem")
						.addAction("dynamodb:DeleteItem")
						.addAction("dynamodb:BatchWriteItem")
						.addResource("arn:aws:dynamodb:*:*:table/table-name")
						.addConditions(IamConditionOperator.STRING_EQUALS
								.addPrefix("ForAllValues:"), 
								"dynamodb:Attributes", 
								List.of("column-name1", "column-name2", "column-name3"))
						.addCondition(b1 -> b1
								.operator(IamConditionOperator.STRING_EQUALS
										.addSuffix("IfExists"))
								.key("dynamodb:Select")
								.value("SPECIFIC_ATTRIBUTES")))
				.build();
		return policy.toJson(IamPolicyWriter.builder()
				.prettyPrint(true).build());
	}
	
	private void runMultiplesConditionsExample() {
		String jsonPolicy = multipleConditionsExample();
		logger.info(jsonPolicy);
	}
	
	public String timeBasedPolicyExample() {
		IamPolicy policy = IamPolicy.builder()
				.addStatement(b -> b
						.effect(IamEffect.ALLOW)
						.addAction("dynamodb:GetItem")
						.addResource(IamResource.ALL)
						.addCondition(b1 -> b1
								.operator(IamConditionOperator.DATE_GREATER_THAN)
								.key("aws:CurrentTime")
								.value("2024-07-03T00:00:00Z"))
						.addCondition(b1 -> b1
								.operator(IamConditionOperator.DATE_LESS_THAN)
								.key("aws:CurrentTime")
								.value("2024-06-30T23:59:59Z")))
				.build();
		
		// Use an IamPolicyWriter to write out the JSON string to a more readable format
		return policy.toJson(IamPolicyWriter.builder()
				.prettyPrint(true)
				.build());
	}
	
	private void runTimesBasedPolicyExample() {
		String policyJson = timeBasedPolicyExample();
		logger.info(policyJson);
	}
	
	public String specifyPrincipalsExample() {
		IamPolicy policy = IamPolicy.builder()
				.addStatement(b -> b
						.effect(IamEffect.DENY)
						.addAction("s3:*")
						.addPrincipal(IamPrincipal.ALL)
						.addResource("arn:aws:s3:::BUCKETNAME/*")
						.addResource("arn:aws:s3:::BUCKETNAME")
						.addCondition(b1 -> b1
								.operator(IamConditionOperator.ARN_NOT_EQUALS)
								.key("aws:PrincipalArn")
								.value("arn:aws:iam::444455556666:user/user-name")))
				.build();
		return policy.toJson(IamPolicyWriter.builder()
				.prettyPrint(true).build());
	}
	
	private void runSpecifyPrincipalsExample() {
		String policyJson = specifyPrincipalsExample();
		logger.info(policyJson);
	}
	
	public String allowCrossAccountAccessExample() {
		IamPolicy policy = IamPolicy.builder()
				.addStatement(b -> b
						.effect(IamEffect.ALLOW)
						.addPrincipal(IamPrincipalType.AWS, "111122223333")
						.addAction("s3:PutObject")
						.addResource("arn:aws:s3:::DOC-EXAMPLE-BUCKET/*")
						.addCondition(b1 -> b1
								.operator(IamConditionOperator.STRING_EQUALS)
								.key("s3:x-amz-ac1")
								.value("bucket-owner-full-control")))
				.build();
		return policy.toJson(IamPolicyWriter.builder()
				.prettyPrint(true).build());
	}
	
	private void runAllowCrossAccountAccessExample() {
		String policyJson = allowCrossAccountAccessExample();
		logger.info(policyJson);
	}
	
	String getAccountID() {
		try (StsClient stsClient = StsClient.create()) {
			return stsClient.getCallerIdentity().account();
		}
	}
}
