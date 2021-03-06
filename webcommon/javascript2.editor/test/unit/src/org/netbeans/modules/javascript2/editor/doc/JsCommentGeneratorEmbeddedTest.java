/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.indent.HtmlIndentTaskFactory;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.indent.api.support.AbstractIndenter;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsCommentGeneratorEmbeddedTest extends JsTestBase {

    public JsCommentGeneratorEmbeddedTest(String testName) {
        super(testName);
    }

    @Override
    protected Formatter getFormatter(IndentPrefs preferences) {
        return null;
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/html";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AbstractIndenter.inUnitTestRun = true;

        MockMimeLookup.setInstances(MimePath.parse("text/javascript"), JsTokenId.javascriptLanguage());
        HtmlIndentTaskFactory htmlReformatFactory = new HtmlIndentTaskFactory();
        MockMimeLookup.setInstances(MimePath.parse("text/html"), htmlReformatFactory, new HtmlKit("text/html"), HTMLTokenId.language());
    }

    public void testIssue222094_1() throws Exception {
        insertBreak(getOriginalContent("html"), getExpectedContent("html"));
    }

    public void testIssue222094_2() throws Exception {
        insertBreak(getOriginalContent("html"), getExpectedContent("html"));
    }

    public void testIssue222650_1() throws Exception {
        insertBreak(getOriginalContent("html"), getExpectedContent("html"));
    }

    public void testIssue222650_2() throws Exception {
        insertBreak(getOriginalContent("html"), getExpectedContent("html"));
    }

    @Override
    public void insertNewline(String source, String reformatted, IndentPrefs preferences) throws Exception {
        int sourcePos = source.indexOf('^');
        assertNotNull(sourcePos);
        source = source.substring(0, sourcePos) + source.substring(sourcePos + 1);
        Formatter formatter = getFormatter(null);

        int reformattedPos = reformatted.indexOf('^');
        assertNotNull(reformattedPos);
        reformatted = reformatted.substring(0, reformattedPos) + reformatted.substring(reformattedPos + 1);

        JEditorPane ta = getPane(source);
        Caret caret = ta.getCaret();
        caret.setDot(sourcePos);
        BaseDocument doc = (BaseDocument) ta.getDocument();
        if (formatter != null) {
            configureIndenters(doc, formatter, true);
        }

        setupDocumentIndentation(doc, preferences);

        runKitAction(ta, DefaultEditorKit.insertBreakAction, "\n");

        // wait for generating comment
        Future<?> future = JsDocumentationCompleter.RP.submit(new Runnable() {
            @Override
            public void run() {
            }
        });
        future.get();

        String formatted = doc.getText(0, doc.getLength());
        assertEquals(reformatted, formatted);

        if (reformattedPos != -1) {
            assertEquals(reformattedPos, caret.getDot());
        }
    }

    private String getTestFolderPath() {
        return "testfiles/doc/commentGenerator/"; //NOI18N
    }

    private String getOriginalContent(String ext) throws IOException {
        File f = new File(getDataDir(), getTestPath(false, ext));
        return readFileAsString(f);
    }

    private String getExpectedContent(String ext) throws IOException {
        File f = new File(getDataDir(), getTestPath(true, ext));
        return readFileAsString(f);
    }

    private static String readFileAsString(File file) throws java.io.IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    private String getTestPath(boolean expected, String ext) {
        return getTestFolderPath() + getTestName(expected) + "." + ext;//NOI18N
    }

    private String getTestName(boolean expected) {
        String name = getName();
        if (expected) {
            return name + "-expected";
        } else {
            return name;
        }
    }
}
