# MAS
## Server queries

###Routes
#### GET

This function is used to obtain the route, the heatmap and the quality value for each of the routes.  To actually display it in the heatmap, we require to add to the array of the heatmap as many as weight*value/10 elements.

##### Query Example

http://173.236.254.243:8080/routes?dest={"lat":33.781761, "lng":-84.405155}&start={"lat":33.781940,"lng": -84.376917} 

##### Query Response 

{"response": {"routes":[<Routes obtained from Google>], "route_index": [<index for each route>], "heatmaps": {"positive": [<positive points>], "negative": [<negative points>]}, "score": [<score for each route>]}, "success": true}

*Explanation of each key*

* *Response*:Contains all the responses of the query, this a json that contains multiple keys
* * *routes*: This contains the normal response of google, this is an array of json, each element in this array is a different route, in the same way as using alternatives=true and mode=walking.
* * *route_index:* Is the index stored in the server, the i-th position of this array is the index for the i-th route, this is used later to do a post to the server.
* * *heatmaps*: it is a json with two keys.
* * * *Positive*: contains an array of the different elements that compose the values stored in the server, that are positive. The structure of this elements is explained later.
* * * *Negative:* contains an array of the different elements that compose the values stored in the server that are over the route, that are negative. The structure i equivalent to the positive.
* * *Score:* it is an array, where the index i is the score of the route in the i-th position of the routes array.
* *Success:* it is a flag that defines if the query was completed succesfully.

#### POST

This function is used by the app to update the approximate times of the route on the server, this call has to be done periodically, but after selecting one route otherwise it would fail.

##### Query Example

http://173.236.254.243:8080/routes?current_position={"lat":33.781761, "lng":-84.405155} &route_index=1425850320099&radius=5000000

##### Query Response

TODO: but basically it should only respond, depending if the update was done succesfully.

{"success": true}

### SetRoute
####POST

This function is used to select the route from the possibilities given in the get route function. It has to be done always after the user press the start button.

####Query Example 

http://173.236.254.243:8080/routes/select/1425850320099

The last number is the index number given in the get route query

##### Query Response

{"success": true}

The server just responds true or false depending on the success of the query

###Heatmaps

1. Positive warm heatmap, it shows what places are good.
2. Negative cold heatmap, it shows what places are bad.


####Get
  This function returns the heatap given the position of the user. To actually display it in the heatmap, we require to add to the array of the heatmap as many as weight*value/10 elements.

#### Query examples

1. http://173.236.254.243:8080/heatmaps/positive?lat=32.725371&lng= -117.160721&radius=2500&total=2
2. http://173.236.254.243:8080/heatmaps/negative?lat=32.725371&lng= -117.160721&radius=2500&total=2
3. http://173.236.254.243:8080/heatmaps/?lat=32.725371&lng= -117.160721&radius=2500&total=2

*Explanation of each key*

* *lat*: latitude of the current position of the user
* *lng*: longitude of the current position of ther user
* *radius*: maximum radius arround the lat/lng that are going to be returned, it should be different depending of the zoom of the app.
* *total*: maximum number of values to be returned 

##### Query response examples

Using the same number as the query examples

1. {"response": [{"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "weight": 10, "value": 10, "lighting_index": 1425843485117, "_id": {"$oid": "54fca51dfc64c614ffab4dee"}, "type": "lighting", "day": "False", "_user_id": 1}, {"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "weight": 10, "value": 10, "lighting_index": 1426176002741, "_id": {"$oid": "5501b802fc64c669415e8f92"}, "type": "lighting", "day": "False", "_user_id": 1}], "success": true}
2. {"response": [{"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "_id": {"$oid": "54f79032fc64c65a0526da49"}, "_user_id": 1}], "success": true}
3. {"response": {"positive": [{"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "weight": 10, "value": 10, "lighting_index": 1425843485117, "_id": {"$oid": "54fca51dfc64c614ffab4dee"}, "type": "lighting", "day": "False", "_user_id": 1}, {"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "weight": 10, "value": 10, "lighting_index": 1426176002741, "_id": {"$oid": "5501b802fc64c669415e8f92"}, "type": "lighting", "day": "False", "_user_id": 1}], "negative": [{"loc": {"type": "Point", "coordinates": [-117.160721, 32.725371]}, "_id": {"$oid": "54f79032fc64c65a0526da49"}, "_user_id": 1}]}, "success": true}

*Explanation of each key*

* *response*: is an array of elements in the heatmap stored in the database
* * *Each element*:
* * * *loc*: it is the element that is stored in the database, you should only use the coordinates of this json, which is an array of the form [<longitude>,<latitude>]
* * * *weight*: is the weight that is given to that type of element
* * * *<type>_index:* you should not use this, it is an internal index for the database
* * * *type*: it is the type of element related to the heapmap ("lighting", "police_tower", "police_station", "incident", "security_rating","scenic_rating")
* * * *value*: it is the rating that an user gave to this type of element.

####Post
Add a new element to the heatmap. First let explain the characteristics of an element of the heatmap

#####Type
"lighting", "police_tower", "police_station", "incident", "security_rating","scenic_rating"

###Values for each new element to the heatmap
They are in the range of [-10,10]

## Query example

http://173.236.254.243:8080/heatmaps/positive?lat=32.725371&lng= -117.160721&type=lighting&value=10
http://173.236.254.243:8080/heatmaps/negative?lat=32.725371&lng= -117.160721&type=lighting&value=10


*Explanation of each element of the query*

* *lat*: latitude of the current position of the user
* *lng*: longitude of the current position of ther user
* *type*: it is the 
* *value*: is the rating from -10 to 10 that the person is giving to that type of element in the route, if we are using an star rating: 5 stars is a 10 and a 0 is a -10, we just need to scale the vales of the user before sending it to the server. 

###Query response example

{"response": {"$oid": "5501c0e4fc64c669a012127c"}, "success": true}

You need to verify the success flag when doing this query.

##Calculation of the score of each route.

1. We obtain all the heatmaps elements that are close to the route.
2. Each element in the heatmap has a weight and a value, the weight is assigned depending of the type of element, and the value was the rating given by any user in the past.
3. We calculate a rating for each line of the route (obtained from the polyline). Using the function
* W(line) = (\sum_{m\in Positive_Heatmap} weight(m)*value(m) + \sum_{m\in Negative_Heatmap} weight(m)*value(m))/ (\sum_{m\in Positive_Heatmap} weight(m))
4. After having each route is calculated we obtain the total score of the route as:
* W(route) = \sum_{line\in Route} W(line)*distance(line) / \sum_{line\in Route} distance(line)

### Note
A response of a score of zero, could mean two things:
1. There is not rating in the route and there are no persons around. 
2. The rating of the place could mean is bad and the good elements are equal to the bad elements. This is highly unlike, so we can assume normally number 1.

## Creating Users
### POST
Add a new user to the database. In the frontend we could verify that the user email has the for of *@gatech.edu to try to improve the security at least a little.

#### Query template
http://173.236.254.243:8080/users?email=<user email>&password=<user password>

#### Example
http://173.236.254.243:8080/users?email=esaurez@gatech.edu&password=test123

### GET
Get the information about the user, the user has to be log in to be able to use it. It does not require any parameter

###Query template
http://173.236.254.243:8080/users

### PUT
It can be used to change the password of the user, has to be logged in

#### Query template
http://173.236.254.243:8080/users?password=<new password>

#### Query example
http://173.236.254.243:8080/users?password=test123

## Login
This is use to login the users, it only supports post. After the user log in, it is required to store the cookie and send it with every other query to the server. (This is not required now, but when everybody enables it in both iOS and Android, I will enforce it on the server).

### POST
This function is used to log in an user.

### Query Template
http://173.236.254.243:8080/login?login=<user email>&password=<password>

### Query example
http://173.236.254.243:8080/login?login=esaurez@gatech.edu&password=test123

## Logout
This function logouts the user from the server. It only support get
### GET
This function log out the user from the server

####Query Template and Example
http://173.236.254.243:8080/logout



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
