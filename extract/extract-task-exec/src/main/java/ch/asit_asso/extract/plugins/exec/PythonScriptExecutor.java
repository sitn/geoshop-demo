package ch.asit_asso.extract.plugins.exec;

import java.io.IOException;
import java.nio.file.Path;

public class PythonScriptExecutor implements ExternalExecutor {

    private static final String EXECUTABLE = "python3";

    @Override
    public void execute(Path executable, Path input, Path output) throws ExecException {
        try {

            Process proc = new ProcessBuilder()
                    .inheritIO()
                    .command(EXECUTABLE, executable.toString(), input.toString(), output.toString())
                    .start();
            var exitCode = proc.waitFor();
            if (exitCode != 0) {
                throw new ExecException(String.format(
                        "Non-zero exit code %d while running \"%s\" "
                        + "with input \"%s\" and output \"%s\"",
                        exitCode, executable, input, output));
            }
        } catch (IOException | InterruptedException ex) {
            throw new ExecException(
                    String.format("Error while running \"%s\" with input \"%s\" and output \"%s\"",
                            executable, input, output), ex);
        }
    }

}
