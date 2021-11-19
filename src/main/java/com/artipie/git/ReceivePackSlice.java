/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithStatus;
import com.artipie.http.rt.RtRule;
import com.artipie.http.slice.SliceSimple;

/**
 * Slice to handle {@code receive-pack} service commands to support {@code git push}
 * commands.
 * <p>
 * {@code receive-pack} service is initiated by client's {@code send-pack}
 * command which is triggered by
 * {@code git push} to send data to server. Sending pack consists of two phases:
 * <ol>
 *   <li>{@code GET} HTTP request to {@code receive-pack} service to fetch references
 *   info and server metadata</li>
 *   <li>{@code POST} HTTP request with payload where client uploads pack to server</li>
 * </ol>
 * </p>
 *
 * @since 1.0
 */
final class ReceivePackSlice extends Slice.Wrap {

    /**
     * Service routing rule.
     */
    static final RtRule RT_RULE = new GitSlice.ByService("git-upload-pack");

    /**
     * New Slice.
     */
    ReceivePackSlice() {
        super(new SliceSimple(new RsWithStatus(RsStatus.NOT_IMPLEMENTED)));
    }

    /**
     * A slice to return info references as a first phase of {@code receive-pack}.
     *
     * @since 1.0
     */
    static final class InfoRefSlice extends Slice.Wrap {

        /**
         * New info refs slice.
         */
        InfoRefSlice() {
            super(new SliceSimple(new RsWithStatus(RsStatus.NOT_IMPLEMENTED)));
        }
    }
}
