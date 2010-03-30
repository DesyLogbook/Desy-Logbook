/**
 * Functions are used to resize the elements in the
 * elog.jsp file.
 */

window.onresize = resizeAll;

function resizeAll()
{
    resizeLeftElem();
    resizeTopElem();
    resizeIframe();
}

function resizeLeftElem()
{
    $('leftElem').setStyle({height:document.viewport.getHeight()-130+'px'});
    /*var height = document.viewport.getHeight()-130;
    new Effect.Morph($('leftElem'),
       {style:'height:'+height+'px',
         duration:1}); */
}

function resizeTopElem()
{
    $('topElem').setStyle({width:document.viewport.getWidth()-20+'px'});

    /*var width = document.viewport.getWidth()-20;
    new Effect.Morph($('topElem'),
       {style:'width:'+width+'px',
         duration:1}); */
}

function resizeIframe()
{
    $('contentFrame').setStyle({height:document.viewport.getHeight()-130+'px',
                                width:document.viewport.getWidth()-170+'px'});
    /*var height = document.viewport.getHeight()-130;
    var width = document.viewport.getWidth()-170;
    new Effect.Morph($('contentFrame'),
       {style:'width:'+width+'px;height:'+height+"px",
         duration:1}); */
}

