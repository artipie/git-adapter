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
 * Slice to handle {@code upload-pack} service commands to support {@code git fetch}
 * commands.
 * <p>
 * {@code upload-pack} service is initiated by client's {@code fetch-pack} command which
 * is triggered by {@code git fetch} to receive data from server. Fetching pack consists
 * of two phases:
 * <ol>
 *   <li>{@code GET} HTTP request to {@code upload-pack} service, where client is sending
 *   own references and expects new updates to calculate the difference. Also, some server
 *   metadata is included into the response</li>
 *   <li>{@code POST} HTTP request to {@code upload-pack} service, where client sends
 *   references to include into pack file, and server uploads pack to client.</li>
 * </ol>
 *
 * @since 1.0
 */
final class UploadPackSlice extends Slice.Wrap {

    /**
     * Service routing name.
     */
    static final RtRule RT_RULE = new GitSlice.ByService("upload-pack");

    /**
     * New upload pack service.
     */
    UploadPackSlice() {
        super(new SliceSimple(new RsWithStatus(RsStatus.NOT_IMPLEMENTED)));
    }

    /**
     * References info phase for upload pack.
     *
     * @since 1.0
     */
    static final class InfoRefSlice extends Slice.Wrap {

        /**
         * Ne info refs slice.
         */
        InfoRefSlice() {
            super(new SliceSimple(new RsWithStatus(RsStatus.NOT_IMPLEMENTED)));
        }
    }
}
