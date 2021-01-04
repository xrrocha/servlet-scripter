package net.xrrocha.scripter.commons.source.io;

import net.xrrocha.scripter.commons.source.Source;

import javax.validation.constraints.NotNull;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Builds a new <code>OutputStream</code> on demand.
 */
public class FileOutputStreamSource
        extends IOStreamSource<OutputStream>
        implements Source<OutputStream>, Serializable {

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private FileOutputStreamSource() {
        super();
    }

    public FileOutputStreamSource(@NotNull String filename) {
        super(filename);
    }

    @Override
    protected OutputStream createFromFilename(@NotNull String filename) throws Exception {
        return new FileOutputStream(filename);
    }
}
