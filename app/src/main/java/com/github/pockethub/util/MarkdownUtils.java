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
package com.github.pockethub.util;

import android.text.TextUtils;

import static java.util.Locale.US;

/**
 * Utilities for dealing with Markdown files
 */
public class MarkdownUtils {

  private static final String[] MARKDOWN_EXTENSIONS = { ".md", ".mkdn",
          ".mdwn", ".mdown", ".markdown", ".mkd", ".mkdown", ".ron" };

  /**
   * Is the the given file name a Markdown file?
   *
   * @param name
   * @return true if the name has a markdown extension, false otherwise
   */
  public static boolean isMarkdown(String name) {
      if (TextUtils.isEmpty(name))
          return false;

      name = name.toLowerCase(US);
      for (String extension : MARKDOWN_EXTENSIONS)
          if (name.endsWith(extension))
              return true;

      return false;
  }
}
