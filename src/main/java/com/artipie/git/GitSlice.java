/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.asto.Storage;
import com.artipie.http.Slice;
import com.artipie.http.rq.RqParams;
import com.artipie.http.rt.ByMethodsRule;
import com.artipie.http.rt.RtRule;
import com.artipie.http.rt.RtRulePath;
import com.artipie.http.rt.SliceRoute;
import java.util.Map.Entry;

/**
 * Git main entry point.
 * <p>
 * Implements git smart-http protocol for git repository.
 * </p>
 *
 * @since 1.0
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
                        ByMethodsRule.Standard.GET
                    ),
                    new ReceivePackSlice.InfoRefSlice()
                ),
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
                        ByMethodsRule.Standard.GET
                    ),
                    new UploadPackSlice.InfoRefSlice()
                ),
                new RtRulePath(
                    new RtRule.All(
                        UploadPackSlice.RT_RULE,
                        ByMethodsRule.Standard.POST
                    ),
                    new UploadPackSlice()
                )
            )
        );
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
            return new RqParams(line).value("service").map(srv -> srv.equals(this.name))
                .orElse(false);
        }
    }
}
