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
    
function StandardProperties() {
    var template = GroupTemplateRegistry.get();
    
    var position = GroupTemplateRegistry.get(POSITION_TEMPLATE);
    template.defineSubGroup("position", position);
    
    return template;
}

function PositionTemplate() {
    var template = GroupTemplateRegistry.get();
    
    template.defineProperty("top", "top", { }, { });
    template.defineProperty("left", "left", { }, { });
    
    template.adjustGroup = function(group) {
        var left = group.left,
            top  = group.top;
        
        // Check that this group is valid
        if (!Y.lang.isValue(top) || !Y.lang.isValue(left)) {
            throw "Invalid property group.";
        }
        
        //================================================
        // Attach Functions
        //================================================
        
        group.set = function(x, y) {
            left.set(x);
            top.set(y);
        };
        
        group.get = function() {
            return [left.get(), top.get()];
        };
        
        /*
         * Attach a custom <code>apply</code> function to the group.
         */
        group.apply = function(el, preview) {
            var x = (preview) ? left.get() : left.getValue(),
                y = (preview) ? top.get() : top.getValue();
            
            if (!Y.lang.isNumber(x)) 
                x = el.offsetLeft;
            if (!Y.lang.isNumber(y)) 
                y = el.offsetTop;
            
            Y.dom.setXY(el, [x, y]);
        };
    };
    
    return template;
}

var STANDARD_TEMPLATE = "org.idch.vprops.templates.Standard";
var POSITION_TEMPLATE = "org.idch.vprops.templates.Position";


function main() {
    var Connect = Y.util.Connect,
        JSON    = Y.lang.JSON,
        PM      = I.vprops.PropertyManager,
        Repo    = I.test.vprops.StaticRepository;
        
        url     = "/CritSpace/test/vpropRepo/PropertyTypes.json";
        
    GroupTemplateRegistry = I.vprops.GroupTemplateRegistry;
    GroupTemplateRegistry.register(POSITION_TEMPLATE, PositionTemplate);
    GroupTemplateRegistry.register(STANDARD_TEMPLATE, StandardProperties);
    
    var pm = new PM({ repository : new Repo(url, 5000) });
    
    var g_propId = 1;
    
    function onPMReady() {
        var template = GroupTemplateRegistry.get(STANDARD_TEMPLATE);
//        alert(JSON.stringify(template.toJSON()));
        pm.createGroup(template, {
            success : function(group) {
                alert(JSON.stringify(group));
            },
            
            failure : function() {
                
            }
        });
        
//        alert("ready");
    }
    
    pm.on("ready", onPMReady);
    
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