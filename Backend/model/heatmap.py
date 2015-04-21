from bson.objectid import ObjectId
import logging
from tornado.gen import Return, coroutine
from datetime import datetime
import calendar
import config

class _Heatmap:
    '''
    ***************************************************************************
    *                        HEATMAP
    ***************************************************************************
    '''    
    @coroutine
    def add_point_to_heatmap(self,idUser,latitude,longitude,store_type,type_item,value,day):
        '''
        idUser: id of the user using the app.
        latitude: number that contains the latitude of the point to be added
        longitude:  number that contains the longitude of the point to be added
        store_type: is the type of item being stored, either positive or negative
        type_item: is the type of item (lighting, rating,distance,people,police,incident,security_rating,scenic_rating
        value: is the value from -10 to 10 that is assigned to that as a rating.
	day: string that contain either "True" or "False" for day or night
        '''
        result = []
        if store_type == "positive":
            inner_dict = {}
            inner_dict['type']="Point"
            inner_dict['coordinates']=  [float(longitude),float(latitude)]
            add_dict = {}
            add_dict['loc']=inner_dict
            add_dict['_user_id']=idUser
            add_dict['type']=type_item
            add_dict['weight']=config.weights[type_item]
            add_dict['value']=value
            add_dict['day']=day
            if type_item in config.ttl_index:
                add_dict[config.ttl_index[type_item]]=self.obtain_utc_time()
            future = self.db.positive_heatmap.insert(add_dict)
            result = yield future
            self.db.positive_heatmap.create_index([('loc', '2dsphere')])            
            if type_item in config.ttl_dictionary:
	        self.db.positive_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
        elif store_type == "negative":
            inner_dict = {}
            inner_dict['type']="Point"
            inner_dict['coordinates']=  [float(longitude),float(latitude)]
            add_dict={}
            add_dict['loc']=inner_dict
            add_dict['_user_id']=idUser            
            add_dict['type']=type_item
            add_dict['weight']=config.weights[type_item]
            add_dict['value']=value
            add_dict['day']=day
            if type_item in config.ttl_index:
                add_dict[config.ttl_index[type_item]]=self.obtain_utc_time()
            future = self.db.negative_heatmap.insert(add_dict)
            result = yield future           
            self.db.negative_heatmap.create_index([('loc', '2dsphere')])            
            if type_item in config.ttl_dictionary:
                self.db.negative_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
        raise Return(result)

    @coroutine
    def add_list_of_points_to_heatmap(self,idUser,list_of_points):
        '''
        idUser: id of the user using the app.
        latitude: number that contains the latitude of the point to be added
        longitude:  number that contains the longitude of the point to be added
        store_type: is the type of item being stored, either positive or negative
        type_item: is the type of item (lighting, rating,distance,people,police,incident,security_rating,scenic_rating
        value: is the value from -10 to 10 that is assigned to that as a rating.
	day: string that contain either "True" or "False" for day or night
        '''
        result = []
        for point in list_of_points:
            new_point = point
            #logging.info(point)
            if 'value' in new_point:
                value = new_point["value"]
                if new_point['value'] >= 0:
                    store_type = "positive"
                else: 
                    store_type = "negative"
            else:
                logging.info("Error no value present in one of the elements")
                result.append({"success":False})
                continue
            if "longitude" in new_point:
                longitude = float(new_point['longitude'])
            else:
                logging.info("Error no longitude present in one of the elements")
                result.append({"success":False})
                continue
            if "latitude" in new_point:
                latitude = float(new_point['latitude'])
            else:
                logging.info("Error no latitude present in one of the elements")
                result.append({"success":False})
                continue
            if "type" in new_point:
                type_item = new_point['type']
            else: 
                logging.info("Error no type present in one of the elements")
                result.append({"success":False})
                continue
            if type_item not in config.weights:
                logging.info("Error weight for the element does not exists, element not yet present")
                result.append({"success":False})
                continue
            if "day" in new_point:
                day = new_point['day']
            else:
                day = False
            if store_type == "positive":
                inner_dict = {}
                inner_dict['type']="Point"
                inner_dict['coordinates']=  [float(longitude),float(latitude)]
                add_dict = {}
                add_dict['loc']=inner_dict
                add_dict['_user_id']=idUser
                add_dict['type']=type_item
                add_dict['weight']=config.weights[type_item]
                add_dict['value']=value
                add_dict['day']=day
                if type_item in config.ttl_index:
                    add_dict[config.ttl_index[type_item]]=self.obtain_utc_time()
                future = self.db.positive_heatmap.insert(add_dict)
                temp_result = yield future
                result.append({'success':True,"response":temp_result})
                self.db.positive_heatmap.create_index([('loc', '2dsphere')])            
                if type_item in config.ttl_dictionary:
	            self.db.positive_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
            elif store_type == "negative":
                inner_dict = {}
                inner_dict['type']="Point"
                inner_dict['coordinates']=  [float(longitude),float(latitude)]
                add_dict={}
                add_dict['loc']=inner_dict
                add_dict['_user_id']=idUser            
                add_dict['type']=type_item
                add_dict['weight']=config.weights[type_item]
                add_dict['value']=value
                add_dict['day']=day
                if type_item in config.ttl_index:
                    add_dict[config.ttl_index[type_item]]=self.obtain_utc_time()
                future = self.db.negative_heatmap.insert(add_dict)
                temp_result = yield future
                result.append({'success':True,"response":temp_result})
                self.db.negative_heatmap.create_index([('loc', '2dsphere')])            
                if type_item in config.ttl_dictionary:
                    self.db.negative_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
        raise Return(result)



    @coroutine
    def get_heatmap(self,latitude,longitude,radius,store_type,total):
        '''
        latitude: number that contains the latitude of the center point of the heatmap
        longitude:  number that contains the longitude of the center point of the heatmap
        radius: maximun distance from the point, that the heatmap is going to contain info about
        '''
        #TODO add authentication and obtaining the user id
        idUser=1
        result = {}
        compare_dict = {}
        compare_dict['type']="Point"
        compare_dict['coordinates']=  [float(longitude),float(latitude)]        
        utc_now = self.obtain_utc_time()
        if store_type == "positive":    
            result = yield self.db.positive_heatmap.find({'loc': { '$nearSphere' : { '$geometry' : compare_dict,'$maxDistance':float(radius),'$minDistance':0}}}).to_list(length=int(total))
            result += yield self.db.persons.find({'loc':{'$nearSphere':{'$geometry':compare_dict,'$maxDistance':float(radius),'$minDistance':0}},'initial':{'$gte':utc_now},'uid':{'$ne':idUser}},{"loc.coordinates":1,"_id":0}).to_list(length=int(total))
        elif store_type == "negative":
            result = yield self.db.negative_heatmap.find({'loc':{'$nearSphere':{'$geometry':compare_dict,'$maxDistance':float(radius),'$minDistance':0}}}).to_list(length=int(total))
        raise Return(result)

    def obtain_utc_time(self):
        time_now = datetime.now()
        if time_now.utcoffset() is not None:
            time_now = time_now - time_now.utcoffset()
        millis = int(calendar.timegm(time_now.timetuple())*1000+time_now.microsecond/1000)
        return millis

