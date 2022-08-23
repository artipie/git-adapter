/*
 * The MIT License (MIT) Copyright (c) 2020-2022 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git.http;

import com.artipie.asto.Storage;
import com.artipie.asto.fs.FileStorage;
import com.artipie.http.Slice;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqParams;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import com.artipie.http.slice.LoggingSlice;
import com.artipie.vertx.VertxSliceServer;
import com.jcabi.log.Logger;
import io.vertx.reactivex.core.Vertx;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Git main entry point.
 * <p>
 * Implements git smart-http protocol for git repository.
 * </p>
 *
 * @since 1.0
 * @checkstyle MethodBodyCommentsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class GitSlice extends Slice.Wrap {

    /**
     * New git slice.
     * @param storage Repository storage
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public GitSlice(final Storage storage) {
        super(
            new SliceRoute(
                new RtRulePath(
                    new RtRule.All(
                        ReceivePackSlice.RT_RULE,
                        ByMethodsRule.Standard.POST
                    ),
                    new ReceivePackSlice()
                ),
                new RtRulePath(
                    new RtRule.All(
                        UploadPackSlice.RT_RULE,
                        ByMethodsRule.Standard.POST
                    ),
                    new UploadPackSlice(storage)
                ),
                new RtRulePath(
                    new RtRule.All(
                        ByMethodsRule.Standard.POST,
                        new RtRule.ByPath("/git-upload-pack")
                    ),
                    new UploadPackSlice(storage)
                ),
                new RtRulePath(
                    new RtRule.All(
                        new RtRule.ByPath("/info/refs"),
                        ByMethodsRule.Standard.GET
                    ),
                    new InfoRefsSlice(
                        "git/artipie",
                        new TreeSet<>(
                            Arrays.asList(
                                "ls-refs=unborn",
                                "fetch=shallow wait-for-done filter"
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Main entry point for debugging with git.
     * @param args First arg is a path to git dir
     */
    public static void main(final String... args) {
        final String repo;
        if (args.length > 0) {
            repo = args[0];
        } else {
            repo = "/tmp/artipie-git";
        }
        final VertxSliceServer server = new VertxSliceServer(
            Vertx.vertx(),
            new LoggingSlice(new GitSlice(new FileStorage(Paths.get(repo)))),
            8080
        );
        final int port = server.start();
        Logger.info(GitSlice.class, "Artipie git server started at http://localhost:%d", port);
    }

    /**
     * Routing rule by service name.
     *
     * @since 1.0
     */
    static final class ByService implements RtRule {

        /**
         * Service name.
         */
        private final String name;

        /**
         * New routing rule.
         * @param name Service name
         */
        ByService(final String name) {
            this.name = name;
        }

        @Override
        public boolean apply(final String line, final Iterable<Entry<String, String>> headers) {
            return new RqParams(new RequestLineFrom(line).uri()).value("service")
                .map(srv -> srv.equals(this.name))
                .orElse(false);
        }
    }
}
