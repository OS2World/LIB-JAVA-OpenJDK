/* ClipboardContentSignedTests.java
 Copyright (C) 2012 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jnlp.ContentReaderListener;
import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.ServerAccess;
import net.sourceforge.jnlp.annotations.Bug;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import net.sourceforge.jnlp.tools.AsyncJavaws;
import static net.sourceforge.jnlp.tools.ClipboardHelpers.pasteFromClipboard;
import static net.sourceforge.jnlp.tools.ClipboardHelpers.putToClipboard;
import net.sourceforge.jnlp.tools.WaitingForStringProcess;
import org.junit.Assert;
import org.junit.Test;

@Bug(id = "PR708")
public class ClipboardContentSignedTests {

    private static final String contentC = "COPY#$REPRODUCER";
    private static final String contentP = "PASTE#$REPRODUCER";
    private static final String emptyContent = "empty content";
    private static ServerAccess server = new ServerAccess();
    private static final List<String> javawsTrustArg = Collections.unmodifiableList(Arrays.asList(new String[]{"-Xtrustall"}));

    @Test
    public void assertClipboardIsWorking() throws Exception {
        putToClipboard(emptyContent);
        Assert.assertEquals("Clipboard must contain new value, did not", emptyContent, pasteFromClipboard());
        putToClipboard(contentC);
        Assert.assertEquals("Clipboard must contain new value, did not", contentC, pasteFromClipboard());
    }

    @Test
    @Bug(id = "PR708")
    public void ClipboardContentSignedTestCopy1() throws Exception {
        putToClipboard(emptyContent);
        Assert.assertEquals("Clipboard must contain new value, did not", emptyContent, pasteFromClipboard());
        WaitingForStringProcess wfsp = new WaitingForStringProcess(server, "/ClipboardContentSignedCopy1.jnlp", javawsTrustArg, true, "copied");
        wfsp.run();
        String ss = pasteFromClipboard();
        Assert.assertEquals("Clipboard must contain new value, did not", contentC, ss);
    }

    @Test
    @Bug(id = "PR708")
    @NeedsDisplay
    public void ClipboardContentSignedTestCopy2() throws Exception {
        putToClipboard(emptyContent);
        Assert.assertEquals("Clipboard must contain new value, did not", emptyContent, pasteFromClipboard());
        WaitingForStringProcess wfsp = new WaitingForStringProcess(server, "/ClipboardContentSignedCopy2.jnlp", javawsTrustArg, false, "copied");
        wfsp.run();
        String ss = pasteFromClipboard();
        Assert.assertEquals("Clipboard must contain new value, did not", contentC, ss);

    }

    @Test
    @Bug(id = "PR708")
    public void ClipboardContentSignedTestPaste1() throws Exception {
        //necessery errasing
        putToClipboard(emptyContent);
        Assert.assertEquals("Clipboard must contain new value, did not", emptyContent, pasteFromClipboard());
        //now put the tested data
        putToClipboard(contentP);
        Assert.assertEquals("Clipboard must contain new value, did not", contentP, pasteFromClipboard());
        ProcessResult pr = server.executeJavawsHeadless(javawsTrustArg, "/ClipboardContentSignedPaste1.jnlp");
        Assert.assertTrue("ClipboardContentSignedTestPaste stdout should contain " + contentP + " but didn't", pr.stdout.contains(contentP));
    }

    @Test
    @Bug(id = "PR708")
    @NeedsDisplay
    public void ClipboardContentSignedTestPaste2() throws Exception {
        //necessery errasing
        putToClipboard(emptyContent);
        Assert.assertEquals("Clipboard must contain new value, did not", emptyContent, pasteFromClipboard());
        //now put the tested data
        putToClipboard(contentP);
        Assert.assertEquals("Clipboard must contain new value, did not", contentP, pasteFromClipboard());
        Assert.assertEquals("Clipboard must contain new value, did not", contentP, pasteFromClipboard());
        ProcessResult pr = server.executeJavaws(javawsTrustArg, "/ClipboardContentSignedPaste2.jnlp");
        Assert.assertTrue("ClipboardContentSignedTestPaste stdout should contain " + contentP + " but didn't", pr.stdout.contains(contentP));
    }
}
