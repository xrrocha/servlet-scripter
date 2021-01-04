package net.xrrocha.scripter.commons;

/**
 * Validate and initialize internal state.
 */
public interface Initializable {

    /**
     * Validate and initialize internal state (presumably) prior to first usage.
     */
    void initialize();
}
