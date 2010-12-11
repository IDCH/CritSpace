/* Created on       Dec 10, 2010
 * Last Modified on $Date: $
 * $Revision: $
 *
 * Copyright (c) 2010 Institute for Digital Christian Heritage (IDCH)
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software. Except as contained in 
 * this notice, the name(s) of the above copyright holders shall not be used 
 * in advertising or otherwise to promote the sale, use or other dealings in
 * this Software without prior written authorization.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.idch.nt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.idch.persist.BadDataException;

public class ScriptureReference implements Comparable<ScriptureReference> {
    
    public enum Book { 
        // TODO add OT books
        MATTHEW         ("Matthew",         "Matt"),
        MARK            ("Mark",            "Mark"),
        LUKE            ("Luke",            "Luke"),
        JOHN            ("John",            "John"),
        ACTS            ("Acts",            "Acts"),

        ROMANS          ("Romans",          "Rom"),
        CORINTHIANS_1   ("1 Corinthians",   "1Cor"),
        CORINTHIANS_2   ("2 Corinthians",   "2Cor"),
        GALATIANS       ("Galatians",       "Gal"),
        EPHESIANS       ("Ephesians",       "Eph"),
        PHILIPPIANS     ("Philippians",     "Phil"),
        COLOSSIANS      ("Colossians",      "Col"),
        THESSALONIANS_1 ("1 Thessalonians", "1Thess"),
        THESSALONIANS_2 ("2 Thessalonians", "2Thess"),
        TIMOTHY_1       ("1 Timothy",       "1Tim"),
        TIMOTHY_2       ("2 Timothy",       "2Tim"),
        TITUS           ("Titus",           "Titus"),
        PHILEMON        ("Philemon",        "Phlm"),
        
        HEBREWS         ("Hebrews",         "Heb"),
        JAMES           ("James",           "Jas"),
        PETER_1         ("1Peter",          "1Pet"),
        PETER_2         ("2 Peter",         "2Pet"),
        JOHN_1          ("1 John",          "1John"),
        JOHN_2          ("2 John",          "2John"),
        JOHN_3          ("3 John",          "3John"),
        JUDE            ("Jude",            "Jude"),
        REVELATION      ("Revelation",      "Rev");
        
        public final String name;
        public final String abbv;
        
        Book(String name, String abbv) {
            this.name = name;
            this.abbv = abbv;
        }
    }
    
    //========================================================================
    // SYMBOLIC CONSTANTS
    //========================================================================
    
    /**
     * Property key to be used in <code>Map</code> structures (e.g. JSON data)
     * to indicate the book value of this reference. This supplies the the 
     * abbreviated form of the book name (e.g. '1Tim'). 
     */
    public static final String PK_BOOK    = "book";
    
    /**
     * Property key to be used in <code>Map</code> structures (e.g. JSON data)
     * to indicate the book value of this reference. This supplies the the 
     * fully spelled out English name of the book (e.g. '1 Timothy'). 
     */
    public static final String PK_NAME = "name";
    
    /**
     * Property key to be used in <code>Map</code> structures (e.g. JSON data)
     * to indicate the chapter number of this reference. 
     */
    public static final String PK_CHAPTER = "chpt";
    
    /**
     * Property key to be used in <code>Map</code> structures (e.g. JSON data)
     * to indicate the verse number of this reference.  
     */
    public static final String PK_VERSE   = "vs";
    
    //========================================================================
    // STATIC METHODS
    //========================================================================
    
    /**
     * Returns the book based on the supplied name or abbreviation. 
     * 
     * TODO this should be extended to look up other common abbreviations.
     * 
     * @param query
     * @return
     */
    public static Book lookup(String query) {
        query = StringUtils.trimToEmpty(query).toLowerCase();
        if (query.length() == 0)
            return null;
        
        for (Book b : Book.values()) {
            if (query.equals(b.name.toLowerCase()) ||
                query.equals(b.abbv.toLowerCase())) 
                
                return b;
        }
        
        return null;
    }
    
    /**
     * 
     * @param book
     * @param chapter
     * @param verse
     * @return
     * @throws BadDataException
     */
    public static Map<String, Object> getMemento(
            String book, int chapter, int verse) 
    throws BadDataException {
        Book bk = lookup(book);
        if (bk == null)
            throw new BadDataException("Could not find the specified " +
            		"book: " + book);
        
        return getMemento(bk, chapter, verse);
    }
    
    /**
     * 
     * @param book
     * @param chapter
     * @param verse
     * @return
     * @throws BadDataException
     */
    public static Map<String, Object> getMemento(
            Book book, int chapter, int verse) {
        assert book != null : "No book supplied.";
        
        Map<String, Object> data = new HashMap<String, Object>(4);
        
        data.put(PK_BOOK, book.abbv);
        data.put(PK_NAME, book.name);
        
        if (chapter > 0) {
            data.put(PK_CHAPTER, chapter);
        } else {  
            data.put(PK_CHAPTER, null);
        }
        
        if (verse > 0) {
            data.put(PK_VERSE, verse);
        } else {  
            data.put(PK_VERSE, null);
        }
        
        return data;
    }
    
    private static int parseNumber(Object obj) {
        int result = 0;
        
        if (obj == null) {
            result = 0;
        } else if (obj instanceof String) {
            String chapter = StringUtils.trimToEmpty((String)obj);
            if (StringUtils.isNumeric(chapter)) {
                result = Integer.parseInt(chapter);
            }
        } else if (obj instanceof Number) {
            result = ((Number)obj).intValue();
        }
        
        return result;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    
    private Book m_book;
    private int  m_chapter = 0;
    private int  m_verse = 0;
    
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    
    /**
     * Creates a new scripture reference based on an OSIS scripture ID. This 
     * is in the general format of Matt.1.1.
     * 
     * @param osisID
     */
    public ScriptureReference(String osisID) {
        String[] parts = osisID.split("\\.");
        
        Book bk = lookup(parts[0]);
        if (bk == null) {
            // TODO throw something.
        }
        
        if ((parts.length > 1) && StringUtils.isNumericSpace(parts[1])) {
            m_chapter = Integer.parseInt(parts[1]);
            
            if ((parts.length > 1) && StringUtils.isNumericSpace(parts[2])) 
                m_verse = Integer.parseInt(parts[2]);
        }
    }
    
    public ScriptureReference(Book bk, int chapter, int verse) {
        m_book    = bk;
        m_chapter = chapter;
        m_verse   = verse;
    }
    
    public ScriptureReference(Map<String, Object> data) 
        throws BadDataException {
        
        try {
            m_book = lookup((String)data.get(PK_BOOK));
            
            m_chapter = parseNumber(data.get(PK_CHAPTER));
            m_verse   = parseNumber(data.get(PK_VERSE));
        } catch (Exception ex) {
            // Trap ClassCastException, NumberFormatException, and 
            // NullPointerException runtime exceptions just in case. 
            throw new BadDataException("Invalid", ex);
        }
    }

    
    public Book getBook() { 
        return m_book;
    }
    
    public String getBookName() {
        return m_book.name;
    }
    
    public String getBookAbbv() {
        return m_book.abbv;
    }
    
    public int getChapter() {
        return m_chapter;
    }
    
    public int getVerse() {
        return m_verse;
    }
    
    public void setBook(String book) {
        m_book = lookup(book);
    }
    
    public void setBook(Book book) {
        m_book = book;
    }
    
    public void setChapter(int ch) {
        // TODO validate chapter
        m_chapter = ch;
    }
    
    public void setVerse(int vs) {
        // TODO validate verse
        m_verse = vs;
    }
    
    /**
     * Returns a string based representation of this scripture reference.
     */
    public String toString() {
        String result = m_book.name;
        
        if (m_chapter > 0) 
            result += " " + m_chapter;
        
        if (m_verse > 0)
            result += ":" + m_verse;
        
        return result;
    }
    
    /** 
     * Returns this reference in the format used by the OSIS ID.
     * 
     * @return
     */
    public String toOSIS() {
        String result = m_book.abbv;
        
        if (m_chapter > 0) 
            result += "." + m_chapter;
        
        if (m_verse > 0)
            result += "." + m_verse;
        
        return result;
    }
    
    public Map<String, Object> toJSON() {
        return getMemento(m_book, m_chapter, m_verse);
    }
    
    public int compareTo(ScriptureReference ref) {
        int result = m_book.compareTo(ref.m_book);
        if (result == 0) {
            if (m_chapter == ref.m_chapter) {
                result = (m_verse == ref.m_chapter) ? 0
                            : (m_verse < ref.m_chapter) ? -1 : 1;
            } else {
                result = (m_chapter < ref.m_chapter) ? -1 : 1;
                
            }
        }
        
        return result;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ScriptureReference) {
            ScriptureReference ref = (ScriptureReference)obj;
            
            return ref.m_book    == m_book &&
                   ref.m_chapter == m_chapter &&
                   ref.m_verse   == m_verse;
        } else { 
            return false;
        }
    }
}
