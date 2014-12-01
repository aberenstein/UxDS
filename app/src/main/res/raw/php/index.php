<?php

/*
 *
 *	index.php?imei=[IMEI]&appid=[appid]<&file=[filename]><&attached=1>
 *
 */

//- turn off compression on the server
//@apache_setenv('no-gzip', 1);
//@ini_set('zlib.output_compression', 'Off');

if (!isset($_REQUEST['imei']) || !isset($_REQUEST['appid']))
{
	header("HTTP/1.1 400 Bad Request Error");
	exit;
}

if (!isset($_REQUEST['file']))
{
	$_REQUEST['file'] = 'items.xml';
}

include 'db.php';

$imei = $_REQUEST['imei'];
$appid = $_REQUEST['appid'];

$mysqli = new mysqli($dbhost, $dbuser, $dbpass, $dbname);
if (!$mysqli)
{
	header("HTTP/1.0 500 Internal Server Error");
	exit;
}

$stmt = $mysqli->prepare("INSERT INTO download(imei,appid,file) VALUES (?,?,?)");
if (!$stmt)
{
	header("HTTP/1.0 500 Internal Server Error");
	exit;
}

$res = $stmt->bind_param('sss', $imei, $appid, $_REQUEST['file']);
if (!$res)
{
	header("HTTP/1.0 500 Internal Server Error");
	exit;
}

$res = $stmt->execute();
if (!$res)
{
	header("HTTP/1.0 500 Internal Server Error");
	exit;
}

// sanitize the file request, keep just the name and extension
// also, replaces the file location with a preset one
$file_path  = $_REQUEST['file'];
$path_parts = pathinfo($file_path);
$file_name  = $path_parts['basename'];
$file_ext   = $path_parts['extension'];
$file_path  = './' . $file_name;

// allow a file to be streamed instead of sent as an attachment
$is_attachment = isset($_REQUEST['attached']) ? true : false;

// make sure the file exists
if (is_file($file_path))
{
	$file_size  = filesize($file_path);

	$file = @fopen($file_path,"rb");
	if ($file)
	{
		// set the headers, prevent caching
		header("Pragma: public");
		header("Expires: -1");
		header("Cache-Control: public, must-revalidate, post-check=0, pre-check=0");
 
	        // set appropriate headers for attachment or streamed file
        	if ($is_attachment)
	              	header("Content-Disposition: attachment; filename=\"$file_name\"");
	        else
        	        header('Content-Disposition: inline;');
 
	        // set the mime type based on extension, add yours if needed.
        	$ctype_default = "application/octet-stream";
	        $content_types = array(
                	"html" => "text/html",
                	"xml" => "text/xml",
	        );
        	$ctype = isset($content_types[$file_ext]) ? $content_types[$file_ext] : $ctype_default;
	        header("Content-Type: " . $ctype);

		while(!feof($file))
		{
			print(@fread($file, 1024*8));
			ob_flush();
			flush();
			if (connection_status()!=0) 
			{
				@fclose($file);
				exit;
			}			
		}
 
		// file save was a success
		@fclose($file);

		exit;
	}
	else 
	{
		// file couldn't be opened
		header("HTTP/1.0 500 Internal Server Error");
		exit;
	}
}
else
{
	// file does not exist
	header("HTTP/1.0 404 Not Found");
	exit;
}

?>