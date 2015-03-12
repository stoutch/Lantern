# MAS
## Server queries

###Routes
#### GET

This function is used to obtain the route, the heatmap and the quality value for each of the routes.

##### Query Example

http://173.236.254.243:8080/routes?dest={"lat":33.781761, "lng":-84.405155}&start={"lat":33.781940,"lng": -84.376917} 

##### Query Response 

{"response": {"routes":[<Routes obtained from Google>], "heatmaps": {"positive": [<positive points>], "negative": [<negative points>]}, score: [<score for each route>]}, "success": true}

*Explanation of each section*

* *Response*:Contains all the responses of the query, this a json that contains multiple keys
* * *routes*: This contains the normal response of google, this is an array of json, each element in this array is a different route, in the same way as using alternatives=true and mode=walking.
* * *heataps*: it is a json with two keys.
* * * *Positive*: contains an array of the different elements that compose the values stored in the server, that are positive. The structure of this elements is explained later.
* * * *Negative:* contains an array of the different elements that compose the values stored in the server that are over the route, that are negative. The structure i equivalent to the positive.
* * *Score:* it is an array, where the index i is the score of the route in the i-th position of the routes array.
* *Success:* it is a flag that defines if the query was completed succesfully.


http://173.236.254.243:8080/routes?dest={"lat":33.781777, "lng":-84.395426}&start={"lat":33.777229,"lng": -84.396187}


http://173.236.254.243:8080/heatmaps/negative?lat=32.725371&lng= -117.160721&radius=2500&total=2


http://173.236.254.243:8080/heatmaps/positive?lat=32.725371&lng= -117.160721&radius=2500&total=2

###Add a new element to the heatmap types

"lighting", "police_tower", "police_station", "incident", "security_rating","scenic_rating"

###Values for each new element to the heatmap

[-10,10]

## Server Interface

For each data query we are going to use json structures. Probably
later we are going to add to each of this structures new 
spaces for the authentication, have that in mind.

All the next queries are going to be embedded into another json with the next structure

{"success":\<True or False\>,"response":\<actual response\>}

### Heatmap
The heatmap response from the server is going to be an 
array of dictionaries. Each dictionary has two different
keywords: latitude and longitude.

[{"lat":"number" ,"lng":"number" },....,{"lat":"number" ,"lng":"number" }]

The query have the form
http://\<server address\>:23436/heatmap?lat=\<latitude\>&lng=\<longitude\>

I think we need to use two different heatmaps with different gradient colors:

1. Positive warm heatmap, it shows what places are good.

2. Negative cold heatmap, it shows what places are bad.

### Routes
Each query to the server for routes is going to have the following keywords: dest and star.
estination and starting point are both
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

## References 

Routes API:
https://developers.google.com/maps/documentation/directions/

We need to define alternatives=true and mode=walking to any query that do.


## Frontend

Added all the permissions, keys, google plays services for Google maps -  see https://developers.google.com/maps/documentation/android/start#display_your_apps_certificate_information 

The app debug key is AA:D7:94:2A:83:86:91:78:29:3A:B0:1E:C2:A0:60:18:0D:DB:3C:F8 .

I used this key to generate an API key for the Maps API from the Google APIs Console. If the app key(if we use the release key) and package name change, we need to regenerate the API key. 

At present, there are 3 activities - 
1. Start - The first is the login screen - I've just put a login button (which directs to the next screen) on the first screen - we can start the demo from the next one
2. InitialMap - The second screen displays a map for now. There's a text field and search button at the bottom. At present, clicking either will just take you to the next screen.
3. Input - Has a text field and search button for entering destination.
