/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.tests.commit;

import android.test.AndroidTestCase;

import com.alorma.github.sdk.bean.dto.response.CommitFile;
import com.github.pockethub.ui.commit.DiffStyler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

/**
 * Tests of {@link DiffStyler}
 */
public class DiffStylerTest extends AndroidTestCase {

    private void compareStyled(String patch) throws IOException {
        assertNotNull(patch);
        String fileName = "file.txt";
        DiffStyler styler = new DiffStyler(getContext().getResources());
        CommitFile file = new CommitFile();
        file.filename = fileName;
        file.patch = patch;
        styler.setFiles(Collections.singletonList(file));
        List<CharSequence> styled = styler.get(fileName);
        assertNotNull(styled);
        BufferedReader reader = new BufferedReader(new StringReader(patch));
        String line = reader.readLine();
        int processed = 0;
        while (line != null) {
            assertEquals(line, styled.get(processed).toString());
            line = reader.readLine();
            processed++;
        }
        assertEquals(processed, styled.size());
    }

    /**
     * Test styler with empty files
     */
    public void testEmptyFiles() {
        DiffStyler styler = new DiffStyler(getContext().getResources());
        styler.setFiles(null);
        assertTrue(styler.get("navigation_drawer_header_background").isEmpty());
        styler.setFiles(Collections.<CommitFile>emptyList());
        assertTrue(styler.get("navigation_drawer_header_background").isEmpty());
    }

    /**
     * Test styler with empty patch
     */
    public void testEmptyPatch() {
        DiffStyler styler = new DiffStyler(getContext().getResources());
        CommitFile file = new CommitFile();
        file.filename = "file.txt";
        styler.setFiles(Collections.singletonList(file));
        assertTrue(styler.get("file.txt").isEmpty());
        file.patch = "";
        assertTrue(styler.get("file.txt").isEmpty());
    }

    /**
     * Test styler for file with only single newline
     *
     * @throws IOException
     */
    public void testOnlyNewline() throws IOException {
        compareStyled("\n");
    }

    /**
     * Test styler for file with an empty patch line with other valid lines
     *
     * @throws IOException
     */
    public void testEmptyPatchLineWithOtherValidLines() throws IOException {
        compareStyled("@@ 0,1 0,1 @@\n\n-navigation_drawer_header_background\n");
    }

    /**
     * Test styler for file with trailing empty line
     *
     * @throws IOException
     */
    public void testTrailingEmptyLine() throws IOException {
        compareStyled("@@ 0,1 0,1 @@\n-navigation_drawer_header_background\n\n");
    }

    /**
     * Test styler for file with only newlines
     *
     * @throws IOException
     */
    public void testOnlyNewlines() throws IOException {
        compareStyled("\n\n\n");
    }

    /**
     * Test styler for patch with no trailing newline after the second line
     *
     * @throws IOException
     */
    public void testNoTrailingNewlineAfterSecondLine() throws IOException {
        compareStyled("@@ 1,2 1,2 @@\n+navigation_drawer_header_background");
    }

    /**
     * Test styler for patch with no trailing newline
     *
     * @throws IOException
     */
    public void testNoTrailingNewline() throws IOException {
        compareStyled("@@ 1,2 1,2 @@");
    }

    /**
     * Test styler for file with valid patch
     *
     * @throws IOException
     */
    public void testFormattedPatch() throws IOException {
        compareStyled("@@ 1,2 1,2 @@\n+navigation_drawer_header_background\n");
    }
}
