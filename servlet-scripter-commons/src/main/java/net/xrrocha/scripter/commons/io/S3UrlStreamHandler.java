package net.xrrocha.scripter.commons.io;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * AWS S3-aware implementation of @see{URLStreamHandler}.
 */
public class S3UrlStreamHandler extends URLStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(S3UrlStreamHandler.class);
    private final AmazonS3 s3;

    S3UrlStreamHandler(@NotNull Regions region, @NotNull AWSCredentials credentials) {
        s3 = AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    protected URLConnection openConnection(@NotNull URL url) {
        return new URLConnection(url) {

            @Override
            public InputStream getInputStream() {

                String bucket = url.getHost().substring(0, url.getHost().indexOf("."));
                logger.debug("bucket: {}", bucket);

                String key = url.getPath().substring(1);
                logger.debug("key: {}", key);

                S3Object s3obj = s3.getObject(bucket, key);

                return s3obj.getObjectContent();
            }

            @Override
            public void connect() {
            }
        };

    }
}
