package io.kr.algotrial.trial.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InputOutputService {

    private final AmazonS3Client s3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String getInputText(int problemId) {
        String fileName = "inputData:" + problemId;
        return getTextData(fileName);
    }

    public String getOutputText(int problemId) {
        String fileName = "outputData:" + problemId;
        return getTextData(fileName);
    }

    private String getTextData(String fileName) {
        try {
            S3Object object = s3Client.getObject(bucket, fileName);
            S3ObjectInputStream inputStream = object.getObjectContent();

            String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            inputStream.close();
            return content;
        } catch (Exception e) {
            throw new RuntimeException("파일을 가져오는 중 에러가 발생했습니다 : " + fileName);
        }
    }

}
