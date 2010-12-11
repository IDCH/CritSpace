/* Created on       Dec 9, 2010
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
package org.idch.nt.mss.persist.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.idch.critspace.persist.mysql.PersistenceProxy;
import org.idch.nt.ScriptureReference;
import org.idch.nt.ScriptureReference.Book;
import org.idch.nt.mss.VsMapping;

public class VsMappingProxy extends PersistenceProxy {
    
    public static final String ID_COL   = "mapping_id";
    public static final String F_ID_COL = "f_id";
    public static final String IMG_COL  = "img_id";
    public static final String X_COL    = "x";
    public static final String Y_COL    = "y";
    public static final String BOOK_COL = "book";
    public static final String CHPT_COL = "chpt";
    public static final String VS_COL   = "vs";
    
    
    private static final int CREATE_F_ID = 1;
    private static final int CREATE_IMG  = 2;
    private static final int CREATE_X    = 3;
    private static final int CREATE_Y    = 4;
    private static final int CREATE_BOOK = 5;
    private static final int CREATE_CHPT = 6;
    private static final int CREATE_VS   = 7;
    
    private final String CREATE_SQL = 
        "INSERT INTO NT_VerseMapping (f_id, img_id, x, y, book, chpt, vs)" +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final int LIST_PG_FACSIM = 1;
    private static final int LIST_PG_IMAGE  = 2;
    private final String LIST_BY_PAGE = 
        "SELECT mapping_id, x, y, book, chpt, vs" +
        "  FROM NT_VerseMapping" +
        " WHERE f_id = ? " + 
        "   AND img_id = ?";
    
    private static final int UPDATE_REF_BOOK = 1;
    private static final int UPDATE_REF_CHPT = 2;
    private static final int UPDATE_REF_VS   = 3;
    private static final int UPDATE_REF_ID   = 4;
    private final String UPDATE_REF_SQL = 
        "UPDATE NT_VerseMapping" +
        "   SET book = ?," +
        "       chpt = ?," +
        "       vs   = ?" +
        " WHERE mapping_id = ?";
    
    private static final int UPDATE_POS_X  = 1;
    private static final int UPDATE_POS_Y  = 2;
    private static final int UPDATE_POS_ID = 3;
    private final String UPDATE_POS_SQL =  
        "UPDATE NT_VerseMapping" +
        "   SET x = ?," +
        "       y = ?" +
        " WHERE mapping_id = ?";
    
    //========================================================================
    // CONSTRUCTORS & FINALIZATION
    //========================================================================
    
    /**
     * 
     * @param connection
     */
    VsMappingProxy(Connection connection) {
        super(connection);
    }
    
    /**
     * Closes any open statements in use by this proxy.
     * 
     * @throws SQLException if there is a database access error
     */
    public final void close() throws SQLException {
        
    }
    
    //========================================================================
    // DB ACCESS METHODS
    //========================================================================
    
    /**
     * Extracts information about a scripture reference from a result set and 
     * sets it to the appropriate fields of the memento object.  
     */
    private final boolean getReference(
            ResultSet results, Map<String, Object> data) 
    throws SQLException {
        boolean success = false;
        String bk   = results.getString(BOOK_COL);
        int    chpt = results.getInt(CHPT_COL);
        int    vs   = results.getInt(VS_COL);
        Book book = ScriptureReference.lookup(bk);
        
        if (book != null) {
            ScriptureReference ref = new ScriptureReference(book, chpt, vs);
            data.put(VsMapping.PK_REF, ref);
        }
        
        return success;
    }
    
    private final boolean getPosition(
            ResultSet results, Map<String, Object> data) 
    throws SQLException {
        data.put(X_COL, results.getInt(X_COL));
        data.put(Y_COL, results.getInt(Y_COL));
        
        return true;
    }
    
    /**
     * 
     * @param facsimId
     * @param imageId
     * @param x
     * @param y
     * @param ref
     * 
     * @return The ID of the newly created mapping. This will be less than 0 
     *      if the mapping was not created.
     *      
     * @throws SQLException If there are problems accessing the database.
     */
    final long create(long facsimId, long imageId, int x, int y, 
            ScriptureReference ref)
    throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(CREATE_SQL);
        
        stmt.setLong(CREATE_F_ID,   facsimId);
        stmt.setLong(CREATE_IMG,    imageId);
        stmt.setLong(CREATE_X,      x);
        stmt.setLong(CREATE_Y,      y);
        stmt.setString(CREATE_BOOK, ref.getBookAbbv().toLowerCase());
        stmt.setLong(CREATE_CHPT,   ref.getChapter());
        stmt.setLong(CREATE_VS,     ref.getVerse());
        
        int numRowsChanged = stmt.executeUpdate();
        ResultSet results = stmt.getGeneratedKeys();
        if (numRowsChanged == 1 && results.next()) {
            return results.getLong(1);
        } else {
            return -1;
        }
    }

    /**
     * 
     * @param id
     * @param ref
     * @return
     * @throws SQLException
     */
    final boolean update(long id, ScriptureReference ref) 
    throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(UPDATE_REF_SQL);
        
        stmt.setLong(UPDATE_REF_ID,     id);
        stmt.setString(UPDATE_REF_BOOK, ref.getBookAbbv().toLowerCase());
        stmt.setLong(UPDATE_REF_CHPT,   ref.getChapter());
        stmt.setLong(UPDATE_REF_VS,     ref.getVerse());
        
        int numRowsChanged = stmt.executeUpdate();
        return (numRowsChanged == 1);
    }
    
    /**
     * 
     * @param id
     * @param ref
     * @return
     * @throws SQLException
     */
    final boolean update(long id, int x, int y) 
    throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(UPDATE_POS_SQL);
        
        stmt.setLong(UPDATE_POS_ID,id);
        stmt.setLong(UPDATE_POS_X, x);  
        stmt.setLong(UPDATE_POS_Y, y);
        
        int numRowsChanged = stmt.executeUpdate();
        return (numRowsChanged == 1);
    }
    
    final List<Map<String, Object>> listReferences(long facsimId, long imageId) 
    throws SQLException {
        List<Map<String, Object>> mappings = 
            new ArrayList<Map<String, Object>>();
        PreparedStatement stmt = m_connection.prepareStatement(LIST_BY_PAGE);
        
        stmt.setLong(LIST_PG_FACSIM, facsimId);
        stmt.setLong(LIST_PG_IMAGE, imageId);
        
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put(VsMapping.PK_F_ID, facsimId);
            data.put(VsMapping.PK_IMG, imageId);
            data.put(VsMapping.PK_ID, results.getLong(ID_COL));
            
            getReference(results, data);
            getPosition(results, data);
            
            mappings.add(data);
        }
        return null;
    }
    
    /**
     * Returns a list of momentos that indicates all images with verse mappings
     * in the specified range.
     *  
     * @param fascimId
     * @param book
     * @param startChpt
     * @param endChapter
     * @param startVs
     * @param endVerse
     * @return
     */
    final List<Map<String, Object>> listPageImages(long fascimId, String book,
            int startChpt, int endChapter, int startVs, int endVerse) {
        return null;
    }
}
