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
package org.idch.nt.mss.persist;

import java.util.List;

import org.idch.nt.ScriptureReference;
import org.idch.nt.mss.VsMapping;
import org.idch.persist.RepositoryAccessException;

public interface IMSSRepository {
    public VsMapping createVerseMapping(long facsimId, long imageId,
            int x, int y, ScriptureReference ref) 
    throws RepositoryAccessException;
    
    /**
     * Returns all mappings for the indicated page image.
     * 
     * @param facsimId
     * @param imageId
     * @return
     * @throws RepositoryAccessException
     */
    public List<VsMapping> getVerseMappings(long facsimId, long imageId)
    throws RepositoryAccessException;
    
    public boolean updateVerse(long mappingId, ScriptureReference ref)
    throws RepositoryAccessException;
    
    
    public boolean updateVersePosition(long mappingId, int x, int y)
    throws RepositoryAccessException;
}
