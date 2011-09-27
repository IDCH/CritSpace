/* Created on       Aug 26, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 *           ALL RIGHTS RESERVED. 
 */
package org.idch.critspace.persist;


import org.idch.persist.DBBackedRepository;
import org.idch.persist.RepositoryAccessException;

public abstract class CritspaceRepository 
extends DBBackedRepository 
implements ICritspaceRepository {
    
    protected static final String LOGGER = CritspaceRepository.class.getName();

    public final static String MODULE_NAME = "critspace";
    
    /** 
     * 
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public static final CritspaceRepository get()
            throws RepositoryAccessException {
        return (CritspaceRepository)get(MODULE_NAME);
    }
}
