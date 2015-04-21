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

class _SelectRoute:
    '''
    ***************************************************************************
                                Select Route
    ***************************************************************************
    '''
    @coroutine
    def rate_route(self,idUser,route_index,rating,day):
        result = yield self.db.persons.find({'uid':idUser,'index':long(route_index)}).to_list(length=int(3092))
        insert_array = []
        type_item = 'security_rating'
        #logging.info(result)
        for element in result:
            logging.info(element)
            add_dict={}
            add_dict['loc']=element['loc']
            add_dict['_user_id']=idUser
            add_dict['type']=type_item
            add_dict['weight']=config.weights[type_item]
            add_dict['value']=rating
            add_dict['day']=day
            if type_item in config.ttl_index:
                add_dict[config.ttl_index[type_item]]=self.obtain_utc_time()
            insert_array.append(add_dict)
        result = []
        if insert_array:
            if rating > 0:
                result = yield self.db.positive_heatmap.insert(insert_array)
                if type_item in config.ttl_dictionary:
                    self.db.positive_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
            else:
                result = yield self.db.negative_heatmap.insert(insert_array)
                if type_item in config.ttl_dictionary:
                    self.db.negative_heatmap.create_index(config.ttl_index[type_item],sparse=True,background=True,expireAfterSeconds=config.ttl_dictionary[type_item])
        raise Return(result)

         
    @coroutine
    def set_route(self,idUser,route_index):
        result = yield self.db.persons.update({'uid':idUser,'index':long(route_index)},{'temporal':False})
        erase_result = yield self.db.persons.remove({'uid':idUser,'index':{'$ne':long(route_index)}})
        #logging.info(result)
        raise Return(result)

    @coroutine
    def update_route(self,idUser,route_index,position,radius):
        temp_result = yield self.db.persons.find({'uid':idUser,'index':long(route_index),'loc':{'$nearSphere':{'$geometry':{'type':'Point','coordinates':[float(position['lng']),float(position['lat'])],'$maxDistance':float(radius)}}}}).to_list(length=int(1))
        result = {}
        #logging.info(temp_result)
        if temp_result:
            utc_now = self.obtain_utc_time()    
            value = temp_result[0]
            utc_initial = value['initial']
            delta = utc_now-utc_initial
            result = yield self.db.persons.update({'uid':idUser,'index':long(route_index),'initial':{'$ge':utc_initial}},{'$inc':{'initial':delta,'finish':delta}})
        else:
            result['success']=False
        raise Return(result)
        #Calculate the vlaue and the weight for each route, normalized for each route.
        
    def obtain_utc_time(self):
        time_now = datetime.now()
        if time_now.utcoffset() is not None:
            time_now = time_now - time_now.utcoffset()
        millis = int(calendar.timegm(time_now.timetuple())*1000+time_now.microsecond/1000)
        return millis




