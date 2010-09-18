/* Created on       Aug 10, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright TEES Center for the Study of Digital Libraries (CSDL),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. PERMISSION TO USE THIS SOFTWARE MAY BE GRANTED 
 * TO INDIVIDUALS OR ORGANIZATIONS ON A CASE BY CASE BASIS. FOR MORE 
 * INFORMATION PLEASE CONTACT THE DIRECTOR OF THE CSDL. IN THE EVENT 
 * THAT SUCH PERMISSION IS GIVEN IT SHOULD BE UNDERSTOOD THAT THIS 
 * SOFTWARE IS PROVIDED ON AN AS IS BASIS. THIS CODE HAS BEEN DEVELOPED 
 * FOR USE WITHIN A PARTICULAR RESEARCH PROJECT AND NO CLAIM IS MADE AS 
 * TO IS CORRECTNESS, PERFORMANCE, OR SUITABILITY FOR ANY USE.
 */
package org.idch.vprops.persist;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.idch.persist.RepositoryAccessException;
import org.idch.util.LogService;

public abstract class PropertyRepository implements IPropertyRepository {
    protected static final String LOGGER = PropertyRepository.class.getName();

    public final static String DB_SRCRIPTS_PROP = "org.idch.vprops.sqldir";
    public final static String DB_URL_PROP      = "org.idch.vprops.db.url";
    public final static String DB_DRIVER_PROP   = "org.idch.vprops.db.driver";
    public final static String DB_USER_PROP     = "org.idch.vprops.db.user";
    public final static String DB_PASS_PROP     = "org.idch.vprops.db.pass";
    
    public final static String DEFAULT_PROP_BUNDLE = "vprops";
    public final static String PROP_REPO_CLASSNAME = 
        "org.idch.vprops.repository";
    
    public static PropertyRepository s_repository = null;
    
    /** 
     * Returns the <code>PropertyRepository</code>. If the 
     * <code>PropertyRepository</code> has not already been loaded using the
     * <code>get(ResourceBundle)</code> method, this will load a 
     * <code>PropertyRepository</code> based on the default configuration 
     * properties.
     * 
     * @return
     * @throws RepositoryAccessException
     */
    public static final PropertyRepository get()
            throws RepositoryAccessException {
    	// TODO This is very inflexible. Need to allow the specification of the 
    	//      prop file to be used, or provide a more flexible method of 
    	//      configuring this at some rate.
    	
    	if (s_repository == null) { 
    		try {
    			s_repository = get(ResourceBundle.getBundle(DEFAULT_PROP_BUNDLE));
    		} catch (MissingResourceException ex) {
    			String msg = null;
    			RepositoryAccessException rae = 
    				new RepositoryAccessException(msg);
    			LogService.logError(msg, LOGGER, rae);

    			throw rae;
    		}
    	}
        
        return s_repository;
    }
    
    @SuppressWarnings("rawtypes")
    public static final PropertyRepository get(ResourceBundle bundle)
        throws RepositoryAccessException {
        
        PropertyRepository repo = null;
        try {
            String classname = bundle.getString(PROP_REPO_CLASSNAME);
            Class cls = Class.forName(classname);
            if (s_repository != null) {
                String repoName = s_repository.getClass().getCanonicalName();
                if (!cls.getCanonicalName().equals(repoName)) {
                    String msg = "Invalid PropertyRepository initialization: " +
                    		"The currently initialized PropertyRepository " +
                    		"does not match the requested class: " + classname;
                    
                    throw new RepositoryAccessException(msg);
                } 
                
            } else {
                repo = (PropertyRepository)Class.forName(classname).newInstance();
                repo.initialize(bundle);
                s_repository = repo;
            }
            
        } catch (Exception ex) {
            throw new RepositoryAccessException(
                    "Could not load PropertyRepository.", ex);
        }
        
        return s_repository;
    }
    
    /** 
     * Attempts to create the required database structures for this Repository.
     *  
     * @throws RepositoryAccessException
     */
    public synchronized void createIfNeeded() throws RepositoryAccessException {
    	if (!s_repository.probe()) {
			// try to create the repository, if we can't find it.
			LogService.logWarn("Property repository not yet initialized. " +
					"Trying to create it now.", LOGGER);

			if (s_repository.create()) {
				// install default types
				s_repository.initTypes();
				LogService.logInfo("Created property repository.", LOGGER);
			} else {
				s_repository = null;
				String msg = "Could not initialize VisualProperty database.";
				LogService.logError(msg, LOGGER);
				throw new RepositoryAccessException(msg);
			}
		}
    }
    
    protected abstract void initialize(ResourceBundle bundle) 
            throws RepositoryAccessException; 
        
}
