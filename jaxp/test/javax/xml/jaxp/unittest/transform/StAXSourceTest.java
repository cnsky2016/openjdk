/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package transform;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * @summary Test parsing from StAXSource.
 */
public class StAXSourceTest {

    @Test
    public final void testStAXSource() throws XMLStreamException {
        XMLInputFactory ifactory = XMLInputFactory.newInstance();
        XMLOutputFactory ofactory = XMLOutputFactory.newInstance();

        String xslStylesheet = "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>"
                + "  <xsl:output method='xml' encoding='utf-8' indent='no'/>" + "  <xsl:preserve-space elements='*'/>" + "  <xsl:template match='*'>"
                + "    <xsl:copy><xsl:copy-of select='@*'/><xsl:apply-templates/></xsl:copy>" + "  </xsl:template>"
                + "  <xsl:template match='comment()|processing-instruction()|text()'>" + "    <xsl:copy/>" + "  </xsl:template>" + "</xsl:stylesheet>";
        StringReader xslStringReader = new StringReader(xslStylesheet);
        StringReader xmlStringReader = new StringReader(xslStylesheet); // identity
                                                                        // on
                                                                        // itself,
        StringWriter xmlStringWriter = new StringWriter();

        XMLEventReader styleReader = ifactory.createXMLEventReader(xslStringReader);
        XMLEventReader docReader = ifactory.createXMLEventReader(xmlStringReader);
        XMLEventWriter writer = ofactory.createXMLEventWriter(xmlStringWriter);

        StAXSource stylesheet = new StAXSource(styleReader);
        StAXSource document = new StAXSource(docReader);
        StAXResult result = new StAXResult(writer);

        try {
            document.setSystemId("sourceSystemId");
        } catch (UnsupportedOperationException e) {
            System.out.println("Expected UnsupportedOperationException in StAXSource.setSystemId()");
        } catch (Exception e) {
            Assert.fail("StAXSource.setSystemId() does not throw java.lang.UnsupportedOperationException");
        }

        TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = tfactory.newTransformer(stylesheet);
            transformer.transform(document, result);
        } catch (TransformerConfigurationException tce) {
            throw new XMLStreamException(tce);
        } catch (TransformerException te) {
            throw new XMLStreamException(te);
        } finally {
            styleReader.close();
            docReader.close();
            writer.close();
        }

        try {
            result.setSystemId("systemId");
        } catch (UnsupportedOperationException e) {
            System.out.println("Expected UnsupportedOperationException in StAXResult.setSystemId()");
        } catch (Exception e) {
            Assert.fail("StAXResult.setSystemId() does not throw java.lang.UnsupportedOperationException");
        }

        if (result.getSystemId() != null) {
            Assert.fail("StAXResult.getSystemId() does not return null");
        }
    }

    @Test
    public final void testStAXSource2() throws XMLStreamException {
        XMLInputFactory ifactory = XMLInputFactory.newInstance();
        ifactory.setProperty("javax.xml.stream.supportDTD", Boolean.TRUE);

        StAXSource ss = new StAXSource(ifactory.createXMLStreamReader(getClass().getResource("5368141.xml").toString(),
                getClass().getResourceAsStream("5368141.xml")));
        DOMResult dr = new DOMResult();

        TransformerFactory tfactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = tfactory.newTransformer();
            transformer.transform(ss, dr);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
