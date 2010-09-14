/* We're going to put our source script in this file to minimize the amount of 
 *  code in our main page. */
(function() {   /* don't polute the main namespace.  */

var ImageScroller = null;
var Facsimile     = null;

function main() {
    ImageScroller = IDCH.images.ImageScroller;
    Facsimile = IDCH.afed.Facsimile;
    
    // construct the ImageScroller
    var resolver = new IDCH.images.APIResolver($P('idch.afed.facsim.image.servlet'));
    var scroller = new ImageScroller($("afed-imstream"), {
            imageProperties : { width   : 152, height  : 146 }, 
            resolver : resolver,
            format : "small"
    });
    scroller.show();
        
    var id = 11237;
    Facsimile.get(id, function(facs, msg) {
        if (facs === false) {
            var msg = "Could not retrieve fasimile: " + id;
            $warn(msg);
            
            return;
        }  
        
        var collations = facs.getCollations();
        var images = collations[0].getImages();
        scroller.setImages(images);
        
        var proxy = scroller.getImageProxy(34);
        alert(proxy.getImageSrc("large"));
        
//        scroller.add(20092, 3);
//        YAHOO.lang.later(3000, null, function() {
//            scroller.remove(scroller.indexOf(7375));
//        });
    }); 
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
        IDCH.load(["images-scroller", "images", 'afed'], true, main, onFail);
    });
});

})();
