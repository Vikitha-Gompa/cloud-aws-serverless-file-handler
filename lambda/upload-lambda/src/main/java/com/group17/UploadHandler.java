// package com.group17;

// import java.util.HashMap;
// import java.util.Map;

// import com.amazonaws.services.lambda.runtime.Context;
// import com.amazonaws.services.lambda.runtime.RequestHandler;

// public class UploadHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
//     @Override
//     public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
//         context.getLogger().log("Handler invoked! Input received: " + input);

//         Map<String, Object> response = new HashMap<>();
//         response.put("statusCode", 200);

//         Map<String, String> headers = new HashMap<>();
//         headers.put("Content-Type", "application/json");
//         response.put("headers", headers);

//         response.put("body", "{\"message\":\"Lambda executed successfully!\"}");

//         return response;
//     }
// }

package com.group17;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UploadHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final String bucket = System.getenv("BUCKET_NAME");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Upload Lambda triggered...\n");

        try {
            // Step 1: extract and parse the JSON body (it's a string inside the input map)
            String rawBody = (String) input.get("body");

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> body = mapper.readValue(rawBody, Map.class);

            // Step 2: extract and decode values
            String fileName = body.get("fileName");
            String fileContent = body.get("fileContent");

            byte[] fileBytes = Base64.getDecoder().decode(fileContent);

            // Step 3: upload to S3
            s3.putObject(bucket, fileName, new String(fileBytes));
            context.getLogger().log("File uploaded: " + fileName + "\n");

            // Step 4: return success response
            return response(200, "{\"message\": \"File uploaded successfully.\"}");
        } catch (Exception e) {
            context.getLogger().log("Upload failed: " + e.getMessage() + "\n");
            return response(500, "{\"error\": \"Upload failed.\"}");
        }
    }

    private Map<String, Object> response(int statusCode, String body) {
        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", statusCode);
        res.put("headers", Map.of("Content-Type", "application/json"));
        res.put("body", body);
        return res;
    }
}
