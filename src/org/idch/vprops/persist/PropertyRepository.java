/* Created on       Aug 10, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *      ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.persist;

import org.idch.persist.DBBackedRepository;
import org.idch.persist.RepositoryAccessException;

public abstract class PropertyRepository
extends DBBackedRepository 
implements IPropertyRepository {
    
    public final static String MODULE_NAME = "vprops";
    
    /** 
     * Returns the <code>PropertyRepository</code>.
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public static final PropertyRepository get()
            throws RepositoryAccessException {
        return (PropertyRepository)get(MODULE_NAME);
    }
}
