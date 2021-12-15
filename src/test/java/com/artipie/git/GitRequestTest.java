/*
 * The MIT License (MIT) Copyright (c) 2020-2021 artipie.com
 * https://github.com/artipie/git-adapter/LICENSE.txt
 */
package com.artipie.git;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link GitRequest}.
 *
 * @since 1.0
 */
class GitRequestTest {

    @Test
    void parseParts() {
        MatcherAssert.assertThat(
            GitRequest.parse("0012AABBCCDDEEFFGG0006BB0000").parts(),
            Matchers.contains("AABBCCDDEEFFGG", "BB")
        );
    }

    @Test
    void parseEmpty() {
        MatcherAssert.assertThat(
            GitRequest.parse("0000").parts(),
            Matchers.emptyIterable()
        );
    }

    @Test
    void skipSmallLengths() {
        MatcherAssert.assertThat(
            GitRequest.parse("0006AB00010006CD0000").parts(),
            Matchers.contains("AB", "CD")
        );
    }

    @Test
    void parseCommand() {
        MatcherAssert.assertThat(
            GitRequest.parse("0010command=foo\n0000").command().get(),
            Matchers.equalTo("foo")
        );
    }
}
