package com.myorg;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.GatewayResponseOptions;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.ResponseType;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

public class Proj3InfraStack extends Stack {

    public Proj3InfraStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public Proj3InfraStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // 1. Create the S3 Bucket
        Bucket imageBucket = Bucket.Builder.create(this, "Proj3Images")
                .bucketName("proj3-images-" + UUID.randomUUID())
                .versioned(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // 2. Create the Lambda Function
        Function uploadLambda = Function.Builder.create(this, "Proj3UploadLambda")
                .runtime(Runtime.JAVA_11)
                .handler("com.group17.UploadHandler")
                .code(Code.fromAsset("../lambda/upload-lambda/target/upload-lambda.jar"))
                .memorySize(512)
                .timeout(Duration.seconds(10))
                .environment(Map.of("BUCKET_NAME", imageBucket.getBucketName()))
                .build();

        imageBucket.grantWrite(uploadLambda);

        Function listLambda = Function.Builder.create(this, "Proj3ListLambda")
                .runtime(Runtime.JAVA_11)
                .handler("com.group17.ListFilesHandler")
                .code(Code.fromAsset("../lambda/upload-lambda/target/upload-lambda.jar"))
                .environment(Map.of("BUCKET_NAME", imageBucket.getBucketName()))
                .memorySize(512)
                .timeout(Duration.seconds(10))
                .build();

        imageBucket.grantRead(listLambda);

        // 3. Use RestApi instead of LambdaRestApi
        RestApi restApi = RestApi.Builder.create(this, "Proj3Api")
                .restApiName("proj3-upload-api")
                .build();

        // 4. Attach Lambda to POST method
        restApi.getRoot().addMethod("POST", new LambdaIntegration(uploadLambda));

        // 5. Add CORS preflight
        restApi.getRoot().addCorsPreflight(CorsOptions.builder()
                .allowOrigins(List.of("*"))
                .allowMethods(List.of("POST", "OPTIONS"))
                .allowHeaders(List.of("*"))
                .build());

        // 6. CORS-friendly 4XX error response
        restApi.addGatewayResponse("Default4XX", GatewayResponseOptions.builder()
                .type(ResponseType.of("DEFAULT_4XX"))
                .responseHeaders(Map.of(
                        "Access-Control-Allow-Origin", "'*'",
                        "Access-Control-Allow-Headers", "'*'",
                        "Access-Control-Allow-Methods", "'POST,OPTIONS'"
                ))
                .build());

        // Add route to /list
        restApi.getRoot().addResource("list").addMethod("GET", new LambdaIntegration(listLambda));
    }
}
