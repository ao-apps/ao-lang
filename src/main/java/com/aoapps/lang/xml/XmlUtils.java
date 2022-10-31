/*
 * ao-lang - Minimal Java library with no external dependencies shared by many other projects.
 * Copyright (C) 2014, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-lang.
 *
 * ao-lang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-lang is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-lang.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.lang.xml;

import com.aoapps.lang.NullArgumentException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utilities that help when working with XML.
 *
 * @author  AO Industries, Inc.
 */
public final class XmlUtils {

  /** Make no instances. */
  private XmlUtils() {
    throw new AssertionError();
  }

  /**
   * The number of space characters per indentation level.
   */
  // 1) Matches indentation of ao-checkstyle-config: https://oss.aoapps.com/checkstyle-config/
  //    Which, in-turn, is derived from Google Java Style Guide: https://google.github.io/styleguide/javaguide.html
  //
  // 2) Matches ao-encoding:Whitespace identation
  public static final int INDENT_SPACES = 2;

  /**
   * Fetches and parses an XML DOM from a URL.
   * Uses the following XML security features:
   * <ol>
   * <li>{@link XMLConstants#FEATURE_SECURE_PROCESSING} = {@code true}</li>
   * <li>{@link XMLConstants#ACCESS_EXTERNAL_DTD} = {@code ""}</li>
   * <li>{@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} = {@code ""}</li>
   * </ol>
   * <ul>
   * <li>See <a href="https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java">OWASP Cheat Sheet</a></li>
   * <li>See <a href="https://rules.sonarsource.com/java/RSPEC-2755">Java: XML parsers should not be vulnerable to XXE attacks</a></li>
   * </ul>
   */
  public static Document parseXml(URL url) throws IOException, ParserConfigurationException, SAXException {
    URLConnection conn = url.openConnection();
    try (InputStream in = conn.getInputStream()) {
      return parseXml(in);
    }
  }

  /**
   * Parses an XML DOM from an input stream.
   * Uses the following XML security features:
   * <ol>
   * <li>{@link XMLConstants#FEATURE_SECURE_PROCESSING} = {@code true}</li>
   * <li>{@link XMLConstants#ACCESS_EXTERNAL_DTD} = {@code ""}</li>
   * <li>{@link XMLConstants#ACCESS_EXTERNAL_SCHEMA} = {@code ""}</li>
   * </ol>
   * <ul>
   * <li>See <a href="https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java">OWASP Cheat Sheet</a></li>
   * <li>See <a href="https://rules.sonarsource.com/java/RSPEC-2755">Java: XML parsers should not be vulnerable to XXE attacks</a></li>
   * </ul>
   */
  public static Document parseXml(InputStream in) throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (ParserConfigurationException e) {
      throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
    }
    // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
    // See https://rules.sonarsource.com/java/RSPEC-2755
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    return dbf.newDocumentBuilder().parse(in);
  }

  /**
   * Iterates over a NodeList.
   */
  public static Iterable<Node> iterableNodes(final NodeList nodeList) {
    // Java 9: new Iterator<>
    return () -> new Iterator<Node>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < nodeList.getLength();
      }

      @Override
      public Node next() throws NoSuchElementException {
        if (hasNext()) {
          return nodeList.item(index++);
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  /**
   * Iterates over a NodeList, only returning Elements.
   */
  public static Iterable<Element> iterableElements(final NodeList nodeList) {
    // Java 9: new Iterator<>
    return () -> new Iterator<Element>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        // Skip past any non-elements
        while (
            index < nodeList.getLength()
                && !(nodeList.item(index) instanceof Element)
        ) {
          index++;
        }
        return index < nodeList.getLength();
      }

      @Override
      public Element next() throws NoSuchElementException {
        if (hasNext()) {
          return (Element) nodeList.item(index++);
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  /**
   * Iterates the children of the given element,
   * returning only returning child elements of the given name.
   */
  public static Iterable<Element> iterableChildElementsByTagName(final Element element, final String childTagName) {
    // Java 9: new Iterator<>
    return () -> new Iterator<Element>() {
      private final NodeList children = element.getChildNodes();
      private int index = 0;

      @Override
      public boolean hasNext() {
        // Skip past any non-elements
        while (index < children.getLength()) {
          Node child = children.item(index);
          if (child instanceof Element) {
            Element childElem = (Element) child;
            if (childTagName.equals(childElem.getTagName())) {
              break;
            }
          }
          index++;
        }
        return index < children.getLength();
      }

      @Override
      public Element next() throws NoSuchElementException {
        if (hasNext()) {
          return (Element) children.item(index++);
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  /**
   * Gets the child element for a tag name or {@code null} if not found.
   * Is an error if more than once child is found for the given name.
   *
   * @throws  IllegalStateException  if there is more than one child element with the given name
   */
  public static Element getChildElementByTagName(Element element, String childTagName) {
    Element matched = null;
    NodeList children = element.getChildNodes();
    for (int index = 0, len = children.getLength(); index < len; index++) {
      Node child = children.item(index);
      if (child instanceof Element) {
        Element childElem = (Element) child;
        if (childTagName.equals(childElem.getTagName())) {
          if (matched != null) {
            throw new IllegalStateException("More than one child found: " + childTagName);
          }
          matched = childElem;
        }
      }
    }
    return matched;
  }

  /**
   * Gets the text content of a child element for a tag name or {@code null} if not found.
   * Is an error if more than once child is found for the given name.
   *
   * @see #getChildElementByTagName(org.w3c.dom.Element, java.lang.String)
   *
   * @throws  IllegalStateException  if there is more than one child element with the given name
   */
  public static String getChildTextContent(Element element, String childTagName) {
    Element childElem = getChildElementByTagName(element, childTagName);
    return childElem == null ? null : childElem.getTextContent();
  }

  /**
   * @deprecated  Use {@link #toString(org.w3c.dom.Node)} instead.
   */
  @Deprecated
  public static String toString(Document document) throws TransformerException {
    return toString((Node) document);
  }

  public static String toString(Node node) throws TransformerException {
    // TODO: New XML Processing Limits (JDK-8270504 (not public)), see https://www.oracle.com/java/technologies/javase/8all-relnotes.html
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (TransformerConfigurationException e) {
      throw new AssertionError("All implementations are required to support the javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING feature.", e);
    }
    // See https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#java
    // See https://rules.sonarsource.com/java/RSPEC-2755
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    transformerFactory.setAttribute("indent-number", INDENT_SPACES);
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(INDENT_SPACES));
    StringWriter writer = new StringWriter();
    try {
      transformer.transform(
          new DOMSource(node),
          new StreamResult(writer)
      );
    } finally {
      try {
        writer.close();
      } catch (IOException e) {
        throw new AssertionError("IOException should never be thrown from StringWriter", e);
      }
    }
    return writer.toString();
  }

  /**
   * See <a href="https://www.w3.org/TR/REC-xml/#NT-NameStartChar">Names and Tokens: NameStartChar</a>.
   */
  public static boolean isNameStartChar(int codePoint) {
    return
        codePoint == ':'
            || (codePoint >= 'A' && codePoint <= 'Z')
            || codePoint == '_'
            || (codePoint >= 'a' && codePoint <= 'z')
            || (codePoint >= 0xC0 && codePoint <= 0xD6)
            || (codePoint >= 0xD8 && codePoint <= 0xF6)
            || (codePoint >= 0xF8 && codePoint <= 0x2FF)
            || (codePoint >= 0x370 && codePoint <= 0x37D)
            || (codePoint >= 0x37F && codePoint <= 0x1FFF)
            || (codePoint >= 0x200C && codePoint <= 0x200D)
            || (codePoint >= 0x2070 && codePoint <= 0x218F)
            || (codePoint >= 0x2C00 && codePoint <= 0x2FEF)
            || (codePoint >= 0x3001 && codePoint <= 0xD7FF)
            || (codePoint >= 0xF900 && codePoint <= 0xFDCF)
            || (codePoint >= 0xFDF0 && codePoint <= 0xFFFD)
            || (codePoint >= 0x10000 && codePoint <= 0xEFFFF);
  }

  /**
   * See <a href="https://www.w3.org/TR/REC-xml/#NT-NameChar">Names and Tokens: NameChar</a>.
   */
  public static boolean isNameChar(int codePoint) {
    return
        isNameStartChar(codePoint)
            || codePoint == '-'
            || codePoint == '.'
            || (codePoint >= '0' && codePoint <= '9')
            || codePoint == 0xB7
            || (codePoint >= 0x0300 && codePoint <= 0x036F)
            || (codePoint >= 0x203F && codePoint <= 0x2040);
  }

  /**
   * Makes sure a valid Name.
   *
   * <ol>
   * <li>See <a href="https://www.w3.org/TR/REC-xml/#NT-Name">Names and Tokens: Name</a></li>
   * </ol>
   */
  public static boolean isValidName(String name, int begin, int end) {
    if (name == null) {
      return false;
    }
    if (begin >= end) {
      return false;
    }
    int pos = begin;
    int codePoint = name.codePointAt(pos);
    pos += Character.charCount(codePoint);
    if (!isNameStartChar(codePoint)) {
      return false;
    }
    while (pos < end) {
      codePoint = name.codePointAt(pos);
      pos += Character.charCount(codePoint);
      if (!isNameChar(codePoint)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Makes sure a valid Name.
   *
   * <ol>
   * <li>See <a href="https://www.w3.org/TR/REC-xml/#NT-Name">Names and Tokens: Name</a></li>
   * </ol>
   */
  public static boolean isValidName(String name) {
    return name != null && isValidName(name, 0, name.length());
  }

  /**
   * Makes sure an ID is valid.
   *
   * <ol>
   * <li>See <a href="https://www.w3.org/TR/xhtml1/#C_8">C.8. Fragment Identifiers</a></li>
   * <li>See <a href="https://www.w3.org/TR/REC-xml/#id">Validity constraint: ID</a></li>
   * </ol>
   *
   * @deprecated  Please use {@link #isValidName(java.lang.String)}, since
   *              "Values of type ID MUST match the Name production."
   */
  @Deprecated
  public static boolean isValidId(String id) {
    return isValidName(id);
  }

  /**
   * Convert a couple other types of hyphens.
   * <a href="http://jkorpela.fi/dashes.html">http://jkorpela.fi/dashes.html</a>,
   * <a href="http://www.unicode.org/versions/Unicode12.0.0/ch06.pdf">Table 6-3</a>.
   */
  public static boolean isHyphen(int codePoint) {
    return
        codePoint == 0x002D
            || codePoint == 0x007E
            // Not in PDF: || codePoint == 0x00AD
            || codePoint == 0x058A
            || codePoint == 0x05BE
            || codePoint == 0x1400
            || codePoint == 0x1806
            || (codePoint >= 0x2010 && codePoint <= 0x2015)
            || codePoint == 0x2053
            || codePoint == 0x207B
            || codePoint == 0x208B
            || codePoint == 0x2212
            || codePoint == 0x2E17
            // Not in PDF: || codePoint == 0x2E3A
            // Not in PDF: || codePoint == 0x2E3B
            || codePoint == 0x301C
            || codePoint == 0x3030
            || codePoint == 0x30A0
            || codePoint == 0xFE31
            || codePoint == 0xFE32
            || codePoint == 0xFE58
            || codePoint == 0xFE63
            || codePoint == 0xFF0D;
  }

  /**
   * Generates a valid ID from an arbitrary string.
   * <p>
   * Strips all character not matching <a href="https://www.w3.org/TR/REC-xml/#NT-Name">Names and Tokens: Name</a>:
   * </p>
   *
   * <p>
   * See <a href="https://www.w3.org/TR/REC-xml/#NT-Name">Names and Tokens: Name</a>
   * </p>
   *
   * @param  template   The preferred text to base the id on
   * @param  defaultId  The base used when template is unusable (must be a valid id or invalid ID's may be generated)
   */
  public static StringBuilder generateId(String template, String defaultId) {
    NullArgumentException.checkNotNull(template, "template");
    NullArgumentException.checkNotNull(defaultId, "defaultId");
    assert isValidName(defaultId);
    final int len = template.length();
    // First character must be [A-Za-z]
    int pos = 0;
    while (pos < len) {
      int codePoint = template.codePointAt(pos);
      if (isNameStartChar(codePoint)) {
        break;
      }
      pos += Character.charCount(codePoint);
    }
    StringBuilder id;
    if (pos == len) {
      // No usable characters from label
      id = new StringBuilder(defaultId);
    } else {
      // Get remaining usable characters from label
      id = new StringBuilder(len - pos);
      int lastCodePoint = Integer.MIN_VALUE;
      while (pos < len) {
        int codePoint = template.codePointAt(pos);
        pos += Character.charCount(codePoint);
        if (
            isNameChar(codePoint)
                // fall-through to hyphen coalesce
                && codePoint != '-'
        ) {
          id.appendCodePoint(codePoint);
          lastCodePoint = codePoint;
        } else if (
            // Convert space to '-'
            Character.isWhitespace(codePoint)
                // Convert other types of hyphens
                || isHyphen(codePoint)
        ) {
          if (!isHyphen(lastCodePoint)) {
            id.append('-');
            lastCodePoint = '-';
          }
        }
      }
      // Trim any trailing hyphens (note: there are no surrage hyphens, so this is OK without code points)
      int trimLen = id.length();
      while (trimLen > 1 && isHyphen(id.charAt(trimLen - 1))) {
        id.setLength(--trimLen);
      }
    }
    assert isValidName(id.toString());
    return id;
  }
}
