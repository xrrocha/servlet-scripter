package net.xrrocha.scripter.commons.io;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Regions;
import net.xrrocha.scripter.commons.Initializable;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Opens an AWS S3 URL returning its <code>InputStream</code>. This interface encapsulates server
 * credentials, bucket names, etc.
 */
public class S3InputStreamOpener implements InputStreamOpener, Initializable, Serializable {

    private final Regions region;
    private final AWSCredentials credentials;

    private S3UrlStreamHandler s3UrlStreamHandler;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private S3InputStreamOpener() {
        region = null;
        credentials = null;
    }

    S3InputStreamOpener(@NotNull Regions region, @NotNull AWSCredentials credentials) {
        this.region = region;
        this.credentials = credentials;
        initialize();
    }

    /**
     * Open an AWS S3 URL returning its <code>InputStream</code>.
     *
     * @param location The AWS S3 URL string representation
     * @return The <code>InputStream</code> containing the AWS S3 URL's contents
     */
    @Override
    public InputStream openInputStream(@NotNull String location) {
        checkNotNull(location, "Location cannot be null");

        try {

            // Graceful fallback
            if (!location.startsWith("s3://")) {
                return new URL(location).openStream();
            }

            URI uri = new URI(location);
            return new URL(
                    uri.getScheme(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    s3UrlStreamHandler
            )
                    .openStream();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void initialize() {
        checkNotNull(region, "Region cannot be null");
        checkNotNull(credentials, "Credentials cannot be null");
        s3UrlStreamHandler = new S3UrlStreamHandler(region, credentials);
    }
}
