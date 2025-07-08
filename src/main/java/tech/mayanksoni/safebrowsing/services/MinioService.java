package tech.mayanksoni.safebrowsing.services;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mayanksoni.safebrowsing.configuration.SafeBrowsignConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final SafeBrowsignConfig safeBrowsignConfig;
    private String bucketName;

    @PostConstruct
    public void initMinioService() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        this.bucketName = safeBrowsignConfig.getMinioBucketName();
        boolean isBucketExist = isBucketExists();
        if (!isBucketExist) {
            log.debug("Bucket {} does not exist. Creating...", bucketName);
            createBucket();
        }
    }

    public void uploadFile(String fileName, InputStream inputStream) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        this.minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(inputStream, inputStream.available(), -1)
                .contentType("text/csv")
                .build());

    }

    public BufferedReader downloadFile(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        InputStream fis = this.minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
        return new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
    }

    private void createBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        this.minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(bucketName)
                .objectLock(false)
                .build());
    }

    public boolean isFilePresent(String fileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        Iterable<Result<Item>> objects = this.minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        for (Result<Item> object : objects) {
            Item item = object.get();
            if (item.objectName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBucketExists() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
    }

}
