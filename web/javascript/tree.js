// last changes 09.04.08 0900
// showElement IE make visible
// first handle an error response from servlet
// changes 10.04.08 1030
// variables are passed from jsp and used
// pass logname parameter to treeServlet
// changes 21.04.08
// removed next prev and added more error prevention
// 05.05.08
// konquerer fix for not beeing able to get parent frame
// removed showFolder, only showData is used
// 27.05.08
// added functionality to mark empty data entries


/*-------------------- CONSTANT VALUES -----------------*/
// !! some constants will be initialized in the init function !!

// time before ajax object is reset
var _ajaxTimeoutInMS = 5000;
// name of the logbook
var _elogbookName = "";
// id of the root html element
var _rootId = "DivRoot";
// content tag used as id suffix
var _contentTag = "-content";
// switch if scrollbar should jump to actualElement
var _jumpToActual = false;
// debug switch to see next/prev element
var _showNextPrev = false;
// debug switch to see show-debug element
var _showDebug = false;
// picture paths
var _iconFolder 		= "/Folder95C.gif";//"/folder.png";
var _iconFolderOpen		= "/Folder95O.gif";//"/folder_open.png";
var _iconFile 			= "/BookC.gif";//"/file.png";
var _iconFileOpen 		= "/BookO.gif";//"/file_open.png";
var _iconFileEmpty 		= "/BookCE.gif";//"/file.png";
var _iconFileOpenEmpty 	= "/BookOE.gif";//"/file_open.png";
var _iconSpacer 		= "/tree.gif";//"/spacer.png";
// picture size
var _picSize = "16";//"20";
// servlet path
//var _servletPath = "http://mcsstrampe:8084/treeProject/treeServlet";
//var _servletPath = "/elog/servlet/treeServlet";
//var _servletPath = "/elogbookManager/Manager";
var _servletPath = "/Manager";
// xmlList call part 1 and 2
//var _xmlList1 = "?file=";
var _xmlList1 = "?file=";
//var _xmlList2 = "&picture=true";
var _xmlList2 = "&name=";
// name of the content frame
//var _contentFrame = "list_frame";
var _contentFrame = "contentFrame";
// content folder path
//var _dataPath = "/";
// position for jump, used with show.jsp
var _jumpPos = "";


/*-------------------- VARIABLES -----------------------*/

// ajax object
var ajax;
// pointer to the actual TableRow element
var actualNode;
// String to remember a target that is not downloaded yet
// is needed with prev/next function
var targetId="";
// variable to save the version of the tree
var treeVersion = 0;
// flag to see if a new treeVerion has arrived
var isNewTreeVersion = false;
// debug text
var debugText  = "";
// flag if an ajax request is in progress
var isAjaxOccupied = false;
// flag if client is an internet explorer or not
var isIE;
// variable to store a running timeout function
var timeout;

/*---------------- AJAX COMMUNICATION ------------------*/

/*********************************************************
* initialization
* host : "http://localhost:8080/newElog"
* logname : "ABCelog"
* actDir : /2009/09
*********************************************************/
function init(host,logname,actDir,jumpPos)
{
	//alert("host "+host+"\n"+"logroot "+logroot+"\n"+"imagedir "+imagedir+"\n"+"viewServlet "+viewServlet+"\n");
    imagedir = "images";
    //viewServlet = "/elog/servlet/XMLlist";
    viewServlet = "/XMLlist";
    dataPath ="";
	_iconFolder 		= imagedir + _iconFolder;
	_iconFolderOpen		= imagedir + _iconFolderOpen;
	_iconFile 			= imagedir + _iconFile;
	_iconFileOpen 		= imagedir + _iconFileOpen;
	_iconFileEmpty 		= imagedir + _iconFileEmpty;
	_iconFileOpenEmpty 	= imagedir + _iconFileOpenEmpty;
	_iconSpacer			= imagedir + _iconSpacer;
	_servletPath 		= host + _servletPath;
	_xmlList1 			= host + viewServlet + _xmlList1;
	_elogbookName		= logname;

    _jumpPos = jumpPos;

	isIE = document.all?true:false;
	
	if (_showDebug)
	{		
		if (!isIE)
		{			
			document.getElementById("debug").setAttribute("style","display:block;");
		}
		else
		{
			document.getElementById("debug").style.setAttribute("display","block",0);
		}
	}	
		
	// init ajax
	ajax = createAjax();
	
	// when a new dir is enterd as query string parameter ?dir=
	// then this path is used an not the workfile_actdir
	/*if (this.frameElement.id != null && this.frameElement.id != "")
	{
		actDir = this.frameElement.id.split("&")[0];
		_jumpPos = this.frameElement.id.split("&")[1];
	}*/
	
	// to start at the right html element
	actualNode = document.getElementById(_rootId);
	
	// first ajax request for base tree if ajax was created 
	if (ajax!=null)
	{		
		if(actDir.substring(0,1)=='/')
		{
			actDir = actDir.substring(1, actDir.length);
		}	
		showElement(actDir);
	}
	else
	{
		alert("Could not create ajax obejct !");
	}
}

/*********************************************************
* creates an instance of the httpRequest object
*********************************************************/
function createAjax()
{
	var xhr = null;
	if (window.XMLHttpRequest) 
	{
  		xhr = new XMLHttpRequest();
	}
	else if (window.ActiveXObject) 
	{
  		xhr = new ActiveXObject("Msxml2.XMLHTTP");
	}
	return xhr;
}

/*********************************************************
* sends an ajax-request for tree data
* havePath is the path that is already at the client
* wantPath empty if Client wants just one subfolder
* or contains the path to the folder which user wants
*********************************************************/
function ajaxRequestTreeData(havePath, wantPath)
{
	debugText += "in request mit isOccupied = "+isAjaxOccupied+"\n";
	if (!isAjaxOccupied)
	{
		debugText += "ajax request tree data\n";
		isAjaxOccupied = true;		
		ajax.open("get", _servletPath + "?name=" + _elogbookName +"&p1=" + havePath + "&p2=" + wantPath, true);
		ajax.onreadystatechange = ajaxReceiveTreeData;		
		ajax.send(null);
		timeout = setTimeout("ajaxTimeout()",_ajaxTimeoutInMS);		
	}
	else
	{
		//alert("ajax was occupied");
	}
}

/*********************************************************
* sends an ajax-request for neighbour data
* path is the tree position whose neighbours
* are requested
*********************************************************/
function ajaxRequestNeighbourData(path)
{	
	if (!isAjaxOccupied)
	{
		debugText += "request neighbour mit path "+path+" und \n";
		isAjaxOccupied = true;
		var address = _servletPath + "?name=" + _elogbookName + "&actual=" + path;
		ajax.open("get", _servletPath + "?name=" + _elogbookName + "&actual=" + path, true);
		ajax.onreadystatechange = ajaxReceiveNeighbourData;		
		ajax.send(null);
		timeout = setTimeout("ajaxTimeout()",_ajaxTimeoutInMS);
	}
	else
	{
		//alert("ajax was occupied");
		debugText += "ajax was occupied\n";
	}
}

/*********************************************************
* Eventhandler launched when ajax response does not
* arrive in time
*********************************************************/
function ajaxTimeout()
{
	ajax.abort();
	isAjaxOccupied = false;
	debugText += "ajax timeout\n";
}

/*********************************************************
* Eventhandler launched when ajax data is received
*********************************************************/
function ajaxReceiveTreeData()
{
	if (ajax.readyState==4)	
    {   	
		var data = null;
		//printXML(ajax.responseXML);
		data = ajax.responseXML;
		if(data==null || data.firstChild==null)
		{
			debugText += "ajax returned null\n";
		}
		else
		{
			var tempNode = data.firstChild;
			// MSIE has a processing instruction node on front
			if (tempNode.nodeType == 7)
			{				
				tempNode = tempNode.nextSibling;
			}
			if (tempNode.nodeName == "R")
			{
				handleTreeData(tempNode,actualNode);
				if (isNewTreeVersion)
				{
					setTimeout("handleNewTreeVersion()",100);
				}				
			}
			else
			{
				//alert("Fehler2");
				debugText += "ajax returned "+tempNode.nodeName+"\n";
			}			
		}
		window.clearTimeout(timeout);
		isAjaxOccupied = false;
	}
}



/*********************************************************
* Eventhandler launched when ajax data is received
*********************************************************/
function ajaxReceiveNeighbourData()
{
	if (ajax.readyState==4)	
    {    	   		
		var data = null;
		//printXML(ajax.responseXML);		
		data = ajax.responseXML;
		if(data==null || data.firstChild==null)
		{
			//alert("Fehler1");
		}
		else
		{
			var tempNode = data.firstChild;
			// MSIE has a processing instruction node on front
			if (tempNode.nodeType == 7)
			{
				tempNode = tempNode.nextSibling;
			}
			if (tempNode.nodeName == "R")
			{
				handleNeighbourData(tempNode,actualNode);
				if (isNewTreeVersion)
				{
					setTimeout("handleNewTreeVersion()",100);
				}				
			}
			else
			{
				//alert("Fehler2");
			}			
		}		
    	window.clearTimeout(timeout);
		isAjaxOccupied = false;
	}
}

/*********************************************************
* event that is launched when tree data has changed
* tree data has changed when another timestamp arrives
*********************************************************/
function handleNewTreeVersion()
{	
	isNewTreeVersion = false;
	// save the actual element
	var elementId = actualNode.getAttribute("id");
	// delete all treedata
	actualNode = document.getElementById(_rootId);
	var deleteContent = document.getElementById(_rootId + _contentTag);
	if (deleteContent!=null)
	{
		deleteContent.parentNode.removeChild(deleteContent);
	}
	// request the saved element
	showElement(elementId);
}

/*----------------- TREE FUNCTIONALITY ---------------*/

/*********************************************************
* make a new table with the elements of data
* locaActualNode should be a non content div element
*********************************************************/
function handleTreeData(data, localActualNode)
{
	// firstchild to access the first data component
	data = data.firstChild;	

	var idPrefix = localActualNode.getAttribute("id")+"/";
	
	// create the content element that will be filled
	var content = document.createElement("div");
	content.setAttribute("id",localActualNode.getAttribute("id") + _contentTag);
	
	// the first element rootId should not be in the prefix
	if (idPrefix==(_rootId+"/"))
	{
		idPrefix="";
	}
	else
	{
		content.setAttribute("class","leftSpace");	
		content.className = "leftSpace";
	}
		
	// run throu all elements
	while(data!=null)
	{
		// only elemet nodes and tree data
		if(isTreeData(data))
		{
			var entry;
			var name = data.getAttribute("n");
			var fullName = idPrefix + name;
			
			// entry with childnodes
			if(data.hasChildNodes())
			{
				entry = makeEntry(name,fullName,3);
				content.appendChild(entry);
				//recursion no data.firstChild because <Root> element is not present anymore
				handleTreeData(data,entry);
			}
			else // entry without childnodes
			{	
				switch(data.nodeName)
				{					
					case "S" : entry = makeEntry(name,fullName,1); break;
					case "D" : 
					{
						// check if data is empty
						if (data.getAttribute("e")==null)
							entry = makeEntry(name,fullName,2); 
						else
							entry = makeEntry(name,fullName,4); 						
						break;
					}
				}
				content.appendChild(entry);
			}
		}
		
		// only VERSION data
		if (isVersionData(data))
		{
			var newVersion = data.firstChild.nodeValue;
			
			// treeVersion is 0 when first data arrives
			if (treeVersion == 0)
			{
				treeVersion = newVersion;
			}
			
			if (treeVersion < newVersion)
			{
				treeVersion = newVersion;
				isNewTreeVersion = true;				
			}
		}
		
		// set data to next element
		data= data.nextSibling;
	}	

	// check if old element does not have the userSwitchClick function
	// happens with prev next function
	var onclick = ""+localActualNode.getAttribute("onclick");
	onclick = ""+localActualNode.onclick;	
	if (onclick!=null && onclick.match(/userFolderClick/))
	{		
		//debugText+= "userFolderClick erkannt\n";
		//localActualNode.setAttribute("onclick","userSwitchClick('"+localActualNode.getAttribute("id")+"');");
		var id = localActualNode.getAttribute("id");
		localActualNode.onclick = function() { userSwitchClick(id);};
	}
	
	//insert element
	insertElementAfter(content,localActualNode);
		
	// on prev/next click it is possible to request a tree
	// with depth > 1. When this tree is inserted we have to
	// select the real "targetId" afterwards.
	var searchNode = document.getElementById(targetId);
	if (searchNode!=null)
	{
		changeActualNode(searchNode);
		if (isDataNode(searchNode))
		{
			showInRightFrameData(targetId);
		}
		else
		{
			// folder has to change to "open"
			switchIcon(searchNode,true);
			showInRightFrameData(targetId);
		}
		targetId="";
	}
}

/*********************************************************
* make a new subfolder entry
* return a complete div element
* type 1 = folderClick
* type 2 = dataClick
* type 3 = switchClick
* type 4 = emptyData
*********************************************************/
function makeEntry(name,fullName,type)
{
	var div = document.createElement("div");
	var outerSpan = document.createElement("span");
	var innerSpan = document.createElement("span");
	var img = document.createElement("img");	

	switch (type)
	{
  		case 1 : div.onclick = function() { userFolderClick (fullName);}; break;
  		case 2 : div.onclick = function() { userDataClick   (fullName);}; break;
  		case 3 : div.onclick = function() { userSwitchClick (fullName);}; break;
  		case 4 : div.onclick = function() { userDataClick   (fullName);}; break;
  	}

	div.setAttribute("id",fullName);
	// type 4 is an empty data entry
	if(type==4)
	{
		innerSpan.setAttribute("class","contentSpanEmpty");	
		innerSpan.className = "contentSpanEmpty";
	}
	else
	{
		innerSpan.setAttribute("class","contentSpan");	
		innerSpan.className = "contentSpan";
	}
	img.setAttribute("width",_picSize);
	img.setAttribute("height",_picSize);
	img.style.width = _picSize;
	img.style.height = _picSize;
	
	switch (type)
	{
		case 1 : img.setAttribute("src",_iconFolder);	 break;
		case 2 : img.setAttribute("src",_iconFile);		 break;
		case 3 : img.setAttribute("src",_iconFolderOpen);break;
		//case 4 : img.setAttribute("src",_iconFile);		 break;
		case 4 : img.setAttribute("src",_iconFileEmpty); break;
	}

	innerSpan.innerHTML = name;	
	outerSpan.appendChild(img);
	outerSpan.appendChild(innerSpan);
	div.appendChild(outerSpan);
	return div;
}

/*********************************************************
* handles a response with neighbour data
* the tree is not changed here
*********************************************************/
function handleNeighbourData(data)
{
	data = data.firstChild;
	while (data!=null)
	{
		// only VERSION data
		if (isVersionData(data))
		{
			var newVersion = data.firstChild.nodeValue;
			
			// treeVersion is 0 when first data arrives
			if (treeVersion == 0)
			{
				treeVersion = newVersion;
			}
						
			if (treeVersion < newVersion)
			{
				treeVersion = newVersion;
				isNewTreeVersion = true;
			}
		}
				
		// set data to next element
		data= data.nextSibling;
	}
}

/*********************************************************
* function which shows data in the right frame
*********************************************************/
function showInRightFrameData(dataId)
{
	// konqueror sometimes doesn't like parents
	var parentFrame = parent.frames[_contentFrame];
	if (parentFrame!=null)
	{
		var file = "/"+ dataId;
		var path = _xmlList1 + file + _xmlList2+_elogbookName;
		// is used with show.jsp
		if (_jumpPos!="")
		{
			path += "#"+_jumpPos;
			_jumpPos = "";
		}
		parentFrame.location.href = path;
	}
}

/*********************************************************
* insert the "insert" object after the "elem" object
*********************************************************/
function insertElementAfter(insert,elem)
{
	if (elem.nextSibling==null)
	{
		elem.parentNode.appendChild(insert);
	}
	else
	{
		elem.parentNode.insertBefore(insert,elem.nextSibling);
	}
}

/*********************************************************
* changes the actualNode to the new Node
*********************************************************/
function changeActualNode(newNode)
{	
	// dont switch the root element
	if(newNode.getAttribute("id") != _rootId)
	{
		switchNodeActiveStatus(newNode);
		switchIcon(newNode,true);
	}	
	if (actualNode.getAttribute("id")!=_rootId)
	{
		switchNodeActiveStatus(actualNode);
		switchIcon(actualNode,false);	
	}		
	actualNode = newNode;
	// when you click on an open file it should not close
	if (isIconFileClose(actualNode))
	{
		switchIcon(actualNode,true);
	}
	// should the window autoscroll to new element
	if (_jumpToActual)
	{	
		window.location.hash = "#"+actualNode.getAttribute("id");
	}
}

/*********************************************************
* switches the icon of an element
* isNew indicates if the node is the one that has just been
* activated or if it the one that was deactivated. 
* the deactivated can only change from "open file" to 
* "closed file"
*********************************************************/
function switchIcon(node, isNew)
{
	var compare = node.firstChild.firstChild.getAttribute("src");
	if( compare == _iconFile && isNew )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFileOpen);
	}
	if( compare == _iconFileOpen )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFile);
	}
	if( compare == _iconFileEmpty )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFileOpenEmpty);
	}
	if( compare == _iconFileOpenEmpty )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFileEmpty);
	}
	
	if( compare == _iconFolder && isNew )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFolderOpen);
	}
	if( compare == _iconFolderOpen && isNew )
	{
		node.firstChild.firstChild.setAttribute("src",_iconFolder);
	}
}

/*********************************************************
* switch a node between active and inactive
*********************************************************/
function switchNodeActiveStatus(node)
{
	if (isIE)
	{
		if (node.className == "active")
		{
			node.className = "";
		}	
		else
		{
			node.className = "active";
		}
	}
	else
	{
		if (node.getAttribute("class")=="active")
		{
			node.removeAttribute("class");			
		}
		else
		{
			node.setAttribute("class","active");
		}
	}	
}

/*------------------ CLICK EVENTS ----------------------*/

/*********************************************************
* User clicks on a folder and data is requested
*********************************************************/
function userFolderClick(name)
{
	debugText+="folder click mit name = "+name+"\n";
	clickedNode = document.getElementById(name);	
	clickedNode.onclick = null;
	var id = clickedNode.getAttribute("id");
	clickedNode.onclick = function() { userSwitchClick(id);};
	changeActualNode(clickedNode);
	showInRightFrameData(name);
	ajaxRequestTreeData(name,"");	
}

/*********************************************************
* User clicks on data
*********************************************************/
function userDataClick(name)
{	
	//debugText+="data click mit name = "+name+" und ajaxO = "+isAjaxOccupied+"\n";
	clickedNode = document.getElementById(name);
	changeActualNode(clickedNode);				
	// show content data
	showInRightFrameData(name);	
	// request new neighbour data
	ajaxRequestNeighbourData(name);
}

/*********************************************************
* User clicks on folder whose content is present and this
* folder will switch its visibility
*********************************************************/
function userSwitchClick(name)
{
	debugText+="switch click mit name = "+name+"\n";
	var clickedNode = document.getElementById(name);
	changeActualNode(clickedNode);	
	// show content data
	showInRightFrameData(name);	
	var elem = document.getElementById( name + _contentTag);
		
	if( elem.getAttribute("style") == null ||
		elem.style.display != "none")
	{
		elem.setAttribute("style","display:none;");
		elem.style.display = "none";		
	}
	else
	{
		elem.removeAttribute("style");
		elem.style.display = "block";		
	}
	// request new neighbour data
	ajaxRequestNeighbourData(name);	
}

/*********************************************************
* the element is shown if it exists, if not it is 
* requested first
*********************************************************/
function showElement(elementId)
{	
	var nextNode = document.getElementById(elementId);

	// nextNode exists and can be activated
	if (nextNode!=null)
	{
		var runner = nextNode;
		
		// if possible get the content of the nextNode and make it visible
		var content = document.getElementById(runner.getAttribute("id") + _contentTag);
		if (content!=null)
		{			
			if( content.getAttribute("style")=="display: none;" ||
				content.style.display == "none")
			{
				content.removeAttribute("style");
				content.style.display = "block"; // ie special
			}			
		}
		
		// go up the dom tree and make all parents visible
		while (runner!=null && runner != document)
		{	
			if( runner.getAttribute("style")=="display: none;"||
				runner.style.display == "none")
			{
				runner.removeAttribute("style");
				runner.style.display = "block"; // ie special
				// special case, content element is set visible
				// above, so previous sibling should be the father
				// element with the folder icon which should
				// be the open folder icon
				if (isIconFolderClose(runner.previousSibling))
				{
					switchIcon(runner.previousSibling,true);
				}
			}
			runner= runner.parentNode;
		}
		
		changeActualNode(nextNode);
		
		// if nextNode is a data-node show the data and request neighbours		
		if (isDataNode(actualNode))
		{
			showInRightFrameData(elementId);
			ajaxRequestNeighbourData(elementId);
		}
		else // it is a folder-node
		{
			showInRightFrameData(elementId);

			// if folder has no content downloaded yet, then send the request
			//if( document.getElementById( actualNode.getAttribute("id")+_contentTag )==null  )
			if (content == null )
			{
				actualNode.removeAttribute("onclick");
				//actualNode.setAttribute("onclick","userSwitchClick('"+actualNode.getAttribute("id")+"');");
				var id = actualNode.getAttribute("id");
				actualNode.onclick = function() { userSwitchClick(id);};
				ajaxRequestTreeData(elementId,"");
			}
			else
			{				
				// a folder with content is already visible, so neighours are requested
				// but its a special case if user clicks next on an open folder, this
				// folder should not be closed, it should stay open
				if(isIconFolderClose(actualNode))
				{
					switchIcon(actualNode,true);
					
				}

				ajaxRequestNeighbourData(elementId);
			}
		}
	}
	else // nextNode does not exist and has to be requested
	{
		var search = elementId;
		var index;
		var lastElemNode;
		// find the last existing element by shortening the path
		do
		{
			index = search.lastIndexOf("/");
			search = search.substring(0,index);
			lastElemNode = document.getElementById(search);
		} while(lastElemNode==null && index!=-1)

		// is null when treeVersion has changed and whole
		// tree is deleted and reloaded
		if (lastElemNode == null)
		{
			lastElemNode = document.getElementById(_rootId);
		}
		
		// make the last existing element the active element
		targetId = elementId;
		var havePath = search;
		var wantPath = targetId.substring(index+1,targetId.length);
		if (wantPath=="/") {wantPath="";}
		changeActualNode(lastElemNode);
		ajaxRequestTreeData(havePath,wantPath);
		
	}
}

/*------------------ HELP FUNCTIONS --------------------*/

/*********************************************************
* finds out if a node is part of a tree date
*********************************************************/
function isTreeData(node)
{
	if (node==null) return FALSE; 
	// must be data type ELEMENT_NODE
	return (node.nodeType==1 && (node.nodeName == "S" || node.nodeName == "D"));
}

/*********************************************************
* finds out if a node is part of a tree date
*********************************************************/
function isVersionData(node)
{
	if (node==null) return FALSE;
	// must be data type ELEMENT_NODE
	return (node.nodeType==1 && node.nodeName == "V");
}

/*********************************************************
* gets an html node and checks if it a data node by comparing
* the image
*********************************************************/
function isDataNode(node)
{	
	var list = node.getElementsByTagName("img");
	// get the last image because the first could be the _iconSpacer
	if (list==null || list.length==0) return false;
	var img = list[list.length-1];
	return (	img.getAttribute("src") == _iconFile ||	
				img.getAttribute("src") == _iconFileEmpty ||
				img.getAttribute("src") == _iconFileOpenEmpty ||
				img.getAttribute("src") == _iconFileOpen );
}

/*********************************************************
* indicates if a node has the closed folder icon
*********************************************************/
function isIconFolderClose(node)
{
	var list = node.getElementsByTagName("img");
	// get the last image because the first could be the _iconSpacer
	if (list==null || list.length==0) return false;
	var img = list[list.length-1];
	return img.getAttribute("src") == _iconFolder;
}

/*********************************************************
* indicates if a node has the closed open file icon
*********************************************************/
function isIconFileClose(node)
{
	var list = node.getElementsByTagName("img");
	// get the last image because the first could be the _iconSpacer
	if (list==null || list.length==0) return false;
	var img = list[list.length-1];
	return img.getAttribute("src") == _iconFile;
}

/*********************************************************
* prints the content of a xml file
*********************************************************/
function printXML(xml)
{
	var count = 0;
	while(xml!=null)
	{
		//debugText += "count : "+count+"\n";
		//debugText += "knoten typ : "+xml.nodeType+"\n";
		if(xml.nodeType != 3)
		{			
			//debugText += "knoten name : "+xml.nodeName+"\n";
			if( xml.attributes != null)
			{
				debugText += "knoten inhalt : "+xml.getAttribute("n")+"\n";
			}
		}
		if(xml.hasChildNodes())
		{
			//debugText += "hat kinder\n------\n";
			printXML(xml.firstChild);
		}
		xml=xml.nextSibling;
		count++;
	}	
}

/*********************************************************
* displays a message containing the debug text
*********************************************************/
function showDebugText()
{
	alert(debugText);
}

/*----------------- PUBLIC FUNCTIONS -------------------*/
// all public functions are called from the content frame

/*********************************************************
* picks the target out of the address location
* address looks like this "http://mcsstrampe.desy.de/elog/servlet/XMLlist?file=/TESTelog/data/2008/02&xsl=/elogbook/xsl/elog.xsl&picture=true"
* so we must get the "2008/02"
*********************************************************/
function showAddress(address)
{
	var part = address.search(/file=/);
	var sub = address.substring(part+5,address.length);
	part = sub.search(/&/);
	sub = sub.substring(0,part);
	sub = sub.substring(1,sub.length);
	showElement(sub);
}

/*********************************************************
* comes from left.jsp show actual button (work.xml data)
* address looks like this /2008/12
* so the first "/" must be removed
*********************************************************/
function showActualAddress(address)
{	
	address = address.substring(1,address.length);
	showElement(address);
}

/*********************************************************
* hides/shows the tree frame
*********************************************************/
function hideShow()
{
	var elem = parent.document.getElementById("frameset1");
	if(window.innerWidth>10)
	{
		elem.setAttribute("cols","5,*");
		//window.resizeTo(10, window.innerHeight);		
	}
	else
	{
		elem.setAttribute("cols","145,*");
		//window.resizeTo(145, window.innerHeight);
	}
}
