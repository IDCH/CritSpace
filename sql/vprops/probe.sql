-- Executes Select queries against all database tables to make sure that they
-- exist and that they have the expected columns

SELECT type_id, css, name, description, format, config FROM PropertyTypes;
SELECT group_id, parent_group, group_type FROM PropertyGroups;
SELECT vprop_id, group_id, type_id, prop_value, enabled FROM VisualProperties;
SELECT vprop_id, prop_value, enabled, units, minimum, maximum, onValue, offValue, options_only, regex, regext FROM ProperityConfigurations;  
SELECT vprop_id, option_value FROM ConfigurationOptions;