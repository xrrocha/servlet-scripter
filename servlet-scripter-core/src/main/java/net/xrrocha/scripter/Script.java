package net.xrrocha.scripter;

import com.google.common.base.MoreObjects;
import net.xrrocha.scripter.commons.Initializable;
import net.xrrocha.scripter.commons.classloader.ParentLastClassLoaderCreator;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static net.xrrocha.scripter.ScriptUsage.INVOCABLE_SCRIPT;

/**
 * Script definition.
 */
public class Script implements Initializable, Serializable {

    private final String id;
    private final String language;
    private final ScriptUsage usage;
    private final String description;
    private final ParentLastClassLoaderCreator classLoaderCreator;
    private final Set<String> services;
    private final Map<String, Object> globalVariables;
    private final String script;

    /**
     * The reflective, field-based constructor for JSON/Yaml field-based bean access.
     */
    private Script() {
        id = UUID.randomUUID().toString();
        language = "javascript";
        usage = INVOCABLE_SCRIPT;
        description = null;
        classLoaderCreator = null;
        services = emptySet();
        globalVariables = emptyMap();
        script = null;
    }

    public Script(@NotNull String id,
                  @NotNull String language,
                  @NotNull ScriptUsage usage,
                  @NotNull String description,
                  @NotNull ParentLastClassLoaderCreator classLoaderCreator,
                  @NotNull Set<String> services,
                  @NotNull Map<String, Object> globalVariables,
                  @NotNull String script) {

        if (id != null) {
            this.id = id;
        } else {
            this.id = UUID.randomUUID().toString();
        }

        if (language != null) {
            this.language = language;
        } else {
            this.language = "javascript";
        }

        if (usage != null) {
            this.usage = usage;
        } else {
            this.usage = INVOCABLE_SCRIPT;
        }

        this.script = script;

        this.description = description;

        if (services != null) {
            this.services = services;
        } else {
            this.services = emptySet();
        }

        if (globalVariables != null) {
            this.globalVariables = globalVariables;
        } else {
            this.globalVariables = emptyMap();
        }

        this.classLoaderCreator = classLoaderCreator;

        initialize();
    }

    @Override
    public void initialize() {

        checkNotNull(id, "Id cannot be null");
        checkNotNull(language, "Language cannot be null");
        checkNotNull(usage, "Usage cannot be null");
        checkNotNull(script, "Script body cannot be null");

        checkArgument(!id.trim().isEmpty(), "Id cannot be blank");

        checkArgument(!language.trim().isEmpty(), "Language name cannot be blank");

        if (description != null) {
            checkArgument(!description.trim().isEmpty(), "Description cannot be empty");
        }

        checkArgument(!script.trim().isEmpty(), "Script body cannot be blank");

        if (this.globalVariables != null && this.services != null) {
            checkArgument(this.services.stream().noneMatch(this.globalVariables::containsKey),
                    "One or more clashes between variable and service names");
        }

        if (services != null) {
            boolean validServices =
                    services.stream().allMatch(service -> service != null && !service.trim().isEmpty());
            if (!validServices) {
                throw new IllegalArgumentException("One or more service names are null or empty");
            }
        }

        if (globalVariables != null) {
            boolean validVariables =
                    globalVariables.entrySet().stream()
                            .allMatch(entry ->
                                    entry.getKey() != null &&
                                            !entry.getKey().trim().isEmpty()
                            );
            if (!validVariables) {
                throw new IllegalArgumentException("One or more variable names are null or empty");
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("language", language)
                .add("usage", usage)
                .add("description", description)
                .add("parentLastClassLoaderCreator", classLoaderCreator)
                .add("services", services)
                .add("globalVariables", globalVariables)
                .add("script", script)
                .toString();
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public ScriptUsage getUsage() {
        return usage;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Set<String> getServices() {
        return services;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public String getScript() {
        return script;
    }

    public Optional<ParentLastClassLoaderCreator> getClassLoaderCreator() {
        return Optional.ofNullable(classLoaderCreator);
    }
}
