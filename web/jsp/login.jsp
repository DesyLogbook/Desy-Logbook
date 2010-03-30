<html>
	<head>
		<title>Login</title>
        <style>
            .main {
                text-align: center;
                margin:0px;
                margin-top: 200px;
                padding: 20px;
                background-color: #eee;
                border-style:solid;
                border-left-style:none;
                border-right-style:none;

                border-color:silver;
                border-width: thick;
            }
            .elem {
                
                padding:8px;

            }
        </style>
	</head>
	<body style="margin:0px;">
        <div class="main">
		<form action="j_security_check" method="POST">
			<div class="elem">
                Username <input type="text" name="j_username"/>
            </div>
            <div class="elem">
                Password <input type="password" name="j_password"/>
            </div>
            <div class="elem">
                <input type="submit" value="login"/>
            </div>
		</form>
        </div>
	</body>
</html>
