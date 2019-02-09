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
package com.github.pockethub.android.tests.commit;

import androidx.test.filters.SmallTest;
import com.github.pockethub.android.ui.commit.DiffStyler;
import com.meisolsson.githubsdk.model.GitHubFile;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests of {@link DiffStyler}
 */
@SmallTest
public class DiffStylerTest {

    private void compareStyled(String patch) throws IOException {
        assertNotNull(patch);
        String fileName = "file.txt";
        DiffStyler styler = new DiffStyler(getTargetContext().getResources());
        GitHubFile file = GitHubFile.builder()
                .filename(fileName)
                .patch(patch)
                .build();

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
    @Test
    public void testEmptyFiles() {
        DiffStyler styler = new DiffStyler(getTargetContext().getResources());
        styler.setFiles(null);
        assertTrue(styler.get("navigation_drawer_header_background").isEmpty());
        styler.setFiles(Collections.emptyList());
        assertTrue(styler.get("navigation_drawer_header_background").isEmpty());
    }

    /**
     * Test styler with empty patch
     */
    @Test
    public void testEmptyPatch() {
        DiffStyler styler = new DiffStyler(getTargetContext().getResources());
        GitHubFile file = GitHubFile.builder()
                .filename("file.txt")
                .build();
        styler.setFiles(Collections.singletonList(file));
        assertTrue(styler.get("file.txt").isEmpty());

        file = file.toBuilder().filename("").build();
        styler.setFiles(Collections.singletonList(file));
        assertTrue(styler.get("file.txt").isEmpty());
    }

    /**
     * Test styler for file with only single newline
     *
     * @throws IOException
     */
    @Test
    public void testOnlyNewline() throws IOException {
        compareStyled("\n");
    }

    /**
     * Test styler for file with an empty patch line with other valid lines
     *
     * @throws IOException
     */
    @Test
    public void testEmptyPatchLineWithOtherValidLines() throws IOException {
        compareStyled("@@ 0,1 0,1 @@\n\n-navigation_drawer_header_background\n");
    }

    /**
     * Test styler for file with trailing empty line
     *
     * @throws IOException
     */
    @Test
    public void testTrailingEmptyLine() throws IOException {
        compareStyled("@@ 0,1 0,1 @@\n-navigation_drawer_header_background\n\n");
    }

    /**
     * Test styler for file with only newlines
     *
     * @throws IOException
     */
    @Test
    public void testOnlyNewlines() throws IOException {
        compareStyled("\n\n\n");
    }

    /**
     * Test styler for patch with no trailing newline after the second line
     *
     * @throws IOException
     */
    @Test
    public void testNoTrailingNewlineAfterSecondLine() throws IOException {
        compareStyled("@@ 1,2 1,2 @@\n+navigation_drawer_header_background");
    }

    /**
     * Test styler for patch with no trailing newline
     *
     * @throws IOException
     */
    @Test
    public void testNoTrailingNewline() throws IOException {
        compareStyled("@@ 1,2 1,2 @@");
    }

    /**
     * Test styler for file with valid patch
     *
     * @throws IOException
     */
    @Test
    public void testFormattedPatch() throws IOException {
        compareStyled("@@ 1,2 1,2 @@\n+navigation_drawer_header_background\n");
    }
}
