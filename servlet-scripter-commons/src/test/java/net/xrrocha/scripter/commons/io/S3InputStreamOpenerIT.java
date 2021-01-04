package net.xrrocha.scripter.commons.io;

import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.regions.Regions;
import com.google.common.io.ByteStreams;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

@Ignore
public class S3InputStreamOpenerIT {

    @Test
    public void retrievesFile() throws Exception {
        InputStream is = new S3InputStreamOpener(Regions.US_EAST_1, new AnonymousAWSCredentials())
                .openInputStream("s3://xrrocha.s3.amazonaws.com/scripter-scripter/scripter-scripter-test.jar");
        assertNotNull(is);
        ByteStreams.exhaust(is);
    }

    @Test
    public void acceptsValidNonS3Location() throws Exception {
        InputStream is = new S3InputStreamOpener(Regions.US_EAST_1, new AnonymousAWSCredentials())
                .openInputStream("https://s3.amazonaws.com/xrrocha/scripter-scripter/scripter-scripter-test.jar");
        assertNotNull(is);
        ByteStreams.exhaust(is);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectInvalidNonS3Location() {
        InputStream is = new S3InputStreamOpener(Regions.US_EAST_1, new AnonymousAWSCredentials())
                .openInputStream("badUrl://bad/bad.bad");
    }
}
