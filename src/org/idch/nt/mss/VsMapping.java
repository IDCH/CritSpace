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
package org.idch.nt.mss;

import java.util.HashMap;
import java.util.Map;

import org.idch.nt.ScriptureReference;
import org.idch.nt.ScriptureReference.Book;
import org.idch.persist.BadDataException;

/**
 * Represents a mapping between a verse and a manuscript page image.
 * @author Neal Audenaert
 */
public class VsMapping {

    public static final String PK_ID   = "id";
    public static final String PK_F_ID = "fId";
    public static final String PK_IMG  = "img";
    public static final String PK_X    = "x";
    public static final String PK_Y    = "y";
    public static final String PK_REF  = "ref";
    
    //========================================================================
    // STATIC METHODS
    //========================================================================

    public Map<String, Object> getMemento(long id, long fId, long imgId,
            int x, int y, ScriptureReference ref) {
        Map<String, Object> data = new HashMap<String, Object>(4);
        
        data.put(PK_ID,   id);
        data.put(PK_F_ID, fId);
        data.put(PK_IMG,  imgId);
        data.put(PK_X,    x);
        data.put(PK_Y,    y);
        data.put(PK_REF,  ref.toJSON());
        
        return data;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    
    private long m_id       = -1;
    private long m_facsimId = -1;
    private long m_imageId  = -1;
    private int m_x         = -1;
    private int m_y         = -1;
    private ScriptureReference m_ref = null; 
        
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    
    public VsMapping() {
        
    }
    
    public VsMapping(long id, long facsimId, long imageId, int x, int y,
            ScriptureReference ref) {
        m_id       = id;
        m_facsimId = facsimId;
        m_imageId  = imageId;
        
        m_x   = x;
        m_y   = y;
        m_ref = ref;
    }
    
    public VsMapping(Map<String, Object> momento) throws BadDataException {
        initialize(momento);
    }
    
    //========================================================================
    // ACCESSORS AND MUTATORS
    //========================================================================
    
    public long getFacsimileId() {
        return m_facsimId;
    }
    
    public void setPosition() {
        
    }
    
    public void setReference(String book, int chpt, int vs) {
        Book bk = ScriptureReference.lookup(book);
        
        m_ref = new ScriptureReference(bk, chpt, vs);
    }
    
    /**
     * Indicates whether this mapping is valid. To be valid, a mapping must 
     * identify a facsimile and an image as well as the ScriptureReference
     * that maps to it. An invalid mapping cannot be saved.
     * 
     * 
     * @return
     */
    public boolean isValid() {
        return (m_facsimId > 0) && (m_imageId > 0) && (m_ref != null); 
    }
    
    //========================================================================
    // MOMENTO METHODS
    //========================================================================
    
    
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> data) throws BadDataException {
        try {
            
            // retore the identifier for this map
            Number id = (Number)data.get(PK_ID);
            m_id = (id != null) ? id.longValue() : -1;
            
            // restore the image this reference maps to
            Number facsim = (Number)data.get(PK_F_ID);
            Number img    = (Number)data.get(PK_IMG);
            m_facsimId = (facsim != null) ? facsim.longValue() : -1;
            m_imageId  = (img    != null) ? img.longValue()    : -1;
            
            // restore the position on the image that this reference maps to 
            Number x = (Number)data.get(PK_X);
            Number y = (Number)data.get(PK_Y);
            m_x = (x != null) ? x.intValue() : 0;
            m_y = (y != null) ? y.intValue() : 0;
            
            // restore the reference
            if (data.containsKey(PK_REF)) {
                Map<String, Object> ref = (Map<String, Object>)data.get(PK_REF);
                m_ref = (ref != null) ? new ScriptureReference(ref) : null;
            }
            
        } catch (ClassCastException cce) {
            throw new BadDataException("Could not cast the supplied data to " +
                    "the appropriate type.", cce);
        }
    }
  
    
    public Map<String, Object> toJSON() {
        return getMemento(m_id, m_facsimId, m_imageId, m_x, m_y, m_ref);
    }
}
