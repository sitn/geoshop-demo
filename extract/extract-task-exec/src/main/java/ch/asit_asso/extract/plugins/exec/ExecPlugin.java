/*
 * Copyright (C) 2024 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.plugins.exec;

import ch.asit_asso.extract.plugins.common.IEmailSettings;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin that runs an executable and collects the output
 *
 * @author Andrey Rusakov
 */
public class ExecPlugin implements ITaskProcessor {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String CONFIG_FILE_PATH = "plugins/exec/properties/configExec.properties";
    private static final String HELP_FILE_NAME = "executorHelp.html";
    private final Logger logger = LoggerFactory.getLogger(ExecPlugin.class);
    private final String code = "EXECUTE";
    private final String pictoClass = "fa-folder-open-o";

    private final ExternalExecutor executor;
    private final LocalizedMessages messages;
    private final Map<String, String> inputs;
    private final PluginConfiguration config;

    public ExecPlugin() {
        this(DEFAULT_LANGUAGE, Map.of(), new PythonScriptExecutor());
    }

    public ExecPlugin(final String language) {
        this(language, Map.of(), new PythonScriptExecutor());
    }

    public ExecPlugin(final Map<String, String> settings) {
        this(DEFAULT_LANGUAGE, settings, new PythonScriptExecutor());
    }

    public ExecPlugin(final String language, final Map<String, String> settings, ExternalExecutor executor) {
        this.inputs = new HashMap<>(settings);
        this.config = new PluginConfiguration(ExecPlugin.CONFIG_FILE_PATH);
        this.messages = new LocalizedMessages(language);
        this.executor = executor;
    }

    @Override
    public final ExecPlugin newInstance(final String language) {
        return new ExecPlugin(language);
    }

    @Override
    public final ExecPlugin newInstance(final String language, final Map<String, String> settings) {
        return new ExecPlugin(language, settings, new PythonScriptExecutor());
    }

    @Override
    public final String getLabel() {
        return this.messages.getString("plugin.label");
    }

    @Override
    public final String getCode() {
        return this.code;
    }

    @Override
    public final String getDescription() {
        return this.messages.getString("plugin.description");
    }

    @Override
    public final String getHelp() {
        return this.messages.getFileContent(ExecPlugin.HELP_FILE_NAME);
    }

    @Override
    public final String getPictoClass() {
        return pictoClass;
    }

    @Override
    public final String getParams() {
        var mapper = new ObjectMapper();
        var parametersNode = mapper.createArrayNode();

        parametersNode.addObject()
                .put("code", this.config.getProperty("pathToScript"))
                .put("label", this.messages.getString("pathToScript.label"))
                .put("type", "text")
                .put("req", true)
                .put("maxlength", 255);
        try {
            return mapper.writeValueAsString(parametersNode);
        } catch (JsonProcessingException exception) {
            logger.error("An error occurred when the parameters were converted to JSON.", exception);
            return null;
        }
    }

    @Override
    public final ITaskProcessorResult execute(final ITaskProcessorRequest request, final IEmailSettings emailSettings) {
        this.logger.info("Starting executor plugin: %s / %s", request, emailSettings);
        var pluginResult = new ExecResult();
        var folderIn = Path.of(request.getFolderIn());
        var folderOut = Path.of(request.getFolderOut());
        var pathToScript = Path.of(inputs.get(config.getProperty("pathToScript")));

        pluginResult.setErrorCode("");
        pluginResult.setMessage(
                String.format("Script: %s, In: %s, Out: %s",
                        pathToScript, folderIn, folderOut));
        pluginResult.setRequestData(request);
        try {
            if (!Files.exists(folderOut)) {
                Files.createDirectories(folderOut);
            }
            if (!Files.exists(folderIn)) {
                Files.createDirectories(folderIn);
            }
            Files.write(folderIn.resolve("request.json"),
                    new ObjectMapper().writeValueAsBytes(request));
            executor.execute(pathToScript, folderIn, folderOut);
            pluginResult.setStatus(ITaskProcessorResult.Status.SUCCESS);
        } catch (IOException | ExecException ex) {
            pluginResult.setStatus(ITaskProcessorResult.Status.ERROR);
            pluginResult.setErrorCode("-1");
            pluginResult.setMessage(ex.toString());
        }
        return pluginResult;
    }
}
