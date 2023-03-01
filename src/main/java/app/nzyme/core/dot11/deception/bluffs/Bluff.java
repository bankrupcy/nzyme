/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.dot11.deception.bluffs;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class Bluff {

    private static final Logger LOG = LogManager.getLogger(Bluff.class);

    protected abstract String scriptCategory();
    protected abstract String scriptName();
    protected abstract Map<String, String> parameters();

    private final NodeConfiguration configuration;

    @Nullable
    private String invokedCommand;

    private final List<String> stderr;

    public Bluff(NodeConfiguration configuration) {
        this.configuration = configuration;
        this.stderr = Lists.newArrayList();
    }

    public void execute() throws BluffExecutionException, InsecureParametersException {
        try {
            File script = ensureScript();

            StringBuilder exec = new StringBuilder()
                    .append("removeme")
                    .append(" ")
                    .append(script.getCanonicalPath())
                    .append(" ");

            // Reminder: Parameters are validated and sanitized during construction.
            for (Map.Entry<String, String> parameter : parameters().entrySet()) {
                exec.append(parameter.getKey())
                        .append(" ")
                        .append(parameter.getValue())
                        .append(" ");
            }

            this.invokedCommand = exec.toString().trim();

            // Check that all parameters and the script information is safe.
            try {
                validateParameters();
            } catch (InsecureParametersException e) {
                LOG.warn("Insecure parameters passed to bluff [{}]. Refusing to execute.", this.getClass().getCanonicalName());
                throw e;
            }

            Process p = Runtime.getRuntime().exec(invokedCommand);
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line;
            while ((line = err.readLine()) != null) {
                stderr.add(line);
            }

            p.waitFor();
            err.close();

            if (!stderr.isEmpty()) {
                debug();
                throw new BluffExecutionException("STDERR is not empty.");
            }
        } catch (InterruptedException e) {
            LOG.info("Bluff [{}] interrupted.", this.getClass().getCanonicalName(), e);
        } catch (IOException e) {
            throw new BluffExecutionException(e);
        }
    }

    public void debug() {
        LOG.info("Bluff [{}]: Invoked command {{}}.", getClass().getCanonicalName(), getInvokedCommand());

        if (stderr.isEmpty()) {
            LOG.info("Bluff [{}]: No lines written to STDERR.", getClass().getCanonicalName());
        } else {
            LOG.info("Bluff [{}]: {} lines written to STDERR:", getClass().getCanonicalName(), stderr.size());

            for (String line : stderr) {
                LOG.info("\t\tSTDERR: {}", line);
            }
        }
    }

    private void validateParameters() throws InsecureParametersException {
        for (Map.Entry<String, String> x : parameters().entrySet()) {
            if (!Tools.isSafeParameter(x.getKey()) || !Tools.isSafeParameter(x.getValue())) {
                throw new InsecureParametersException();
            }
        }

    }

    /**
     * Copies the script from the resources folder to BLUFF_DIRECTORY. We do this because the python interpreter
     * cannot reach into the .jar to execute the scripts directly.
     *
     * @return The file that holds the script
     * @throws IOException
     */
    private File ensureScript() throws IOException {
        // TODO if this doesn't work packaged, use resourceAsStream like EmailCallback does.
        URL url = Resources.getResource("bluffs/" + scriptCategory() + "/" + scriptName());
        String text = Resources.toString(url, Charsets.UTF_8);
        File target = new File("removeme");

        Files.asByteSink(target).write(text.getBytes());

        return target;
    }

    @Nullable
    public String getInvokedCommand() {
        return invokedCommand;
    }

    public class InsecureParametersException extends Exception {
    }

    public class BluffExecutionException extends Exception {
        public BluffExecutionException(String s) {
            super(s);
        }

        public BluffExecutionException(Throwable t) {
            super(t);
        }
    }
}
