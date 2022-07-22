/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.it;

import com.artipie.asto.misc.UncheckedConsumer;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.io.IOUtils;

/**
 * Local command to execute.
 * @since 1.0
 */
final class Cmd {

    /**
     * Command list.
     */
    private final List<String>  commands;

    /**
     * New command.
     * @param cmd Command list
     */
    Cmd(final String... cmd) {
        this.commands = Arrays.asList(cmd);
    }

    /**
     * Exec command with stdin string.
     * @param stdin Stdin
     * @return Stdout
     * @throws IOException On error
     */
    public String exec(final String stdin) throws IOException {
        return this.patchExec(
            new UncheckedConsumer<>(
                proc -> {
                    proc.getOutputStream().write(stdin.getBytes(StandardCharsets.US_ASCII));
                    proc.getOutputStream().close();
                }
            )
        );
    }

    /**
     * Exec command.
     * @return Stdout
     * @throws IOException On error
     */
    public String exec() throws IOException {
        return this.patchExec(
            ignore -> {
            }
        );
    }

    /**
     * Patch process and execute it.
     * @param patcher Patcher func
     * @return Stdout ASCI string
     * @throws IOException On error
     * @checkstyle ReturnCountCheck (30 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    private String patchExec(final Consumer<? super Process> patcher) throws IOException {
        Logger.info(this, "$ %s", String.join(" ", this.commands));
        final Process proc = Runtime.getRuntime().exec(this.commands.toArray(new String[0]));
        patcher.accept(proc);
        final int exit;
        try {
            exit = proc.waitFor();
        } catch (final InterruptedException ignore) {
            Thread.currentThread().interrupt();
            return null;
        }
        if (exit != 0) {
            throw new IOException(
                String.format(
                    "cmd '%s' exit with %d\n\t%s\n",
                    String.join(" ", this.commands),
                    exit,
                    new String(
                        IOUtils.toByteArray(proc.getErrorStream()), StandardCharsets.UTF_8
                    )
                )
            );
        }
        final String res = new String(
            IOUtils.toByteArray(proc.getInputStream()), StandardCharsets.UTF_8
        );
        Logger.info(this, "> %s\n", res);
        return res.trim();
    }
}
