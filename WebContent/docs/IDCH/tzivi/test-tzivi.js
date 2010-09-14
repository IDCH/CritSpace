/* Title: test-tzivi.js
 *  Version: 
 *  Author: Neal Audenaert (neal@idch.org)
 *  Copyright: Institute for Digital Christian Heritage (IDCH) 
 *             All Rights Reserved.
 *  
 *  Description: Tests the Tzivi framework.
 * 
 */

(function() {
//-----------------------------------------------------------------------------
// Layer Tests
//-----------------------------------------------------------------------------
    
    
var ViewportTestCase = new YAHOO.tool.TestCase({
    
    name: "Viewport Tests",
 
    //---------------------------------------------
    // Setup and tear down
    //---------------------------------------------
 
    setUp : function () {
        this.data = { name : "Nicholas", age : 28 };
    },
 
    tearDown : function () {
        delete this.data;
    },
 
    //---------------------------------------------
    // Tests
    //---------------------------------------------
 
    testName: function () {
        YAHOO.util.Assert.areEqual("Nicholas", this.data.name, "Name should be 'Nicholas'");
    },
 
    testAge: function () {
        YAHOO.util.Assert.areEqual(28, this.data.age, "Age should be 28");
    }    
    
    // TEST EVENTS
    // Test that the events fire when they are supposed to and that the 
    // resulting event handlers are invoked with the correct arguments. 
    // Test out of bounds ranges for the zoom and pan events.
});

/** 
 * A proxy class to stand in for the <code>Layer</code> class implemented
 * in the core tzivi module. This allows us to test the core 
 * <code>Viewport</code> implementation and to ensure that it correctly 
 * invokes the <code>Layer</code> API.
 * 
 * @class LayerTestProxy
 */
LayerTestProxy = {
        
};



//-----------------------------------------------------------------------------
// Layer Tests
//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------
// TziSource Tests
//-----------------------------------------------------------------------------

var TziSourceTestCase = new YAHOO.tool.TestCase({
    
    name: "TziSource Tests",
 
    //---------------------------------------------
    // Setup and tear down
    //---------------------------------------------
 
    setUp : function () {
        this.data = { name : "Nicholas", age : 28 };
    },
 
    tearDown : function () {
        delete this.data;
    },
 
    //---------------------------------------------
    // Tests
    //---------------------------------------------
 
    testName: function () {
        YAHOO.util.Assert.areEqual("Nicholas", this.data.name, "Name should be 'Nicholas'");
    },
 
    testAge: function () {
        YAHOO.util.Assert.areEqual(28, this.data.age, "Age should be 28");
    }    
    
    // TEST EVENTS
    // Test that the events fire when they are supposed to and that the 
    // resulting event handlers are invoked with the correct arguments. 
    // Test out of bounds ranges for the zoom and pan events.
}); 
})();