/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
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
package io.gravitee.rest.api.service.sanitizer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.assertj.core.api.BDDSoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.rules.ErrorCollector;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(SoftAssertionsExtension.class)
public class HtmlSanitizerTest {

    @InjectSoftAssertions
    public BDDSoftAssertions softly;

    private static String ONLY_OPENED_IMG_TAG = "<img src=\"myPic.png\">";
    private static String SELF_CLOSING_IMG_TAG = "<img src=\"myPic.png\"/>";
    private static String CLOSED_IMG_TAG = "<img src=\"myPic.png\"></img>";
    private static String BASE64_IMG_TAG =
        "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==\"/>";

    private static String DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD = "<div style=\"margin:auto\"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES = "<div style=\" margin : auto \"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SEMICOLON = "<div style=\"margin:auto;\"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES_AND_SEMICOLON = "<div style=\" margin : auto ; \"></div>";

    private static String DIV_TAG_WITH_STYLE_ATT_WITH_FLOAT_FIELD = "<div style=\"float:left\"></div>";

    private static String DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD = "<div style=\"margin:auto;width:100px\"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES = "<div style=\" margin : auto ; width :   100px    \"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SEMICOLON = "<div style=\"margin:auto;width:100px;\"></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES_AND_SEMICOLON =
        "<div style=\" margin : auto ; width :   100px   ;  \"></div>";

    private static String DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_QUOTE = "<div style='margin:auto'></div>";
    private static String DIV_TAG_WITH_STYLE_ATT_WITH_TWO_SEMICOLON = "<div style=\"margin:auto;;\"></div>";

    private static String SUMMARY_DETAILS =
        "<details>\n" + "    <summary>Details</summary>\n" + "    Something small enough to escape casual notice.\n" + "</details>\n";

    @Test
    public void sanitize() {
        String html = getSafe();
        assertEquals(html, HtmlSanitizer.sanitize(html));
    }

    @Test
    public void sanitizeOlStart() {
        var ol = "<ol start=\"48\"><li>First</li><li>Second</li></ol>";
        assertEquals(ol, HtmlSanitizer.sanitize(ol));
    }

    @Test
    public void sanitizeExcludeSensitive() {
        String html = getNotSafe();
        assertEquals("", HtmlSanitizer.sanitize(html));
    }

    @Test
    public void isSafe() {
        HtmlSanitizer.SanitizeInfos sanitizeInfos = HtmlSanitizer.isSafe(getSafe());

        assertTrue(sanitizeInfos.isSafe());
        assertEquals("[]", sanitizeInfos.getRejectedMessage());
    }

    @Test
    public void isNotSafe() {
        HtmlSanitizer.SanitizeInfos sanitizeInfos = HtmlSanitizer.isSafe(getNotSafe());

        assertFalse(sanitizeInfos.isSafe());
        assertEquals("[Tag not allowed: script]", sanitizeInfos.getRejectedMessage());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "<!-><img src=x onerror=alert(1) />",
            "<!--><img src=x onerror=alert(1) />",
            "<!---><img src=x onerror=alert(1) />",
            "<!--->\\n<img src=x onerror=alert(1) />",
            "<!--->\\n\\n\\n\\n<img src=x onerror=alert(1) />",
            "<!------->\\n<img src=x onerror=alert(1) />",
        }
    )
    public void isXssNotSafe(String content) {
        HtmlSanitizer.SanitizeInfos sanitizeInfos = HtmlSanitizer.isSafe(content);

        assertFalse(sanitizeInfos.isSafe());
        assertEquals("[Attribute not allowed: [img][onerror]]", sanitizeInfos.getRejectedMessage());
    }

    private String getSafe() {
        String html = "";
        html += "<div>Test div</div>";
        html += "<a href=\"/internal\">Test internal link</a>";
        html += "<a href=\"https://external.com\">Test external link</a>";
        html +=
            "<table><tbody><tr><th>Data1</th><th>Data2</th></tr><tr><td>Calcutta</td><td>Orange</td></tr><tr><td>Robots</td><td>Jazz</td></tr></tbody></table>";
        html += "<ul><li>Test ul/li</li></ul>";
        html += "<img src=\"/internal.jpg\" title=\"test\" />";
        html += "<img src=\"/external.jpg\" title=\"test\" />";

        return html;
    }

    @Test
    public void isNotSafe_markdownLink() {
        final String content = "[my_link](javascript:alert('xss'))";
        HtmlSanitizer.SanitizeInfos sanitizeInfos = HtmlSanitizer.isSafe(content);

        assertFalse(sanitizeInfos.isSafe());
        assertEquals("[Tag not allowed: a]", sanitizeInfos.getRejectedMessage());
    }

    private String getNotSafe() {
        String html = "";
        html += "<script src=\"/external.jpg\" />";
        html += "<div onClick=\"alert('test');\" style=\"margin: auto\">onclick alert<div>";

        return html;
    }

    @Test
    public void shouldBeSafe() {
        softly.then(HtmlSanitizer.isSafe(ONLY_OPENED_IMG_TAG).isSafe()).as("ONLY_OPENED_IMG_TAG").isTrue();
        softly.then(HtmlSanitizer.isSafe(SELF_CLOSING_IMG_TAG).isSafe()).as("SELF_CLOSING_IMG_TAG").isTrue();
        softly.then(HtmlSanitizer.isSafe(CLOSED_IMG_TAG).isSafe()).as("CLOSED_IMG_TAG").isTrue();
        softly.then(HtmlSanitizer.isSafe(BASE64_IMG_TAG).isSafe()).as("BASE64_IMG_TAG").isTrue();
        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD")
            .isTrue();
        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SEMICOLON).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SEMICOLON")
            .isTrue();
        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES_AND_SEMICOLON).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_FIELD_WITH_SPACES_AND_SEMICOLON")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_FLOAT_FIELD).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_FLOAT_FIELD")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SEMICOLON).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SEMICOLON")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES_AND_SEMICOLON).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_TWO_FIELD_WITH_SPACES_AND_SEMICOLON")
            .isTrue();

        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_QUOTE).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_SINGLE_QUOTE")
            .isTrue();
        softly
            .then(HtmlSanitizer.isSafe(DIV_TAG_WITH_STYLE_ATT_WITH_TWO_SEMICOLON).isSafe())
            .as("DIV_TAG_WITH_STYLE_ATT_WITH_TWO_SEMICOLON")
            .isTrue();

        softly.then(HtmlSanitizer.isSafe(SUMMARY_DETAILS).isSafe()).as("SUMMARY_DETAILS").isTrue();
    }
}
