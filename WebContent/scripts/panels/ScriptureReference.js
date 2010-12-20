
IDCH.namespace("nt");
(function() {
    
var NT_BOOKS = [
                 "Matthew",
                 "Mark",
                 "Luke",
                 "John",
                 "Acts",
                 "Romans",
                 "I Corinthians"
                 
];

/**
 * Implements a reference to a particular book, chapter or verse in the 
 * New Testament. This class also provides static utility methods to lookup 
 * verses, normalize references and determine the validity of different 
 * references.  
 */
function ScriptureReference() {
    
    
   
}
    
ScriptureReference.prototype = {
    book : "Matthew",
    chapter : 1,
    verse : 1,
    
    /**
     * Returns the next verse.
     */
    next : function() {
        
    },
    
    /**
     * Returns the next verse.
     */
    previous : function() {
        
    },
}
})();