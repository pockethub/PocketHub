window.onload = function(){
    addTouchEvents(document.getElementsByTagName("pre"));
    addTouchEvents(document.getElementsByTagName("table"));
};

function addTouchEvents(elements){
    for(var i = 0; i < elements.length; i++){
        elements[i].addEventListener("touchstart", touchStart, false);
        elements[i].addEventListener("touchend", touchEnd, false);
    }
}

function touchStart(event){
    //if(event.target.scrollWidth > event.target.clientWidth)
        Readme.startIntercept();
}

function touchEnd(event){
    //if(event.target.scrollWidth > event.target.clientWidth)
        Readme.stopIntercept();
}