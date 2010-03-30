/*-------------------- COMMAND CONSTANTS ---------------*/
var CMD_START_JOB = "startJob";
var CMD_STOP_JOB = "stopJob";
var CMD_SAVE_CONF = "saveConf";
var CMD_SAVE_WHOLE_CONF = "saveWholeConf";
var CMD_RELOAD_TREE = "reloadTree";
var CMD_CREATE_TREE_DATABASE = "createTreeDatabase";
var CMD_RELOAD_LOGBOOK = "reloadLogbook";
var CMD_RELOAD_MANAGER = "reloadManager";


/*-------------------- CONSTANT VALUES -----------------*/
// time before ajax object is reset
var _ajaxTimeoutInMS = 8000;
// id of the root html element
var _rootId = "root";
// path to the servlet
var _servletPath = "Manager";
var _infoIcon = "images/info.gif";
var _closeIcon = "images/close.gif";
var _openIcon = "images/open.gif";

// name of the communication cookie
var _cookieName = "rqstCookie";
// id of the open/Close button
var _buttonId = "OCbutton";


/*-------------------- VARIABLES -----------------------*/
// ajax object
var MyAjax;
// flag if an ajax request is in progress
var isAjaxOccupied = false;
// flag if client is an internet explorer or not
var isIE;
// variable to store a running timeout function
var timeout;
// pointer to the actual element
var actualNode;
// debug variable
var debugText = "";

/*---------------- AJAX COMMUNICATION ------------------*/

/*********************************************************
* initialization
*********************************************************/
function init()
{
    isIE = document.all?true:false;
	actualNode= $(_rootId);
	ajaxSendRequest("empty");
}

/*********************************************************
* sends an ajax-request for tree data
* havePath is the path that is already at the client
* wantPath empty if Client wants just one subfolder
* or contains the path to the folder which user wants
*********************************************************/
function ajaxSendRequest(cValue)
{
	debug("sende request "+cValue);
	if (!isAjaxOccupied)
	{
		isAjaxOccupied = true;
		setCookie(_cookieName, cValue);
        var path = _servletPath;
		if (isIE) path = path+"?IE_Sux="+new Date();
		new Ajax.Request(path,
  		{
    		method:'GET',
    		onComplete: function(transport)
    		{
    			//debugText="";
    			//printResponse(transport.responseXML, "-");
    			//alert(document.cookie +"\n"+debugText);
                $("time").innerHTML = (new Date()).toString();
      			ajaxResponseHandle(transport.responseXML);
    		},
    		onFailure: function()
    		{ 
    			alert('Error. No ajax response.');
    		}
  		});
	}
	else
	{
		alert("ajax was occupied");
	}
}

/*********************************************************
* Eventhandler launched when ajax data is received
*********************************************************/
function ajaxResponseHandle(data)
{
	if(data==null || data.firstChild==null)
	{
		// ajax returned null
	}		
	else
	{
		var tempNode = data.firstChild;
		// MSIE has a processing instruction node on front
		if (tempNode.nodeType == 7)
		{				
			tempNode = tempNode.nextSibling;
		}
		handleData(tempNode,actualNode);
	}
	isAjaxOccupied = false;
	//window.clearTimeout(timeout);
   	deleteCookie(_cookieName);
}

/*********************************************************
* Eventhandler launched when ajax response does not
* arrive in time
*********************************************************/
function ajaxTimeout()
{
	ajax.abort();
	deleteCookie(_cookieName);
	isAjaxOccupied = false;
	//alert("ajax timeout (todo implement abort function)");
}

/*********************************************************
* deletes the cookie when called
*********************************************************/
function deleteCookie( cName ) 
{
	document.cookie = cName + " = empty; expires=Fri, 02 Jan 1970 00:00:00 UTC;";
}

/*********************************************************
* sets a cookie with a given value
*********************************************************/
function setCookie( cName, cValue)//, expires, path, domain, secure ) 
{
	var expires_date = new Date( new Date().getTime() + _ajaxTimeoutInMS );
	document.cookie = cName + "=" + cValue +
						";expires =" + expires_date;
}

/*********************************************************
* handles the response data from the manager servlet.
* data is a xml node
* localActualNode is the html element where the data
* should be appended
*********************************************************/
function handleData(data, localActualNode)
{

	data = data.firstChild;
	//if (localActualNode==null) localActualNode=$(_rootId);

	var idPrefix = localActualNode.readAttribute('id')+"/";
	if(idPrefix==_rootId+"/") idPrefix="";
	
	while (data != null)
	{
		debug("eintrag : "+data.nodeName);	
		switch (data.nodeName)
		{
			case "entry" :
				var newNode = mkEntry(data, idPrefix);
				localActualNode.insert(newNode);
				// the root node should not be closed on click
				if(idPrefix!="")
				{
					changeToOpen(localActualNode);
				}
				//handle data does data.firstChild
				handleData(data, newNode);
				break;
			case "label" :
				localActualNode.insert(mkLabel(data));
				break;
			case "status" :
				localActualNode.insert(mkActive(data));
				break;
			case "command" :
				localActualNode.insert(mkCommand(data, idPrefix));
				break;
			case "edit" :
				localActualNode.insert(mkEdit(data, idPrefix));
				break;
			case "info" :
				localActualNode.insert(mkInfo(data, idPrefix));
				break;
			case "link" :
				localActualNode.insert(mkLink(data));
				break;
             case "textArea" :
                localActualNode.insert(mkTextArea(data,idPrefix));
				break;
		}
		data=data.nextSibling;
	}	
}

/*********************************************************
* node is a html element and its "open" button
* is changed to a "close" button
*********************************************************/
function changeToOpen(node)
{
	var id  = node.readAttribute('id');
	var runner = node.down();
	while(runner!=null && isOpenCloseButton(runner))
	{
		runner=runner.next();
	}
	if (runner!=null)
	{
		runner.writeAttribute("href","javascript:close('"+id+"')");
		runner.down().writeAttribute("src",_closeIcon);
	}
}

/*********************************************************
* node is a html element and its "close" button
* is changed to a "open" button
*********************************************************/
function changeToClose(node)
{
	var id  = node.readAttribute('id');
	var runner = node.down();
	while(runner!=null && isOpenCloseButton(runner))
	{
		runner=runner.next();
	}
	if (runner!=null)
	{
		runner.writeAttribute("href", "javascript:open('"+id+"')");
		runner.down().writeAttribute("src",_openIcon);
	}
}

/*********************************************************
* create an "entry" html element
*********************************************************/
function mkEntry(data, idPrefix)
{
	var div = new Element('div');
	var id = idPrefix+data.getAttribute("id");
	div.writeAttribute("id", id);
	div.writeAttribute("class","entry");
	
	if(data.getAttribute("hasSub")!=null)
	{
		var a = new Element('a');
		a.writeAttribute("href", "javascript:open('"+id+"')");
		a.writeAttribute("class", "OCButtonLink");
		var img = new Element("img");
		img.writeAttribute("class", "OCButton");
		img.writeAttribute("src",_openIcon);
		a.insert(img);
		div.insert(a);		
	}
	return div;
}

/*********************************************************
* create a "label" html element
*********************************************************/
function mkLabel(data)
{
	var elem = new Element("span");
	elem.writeAttribute("class","label");
	var content = "";
	if (data.firstChild!=null)
	{
		content = data.firstChild.nodeValue;
	}
	
	elem.insert(content);
	return elem;
}

/*********************************************************
* create a "link" html element
*********************************************************/
function mkLink(data)
{
	var elem = new Element("a");
	var link = data.getAttribute("link");
	elem.writeAttribute("class","linkElem");
	elem.writeAttribute("href",link);
	var content = "";
	if (data.firstChild!=null)
	{
		content = data.firstChild.nodeValue;
	}
	
	elem.insert(content);
	return elem;
}

/*********************************************************
* create an "active" html element that shows status
* running or stopped
*********************************************************/
function mkActive(data)
{
	var elem = new Element("span");	
	var isRunning = data.getAttribute("running");
	if (isRunning == "true")
	{
		elem.insert("on");
		elem.writeAttribute("class","active");
		elem.writeAttribute("title","running ...");
	}
	else
	{
		elem.insert("off");
		elem.writeAttribute("class","inactive");
		elem.writeAttribute("title","shut down ...");
	}
	
	return elem;
}

/*********************************************************
* create a "command" html element
*********************************************************/
function mkCommand(data, idPrefix)
{
	var code = data.getAttribute("code");
    var elem = new Element("a");
	if ((code==CMD_START_JOB) || (code==CMD_STOP_JOB))
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);		
		elem.writeAttribute("href","javascript:switchJobStatus('"+idPrefix+code+"')");
		return elem;
	}
	if (code==CMD_SAVE_CONF)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);		
		elem.writeAttribute("href","javascript:saveConf('"+idPrefix+CMD_SAVE_CONF+"')");
		return elem;
	}
	if (code==CMD_RELOAD_TREE)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);		
		elem.writeAttribute("href","javascript:reloadTree('"+idPrefix+CMD_RELOAD_TREE+"')");
		return elem;
	}
	if (code==CMD_CREATE_TREE_DATABASE)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);		
		elem.writeAttribute("href","javascript:createTreeDatabase('"+idPrefix+CMD_CREATE_TREE_DATABASE+"')");
		return elem;
	}
	if (code==CMD_RELOAD_LOGBOOK)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);		
		elem.writeAttribute("href","javascript:reloadLogbook('"+idPrefix+CMD_RELOAD_LOGBOOK+"')");
		return elem;
	}
    if (code==CMD_RELOAD_MANAGER)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);
		elem.writeAttribute("href","javascript:reloadManager('"+idPrefix+CMD_RELOAD_MANAGER+"')");
		return elem;
	}
    if (code==CMD_SAVE_WHOLE_CONF)
	{
		//var elem = new Element("a");
		elem.writeAttribute("class","command");
		elem.insert(data.firstChild.nodeValue);
		elem.writeAttribute("href","javascript:saveWholeConf('"+idPrefix+CMD_SAVE_WHOLE_CONF+"')");
		return elem;
	}
	
	return new Element("span");
}

/*********************************************************
* create an "edit" html element
*********************************************************/
function mkEdit(data,idPrefix)
{
    var elem = new Element("input");
	elem.writeAttribute("class","edit");
	elem.writeAttribute("type","text");
	elem.writeAttribute("size","70");
	elem.writeAttribute("value",data.firstChild.nodeValue);
	elem.writeAttribute("id",idPrefix+CMD_SAVE_CONF);
	return elem;
}

function mkTextArea(data,idPrefix)
{

    var elem = new Element("textarea");
	elem.writeAttribute("class","textarea");
	elem.writeAttribute("name","conf");
	elem.writeAttribute("cols","90");
    elem.writeAttribute("rows","20");
	elem.innerHTML = data.firstChild.nodeValue;
	elem.writeAttribute("id",idPrefix+CMD_SAVE_WHOLE_CONF);
	return elem;
}

/*********************************************************
* create an "info" html element
*********************************************************/
function mkInfo(data,idPrefix)
{
	var content = data.firstChild.nodeValue;	
	//var elem = new Element("a");
	//elem.writeAttribute("class","info");
    //elem.writeAttribute("href", "");
	//elem.onclick = function() { showInfo(content)};
	var img = new Element("img");
	img.writeAttribute("class", "info");
	img.writeAttribute("src",_infoIcon);
    img.onclick = function() {showInfo(content)};
	//elem.insert(img);
	//return elem;
    return img;
}

/*********************************************************
* shows the info popup with the passed text content
*********************************************************/
function showInfo(content)
{
	if (isIE)
	{
		alert(content);
	}
	else
	{
		// hideInfo when its already created
		hideInfo();

		//create frame
		var elem = new Element("span");
		elem.writeAttribute("class","infoPopup");
		elem.writeAttribute("id","infoPopup");
		
		// create close hint
		var clickToClose = new Element("span");
		clickToClose.writeAttribute("class","infoPopupCloseHint");
        clickToClose.onclick = function() { hideInfo()};
		clickToClose.insert("click <b>here</b> to close");

		// prototype has problems creating textnodes !
		// create content 
		var elemCont = document.createElement("pre");
		elemCont.setAttribute("class","infoPopupContent");
		elemCont.appendChild(document.createTextNode(content));

        // join elements
		elem.insert(clickToClose);
		elem.insert(elemCont);
		$(_rootId).insert(elem);
	}
}

/*********************************************************
* deletes the info popup
*********************************************************/
function hideInfo()
{
	var elem = $("infoPopup");
	if (elem!=null)
	{
		$(_rootId).removeChild(elem);
	}
}

/*********************************************************
* close gets the html element by the id and deletes all
* children. The close button is switched to an open button
*********************************************************/
function close(id)
{
	var node = $(id);
	removeAllSubelements(node);
	changeToClose(node);
}

/*********************************************************
* sets the actualNode and sends a request to the servlet
*********************************************************/
function open(id)
{
	if(!isAjaxOccupied)
	{
		actualNode= $(id);
		ajaxSendRequest(id);
	}
}

/*********************************************************
* deletes all entry elements, sets the actualnode and 
* sends a request to the servlet. The response will 
* refill the deleted elements
*********************************************************/
function switchJobStatus(command)
{
	if(!isAjaxOccupied)
	{
		var parentId = command.substr(0,command.lastIndexOf("/"));
		actualNode = $(parentId);
		removeAllChildren(actualNode);
		ajaxSendRequest(command);
	}
}

/*********************************************************
* deletes all entry elements, sets the actualnode and 
* sends a request to the servlet. The response will 
* refill the deleted elements
*********************************************************/
function createTreeDatabase(command)
{
	if(!isAjaxOccupied)
	{
		var parentId = command.substr(0,command.lastIndexOf("/"));
		actualNode = $(parentId);
		removeAllChildren(actualNode);
		ajaxSendRequest(command);
	}
}

/*********************************************************
* deletes all entry elements, sets the actualnode and 
* sends a request to the servlet. The response will 
* refill the deleted elements
*********************************************************/
function reloadTree(command)
{
	if(!isAjaxOccupied)
	{
		var parentId = command.substr(0,command.lastIndexOf("/"));
		actualNode = $(parentId);
		removeAllChildren(actualNode);
		ajaxSendRequest(command);
	}
}

/*********************************************************
* deletes all entry elements, sets the actualnode and 
* sends a request to the servlet. The response will 
* refill the deleted elements
*********************************************************/
function reloadLogbook(command)
{
	if(!isAjaxOccupied)
	{
		var parentId = command.substr(0,command.lastIndexOf("/"));
		parentId = parentId.substr(0,parentId.lastIndexOf("/"));
		actualNode = $(parentId);
		removeAllSubelements(actualNode);
		ajaxSendRequest(command);
	}
}

/*********************************************************
* deletes all elements, sets the actualnode to the root
* element, then sends a request to the servlet.
* The response will be a message to reload the whole page
*********************************************************/
function reloadManager(command)
{
	if(!isAjaxOccupied)
	{
		actualNode = $(_rootId);
		removeAllSubelements(actualNode);
		ajaxSendRequest(command);
	}
}

/*********************************************************
* deletes all entry elements, sets the actualnode and 
* sends a request to the servlet. The response will 
* refill the deleted elements
*********************************************************/
function saveConf(id)
{
	var node = $(id);
	var value = node.value;
	// mask some characters. Otherwise they won't get to the servlet
	value = value.replace(/>/g, "&gt");
	value = value.replace(/</g, "&lt");
	value = value.replace(/\s/g, "&space");
        value = value.replace(/\?/g, "&question");
	actualNode = node.up();
	removeAllChildren(actualNode);	
	ajaxSendRequest(id+"/"+value);
}

function saveWholeConf(id)
{
    var node = $(id);
	var value = node.value;
	// mask some characters. Otherwise they won't get to the servlet
	value = value.replace(/>/g, "&gt");
	value = value.replace(/</g, "&lt");
    value = value.replace(/\n/g, "&n");
	value = value.replace(/\s/g, "&s");
    value = value.replace(/\?/g, "&que");
    value = value.replace(/=/g, "&e");
    value = value.replace(/"/g, "&quo");
    value = value.replace(/:/g, "&d");
    if (value.length>4000)
    {
        alert("This conf is too long, please use a custom editor.");
    }
    else
    {
        actualNode = node.up();
        removeAllChildren(actualNode);
        ajaxSendRequest(id+"/"+value);
    }
}

/*********************************************************
* deletes all Children of a node
*********************************************************/
function removeAllChildren(node)
{
	var runner = node.down();
	while (runner!=null)
	{
		var deleter=runner;
		runner=runner.next();
		deleter.remove();	
	}
}

/*********************************************************
* deletes all Children that have an id and therefore are
* subelements
*********************************************************/
function removeAllSubelements(node)
{
	var runner = node.down();
	while (runner!=null)
	{
		var deleter=runner;
		runner=runner.next();
		if(isSubElement(deleter))
		{	
			deleter.remove();			
		}		
	}
}

/*********************************************************
* is used to only delete subelements and not the headers
*********************************************************/
function isSubElement(node)
{
	return (node.readAttribute("id")!=null && node.readAttribute("id")!="");
}

/*********************************************************
* checks if an html node represents a button
*********************************************************/
function isOpenCloseButton(node)
{
	return (node.readAttribute("id")==_buttonId);
}

function debug(text)
{
	debugText += "\n"+text;
}

/* debug function */
function showDebug()
{
	alert(debugText);
}

/* debug function */
function printResponse(node,tab)
{
	while (node!= null)
	{
		debugText+= tab+node.nodeName + "\n";
		if (node.hasChildNodes())
		{
			printResponse(node.firstChild,tab+" ");
		}
		node = node.nextSibling;
	}
}
