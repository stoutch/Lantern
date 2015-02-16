# MAS
## Server Interface

For each data query we are going to use json structures. Probably
later we are going to add to each of this structures new 
spaces for the authentication, have that in mind.

### Heatmap
The heatmap response from the server is going to be an 
array of dictionaries. Each dictionary has two different
keywords: latitude and longitude.

[{"lat":"number" ,"lng":"number" },....,{"lat":"number" ,"lng":"number" }]

The query have the form
http://\<server address\>:23436/heatmap?lat=\<latitude\>&lng=\<longitude\>

### Routes
Each query to the server for routes is going to have the following keywords: userid, dest and star.
Userid is an integer, destination and starting point are both
dictionaries of the form {lat: , long: } that describe the
final and initial position of the routes respectively

The query have the form (for now lets hardcode the userid to 1):

http://\<server address\>:23436/route?userid=1&start={"lat":\<start latitute\>,lng:\<start longitude\>}&dest={"lat":\<destination latitute\>,"lng":\<destination longitude\>}

For this query everything is a string, so in the dictionaries include the quotation marks "" arount both the lat and long, aka you need to serialize the json representation to fit the previous query.

A way to store it in the program would be:
{"userid":"number" ,"dest": {"lat":"number" ,lng:"number" }, start: {lat:"number","lng":"number"}}

The response to the query is going to be an array of dictionaries.
Each dictionary has two keywords: name and waypoints. As follows

[{"name":"String","waypoints":[{"lat":"number","lng":"number"},{"lat":"number","lng":"number"},....]},...,{"name":"String","waypoints":[{"lat":"number","lng":"number"},{"lat":"number","lng":"number"},....]}]
