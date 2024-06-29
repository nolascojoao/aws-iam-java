package com.example.iam.accesskeys;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyRequest;
import software.amazon.awssdk.services.iam.model.CreateAccessKeyResponse;
import software.amazon.awssdk.services.iam.model.IamException;

public class CreateAccessAndSecretKeys {
	//private static final String USER = "YOUR_IAM_USER";

	public static void main(String[] args) {
		final String USAGE = """

                Usage:
                   <user>

                Description:
                   The <user> parameter specifies the IAM user for whom you want to create an access key.
                   This IAM user should already exist in your AWS account.

                Example:
                   java com.example.iam.CreateAccessAndSecretKeys <user>
                """;
		
		if (args.length != 1) {
			System.out.println(USAGE);
			System.exit(1);
		}
		
		String user = args[0];
		Region region = Region.AWS_GLOBAL;
		IamClient iam = IamClient.builder()
				.region(region)
				.build();
		
		String[] keys = createIAMAccessAndSecretKeys(iam, user);
		
		System.out.println("Access Key ID: " + keys[0]);
		System.out.println("Secret Key: " + keys[1]);
		
		iam.close();
	}

	public static String[] createIAMAccessAndSecretKeys(IamClient iam, String user) {
		try {
			CreateAccessKeyRequest request = CreateAccessKeyRequest.builder()
					.userName(user)
					.build();
			CreateAccessKeyResponse response = iam.createAccessKey(request);
			
			String accessKeyId = response.accessKey().accessKeyId();
			String secretKey = response.accessKey().secretAccessKey();
			
			return new String[] {accessKeyId, secretKey};
		} catch (IamException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		}
		return null;
	}
}
