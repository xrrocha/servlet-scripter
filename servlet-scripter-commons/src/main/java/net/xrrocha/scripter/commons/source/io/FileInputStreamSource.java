package net.xrrocha.scripter.commons.source.io;

import net.xrrocha.scripter.commons.source.Source;

import javax.validation.constraints.NotNull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Builds a new <code>InputStream</code> on demand.
 */
public class FileInputStreamSource
        extends IOStreamSource<InputStream>
        implements Source<InputStream>, Serializable {

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    protected FileInputStreamSource() {
        super();
    }

    public FileInputStreamSource(@NotNull String filename) {
        super(filename);
    }

    @Override
    protected InputStream createFromFilename(@NotNull String filename) throws Exception {
        return new FileInputStream(filename);
    }
}
