package ch.asit_asso.extract.plugins.exec;

import java.nio.file.Path;

public interface ExternalExecutor {

    public void execute(Path executable, Path input, Path output) throws ExecException;
}
