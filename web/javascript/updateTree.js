//To be called from FileEdit input form to update the tree data
// Is called in elog-fileform.xml
var fillTree=false;
function send(url)
{
	if (fillTree)
	{
		return true;
	}
	else
	{
		fillTree=true;
		$('fenster').src = url;
		setTimeout(checkIframe,1500);
		return false;
	}
}
function checkIframe()
{
	if ($('fenster').src.search(/Manager/) !=-1)
	{
		document.inputForm.submit();
	}
}

