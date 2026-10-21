package com.app.data.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import junit.framework.TestCase;

public class TaggedTest extends TestCase {

    Tagged tagged;

    public void setUp() throws Exception {
        super.setUp();

        tagged = new Tagged();
        tagged.addTaggedEntry("1", "haris");
        tagged.addTaggedEntry("2", "harris_jeyaraj");
        tagged.addTaggedEntry("3", "ghost");
        tagged.addTaggedEntry("4", "groot");
    }

    public void tearDown() throws Exception {
        tagged = null;
    }

    public void testGetCommaSeparatedTaggedNameList() {
        assertThat("Comma separated names", tagged.getCommaSeparatedTaggedNameList(), is(equalTo("haris,harris_jeyaraj,ghost,groot")));
    }

    public void testGetCommaSeparatedTaggedIdList() {
        assertThat("Comma separated ids", tagged.getCommaSeparatedTaggedIdList(), is(equalTo("1,2,3,4")));
    }
}