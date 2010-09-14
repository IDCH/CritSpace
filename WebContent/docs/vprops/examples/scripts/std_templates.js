/* Title: groups.js
 * Date: 26 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 */


(function() {
    var Y = YAHOO, 
        I = IDCH,
        GroupTemplateRegistry;
    
function main() {
    var Connect = Y.util.Connect,
        JSON    = Y.lang.JSON,
        PM      = I.vprops.PropertyManager,
        Repo    = I.vprops.BasicRepository,
        
        url     = "/CritSpace/test/vpropRepo/PropertyTypes.json";
    
    var repo = new Repo (
            "/CritSpace/vprops/types",
            "/CritSpace/vprops/properties",
            "/CritSpace/vprops/groups",
            5000);
    
    var pm = new PM({ repository : repo });
    
    var g_propId = 1;
    
    function onPMReady() {
//        var // repo, type, 
//            // types = pm.listPropertyTypes(),
//            i = types.length;
        
        GroupTemplateRegistry  = I.vprops.GroupTemplateRegistry
        var template = GroupTemplateRegistry.get(IDCH.vprops.templates.BASIC);
        pm.createGroup(template, {
            success : function(group) {
                alert(JSON.stringify(group));
            },
          
            failure : function(msg) {
                alert("failed: " + msg)
            }
        });
        
//        while (i--) {
//            type = pm.getPropertyType(types[i]);
//            alert(type);
////            repo.createPropertyType(type);
//        }
    }

    pm.on("ready", onPMReady);
    
}

function loadVProps() {
    // We're going to work with these independently to ease development until 
    // we're able to roll them into a single file.
    var js = ["vprops/vprops.js",
              "vprops/repository.js",
              "vprops/templates.js"];
    
    var i = 0;
    while (i < js.length) { 
        js[i] = "/CritSpace/scripts/IDCH/" + js[i];
        i++;
    }
    
    Y.util.Get.script(js, {
        onSuccess : main
    });
}

// configure source code dependencies
YAHOO.util.Event.addListener(window, "load", function() {
    // Tell the IDCH loader where to find the base YUI and IDCH scripts
    $P('yui.base.url',               "/CritSpace/yui/yui_2.8.0/");
    $P('idch.scripts.url',           "/CritSpace/scripts/IDCH/");
    
    // where the panels JSON descriptor can be found
    $P('idch.crispace.config.panels', "panels.json");
    
    // load the logger
    IDCH.configLogger("logger", function() {
        function onFail() { 
            alert("Failed to load IDCH modules."); 
        }
        
        IDCH.load(["get", "dom", "event", "json", "connection"], true, loadVProps, onFail);
    });
});


})();