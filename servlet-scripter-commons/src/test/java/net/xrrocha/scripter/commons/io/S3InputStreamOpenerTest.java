package net.xrrocha.scripter.commons.io;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import org.junit.Test;

public class S3InputStreamOpenerTest {

    @Test(expected = NullPointerException.class)
    public void rejectsNullRegion() {
        AWSCredentials credentials = new BasicAWSCredentials("key", "secret");
        new S3InputStreamOpener(null, credentials);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullCredentials() {
        new S3InputStreamOpener(Regions.US_EAST_1, null);
    }

    @Test(expected = NullPointerException.class)
    public void rejectsNullLocation() {
        new S3InputStreamOpener(Regions.US_EAST_1, new AnonymousAWSCredentials()).openInputStream(null);
    }
}

