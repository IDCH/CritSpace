/* We're going to put our source script in this file to minimize the amount of 
 *  code in our main page. */
(function() {   /* don't polute the main namespace.  */

var ImageScroller = null;
var Facsimile     = null;

function testBasicProxy() {
    var image = "/CritSpace/images/tzi/NT1";
    var resolver = new IDCH.images.Resolver();
    var proxy    = new IDCH.images.ImageProxy(image, resolver);
    
    proxy.hasRepresentation("tzi", function(success) {
        if (success) {
            var tziSource = proxy.getTziSource();
            tziSource.on("ready", function() {
                alert("TziSource is ready.");
            });
            tziSource.configure();
        } else {
            alert("Failed to find image: " + image + " -- Status: " + url);
        }
    });
}

function testBasicResolver() {
    var image = "/CritSpace/images/tzi/NT1";
    var resolver = new IDCH.images.Resolver();
    
    resolver.hasRepresentation("tzi", image, function(success, url, mime) {
        if (success) {
            var tziSource = resolver.getTziSource(image);
            tziSource.on("ready", function() {
                alert("TziSource is ready.");
            });
            tziSource.configure();
        } else {
            alert("Failed to find image: " + image + " -- Status: " + url);
        }
    });
}

function testAPIResolver() {
    var image = "11404";
    var resolver = new IDCH.images.APIResolver("/AFED/image");
    
    resolver.hasRepresentation("small", image, function(success, url, mime) {
        if (success) {
            alert(resolver.getImageSrc(image, "small"));
        } else {
            alert("Failed to find image: " + image + " -- Status: " + url);
        }
    });
}
function main() {
    testBasicProxy();
}

// ------------------------------------------------------------------------
// INITIALIZE THE ENVIRONMENT ON PAGE LOAD
// ------------------------------------------------------------------------

YAHOO.util.Event.addListener(window, "load", function() {
    $P('yui.base.url',     "/CritSpace/yui/yui_2.8.0/");
    $P('idch.scripts.url', "/CritSpace/scripts/IDCH/");
    
    $P('idch.afed.facsim.servlet', "/AFED/facsimile");
    $P('idch.afed.facsim.image.servlet', "/AFED/image");
    
    function onFail() { alert("failed to load modules."); }
    
    // load the logger
    IDCH.configLogger("logreader", function() {
        IDCH.load(['images', 'tzivi-images'], true, main, onFail);
    });
});

})();
