/* Created on       Aug 11, 2010
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.idch.persist.DBBackedRepository;
import org.idch.vprops.Group;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.PropertyType;
import org.idch.vprops.VisualProperty;
import org.idch.vprops.persist.TypeUtils.PropTypeStruct;
import org.idch.vprops.persist.VPropUtils.VPropStruct;

import junit.framework.TestCase;

public class RepositoryDataAccessTests extends TestCase {
    
    private static final String PROP_BUNDLE = "test_repo";
    
    //======================================================================
    // 
    //======================================================================
    
    private PropertyRepository m_repository = null;
    
    private VisualProperty m_topProp = null;
    private VisualProperty m_fontProp = null;
    private VisualProperty m_boldProp = null;
    
    public void setUp() throws Exception {
        // Create a fresh new database every time we run a test.
        DBBackedRepository.setPropertyBundle(PROP_BUNDLE);
        m_repository = PropertyRepository.get();
        m_repository.create();
    }

    public void tearDown() throws Exception {
        // drop the database that we just created
        m_repository.drop();    
    }
    
    //======================================================================
    // HELPERS TO CREATE & EVALUATE STANDARD DATA OBJECTS
    //======================================================================
    
    
    private PropertyType constructPropType(TypeUtils.PropTypeStruct data) throws Exception {
        return m_repository.createPropertyType(
                data.id, data.css, data.name, data.desc, data.config);
    }
    
    private void assertPropType(PropertyType type, TypeUtils.PropTypeStruct data) {
        assertEquals("Incorrect ID value", type.getId(), data.id);
        assertEquals("Incorrect CSS value", type.getCSS(), data.css);
        assertEquals("Incorrect name", type.getName(), data.name);
        assertEquals("Incorrect description", type.getDescription(), data.desc);
        assertTrue("Incorrect configuration", 
                type.getConfig().equals(data.config));
    }
    
    private VisualProperty construct(VPropStruct data) throws Exception {
        PropertyType type = m_repository.findPropertyType(data.type);
        assertNotNull("Could not create property type", type);
        
        VisualProperty prop = null;
        if (data.defaults == null) {
            prop = m_repository.createVisualProperty(type);
        } else {
            prop = m_repository.createVisualProperty(
                type, data.defaults, data.value, data.enabled);
        }
        
        return prop;
    }
    
    private void assertVProp(VisualProperty prop, VPropStruct data) 
            throws Exception  {
        assertNotNull("Property was not created", prop);
        assertEquals("Invalid property type.", prop.getType(), data.type);
        assertEquals("Invalid value for value.", prop.getValue(), data.value);
        assertEquals("Invalid value for enabled.", prop.getEnabled(), data.enabled);
        
        PropertyConfig config = data.defaults;
        if (data.defaults == null) { 
            config = m_repository.findPropertyType(data.type).getConfig();
        }
        assertTrue("Invalid default configuration.", 
                prop.getDefaults().equals(config));
    }
    
    //======================================================================
    // PROP TYPE TEST METHODS
    //======================================================================
    
    public void testCreatePropType() throws Exception {
        PropertyType top    = constructPropType(TypeUtils.ptTop);
        PropertyType font   = constructPropType(TypeUtils.ptFont);
        PropertyType bold   = constructPropType(TypeUtils.ptBold);
        
        assertPropType(top,  TypeUtils.ptTop);
        assertPropType(font, TypeUtils.ptFont);
        assertPropType(bold, TypeUtils.ptBold);
        
    }
    
    public void testFindPropType() throws Exception {
        PropertyType top   = constructPropType(TypeUtils.ptTop);
        PropertyType font  = constructPropType(TypeUtils.ptFont);
        PropertyType bold  = constructPropType(TypeUtils.ptBold);
        
        PropertyType rTop  =  m_repository.findPropertyType(TypeUtils.ptTop.id, true);
        PropertyType rFont =  m_repository.findPropertyType(TypeUtils.ptFont.id, true);
        PropertyType rBold =  m_repository.findPropertyType(TypeUtils.ptBold.id, true);
        
        assertPropType(rTop, TypeUtils.ptTop);
        assertPropType(rFont, TypeUtils.ptFont);
        assertPropType(rBold, TypeUtils.ptBold);
        
        assertFalse("Retrieved the same object as the created type", 
                (top == rTop) || (font == rFont) || (bold == rBold));
    }
    
    public void testListPropTypes() throws Exception {
        PropertyType top   = constructPropType(TypeUtils.ptTop);
        PropertyType font  = constructPropType(TypeUtils.ptFont);
        PropertyType bold  = constructPropType(TypeUtils.ptBold);
        
        List<PropertyType> types = m_repository.listPropertyTypes();
        assertTrue("Unexpected number of types returned", types.size() == 3);
        
        boolean foundTop = false;
        boolean foundFont = false;
        boolean foundBold = false;
        for (PropertyType type : types) {
            String id = type.getId();
            if (id.equals(top.getId()))
                foundTop = true;
            else if (id.equals(font.getId()))
                foundFont = true;
            else if (id.equals(bold.getId()))
                foundBold = true;
        }
        
        assertTrue("Not all created types restored.", 
                foundTop && foundFont && foundBold);
           
        
    }
    
    // TODO test duplicate creation (should fail)
    
    //======================================================================
    // VPROP TEST METHODS
    //======================================================================
    
    public void testCreateVProp() throws Exception {
        // numeric type property
        constructPropType(TypeUtils.ptTop);
        VisualProperty topProp = construct(VPropUtils.vpTop);
        assertVProp(topProp, VPropUtils.vpTop);

        // text (with options) type property
        constructPropType(TypeUtils.ptFont);
        VisualProperty fontProp = construct(VPropUtils.vpFont);
        assertVProp(fontProp, VPropUtils.vpFont);
        
        // toggle type property
        constructPropType(TypeUtils.ptBold);
        VisualProperty boldProp = construct(VPropUtils.vpBold);
        assertVProp(boldProp, VPropUtils.vpBold);
    }
    
    public void testCreateSimpleVProp() throws Exception {
        PropertyType top = constructPropType(TypeUtils.ptTop);
        VisualProperty prop = m_repository.createVisualProperty(top);
        
        assertVProp(prop, VPropUtils.vpTopSimple);
    }
   
    /**
     * Helper method to generate, restore and test a specific visual property
     * based on supplied PropType and VProp templates.
     * 
     * @param typeTemplate
     * @param vpropTemplate
     * @throws Exception
     */
    private void restoreVProp(
            PropTypeStruct typeTemplate, VPropStruct vpropTemplate) 
        throws Exception {
        
        constructPropType(typeTemplate);
        VisualProperty prop = construct(vpropTemplate);
        
        VisualProperty restored  = 
            m_repository.findVisualProperty(prop.getId(), true);
        
        assertVProp(restored, vpropTemplate);
        assertEquals("IDs are not equal.", restored.getId(), prop.getId());
        assertFalse("Not retrieved from the database.", prop == restored);
    }
    
    public void testRestoreVProp() throws Exception {
        restoreVProp(TypeUtils.ptTop, VPropUtils.vpTop);
        restoreVProp(TypeUtils.ptFont, VPropUtils.vpFont);
        restoreVProp(TypeUtils.ptBold, VPropUtils.vpBold);
    }
    
    public void testUpdateVProp() throws Exception {
        constructPropType(TypeUtils.ptTop);
        VisualProperty prop = construct(VPropUtils.vpTop);
        prop.setValue(50 + "");
        prop.enable(true);
        
        boolean success = m_repository.updataVisualProperty(prop);
        assertTrue("Could not update visual property", success);
        
        VisualProperty restored = 
            m_repository.findVisualProperty(prop.getId(), true);
        
        VPropStruct data = new VPropStruct(VPropUtils.vpTop);
        data.value = 50 + "";
        data.enabled = true;
        
        assertVProp(restored, data);
        assertEquals("IDs are not equal.", restored.getId(), prop.getId());
        assertFalse("Not retrieved from the database.", prop == restored);
    }
    
    //======================================================================
    // GROUP TEST METHODS
    //======================================================================
    
    public void testEmptyGroup() throws Exception {
        Group group = new Group("org.idch.vprops.testGroup");
        
        m_repository.createGroup(group);
        assertTrue("Group id not set", group.getId() > 0);
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
    
    private Group configureTestGroup() throws Exception {
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
    
    public void testGroup() throws Exception {
        setupPropertyTypes();
        setupVProps();
        Group group = configureTestGroup();
        Group typography = group.getSubGroup("typography");
        
        m_repository.createGroup(group);
        assertTrue("Group id not set", group.getId() > 0);
        assertTrue("Group id not set", typography.getId() > 0);
        assertTrue("Property id (top) not set", m_topProp.getId() > 0);
        assertTrue("Property id (font) not set", m_fontProp.getId() > 0);
        assertTrue("Property id (bold) not set", m_boldProp.getId() > 0);
    }
    
    public void testRestoreGroup() throws Exception {
        setupPropertyTypes();
        setupVProps();
        Group group = configureTestGroup();
        Group typography = group.getSubGroup("typography");
        
        m_repository.createGroup(group);
        
        Group rGroup = m_repository.findGroup(group.getId());
        Group rTypography = group.getSubGroup("typography");
        
        assertEquals(rGroup.getId(), group.getId());
        assertEquals(rTypography.getId(), typography.getId());
        
        VisualProperty topProp = group.getProperty("top");
        VisualProperty rTopProp = rGroup.getProperty("top");
        assertEquals(topProp.getId(), rTopProp.getId());
        
        VisualProperty fontProp = typography.getProperty("font");
        VisualProperty rFontProp = rTypography.getProperty("font");
        assertEquals(fontProp.getId(), rFontProp.getId());
        
        VisualProperty boldProp = typography.getProperty("bold");
        VisualProperty rBoldProp = rTypography.getProperty("bold");
        assertEquals(boldProp.getId(), rBoldProp.getId());
    }
    
}
