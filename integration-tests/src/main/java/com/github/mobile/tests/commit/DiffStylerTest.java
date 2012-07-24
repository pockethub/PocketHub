/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.tests.commit;

import android.test.AndroidTestCase;

import com.github.mobile.ui.commit.DiffStyler;

import java.util.Collections;

import org.eclipse.egit.github.core.CommitFile;

/**
 * Tests of {@link DiffStyler}
 */
public class DiffStylerTest extends AndroidTestCase {

    private void compareStyled(String patch) {
        assertNotNull(patch);
        String fileName = "file.txt";
        DiffStyler styler = new DiffStyler(getContext().getResources());
        CommitFile file = new CommitFile();
        file.setFilename(fileName);
        file.setPatch(patch);
        styler.setFiles(Collections.singletonList(file));
        CharSequence styled = styler.get(fileName);
        assertNotNull(styled);
        assertEquals(patch, styled.toString());
    }

    /**
     * Test styler with empty files
     */
    public void testEmptyFiles() {
        DiffStyler styler = new DiffStyler(getContext().getResources());
        styler.setFiles(null);
        assertNull(styler.get("test"));
        styler.setFiles(Collections.<CommitFile> emptyList());
        assertNull(styler.get("test"));
    }

    /**
     * Test styler with empty patch
     */
    public void testEmptyPatch() {
        DiffStyler styler = new DiffStyler(getContext().getResources());
        CommitFile file = new CommitFile();
        file.setFilename("file.txt");
        styler.setFiles(Collections.singletonList(file));
        assertNull(styler.get("file.txt"));
        file.setPatch("");
        assertNull(styler.get("file.txt"));
    }

    /**
     * Test styler for file with only single newline
     */
    public void testOnlyNewline() {
        compareStyled("\n");
    }

    /**
     * Test styler for file with an empty patch line with other valid lines
     */
    public void testEmptyPatchLineWithOtherValidLines() {
        compareStyled("@@ 0,1 0,1 @@\n\n-test\n");
    }

    /**
     * Test styler for file with trailing empty line
     */
    public void testTrailingEmptyLine() {
        compareStyled("@@ 0,1 0,1 @@\n-test\n\n");
    }

    /**
     * Test styler for file with only newlines
     */
    public void testOnlyNewlines() {
        compareStyled("\n\n\n");
    }

    /**
     * Test styler for patch with no trailing newline after the second line
     */
    public void testNoTrailingNewlineAfterSecondLine() {
        compareStyled("@@ 1,2 1,2 @@\n+test");
    }

    /**
     * Test styler for patch with no trailing newline
     */
    public void testNoTrailingNewline() {
        compareStyled("@@ 1,2 1,2 @@");
    }

    /**
     * Test styler for file with valid patch
     */
    public void testFormattedPatch() {
        compareStyled("@@ 1,2 1,2 @@\n+test\n");
    }
}
