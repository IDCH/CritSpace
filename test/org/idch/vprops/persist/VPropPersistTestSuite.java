/* Created on       Aug 18, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Neal Audenaert 
 *           ALL RIGHTS RESERVED
 */
package org.idch.vprops.persist;

import junit.framework.Test;
import junit.framework.TestSuite;

public class VPropPersistTestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("VProps Persistence Tests");
        
        suite.addTestSuite(RepositoryDBTests.class);
        suite.addTestSuite(RepositoryDataAccessTests.class);
        
        return suite;
    }
}
