/*
 * The MIT License (MIT) Copyright (c) 2020-2023 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.it;

import com.artipie.asto.fs.FileStorage;
import com.artipie.git.http.GitSlice;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

/**
 * Integration test for Artipie Git server.
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ExecutableStatementCountCheck (500 lines)
 * @checkstyle MethodBodyCommentsCheck (500 lines)
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.TooManyMethods"})
@DisabledOnOs(
    value = OS.WINDOWS,
    disabledReason = "this test depends on alpine:3.14 Linux Docker container"
)
final class GitITCase {

    /**
     * Vertx instance.
     */
    private Vertx vertx;

    /**
     * Vertx server.
     */
    private VertxSliceServer server;

    /**
     * Container with git client.
     */
    private GitContainer container;

    @BeforeEach
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    void beforeEach(@TempDir final Path tmp) throws Exception {
        this.vertx = Vertx.vertx();
        // init bare repository with test data
        final String gitdir = tmp.toAbsolutePath().toString();
        new Cmd("git", "--git-dir", gitdir, "init", "--bare").exec();
        new Cmd("git", "--git-dir", gitdir, "config", "--local", "user.name", "host@test.com");
        new Cmd("git", "--git-dir", gitdir, "config", "--local", "user.name", "Host");
        final String hash = new Cmd(
            "git", "--git-dir", gitdir, "hash-object", "-w", "--stdin"
        ).exec("test\n");
        new Cmd(
            "git", "--git-dir", gitdir, "update-index", "--add",
            "--cacheinfo", "10644", hash, "test.txt"
        ).exec();
        final String tree = new Cmd("git", "--git-dir", gitdir, "write-tree").exec();
        final String commit = new Cmd(
            "git", "--git-dir", gitdir, "commit-tree", "-m", "test commit", tree
        ).exec();
        new Cmd(
            "git", "--git-dir", gitdir, "update-ref", "refs/heads/master", commit
        ).exec();
        Logger.info(this, "initialize git bare repo at '%s'\n", gitdir);
        this.server = new VertxSliceServer(
            this.vertx,
            new LoggingSlice(Level.INFO, new GitSlice(new FileStorage(tmp)))
        );
        final int port = this.server.start();
        final String base = String.format("http://host.testcontainers.internal:%d", port);
        Logger.info(this, "Artipie git server started on %s\n", base);
        this.container = new GitContainer()
            .withWorkingDirectory("/w")
            .withEnv("GIT_CURL_VERBOSE", "1")
            .withCommand("tail", "-f", "/dev/null");
        Testcontainers.exposeHostPorts(port);
        this.container.start();
        this.container.execInContainer("apk", "--update", "add", "git", "bash");
        this.container.execInContainer("git", "init");
        this.container.execInContainer("git", "remote", "add", "origin", base);
        this.container.execInContainer("git", "config", "user.email", "none@none.com");
        this.container.execInContainer("git", "config", "user.name", "test");
    }

    @AfterEach
    void tearDown() {
        if (this.server != null) {
            this.server.close();
        }
        if (this.vertx != null) {
            this.vertx.close();
        }
        if (this.container != null) {
            this.container.stop();
            this.container.close();
        }
    }

    @Test
    @Disabled
    void pushToRemote() throws Exception {
        final String path = "/tmp/data";
        // @checkstyle MagicNumberCheck (5 lines)
        this.container.copyFileToContainer(
            Transferable.of(new byte[]{0x74, 0x65, 0x73, 0x74, 0x0A}),
            path
        );
        final String hash = this.gitHashBlobObjct(path);
        this.gitUpdateIndexCache(hash, "test");
        final String tree = this.gitWriteTree();
        final String commit = this.gitCommitTree(tree, "test-commit");
        this.gitUpdateRef("refs/heads/test", commit);
        this.bash("git push origin refs/head/master");
    }

    @Test
    @Disabled
    void fetchFromRemote() {
        this.bash("git fetch -pvt");
    }

    @Test
    void lsRemote()throws Exception {
        final String result = this.bash("git ls-remote --quiet --heads --refs");
        MatcherAssert.assertThat(result, new StringContains("refs/heads/master"));
    }

    /**
     * Write data from path to git object store as blob.
     * @param path Data
     * @return Blob object hash
     */
    private String gitHashBlobObjct(final String path) {
        return this.bash("git hash-object -w %s", path);
    }

    /**
     * Update git index cache with new blob object.
     * @param obj Blob object hash
     * @param path Index path
     */
    private void gitUpdateIndexCache(final String obj, final String path) {
        this.bash("git update-index --add --cacheinfo 10644 %s %s", obj, path);
    }

    /**
     * Write current index to tree object.
     * @return Tree object hash
     */
    private String gitWriteTree() {
        return this.bash("git write-tree");
    }

    /**
     * Commit current working tree with message.
     * @param tree Tree object hash
     * @param cmt Commit message
     * @return Commit object hash
     */
    private String gitCommitTree(final String tree, final String cmt) {
        return this.bash("git commit-tree -m %s %s", cmt, tree);
    }

    /**
     * Update git references.
     * @param ref Reference name
     * @param commit Commit object hash
     */
    private void gitUpdateRef(final String ref, final String commit) {
        this.bash("git update-ref %s %s", ref, commit);
    }

    /**
     * Executes a command.
     * @param fmt Command formatStderr
     * @param args Format args
     * @return Stdout
     * @checkstyle ReturnCountCheck (20 lines)
     */
    @SuppressWarnings("PMD.OnlyOneReturn")
    private String bash(final String fmt, final Object... args) {
        final String command = String.format(fmt, args);
        System.out.printf("$ %s\n", command);
        final Container.ExecResult exec;
        try {
            exec = this.container.execInContainer(
                "/bin/bash",
                "-c",
                command
            );
        } catch (final InterruptedException iex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (final IOException err) {
            throw new UncheckedIOException("Bash command failed in container", err);
        }
        if (exec.getExitCode() != 0) {
            throw new IllegalStateException(
                String.format("command '%s' returned %d code", command, exec.getExitCode()),
                new IllegalStateException(exec.getStderr())
            );
        }
        if (!exec.getStderr().isEmpty()) {
            System.out.printf("ERROR: %s\n", exec.getStderr());
        }
        System.out.println(exec.getStdout());
        return exec.getStdout().trim();
    }

    /**
     * Git container.
     * @since 1.0
     */
    private static class GitContainer extends GenericContainer<GitContainer> {
        GitContainer() {
            super("alpine:3.14");
        }
    }
}
