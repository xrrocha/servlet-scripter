package net.xrrocha.scripter.transformer;

import net.xrrocha.scripter.commons.Initializable;

/**
 * Transpiles Ecmascript7 to Ecmascript5.1 Javascript.
 */
public class BabelEcmascript7Transformer extends JavascriptTransformer implements Initializable {

    private static final String TRANSPILER_NAME = "Babel";

    public BabelEcmascript7Transformer() {
        super(TRANSPILER_NAME,
                "scripter/transpiler/javascript/ecmascript7/babel-6.23.0.js",
                () -> TRANSPILER_NAME + ".transform(scriptBody, { presets: ['es2015'] }).code;"
        );
        initialize();
    }

    @Override
    public void initialize() {
        super.initialize();
        // Perform long-running transpiler compilation on separate thread
        new Thread(() -> getTranspiler()).start();
    }
}
