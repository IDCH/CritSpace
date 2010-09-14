// Right now this is just a test framework to think through how we might 
// build out a modules framework.

var module = {
    base_uri : 'js.idch.org/idch/dev',      /* The base URI that we'll use to lookup and load files. */
    version  : '0.2',
    
    images : {
        name : 'images',
        namespace : 'IDCH.images',
        dir  : '/images',
        
        classes : {
            ScrollViewer : {
                scripts : ['ScrollPane.js'],
                css     : [],
                dependancies : ['', '']
}
    },
    
    tzivi : {
        
    }
}
};