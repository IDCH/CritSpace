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
package org.idch.vprops.persist.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.idch.persist.BadDataException;
import org.idch.persist.DBUtils;
import org.idch.util.LogService;
import org.idch.vprops.Group;
import org.idch.vprops.NumericConfig;
import org.idch.vprops.PropertyConfig;
import org.idch.vprops.PropertyType;
import org.idch.vprops.TextualConfig;
import org.idch.vprops.ToggleConfig;
import org.idch.vprops.VisualProperty;

class VisualPropertyProxy {
    private final static String LOGGER = VisualPropertyProxy.class.getName();
    
    /** SQL to update a visual property. */
    private final static String UPDATE_SQL =
        "UPDATE VisualProperties" +
        "   SET prop_value = ?," +
        "       enabled = ?" +
        " WHERE vprop_id = ?";
    private final static int UPDATE_VALUE = 1;
    private final static int UPDATE_ENABLED = 2;
    private final static int UPDATE_ID = 3;
    
    //========================================================================
    // MEMBER VARIABLES
    //========================================================================
    private final Connection m_connection;
    
    private PreparedStatement createVPropStatement = null;
    private PreparedStatement createOptionStatement = null;
    
    private PreparedStatement createNumCfgStatement = null;
    private PreparedStatement createTxtCfgStatement = null;
    private PreparedStatement createTogCfgStatement = null;
    
    private PreparedStatement restoreByGroup = null;
    
    /**
     * 
     * @param connection
     */
    VisualPropertyProxy(Connection connection) {
        m_connection = connection;
    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        } catch (Exception e) {
            /** log and supress errors */
            LogService.logError("Finalization failed", LOGGER, e);
        } finally {
            super.finalize();
        }
    }
    
    void close(PreparedStatement stmt) {
        try {
            if (stmt != null) 
                stmt.close();
        } catch (SQLException ex) {
            LogService.logError("Failed to close statement.", LOGGER, ex);
        } 
    }
    
    /**
     * Closes any open statements in use by this proxy. Note that this does 
     * not close the connection associated with this proxy (since that 
     * connection was created externally).
     * 
     * @throws SQLException if there is a database access error
     */
    void close() throws SQLException {
        
        DBUtils.close(createVPropStatement);
        DBUtils.close(createOptionStatement);
        
        DBUtils.close(createNumCfgStatement);
        DBUtils.close(createTxtCfgStatement);
        DBUtils.close(createTogCfgStatement);
        
        DBUtils.close(restoreByGroup);
    }
    
    //========================================================================
    // VISUAL PROPERTY CREATION METHODS & CONSTANTS
    //========================================================================
    
    private final static int CREATE_GRP_ID  = 1;
    private final static int CREATE_NAME    = 2;
    private final static int CREATE_TYPE    = 3;
    private final static int CREATE_VALUE   = 4;
    private final static int CREATE_ENABLED = 5;
    private final static int CREATE_FORMAT  = 6;
    private final static String CREATE_SQL = 
        "INSERT INTO VisualProperties " +
            "(group_id, prop_name, type_id, prop_value, enabled, format)" +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private final static String CREATE_NUM_SQL = 
        "INSERT INTO PropertyConfigurations " +
            "(vprop_id, prop_value, enabled, units, minimum, maximum)" +
        "VALUES (?, ?, ?, ?, ?, ?)";
    private final static int CREATE_CFG_ID      = 1;
    private final static int CREATE_CFG_VALUE   = 2;
    private final static int CREATE_CFG_ENABLED = 3;
    private final static int CREATE_NUM_UNITS   = 4;
    private final static int CREATE_NUM_MIN     = 5;
    private final static int CREATE_NUM_MAX     = 6;
    
    private final static String CREATE_TXT_SQL = 
        "INSERT INTO PropertyConfigurations " +
            "(vprop_id, prop_value, enabled, options_only, regex, regext)" +
        "VALUES (?, ?, ?, ?, ?, ?)";
    private final static int CREATE_TXT_RESTRICT = 4;
    private final static int CREATE_TXT_REGEX = 5;
    private final static int CREATE_TXT_REGEXT = 6;
    
    private final static String CREATE_TOG_SQL = 
        "INSERT INTO PropertyConfigurations " +
            "(vprop_id, prop_value, enabled, on_value, off_value)" +
        "VALUES (?, ?, ?, ?, ?)";
    private final static int CREATE_TOG_ON = 4;
    private final static int CREATE_TOG_OFF = 5;
    
    /** SQL to create a configuration option for a textual visual property. */
    private final static String CREATE_OPTION_SQL = 
        "INSERT INTO ConfigurationOptions " +
            "(vprop_id, option_value)" +
        "VALUES (?, ?)";
    private final static int CREATE_OPT_ID = 1;
    private final static int CREATE_OPT_VAL = 2;
    
    /**
     * Sets the option values for a textual VisualProperty.
     * 
     * @param id The ID of the property whose options are being specified
     * @param options The list of options to specifiy
     * @return <code>true</code> if all options were created, 
     *      <code>false</code> if they were not.
     * @throws SQLException if there is a database access error
     */
    private void createOptions(long id, List<String> options)
        throws SQLException {
        
        if (createOptionStatement == null) {
            createOptionStatement = 
                m_connection.prepareStatement(CREATE_OPTION_SQL);
        }
        
        createOptionStatement.setLong(CREATE_OPT_ID, id);
        for (String option : options) {
            createOptionStatement.setString(CREATE_OPT_VAL, option);
            
            int numRowsChanged = createOptionStatement.executeUpdate();
            if (numRowsChanged != 1) {
                throw new SQLException("Could not create option " +
            		"(" + id + ":" + option + "): No rows were updated");
            }
        }
    }
    
    /**
     * Creates the configuration data for a numeric property
     * 
     * @param id The ID of the property whose configuration is to be created
     * @param config The configuration data to be created
     * @return <code>true</code> if the data was created, <code>false</code>
     *      if it was not
     * @throws SQLException if there is a database access error
     */
    private void createNumericConfig(long id, PropertyConfig config) 
    throws SQLException {
        NumericConfig cfg = (NumericConfig)config;
        if (createNumCfgStatement == null) {
            createNumCfgStatement = 
                m_connection.prepareStatement(CREATE_NUM_SQL);
        }
        
        createNumCfgStatement.setString(CREATE_NUM_UNITS, cfg.getUnits());
        DBUtils.setDouble(createNumCfgStatement, CREATE_NUM_MIN, cfg.getMinRange());
        DBUtils.setDouble(createNumCfgStatement, CREATE_NUM_MAX, cfg.getMaxRange());
        
        createNumCfgStatement.setLong(CREATE_CFG_ID, id);
        createNumCfgStatement.setString(CREATE_CFG_VALUE, config.getDefaultValue());
        createNumCfgStatement.setBoolean(CREATE_CFG_ENABLED, config.isEnabled());
        
        int numberOfRows = createNumCfgStatement.executeUpdate();
        if (numberOfRows != 1) {
            throw new SQLException("Could not create property configuation: " +
                    "No rows updated");
        }
    }
   
    /**
     * Creates the configuration data for a textual property
     * 
     * @param id The ID of the property whose configuration is to be created
     * @param config The configuration data to be created
     * @return <code>true</code> if the data was created, <code>false</code>
     *      if it was not
     * @throws SQLException if there is a database access error
     */
    private void createTextualConfig(long id, PropertyConfig config) 
    throws SQLException {
        TextualConfig cfg = (TextualConfig)config;
        if (createTxtCfgStatement == null) {
            createTxtCfgStatement = 
                m_connection.prepareStatement(CREATE_TXT_SQL);
        }
        
        createTxtCfgStatement.setBoolean(CREATE_TXT_RESTRICT, cfg.areOptionsRequired());
        createTxtCfgStatement.setString(CREATE_TXT_REGEX, cfg.getRegexPattern());
        createTxtCfgStatement.setString(CREATE_TXT_REGEXT, cfg.getRegexModifier());
        
        createTxtCfgStatement.setLong(CREATE_CFG_ID, id);
        createTxtCfgStatement.setString(CREATE_CFG_VALUE, config.getDefaultValue());
        createTxtCfgStatement.setBoolean(CREATE_CFG_ENABLED, config.isEnabled());

        int numberOfRows = createTxtCfgStatement.executeUpdate();
        if (numberOfRows == 1) {
            createOptions(id, cfg.getOptions());
        } else {
            throw new SQLException("Could not create property configuation: " +
                    "No rows updated");
        }
    }
    
    /**
     * Creates the configuration data for a toggled property
     * 
     * @param id The ID of the property whose configuration is to be created
     * @param config The configuration data to be created
     * @return <code>true</code> if the data was created, <code>false</code>
     *      if it was not
     * @throws SQLException if there is a database access error
     */
    private void createToggleConfig(long id, PropertyConfig config) 
    throws SQLException {
        ToggleConfig cfg = (ToggleConfig)config;
        if (createTogCfgStatement == null) {
            createTogCfgStatement = 
                m_connection.prepareStatement(CREATE_TOG_SQL);
        }
        
        createTogCfgStatement.setString(CREATE_TOG_ON, cfg.getOnValue());
        createTogCfgStatement.setString(CREATE_TOG_OFF, cfg.getOffValue());
        
        createTogCfgStatement.setLong(CREATE_CFG_ID, id);
        createTogCfgStatement.setString(CREATE_CFG_VALUE, config.getDefaultValue());
        createTogCfgStatement.setBoolean(CREATE_CFG_ENABLED, config.isEnabled());

        int numberOfRows = createTogCfgStatement.executeUpdate();
        if (numberOfRows != 1) {
            throw new SQLException("Could not create property configuation: " +
            		"No rows updated");
        }
    }
    
    /**
     * Creates the configuration data for a property
     * 
     * @param id The ID of the property whose configuration is to be created
     * @param config The configuration data to be created
     * @return <code>true</code> if the data was created, <code>false</code>
     *      if it was not
     * @throws SQLException if there is a database access error
     */
    private void createConfig(long id, PropertyConfig config) 
        throws SQLException {
        
        String format = config.getFormat().toLowerCase();
        if (format.equals(PropertyType.NUMERIC)) {
            createNumericConfig(id, config);
            
        } else if (format.equals(PropertyType.TEXTUAL)) {
            createTextualConfig(id, config);
            
        } else if (format.equals(PropertyType.TOGGLE)) {
            createToggleConfig(id, config);
            
        } else {
            throw new SQLException("Invalid property type");
        }
    }
    
    /**
     * 
     * @param group
     * @param vprops
     * @return
     * @throws SQLException
     */
    void create(Group group, Map<String, VisualProperty> vprops) 
    throws SQLException {
        if (createVPropStatement == null)
            createVPropStatement = m_connection.prepareStatement(CREATE_SQL, 
                    PreparedStatement.RETURN_GENERATED_KEYS);
        
        // set the group id
        if (group != null) {
            createVPropStatement.setLong(CREATE_GRP_ID, group.getId());
        } else {  
            createVPropStatement.setNull(CREATE_GRP_ID, Types.INTEGER);
        }
        
        // process each property
        for (String vpropName : vprops.keySet()) {
            VisualProperty vprop = vprops.get(vpropName);
            assert vprop.getId() < 0 : "Cannot recreate a visual property.";

            PropertyConfig cfg = vprop.getDefaults();
            createVPropStatement.setString(CREATE_NAME, vpropName);
            createVPropStatement.setString(CREATE_TYPE,   vprop.getType());
            createVPropStatement.setString(CREATE_VALUE,  vprop.getValue());
            createVPropStatement.setString(CREATE_FORMAT, cfg.getFormat());
            DBUtils.setBoolean(createVPropStatement, CREATE_ENABLED, 
                    vprop.getEnabled());

            int numRowsChanged = createVPropStatement.executeUpdate();
            ResultSet results = createVPropStatement.getGeneratedKeys();
            if ((numRowsChanged == 1) && results.next()) {
                long id = results.getLong(1);
                createConfig(id, cfg);
                vprop.setId(id);
            } else {
                throw new SQLException("Could not create property " +
                		"(" + group.getId() + ":" + vpropName + "). " +
        				"No rows changed.");
            }
        }
    }
    
    VisualProperty create(PropertyType type, PropertyConfig config,
            String value, Boolean enabled) 
            throws SQLException  {
        long id = -1;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(CREATE_SQL, 
                    PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(CREATE_TYPE,   type.getId());
            stmt.setNull(CREATE_GRP_ID, Types.INTEGER);
            stmt.setNull(CREATE_NAME, Types.CHAR);
            stmt.setString(CREATE_VALUE,  value);
            stmt.setString(CREATE_FORMAT, type.getFormat());
            DBUtils.setBoolean(stmt, CREATE_ENABLED, enabled);
            
            int numRowsChanged = stmt.executeUpdate();
            ResultSet results = stmt.getGeneratedKeys();
            if ((numRowsChanged == 1) && results.next()) {
                id = results.getLong(1);
                createConfig(id, config);
            } else {
                throw new SQLException("Could not create property " +
                        "(" + type.getName() + ":" + value + "). " +
                        "No rows changed.");
            }
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return new VisualProperty(id, type.getId(), value, enabled, config);
    }
    
    //========================================================================
    // VISUAL PROPERTY RESTORATION METHODS & CONSTANTS
    //========================================================================
    
    // symbolic constants for column names
    private final static String ID_COL        = "vprop_id";
    private final static String PROP_NAME_COL = "prop_name";
    private final static String TYPE_COL      = "type_id";
    private final static String VALUE_COL     = "prop_value";
    private final static String ENABLED_COL   = "enabled";
    private final static String FORMAT_COL    = "format";
    
    private final static String D_VALUE_COL   = "d_value";
    private final static String D_ENABLED_COL = "d_enabled";
    private final static String UNITS_COL     = "units";
    private final static String MIN_COL       = "minimum";
    private final static String MAX_COL       = "maximum";
    private final static String RESTRICT_COL  = "options_only";
    private final static String REGEX_COL     = "regex";
    private final static String REGEXT_COL    = "regext";
    private final static String ON_COL        = "on_value";
    private final static String OFF_COL       = "off_value";
    
    private final static String OPT_VALUE_COL = "option_value";

    /** Field names for use in SELECT query. */
    private final static String PROP_FIELDS = 
        " vp.vprop_id, vp.type_id, vp.prop_name, vp.prop_value, vp.enabled, vp.format, " +
        " pc.prop_value as d_value, pc.enabled as d_enabled," +
        " pc.units, pc.minimum, pc.maximum," +
        " pc.options_only, pc.regex, pc.regext," +
        " pc.on_value, pc.off_value";
    
    /** SQL to restore all options for a visual property. */
    private final static String RESTORE_OPTIONS_SQL = 
        "SELECT option_value" +
        "  FROM ConfigurationOptions" +
        " WHERE vprop_id = ?";
    private final static int RESTORE_OPT_ID = 1;
    
    /** SQL to restore a single visual property from the database. */
    private final static String RESTORE_SQL = 
        "SELECT " + PROP_FIELDS +
        "  FROM VisualProperties AS vp " +
        "       LEFT JOIN PropertyConfigurations AS pc USING (vprop_id)" +
        " WHERE vp.vprop_id = ?";
    
    /** SQL to restore the visual properties for a group from the database. */
    private final static String RESTORE_BY_GRP_SQL = 
        "SELECT " + PROP_FIELDS +
        "  FROM VisualProperties AS vp " +
        "       LEFT JOIN PropertyConfigurations AS pc USING (vprop_id)" +
        " WHERE vp.group_id = ?";
    
    /** Specifies the parameter index for the id of the group/property 
     *  to restore. */
    private final static int RESTORE_ID = 1;
    
   
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     */
    private List<String> restoreOptions(long id) throws SQLException {
        List<String> options = new ArrayList<String>();
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(RESTORE_OPTIONS_SQL);
            stmt.setLong(RESTORE_OPT_ID, id);
            ResultSet results = stmt.executeQuery();
            while (results.next()) {
                options.add(results.getString(OPT_VALUE_COL));
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
        
        return (options.size() > 0) ? options : null;
    }
   
    /**
     * 
     * @param results
     * @return
     * @throws SQLException
     */
    private Map<String, Object> restoreNumericConfig(ResultSet results) 
            throws SQLException {
        String value  = results.getString(D_VALUE_COL);
        Boolean enabled = results.getBoolean(D_ENABLED_COL);
        enabled = (results.wasNull()) ? null : enabled;
        
        String units = results.getString(UNITS_COL);
        
        Double min = results.getDouble(MIN_COL);
        min = (results.wasNull()) ? Double.NaN : min;
        
        Double max = results.getDouble(MAX_COL);
        max= (results.wasNull()) ? Double.NaN : max;

        return NumericConfig.getMomento(value, enabled, units, min, max);
    }
    
    /**
     * 
     * @param results
     * @param id
     * @return
     * @throws SQLException
     */
    private Map<String, Object> restoreTextualConfig(ResultSet results, long id) 
            throws SQLException {
        String value  = results.getString(D_VALUE_COL);
        Boolean enabled = results.getBoolean(D_ENABLED_COL);
        enabled = (results.wasNull()) ? null : enabled;
        
        Boolean restrict = results.getBoolean(RESTRICT_COL);
        String regex = results.getString(REGEX_COL);
        String regext = results.getString(REGEXT_COL);
        
        List<String> options = restoreOptions(id);
        
        return TextualConfig.getMomento(value, enabled, 
                regex, regext, options, restrict);
    }
    
    /**
     * 
     * @param results
     * @return
     * @throws SQLException
     */
    private Map<String, Object> restoreToggleConfig(ResultSet results) 
        throws SQLException {
        String value  = results.getString(D_VALUE_COL);
        Boolean enabled = results.getBoolean(D_ENABLED_COL);
        enabled = (results.wasNull()) ? null : enabled;
        
        return ToggleConfig.getMomento(value, enabled, 
                results.getString(ON_COL), 
                results.getString(OFF_COL));
    }
    
    /**
     * 
     * @param results
     * @return
     * @throws SQLException
     * @throws BadDataException
     */
    private Map<String, Object> restore(ResultSet results) 
            throws SQLException {
        
        long id         = results.getLong(ID_COL);
        String type     = results.getString(TYPE_COL);
        String value    = results.getString(VALUE_COL);
        String fmt      = results.getString(FORMAT_COL);
        Boolean enabled = results.getBoolean(ENABLED_COL);
        enabled = (results.wasNull()) ? null : enabled;
        
        Map<String, Object> conf = null;
        if (fmt.equals(PropertyType.NUMERIC)) {
            conf = restoreNumericConfig(results);
        } else if (fmt.equals(PropertyType.TEXTUAL)) {
            conf = restoreTextualConfig(results, id);
        } else if (fmt.equals(PropertyType.TOGGLE)) {
            conf = restoreToggleConfig(results);
        } else {
            throw new SQLException(
                    "Invalid property format: " + fmt);
        }
            
        return VisualProperty.getMomento(id, type, value, enabled, conf);
    }
    
    /**
     * 
     * @param groupId
     * @return
     */
    Map<String, Object> restoreGroupProperties(long groupId)
    throws SQLException {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        Map<String, Object> prop = null;
    
        if (restoreByGroup == null) {
            restoreByGroup = m_connection.prepareStatement(RESTORE_BY_GRP_SQL);
        }
        restoreByGroup.setLong(RESTORE_ID, groupId);
        
        ResultSet results = restoreByGroup.executeQuery();
        while (results.next()) {
            String name = results.getString(PROP_NAME_COL);
            prop = restore(results);
            properties.put(name, prop);
        }
    
        return properties;
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws SQLException
     * @throws BadDataException
     */
    VisualProperty restore(long id) 
        throws SQLException, BadDataException {
        
        VisualProperty prop = null;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(RESTORE_SQL);
            stmt.setLong(RESTORE_ID, id);
            
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                prop = new VisualProperty(restore(results));
            }
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return prop;
    }
    
    //========================================================================
    // VISUAL PROPERTY UPDATE METHODS
    //========================================================================
    
    boolean update(VisualProperty vprop) throws SQLException {
        // XXX Implement optimistic locking
        boolean success = false;
        PreparedStatement stmt = null;
        try {
            stmt = m_connection.prepareStatement(UPDATE_SQL);
            stmt.setLong(UPDATE_ID, vprop.getId());
            stmt.setString(UPDATE_VALUE, vprop.getValue());
            DBUtils.setBoolean(stmt, UPDATE_ENABLED, vprop.getEnabled());
            
            int numRowsChanged = stmt.executeUpdate();
            if (numRowsChanged == 1) {
                success = true;
            }
        } finally {
            if (stmt != null) 
                stmt.close();   
        }
        
        return success;
    }
}
