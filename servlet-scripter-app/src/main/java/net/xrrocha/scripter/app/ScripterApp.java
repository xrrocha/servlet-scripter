package net.xrrocha.scripter.app;

import net.xrrocha.scripter.Scripter;
import net.xrrocha.scripter.commons.Initializable;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static net.xrrocha.scripter.commons.YamlUtils.YAML;
import static spark.Spark.*;

/**
 * HTTP service exposing @see{Scripter} operations through a REST API.
 */
public class ScripterApp implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ScripterApp.class);
    private final Scripter scripter;

    private ScripterApp() {
        scripter = null;
    }

    public ScripterApp(Scripter scripter) {
        this.scripter = scripter;
        initialize();
    }

    public void start() {

        post("/scripts", (req, res) -> {

            String scriptYamlString = req.body();
            boolean replace = req.queryMap("replace").booleanValue();
            Optional<String> previousScript = scripter.addScript(scriptYamlString, replace);

            res.status(HttpStatus.CREATED_201);
            res.header("Content-Type", "text/vnd.yaml");

            return previousScript.orElse("");
        });

        get("/scripts", (req, res) -> {

            String[] regexes = req.queryMap("pattern").values();
            List<Pattern> patterns = Arrays.stream(regexes)
                    .map(Pattern::compile)
                    .collect(toList());

            Iterable<String> scriptNames = scripter.listScriptIds();
            List<String> selectedNames =
                    StreamSupport.stream(scriptNames.spliterator(), false)
                            .map(scriptName ->
                                    patterns.isEmpty() ? Optional.of(scriptName) :
                                            patterns.stream()
                                                    .filter(pattern -> pattern.matcher(scriptName).find())
                                                    .findFirst()
                                                    .map(pattern -> scriptName))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(toList());

            res.status(HttpStatus.OK_200);
            res.header("Content-Type", "text/plain");

            String result = selectedNames.stream().collect(joining("\n"));
            logger.debug("scriptNames: {}", result);

            return result;
        });

        post("/scripts/:scriptId", (req, res) -> {

            String paramYamlString = req.body();
            final Map<String, Object> params;

            if (paramYamlString != null && !paramYamlString.trim().isEmpty()) {
                params = YAML.load(paramYamlString);
            } else {
                params = emptyMap();
            }

            String scriptId = req.params(":scriptId");
            Object result = scripter.executeScript(scriptId, params);

            res.header("Content-Type", "text/vnd.yaml");
            if (result != null) {
                res.status(HttpStatus.OK_200);
                // TODO(rrocha) How to serializes language-specific object results?
                return YAML.dump(result);
            } else {
                res.status(HttpStatus.NO_CONTENT_204);
                return "";
            }
        });

        get("/scripts/:scriptId", (req, res) -> {

            String scriptId = req.params(":scriptId");

            return scripter.getScriptText(scriptId)
                    .map(script -> {

                        res.status(HttpStatus.OK_200);
                        res.header("Content-Type", "text/vnd.yaml");
                        return script;
                    })
                    .orElseGet(() -> {

                        res.status(HttpStatus.NOT_FOUND_404);
                        return "";
                    });
        });

        delete("/scripts/:scriptId", (req, res) -> {
            String scriptId = req.params(":scriptId");
            scripter.removeScript(scriptId);
            res.status(HttpStatus.NO_CONTENT_204);
            return "";
        });

    }

    public void stop() {

    }

    @Override
    public void initialize() {
        checkNotNull(scripter, "Scripter cannot be null");

    }
}
