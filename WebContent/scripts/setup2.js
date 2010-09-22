/* Title: image.js
 * Date: 23 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 */

(function() {
    
var panelDefns = [
      { js   : ["scripts/TextPanel.js"],
        type : "org.idch.examples.TextPanel",
        menu : "Text Panel",
        desc : "A simple panel for displaying some text.",
        depends : []
      }];

function main() {
//    alert("success");
}

function config() {
    var CritSpace = IDCH.critspace.CritSpace,
    	config = {
	    	panels : panelDefns,
	    	vpropsRepository : new IDCH.vprops.BasicRepository(
		            $P(CritSpace.VPROP_URL_TYPES), $P(CritSpace.VPROP_URL_GROUP),
		            $P(CritSpace.VPROP_URL_PROPS), $P(CritSpace.VPROP_TIMEOUT))
	    };
    
    CritSpace.init(config,  {
	    	success : main,
	    	failure : function(msg) {
	    		alert("Failed to initialize CritSpace: " + msg);
	    	}
	    });
}


// configure source code dependencies
YAHOO.util.Event.addListener(window, "load", function() {
    // Tell the IDCH loader where to find the base YUI and IDCH scripts
    $P('yui.base.url',               "/js/yui/yui_2.8.1/");
    $P('idch.scripts.url',           "/js/IDCH/");
    
    $P('idch.tzivi.sliderthumb',         "/CritSpace/assets/thumb-v.png");
    $P('idch.afed.facsim.servlet',       "/AFED/facsimile");
    $P('idch.afed.facsim.image.servlet', "/AFED/image");
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.critspace.urls.ws",     '/CritSpace/workspaces');
    $P("idch.critspace.urls.panels", '/CritSpace/panels');
    
    $P("idch.vprops.urls.types",  "/CritSpace/vprops/types");
    $P("idch.vprops.urls.groups", "/CritSpace/vprops/groups");
    $P("idch.vprops.urls.props",  "/CritSpace/vprops/properties");
    $P("idch.vprops.timeout",     5000);
    
    // load the logger
    IDCH.configLogger("logger", function() {
    	var modules = ["critspace", "images-filmstrip", "tzivi-panel", "afed"];

    	function onFail() {
    		// TODO Need a better implementation for this
    		alert("Failed to load IDCH modules."); 
    	}
    	
    	IDCH.load(modules, true, config, onFail); 
    });
});


})();