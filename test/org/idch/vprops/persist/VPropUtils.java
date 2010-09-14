/* Created on       Aug 12, 2010
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

import org.idch.vprops.NumericConfig;
import org.idch.vprops.PropertyConfig;

public class VPropUtils {
    
    public static final VPropStruct vpTopSimple = 
        new VPropStruct("top", null, null, null);

    public static final VPropStruct vpTop = 
        new VPropStruct("top", "" + 25, false, 
                new NumericConfig(16, false, "px", 0, Double.NaN));
    
    public static final VPropStruct vpProp = 
        new VPropStruct("borderStyle", "" + 25, false, 
                new NumericConfig(16, false, "px", 0, Double.NaN));
    
    public static final VPropStruct vpBold = 
        new VPropStruct("bold", null, null, null);;
    
    public static final VPropStruct vpFont = 
        new VPropStruct("font", null, null, null);;
    
    public static final class VPropStruct {
        public String type = "";
        public String value   = null;
        public Boolean enabled = null;
        public PropertyConfig defaults = null;
        
        VPropStruct(VPropStruct obj) {
            this.type = obj.type;
            this.value = obj.value;
            this.enabled = obj.enabled;
            this.defaults = obj.defaults;
        }
        
        VPropStruct(String type, String value, Boolean enabled, 
                PropertyConfig defaults) {
            this.type = type;
            this.value = value;
            this.enabled = enabled;
            this.defaults = defaults;
        }
    }
}
