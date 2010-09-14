/* Created on       Aug 7, 2010
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
package org.idch.vprops.persist.mysql;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.idch.persist.BadDataException;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.PropertyType;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class PropertyTypeProxy {
    private final static String ID_COL   = "type_id";
    private final static String CSS_COL  = "css";
    private final static String NAME_COL = "name";
    private final static String DESC_COL = "description";
    private final static String FMT_COL  = "format";
    private final static String CFG_COL  = "config";
    
    private final static int EXISTS_ID     = 1;
    private final static String EXISTS_SQL = 
        "SELECT type_id FROM PropertyTypes WHERE id = ?";
    
    private final static int CREATE_ID     = 1;
    private final static int CREATE_CSS    = 2;
    private final static int CREATE_NAME   = 3;
    private final static int CREATE_DESC   = 4;
    private final static int CREATE_FMT    = 5;
    private final static int CREATE_CONFIG = 6;
    
    private final static String CREATE_SQL =
        "INSERT INTO PropertyTypes " +
            "(type_id, css, name, description, format, config)" +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    
    private final static int RESTORE_ID     = 1;
    private final static String RESTORE_SQL = 
        "SELECT type_id, css, name, description, format, config " +
        "  FROM PropertyTypes" +
        " WHERE type_id = ?";
    
    private final static String RESTORE_ALL_SQL = 
        "SELECT type_id, css, name, description, format, config " +
        "  FROM PropertyTypes";
    
    private final static int UPDATE_NAME   = 1;
    private final static int UPDATE_DESC   = 2;
    private final static int UPDATE_CONFIG = 3;
    private final static int UPDATE_ID     = 4;
    
    private final static String UPDATE_SQL =
        "UPDATE PropertyTypes " +
        "   SET name = ?," +
        "       description = ?," +
        "       config = ?" +
        " WHERE type_id = ?";
    
    
    private final JSONParser parser = new JSONParser();
    
    private final Connection m_connection; 
    
    /**
     * 
     * @param connection
     */
    PropertyTypeProxy(Connection connection) {
        m_connection = connection;
    }
    
    /**
     * 
     * @param config
     * @return
     * @throws IOException
     */
    private final String stringifyConfig(PropertyConfig config) 
            throws IOException {
        StringWriter out = new StringWriter();
        JSONValue.writeJSONString(config.toJSON(), out);
        return out.toString();
    }
    
    /**
     * 
     * @param config
     * @return
     */
    final boolean checkFormat(PropertyType type, PropertyConfig config) {
        if (config.getFormat().equalsIgnoreCase(type.getFormat()))
            return true;
        else 
            return false;
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     */
    final boolean exists(String id) throws SQLException {
        PreparedStatement stmt = m_connection.prepareStatement(EXISTS_SQL);
        stmt.setString(EXISTS_ID, id);
        ResultSet results = stmt.executeQuery();
        
        boolean exists = results.next();
        stmt.close();
        
        return exists;
    }
    
    /**
     * 
     * @return
     * @throws SQLException
     * @throws IOException
     */
    final boolean create(PropertyType type) throws SQLException, IOException {
        if (type == null) 
            return false;
        
        PropertyConfig config = type.getConfig();
        assert this.checkFormat(type, config) : "Configuration format mismatch.";
        
        boolean success = false;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(CREATE_SQL);
            stmt.setString(CREATE_ID,     type.getId());
            stmt.setString(CREATE_CSS,    type.getCSS());
            stmt.setString(CREATE_NAME,   type.getName());
            stmt.setString(CREATE_DESC,   type.getDescription());
            stmt.setString(CREATE_FMT,    type.getFormat());
            stmt.setString(CREATE_CONFIG, stringifyConfig(config));
            
            int numRowsChanged = stmt.executeUpdate();
            if (numRowsChanged == 1)
                success = true;
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return success;
    }
    
    /**
     * 
     * @param results
     * @return
     * @throws SQLException
     * @throws BadDataException
     */
    @SuppressWarnings("unchecked")
    private PropertyType restoreFromResults(ResultSet results) 
        throws SQLException, BadDataException {
        String id   = results.getString(ID_COL);
        String css  = results.getString(CSS_COL);
        String name = results.getString(NAME_COL);
        String desc = results.getString(DESC_COL);
        String fmt  = results.getString(FMT_COL);
        String cfg  = results.getString(CFG_COL);

        PropertyConfig config = null;
        String configErroMsg = 
            "Bad configuration data retrieved from DB " +
            "for PropertyType '" + id + "'";
        try {
            config = PropertyConfig.get(
                    (Map<String, Object>)parser.parse(cfg));
            if (config == null) 
                throw new BadDataException(configErroMsg);
            
        } catch (ParseException pe) {
            throw new BadDataException(configErroMsg, pe);
        }
            
        assert config.getFormat().equalsIgnoreCase(fmt) : 
            "Configuration format mismatch.";
        
        return new PropertyType(id, css, name, desc, config);
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     * @throws BadDataException
     */
    PropertyType restore(String id) throws SQLException, BadDataException {
        
        PropertyType type = null;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(RESTORE_SQL);
            stmt.setString(RESTORE_ID, id);
            
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                type = restoreFromResults(results);
            }
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return type;
    }
    
    /**
     * 
     * @return
     * @throws SQLException
     * @throws BadDataException
     */
    List<PropertyType> listAll() throws SQLException, BadDataException {
        
        List<PropertyType> types = new ArrayList<PropertyType>();
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(RESTORE_ALL_SQL);
            
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                types.add(restoreFromResults(results));
            }
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return types;
    }
    
    /**
     * 
     * @return
     * @throws SQLException
     * @throws IOException
     */
    boolean update(PropertyType type) throws SQLException, IOException {
        if (type == null) 
            return false;
        
        PropertyConfig config = type.getConfig();
        assert this.checkFormat(type, config) : 
            "Configuration format mismatch.";
        
        boolean success = false;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(UPDATE_SQL);
            stmt.setString(UPDATE_ID,     type.getId());
            stmt.setString(UPDATE_NAME,   type.getName());
            stmt.setString(UPDATE_DESC,   type.getDescription());
            stmt.setString(UPDATE_CONFIG, stringifyConfig(config));
            
            int numRowsChanged = stmt.executeUpdate();
            if (numRowsChanged == 1)
                success = true;
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return success;
    }
    
    /**
     * 
     * @return
     */
    boolean delete() {
        return false;
    }
}
