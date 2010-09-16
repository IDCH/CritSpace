/* Title: TextPanel.js
 * Date: 23 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 */

IDCH.namespace("examples.Panels");
(function() {

var util          = YAHOO.util,
    lang          = YAHOO.lang,
    dom           = util.Dom;

//========================================================================= 
// PANEL IMPLEMENTATION
//=========================================================================
var PANEL_TYPE = "org.idch.examples.TextPanel";

/** 
 * Demonstrates a basic panel implementation. This panel will display user 
 * specified text.
 * 
 * @class TextPanel
 * @namespace IDCH.examples.Panels
 * @private
 * @constructor
 * 
 * @param m_panel { Panel } The base panel objecjt that this method extends.
 * @param m_cfg { Object } The user supplied configuration object.
 */
function TextPanel(m_panel, m_cfg) {
    m_panel.configure(m_cfg);
    
    var m_body = m_panel.getBody();
    
    m_body.innerHTML = m_cfg.text || "Why don't you love me?";
}

// PUBLICIZE & ATTACH TO WORKSPACE

/**
 * Symbolic constant for the TextPanel type. 
 * @property TEXT_PANEL
 * @public
 */
IDCH.examples.Panels.TEXT_PANEL = PANEL_TYPE;

// finally, register this panel constructor with the workspace
IDCH.critspace.PanelRegistry.register(PANEL_TYPE, TextPanel);

})();
