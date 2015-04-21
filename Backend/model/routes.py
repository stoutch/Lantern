from bson.objectid import ObjectId
import logging
from math import pow,sqrt
from tornado.gen import Return, coroutine
from urllib2 import urlopen
import config
import boxer
import calendar
from datetime import datetime
from geoutils import *

class _Route:
    '''
    ***************************************************************************
                                Route
    ***************************************************************************
    '''
    @coroutine
    def get_route(self,idUser,destination_point,starting_point,total=50):
        '''
        id_user: id of the user trying to get the route.
        destination_point: {lat:,long:} of the geographical position of the final destination.
        starting_point: {lat:,long:} of the geographical position of the starting point
        '''
        result = {}
        result = self.obtain_routes(starting_point,destination_point,"walking",True)
        #Poins obtained from the polyline of the routes
        points = self.obtain_point_in_routes(result)
        routes_bbs=[]
        routes_distances=[]
        #Obtain the bounding boxes for the 
        for index in range(len(result["routes"])):
            temp = self.obtain_heatmap_slow(points[index])
            routes_bbs.append(temp['bounding_boxes'])
            routes_distances.append(temp['distance'])
        new_dict = {}
        result['heatmaps']=[]
        new_dict['positive']=[]
        new_dict['negative']=[]
        result['route_index']=[]
        result['score']=[]
        utc_time = self.obtain_utc_time()
        total_numerator = 0
        total_denominator = 0
        for routeIndex in range(len(routes_bbs)):
            #logging.info(routeIndex)
            #time in seconds in milliseconds
            time = (result['routes'][routeIndex]["legs"][0]["duration"]["value"])*1000
            #distance in meters
            totalDistance=result['routes'][routeIndex]["legs"][0]["distance"]["value"]
            utc_now=utc_time
            person_index_route = utc_time+routeIndex
            result['route_index'].append(person_index_route)
            point1 = points[routeIndex][0]
            for boxIndex in  range(len(routes_bbs[routeIndex])):
                lineLength = routes_distances[routeIndex][boxIndex]
                deltaTime = time*lineLength/totalDistance
                utc_future = deltaTime + utc_now
                point2 = points[routeIndex][boxIndex+1]
                midpoint = midDistance(coordinate(point1[1],point1[0]),coordinate(point2[1],point2[0]))
                future = self.db.persons.insert({'uid':idUser,'loc':{'type':'Point','coordinates':[float(midpoint.lng),float(midpoint.lat)]},'initial':utc_now,'finish':utc_future,'index':person_index_route,'temporal':True})
                insert_result=yield future
                #logging.info(person_index_route)
                #logging.info(insert_result)
                box = routes_bbs[routeIndex][boxIndex]
                pos_temparray =[]
                neg_temparray =[]
                pos_temparray += yield self.db.positive_heatmap.find({'loc': { '$geoWithin' :{'$geometry': {"type":"Polygon","coordinates":box}}}},{"_id":0}).to_list(length=int(total))
                pos_temparray += yield self.db.persons.find({'loc':{'$geoWithin':{'$geometry':{'type':'Polygon','coordinates':box}}},'initial':{'$gte':utc_now},'finish':{'$lte':utc_future},'uid':{'$ne':idUser},'temporal':False},{"_id":0}).to_list(length=int(total))
                neg_temparray += yield self.db.negative_heatmap.find({'loc': { '$geoWithin' : { '$geometry' : {"type":"Polygon","coordinates":box}}}},{"_id":0}   ).to_list(length=int(total))
                numerator = 0
                denominator = 0
                for pos_item in pos_temparray:
                    if "weight" in pos_item:
                        weight = float(pos_item["weight"])
                        value = float(pos_item["value"])
                        numerator += value*weight/10.0
                        denominator += weight
                    else:
                        logging.info("Item does not has a weight value")
                for neg_item in neg_temparray:
                    if "weight" in neg_item:
                        weight = float(neg_item["weight"])
                        value = float(neg_item["value"])
                        numerator += value*weight/10.0
                        denominator += abs(neg_item["weight"])
                    else:
                        logging.info("Item does not has a weight value")
                if denominator != 0:
                    total_numerator += lineLength*numerator/denominator
                    total_denominator += lineLength
                new_dict['positive'] += pos_temparray
                new_dict['negative'] += neg_temparray
                utc_now = utc_future
            if total_denominator !=0:
                result['score'].append(total_numerator/total_denominator)    
            else:
                result['score'].append(0)
        result['heatmaps']=(new_dict)
        self.db.persons.create_index(config.ttl_index["people"],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary["people"])
        self.db.persons.create_index([('loc', '2dsphere')],background=True)

        raise Return(result)
      #TODO Calculate the vlaue and the weight for each route, normalized for each route.
        
     



    @coroutine
    def get_route_old(self,idUser,destination_point,starting_point,total=50):
        '''
        id_user: id of the user trying to get the route.
        destination_point: {lat:,long:} of the geographical position of the final destination.
        starting_point: {lat:,long:} of the geographical position of the starting point
        '''
        result = {}
        result = self.obtain_routes(starting_point,destination_point,"walking",True)
        #Points obtained from the polyline of the routes
        points = self.obtain_point_in_routes(result)
        bounding_boxes=[]
        #Obtain the bounding boxes of the routes
        for index in range(len(result["routes"])):
            bounding_boxes.append(self.obtain_heatmap(points[index],result['routes'][index]['bounds']))
        result['heatmaps'] = []
        #Obtain all the points in the heatmaps that are inside of this bounding boxes
        for bounding_box in bounding_boxes:
            coordinates=[]
            for rectangle in bounding_box:
                box = [[]]
                #Create a rectangle in the multipolygon, we need 5 positions, first and last position is the same
                box[0].append([rectangle['ul']['lng'],rectangle['ul']['lat']])
                box[0].append([rectangle['lr']['lng'],rectangle['ul']['lat']])
                box[0].append([rectangle['lr']['lng'],rectangle['lr']['lat']])
                box[0].append([rectangle['ul']['lng'],rectangle['lr']['lat']])
                box[0].append([rectangle['ul']['lng'],rectangle['ul']['lat']])                
                coordinates.append(box)
            #Create the dictionary with the responses for the query of the multipolygon
            new_dict = {}
            new_dict['positive'] = yield self.db.positive_heatmap.find({'loc': { '$geoWithin' : { '$geometry' : {"type":"MultiPolygon","coordinates":coordinates}}}},{"loc.coordinates":1,"_id":0}).to_list(length=int(total))
            new_dict['negative'] = yield self.db.positive_heatmap.find({'loc': { '$geoWithin' : { '$geometry' : {"type":"MultiPolygon","coordinates":coordinates}}}},{"loc.coordinates":1,"_id":0}   ).to_list(length=int(total))
            result['heatmaps'].append(new_dict)
        raise Return(result)        

    '''Provides utility functions for encoding and decoding linestrings using the 
    Google encoded polyline algorithm.
    '''
     
    def encode_coords(self,coords):
        '''Encodes a polyline using Google's polyline algorithm
        
        See http://code.google.com/apis/maps/documentation/polylinealgorithm.html 
        for more information.
        
        :param coords: Coordinates to transform (list of tuples in order: latitude, 
        longitude).
        :type coords: list
        :returns: Google-encoded polyline string.
        :rtype: string    
        '''
        
        result = []
        
        prev_lat = 0
        prev_lng = 0
        
        for x, y in coords:        
            lat, lng = int(y * 1e5), int(x * 1e5)
            
            d_lat = _encode_value(lat - prev_lat)
            d_lng = _encode_value(lng - prev_lng)        
            
            prev_lat, prev_lng = lat, lng
            
            result.append(d_lat)
            result.append(d_lng)
        
        return ''.join(c for r in result for c in r)
    
    def _split_into_chunks(self,value):
        while value >= 32: #2^5, while there are at least 5 bits
            
            # first & with 2^5-1, zeros out all the bits other than the first five
            # then OR with 0x20 if another bit chunk follows
            yield (value & 31) | 0x20 
            value >>= 5
        yield value
     
    def _encode_value(self,value):
        # Step 2 & 4
        value = ~(value << 1) if value < 0 else (value << 1)
        
        # Step 5 - 8
        chunks = _split_into_chunks(value)
        
        # Step 9-10
        return (chr(chunk + 63) for chunk in chunks)
     
    def decode(self,point_str):
        '''Decodes a polyline that has been encoded using Google's algorithm
        http://code.google.com/apis/maps/documentation/polylinealgorithm.html
        
        This is a generic method that returns a list of (latitude, longitude) 
        tuples.
        
        :param point_str: Encoded polyline string.
        :type point_str: string
        :returns: List of 2-tuples where each tuple is (latitude, longitude)
        :rtype: list
        
        '''
                
        # sone coordinate offset is represented by 4 to 5 binary chunks
        coord_chunks = [[]]
        for char in point_str:
            
            # convert each character to decimal from ascii
            value = ord(char) - 63
            
            # values that have a chunk following have an extra 1 on the left
            split_after = not (value & 0x20)         
            value &= 0x1F
            
            coord_chunks[-1].append(value)
            
            if split_after:
                    coord_chunks.append([])
            
        del coord_chunks[-1]
        
        coords = []
        
        for coord_chunk in coord_chunks:
            coord = 0
            
            for i, chunk in enumerate(coord_chunk):                    
                coord |= chunk << (i * 5) 
            
            #there is a 1 on the right if the coord is negative
            if coord & 0x1:
                coord = ~coord #invert
            coord >>= 1
            coord /= 100000.0
                        
            coords.append(coord)
        
        # convert the 1 dimensional list to a 2 dimensional list and offsets to 
        # actual values
        points = []
        prev_x = 0
        prev_y = 0
        for i in xrange(0, len(coords) - 1, 2):
            if coords[i] == 0 and coords[i + 1] == 0:
                continue
            
            prev_x += coords[i + 1]
            prev_y += coords[i]
            # a round to 6 digits ensures that the floats are the same as when 
            # they were encoded
            points.append((round(prev_x, 6), round(prev_y, 6)))
        
        return points            

    def obtain_routes(self,origin,destination,mode="walking",alternatives=True):
        '''
        * @origin: dictionary with keys 'lat' and 'lng' that define the origin.
        * @destination: dictionary with keys 'lat' and 'lng' that define the destination.
        * @mode: string mode of the query. E.g. walking, etc.
        * @alternatives: boolean, true or false, to obtain more than one route (if true)
        '''
        conf = config.envs["dev"]
        http_dir = "https://maps.googleapis.com/maps/api/directions/json?"
        http_dir +=("origin="+str(origin["lat"])+","+str(origin["lng"]))
        http_dir +=("&destination="+str(destination["lat"])+","+str(destination["lng"]))
        if mode is not None:
            http_dir += "&mode="+mode
        if alternatives:
            http_dir += "&alternatives=true"
        else:
            http_dir += "&alternatives=false"
	http_dir += "&key="+conf.google_api_key
        routes = eval(urlopen(http_dir).read())
        return routes

    def obtain_point_in_routes(self,routes):
        '''
        This function returns an array of arrays with the points that compose each route.
        @routes: is the json that is formed from the routes returned by the Google Maps API query.
        '''
        output=[]
        for route in routes["routes"]:
            output.append(self.decode(route["overview_polyline"]["points"]))
        return output

    def get_line_bounding_box(self,coord1,coord2):
        point1 = coord2MercPoint(coord1)
        point2 = coord2MercPoint(coord2)
        Delta = point(point1.x-point2.x,point1.y-point2.y)
        perp = point(-Delta.y,Delta.x)
        norm = sqrt(pow(perp.x,2)+pow(perp.y,2))
        perp_norm = point(perp.x/norm,perp.y/norm)
        extension = point(config.envs['dev'].box_edge_size*perp_norm.x,config.envs['dev'].box_edge_size*perp_norm.y)
        neg = point(-extension.x,-extension.y)
        ul= point(point1.x+extension.x,point1.y+extension.y)
        ll = point(point1.x+neg.x,point1.y+neg.y)
        lr = point(point2.x+neg.x,point2.y+neg.y)
        ur = point(point2.x+extension.x,point2.y+extension.y)
        box = [[]]
        #Create a rectangle in the multipolygon, we need 5 positions, first and last position is the same
        box[0].append([x2lng(ul.x),y2lat(ul.y)])
        box[0].append([x2lng(ur.x),y2lat(ur.y)])
        box[0].append([x2lng(lr.x),y2lat(lr.y)])
        box[0].append([x2lng(ll.x),y2lat(ll.y)])
        box[0].append([x2lng(ul.x),y2lat(ul.y)])
        return box

    def obtain_heatmap_slow(self,points):
        #latitude,longitude
        point1 = coordinate(points[0][1],points[0][0])
        bounding_boxes=[]
        distance=[]
        for point in points[1::]:
            point2=coordinate(point[1],point[0])
            bounding_boxes.append(self.get_line_bounding_box(point1,point2))
            distance.append(coordDistance(point1,point2))
            point1=point2
        return {"bounding_boxes":bounding_boxes,"distance":distance}

    def obtain_heatmap(self,points,boundingBox):
        boundingBoxDict = {"ul":{"lat": boundingBox["northeast"]["lat"],"lng":boundingBox["southwest"]["lng"]},"lr":{"lat": boundingBox["southwest"]["lat"],"lng":boundingBox["northeast"]["lng"]}}
        ulDict = boundingBoxDict['ul']
        lrDict = boundingBoxDict['lr']
        upperLeft = coordinate(ulDict['lat'], ulDict['lng'])
        lowerRight = coordinate(lrDict['lat'], lrDict['lng'])
        shapePointsDict = points
        routeCoords = []
        for obj in shapePointsDict:
            coord = coordinate(obj[1], obj[0])
            routeCoords.append(coord)
        rb = boxer.RouteBoxer(routeCoords, upperLeft, lowerRight)
        rb.buildGrid()
        rb.mergeIntersectingCells()
        boxes = rb.boxes()       
        boxCoords = rb.boxCoords()
        return boxCoords

    def obtain_utc_time(self):
        time_now = datetime.now()
        if time_now.utcoffset() is not None:
            time_now = time_now - time_now.utcoffset()
        millis = int(calendar.timegm(time_now.timetuple())*1000+time_now.microsecond/1000)
        return millis
