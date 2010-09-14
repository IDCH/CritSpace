/* Created on       Aug 6, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops.persist;

import java.sql.SQLException;
import java.util.List;

import org.idch.persist.RepositoryAccessException;
import org.idch.vprops.Group;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.PropertyType;
import org.idch.vprops.VisualProperty;

/**
 * 
 * @author Neal Audenaert
 */
public interface IPropertyRepository {

    // XXX Methods should not expose the underlying implemenation. 
    //     Change IOException, SQLException to 
    
    /**
     * Attempts to determine whether or not the proper tables are defined for 
     * use by the <code>PropertyRepository</code>. 
     * 
     * @return <code>true</code> if the required tables are defined, 
     *      <code>false</code> if they are not.
     */
    public boolean probe() throws RepositoryAccessException; 
    
    /**
     * Create the database tables required for the MySQL PropertyRepository,
     * silently deleting any existing tables or data. Use with caution. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was created succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException If there is an error creating the
     *      persistent layer
     */
    public boolean create() throws RepositoryAccessException;
    
    /** 
     * Initializes the default types for the repository, if possible.
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public boolean initTypes() throws RepositoryAccessException;
    /**
     * Deletes all data (but not the database tables) from the database. 
     * <code>probe</code> should return true after successfull completion of 
     * this method. 
     * 
     * @return <code>true</code> if the database was cleaned succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException If there is an error cleaning the
     *      persistent layer
     */
    public boolean clean() throws RepositoryAccessException;
    
    /**
     * Drops all database tables and data associated with this 
     * PropertyRepsoitory. <code>probe</code> should return false after 
     * successfull completion of this method. 
     * 
     * @return <code>true</code> if the database was deleted succesfully, 
     *      <code>false</code> if it was not.
     * @throws RepositoryAccessException If there is an error dropping the
     *      persistent layer
     */
    public boolean drop() throws RepositoryAccessException;
    
    //=======================================================================
    // PROPERTY TYPE METHOS
    //=======================================================================
    
    /**
     * 
     * @param id
     * @param css
     * @param name
     * @param desc
     * @param config
     * 
     * @return
     * @throws SQLException
     */
    public PropertyType createPropertyType(String id, String css, 
            String name, String desc, PropertyConfig config) 
            throws RepositoryAccessException;
 
    /**
     * 
     * @param type
     * @return
     * @throws RepositoryAccessException
     */
    public boolean createPropertyType(PropertyType type) throws RepositoryAccessException;
    
    /**
     * 
     * @param id
     * @return
     * @throws RepositoryAccessException
     */
    public PropertyType findPropertyType(String id)
            throws RepositoryAccessException;
    
    /**
     * 
     * @param id
     * @param bypassCache
     * @return
     * @throws RepositoryAccessException
     */
    public PropertyType findPropertyType(String id, boolean bypassCache)
            throws RepositoryAccessException;
    
    /**
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public List<PropertyType> listPropertyTypes() 
            throws RepositoryAccessException;
    
    /**
     * 
     * @param type
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty createVisualProperty(PropertyType type) 
        throws RepositoryAccessException;
    
    /**
     * 
     * @param type
     * @param defaults
     * @param value
     * @param enabled
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty createVisualProperty(PropertyType type,
            PropertyConfig defaults, String value, Boolean enabled)
        throws RepositoryAccessException;
    
    /**
     * 
     * @param prop
     * @return
     * @throws RepositoryAccessException
     */
    public boolean updataVisualProperty(VisualProperty prop) 
    throws RepositoryAccessException;
    
    /**
     * 
     * @param id
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty findVisualProperty(long id) 
    throws RepositoryAccessException;

    /**
     * 
     * @param id
     * @param bypassCache
     * @return
     * @throws RepositoryAccessException
     */
    public VisualProperty findVisualProperty(long id, boolean bypassCache) 
    throws RepositoryAccessException;
    
    /**
     * 
     * @param group
     * @return
     * @throws RepositoryAccessException
     */
    public Group createGroup(Group group) throws RepositoryAccessException;
    
    /**
     * 
     * @param id
     * @return
     * @throws RepositoryAccessException
     */
    public Group findGroup(long id) throws RepositoryAccessException;
}
