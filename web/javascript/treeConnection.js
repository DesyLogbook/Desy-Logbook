var target;

function jumpTo(address)
{
	target = address;
	var success = false;
	if (parent!=null)
	{
		var  father = parent;//.frames['left_frame'];
		if (father!=null)
		{1
			father.showAddress(address);
			success = true;
		}
		setTimeout("pageDidNotUpdateInTime()",1000);
		
	}
	if (!success)
	{
		window.location.href = address;
	}
}


function pageDidNotUpdateInTime()
{
	window.location.href = target;
}
