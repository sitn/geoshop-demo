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

import ch.asit_asso.extract.plugins.common.ITaskProcessorResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Path;
import java.util.Map;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 *
 * @author arusakov
 */
@Tag("fast")
public class ExecPluginTest {

    private static final String TEST_SCRIPT_NAME = "script";

    ExternalExecutor executor;
    ExecPlugin plugin;
    ExecRequest request;

    @BeforeEach
    public void setUp() {
        executor = mock(ExternalExecutor.class);
        plugin = new ExecPlugin("en", Map.of("path", TEST_SCRIPT_NAME), executor);
        request = new ExecRequest();
        request.setFolderIn("input_folder");
        request.setFolderOut("output_folder");
    }

    @Test
    public void testDocsLoaded() {
        assertNotEquals("", new ExecPlugin().getHelp());
    }

    @Test
    public void testExecute_success() {
        var result = plugin.execute(request, null);

        verify(executor, times(1)).execute(
                Path.of(TEST_SCRIPT_NAME),
                Path.of(request.getFolderIn()),
                Path.of(request.getFolderOut()));

        assertEquals(result.getStatus(), ITaskProcessorResult.Status.SUCCESS);
    }

    @Test
    public void testExecute_failure() {
        doThrow(new ExecException("Some error", new Exception()))
                .when(executor).execute(any(), any(), any());

        var result = plugin.execute(request, null);

        verify(executor, times(1)).execute(
                Path.of(TEST_SCRIPT_NAME),
                Path.of(request.getFolderIn()),
                Path.of(request.getFolderOut()));

        assertEquals(result.getStatus(), ITaskProcessorResult.Status.ERROR);
    }
}
