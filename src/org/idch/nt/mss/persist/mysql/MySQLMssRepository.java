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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.idch.nt.ScriptureReference;
import org.idch.nt.mss.VsMapping;
import org.idch.nt.mss.persist.MSSRepository;
import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;

public class MySQLMssRepository extends MSSRepository{

    @Override
    public boolean probe() {
        List<String> sql = new ArrayList<String>(3);
        sql.add("SELECT mapping_id, f_id, img_id, x, y, book, chpt, vs" +
                "  FROM NT_VerseMapping;");
        return probe(sql);
    }
    
    public VsMapping createVerseMapping(long facsimId, long imageId, 
            int x, int y, ScriptureReference ref) 
        throws RepositoryAccessException {
        VsMapping    mapping = null;
        Connection   conn    = null;
        
        try {
            conn = openTransaction();

            VsMappingProxy proxy = new VsMappingProxy(conn);
            long id = proxy.create(facsimId, imageId, x, y, ref);
            mapping = new VsMapping(id, facsimId, imageId, x, y, ref);
            
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not create verse mapping for image: " + 
                    facsimId + ", " + imageId + ". " +
                    ex.getMessage();
        
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return mapping;
    }

    public List<VsMapping> getVerseMappings(long facsimId, long imageId) 
    throws RepositoryAccessException {
        Connection conn = null;
        SortedSet<VsMapping> mappings = new TreeSet<VsMapping>();     
        
        try {
            conn = this.openReadOnly();
            VsMappingProxy proxy = new VsMappingProxy(conn);
            List<Map<String, Object>> mappingList = 
                proxy.listReferences(facsimId, imageId);
            
            for (Map<String, Object> mapping : mappingList) {
                mappings.add(new VsMapping(mapping));
            }
            
        } catch (Exception ex) {
            close(conn);
            String msg = "Could not retrieve verse mapping for image: " + 
                    facsimId + ", " + imageId + ". " +
                    ex.getMessage();
        
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return new ArrayList<VsMapping>(mappings);
    }
    
    public boolean updateVerse(long mappingId, ScriptureReference ref)
    throws RepositoryAccessException {
        Connection conn    = null;
        boolean    success = false;
        
        try {
            conn = openTransaction();
            VsMappingProxy proxy = new VsMappingProxy(conn);
            success = proxy.update(mappingId, ref);
            
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not update scripture reference for mapping: " + 
                    mappingId + ex.getMessage();
        
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return success;
    }
    
    public boolean updateVersePosition(long mappingId, int x, int y)
    throws RepositoryAccessException {
        Connection conn    = null;
        boolean    success = false;
        
        try {
            conn = openTransaction();
            VsMappingProxy proxy = new VsMappingProxy(conn);
            success = proxy.update(mappingId, x, y);
            
            conn.commit();
        } catch (Exception ex) {
            rollback(conn);
            String msg = "Could not update position for mapping: " + 
                    mappingId + ex.getMessage();
        
            LogService.logWarn(msg, LOGGER);
            throw new RepositoryAccessException(msg, ex);
        } finally {
            close(conn);
        }
        
        return success;
    }

    

}
