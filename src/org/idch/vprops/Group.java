/* Created on       Aug 7, 2010
 * Last Modified on $Date: $
 * $Revision: $
 * $Log: $
 *
 * Copyright Institute for Digital Christian Heritage (IDCH),
 *           Neal Audenaert
 *
 * ALL RIGHTS RESERVED. 
 */
package org.idch.vprops;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.idch.persist.BadDataException;
import org.idch.persist.DTO;

/**
 * DESCRIPTION GOES HERE.
 * 
 * Expected JSON serialization format:
 * <pre>
 *   {
        id : m_id,
        type : m_type,
        groups : { },
        properties : { }
    };
   </pre>
 *  
 * @author Neal Audenaert
 */
public class Group implements DTO {
    
    //========================================================================
    // SYMBOLIC CONSTANTS
    //========================================================================
    public static final String PK_ID         = "id";
    public static final String PK_TYPE       = "type";
    public static final String PK_GROUPS     = "groups";
    public static final String PK_PROPERTIES = "properties";
    
    //========================================================================
    // STATIC METHODS
    //========================================================================
    public static Map<String, Object> getMomento(String id, String type,
            Map<String, Group> subgroups,
            Map<String, VisualProperty> properties) {
        Map<String, Object> data = new HashMap<String, Object>(4);
        
        data.put(PK_ID, id);
        data.put(PK_TYPE, type);
        data.put(PK_PROPERTIES, new HashMap<String, Object>(properties));
        
        Map<String, Object> groups = 
            new HashMap<String, Object>(subgroups.size());
        for (String groupName : subgroups.keySet()) {
            Group group = subgroups.get(groupName);
            if (group != null) {
                groups.put(groupName, group.toJSON());
            }
        }
        
        return data;
    }
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    long m_id = -1;
    String m_type = null;
    Map<String, Group> m_groups = 
        new HashMap<String, Group>();
    Map<String, VisualProperty> m_properties =
        new HashMap<String, VisualProperty>();
    
    //========================================================================
    // CONSTRUCTORS
    //========================================================================
    /**
     * 
     */
    public Group() {
        
        
    }
    
    public Group(String type) {
        m_type = type;
    }
    
    public Group(String type, 
                 Map<String, Group> subgroups,
                 Map<String, VisualProperty> properties) {
        
        m_type = type;
        if (properties != null)
            m_properties = new HashMap<String, VisualProperty>(properties);
        if (subgroups != null)
            m_groups = new HashMap<String, Group>(subgroups);
    }

    public Group(Map<String, Object> data) throws BadDataException {
        initialize(data);
    }
    
    //========================================================================
    // ACCESSORS & MUTATORS
    //========================================================================
    public long getId() {
        return m_id;
    }
    
    /**
     * Sets the ID of this group.
     * @param id
     */
    public void setId(long id) {
        m_id = id;
    }
    
    /**
     * Returns the type of this group. Applications may define different group
     * 'types' and attach specific functionality to the visual properties of 
     * that type. 
     *  
     * @return
     */
    public String getType() {
        return m_type;
    }
    
    public Set<String> listSubGroups() {
        return m_groups.keySet();
    }
    
    public Group getSubGroup(String groupName) {
        return m_groups.get(groupName);
    }
    
    public Set<String> listProperties() {
        return m_properties.keySet();
    }
    
    public VisualProperty getProperty(String propName) {
        return m_properties.get(propName);
    }
    
    public Map<String, VisualProperty> getProperties() {
        return new HashMap<String, VisualProperty>(m_properties);
    }
    
    public boolean add(String name, Group group) {
        assert group.getId() < 0 : 
                "Cannot add a previously created group";
        boolean success = false;
        
        if ((group.getId() < 0) && 
             !m_groups.containsKey(name) && 
             !m_properties.containsKey(name)) {
            m_groups.put(name, group);
            success = true;
        }
        
        return success;
    }
    
    public boolean add(String name, VisualProperty vprop) {
        assert vprop.getId() < 0 : 
                "Cannot add a previously created property";
        boolean success = false;
        
        if ((vprop.getId() < 0) && 
             !m_groups.containsKey(name) && 
             !m_properties.containsKey(name)) {
            m_properties.put(name, vprop);
            success = true;
        }
        
        return success;
    }
    
    //========================================================================
    // MOMENTO METHODS
    //========================================================================
    @SuppressWarnings("unchecked")
    public void initialize(Map<String, Object> data) throws BadDataException {
        try {
            Number id = (Number)data.get(PK_ID);
            if (id != null) 
                m_id = id.longValue();
            
            m_type = (String)data.get(PK_TYPE);
            
            Map<String, Object> groups = 
                (Map<String, Object>)data.get(PK_GROUPS);
            for (String groupName : groups.keySet()) {
                Map<String, Object> groupData = 
                    (Map<String, Object>)groups.get(groupName);
                Group group = new Group(groupData);
                m_groups.put(groupName, group);
            }
            
            Map<String, Object> properties = 
                (Map<String, Object>)data.get(PK_PROPERTIES);
            for (String propertyName : properties.keySet()) {
                Map<String, Object> propertyData = 
                    (Map<String, Object>)properties.get(propertyName);

                 
                VisualProperty property = new VisualProperty(propertyData);
                m_properties.put(propertyName, property);
            }
        } catch (ClassCastException cce) {
            throw new BadDataException("Supplied data could not be cast to " +
                    "the appropriate type.", cce);
        }
    }

    public Map<String, Object> toJSON() {
        Map<String, Object> data = new HashMap<String, Object>(4);
        
        data.put(PK_ID, m_id);
        data.put(PK_TYPE, m_type);
        
        Map<String, Object> props = 
            new HashMap<String, Object>(m_properties.size());
        for (String propName : m_properties.keySet()) {
            VisualProperty prop = m_properties.get(propName);
            if (prop != null) {
                props.put(propName, prop.toJSON());
            }
        }
        
        Map<String, Object> groups = 
            new HashMap<String, Object>(m_groups.size());
        for (String groupName : m_groups.keySet()) {
            Group group = m_groups.get(groupName);
            if (group != null) {
                groups.put(groupName, group.toJSON());
            }
        }
        
        data.put(PK_PROPERTIES, props);
        data.put(PK_GROUPS, groups);
        return data;
    }

}