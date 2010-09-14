/* Created on       Aug 27, 2010
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
package org.idch.critspace.persist;

import java.util.HashMap;
import java.util.Map;

import org.idch.vprops.Group;
import org.idch.vprops.PropertyType;
import org.idch.vprops.VisualProperty;
import org.idch.vprops.persist.PropertyRepository;
import org.idch.vprops.persist.TypeUtils;
import org.idch.vprops.persist.VPropUtils;
import org.idch.vprops.persist.VPropUtils.VPropStruct;

public class WSUtils {
    private PropertyRepository m_vpropRepo = null;
    
    private VisualProperty m_topProp = null;
    private VisualProperty m_fontProp = null;
    private VisualProperty m_boldProp = null;
    
    WSUtils(PropertyRepository repo) {
        m_vpropRepo = repo;
    }
    private PropertyType constructPropType(TypeUtils.PropTypeStruct data) 
    throws Exception {
        return m_vpropRepo.createPropertyType(
                data.id, data.css, data.name, data.desc, data.config);
    }
    
    private VisualProperty construct(VPropStruct data) throws Exception {
        PropertyType type = m_vpropRepo.findPropertyType(data.type);
        
        VisualProperty prop = null;
        if (data.defaults == null) {
            prop = m_vpropRepo.createVisualProperty(type);
        } else {
            prop = m_vpropRepo.createVisualProperty(
                type, data.defaults, data.value, data.enabled);
        }
        
        return prop;
    }
    
    private void setupPropertyTypes() throws Exception {
        constructPropType(TypeUtils.ptTop);
        constructPropType(TypeUtils.ptFont);
        constructPropType(TypeUtils.ptBold);
    }
    
    private void setupVProps() throws Exception {
        m_topProp = construct(VPropUtils.vpTop);
        m_fontProp = construct(VPropUtils.vpFont);
        m_boldProp = construct(VPropUtils.vpBold);
    }
    
    public Group configureTestGroup() throws Exception {
        setupPropertyTypes();
        setupVProps();
        
        // create the typography group
        Map<String, VisualProperty> typoProps = 
            new HashMap<String, VisualProperty>();
        typoProps.put("font", m_fontProp);
        typoProps.put("bold", m_boldProp);
        
        String typoType = "org.idch.vprops.typography";
        Group typography = new Group(typoType, null, typoProps);
        
        // create the main test group
        Map<String, VisualProperty> posProps = 
            new HashMap<String, VisualProperty>();
        posProps.put("top", m_topProp);
        
        Map<String, Group> subgroup = new HashMap<String, Group>();
        subgroup.put("typography", typography);
        
        String testType = "org.idch.vprops.testGroup";
        return  new Group(testType, subgroup, posProps);
    }
}
