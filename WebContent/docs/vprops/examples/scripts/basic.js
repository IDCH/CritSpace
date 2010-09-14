/* Title: basic.js
 * Date: 26 July 2010
 * Author: Neal Audenaert (neal@idch.org)
 * Copyright: Institute for Digital Christian Heritage (IDCH) 
 *            All Rights Reserved.
 *            
 */


(function() {
    var Y = YAHOO, 
        I = IDCH;

function main() {
    var Connect = Y.util.Connect,
        JSON    = Y.lang.JSON,
        PM      = I.vprops.PropertyManager,
        Repo    = I.test.vprops.StaticRepository;
        
        url     = "/CritSpace/test/vpropRepo/PropertyTypes.json";
    var pm = new PM({ repository : new Repo(url, 5000) });
    
    var g_propId = 1;
    
    pm.on("ready", function() {
        var type, cfg, prop,
            types = pm.listPropertyTypes(),
            i = types.length;
        
        ct = 0;
        while (i--) {
            type = pm.getPropertyType(types[i]);
            pm.createProperty(type, null, null, {
                success : function(prop) {
                    ct++;
//                    alert(prop.toString());
                },
                
                failure : function() {
                    alert("Could not create visual property: " + type);
                }
            });
        }
        
        Y.lang.later(1000, null, function() {
            alert(ct == types.length);
        });
    });
    
}

function loadVProps() {
    // We're going to work with these independently to ease development until 
    // we're able to roll them into a single file.
    var js = ["vprops/vprops.js",
              "vprops/StaticRepository.js"];
    
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