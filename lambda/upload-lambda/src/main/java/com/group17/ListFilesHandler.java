package com.group17;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;

public class ListFilesHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
    private final String bucket = System.getenv("BUCKET_NAME");

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        context.getLogger().log("Listing files from bucket: " + bucket + "\n");

        try {
            ObjectListing listing = s3.listObjects(bucket);

            List<Map<String, String>> files = listing.getObjectSummaries().stream()
                .map(s3Object -> {
                    Map<String, String> fileInfo = new HashMap<>();
                    String fileName = s3Object.getKey();
                    String fileUrl = "https://" + bucket + ".s3.amazonaws.com/" + fileName;
    
                    fileInfo.put("name", fileName);
                    fileInfo.put("url", fileUrl);
                    return fileInfo;
                })
                .collect(Collectors.toList());
    
            Map<String, Object> body = new HashMap<>();
            body.put("files", files);
    
            return response(200, body);
        } catch (Exception e) {
            context.getLogger().log("Error listing files: " + e.getMessage());
            return response(500, Map.of("error", "Could not list files"));
        }
    }

    private Map<String, Object> response(int statusCode, Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        res.put("statusCode", statusCode);
        res.put("headers", Map.of("Content-Type", "application/json"));
        res.put("body", body.toString()); // Quick conversion
        return res;
    }
}
