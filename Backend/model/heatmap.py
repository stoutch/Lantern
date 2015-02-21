from bson.objectid import ObjectId
from tornado.gen import Return, coroutine

class _Heatmap:
    '''
    ***************************************************************************
    *                        HEATMAP
    ***************************************************************************
    '''    
    @coroutine
    def add_point_to_heatmap(self,idUser,latitude,longitude,store_type):
        '''
        idUser: id of the user using the app.
        latitude: number that contains the latitude of the point to be added
        longitude:  number that contains the longitude of the point to be added
        '''
        result = []
        if store_type == "positive":
            inner_dict = {}
            inner_dict['type']="Point"
            inner_dict['coordinates']=  [float(longitude),float(latitude)]
            add_dict = {}
            add_dict['loc']=inner_dict
            add_dict['_user_id']=idUser
            future = self.db.positive_heatmap.insert(add_dict)
            result = yield future
            self.db.positive_heatmap.create_index([('loc', '2dsphere')])            
        elif store_type == "negative":
            inner_dict = {}
            inner_dict['type']="Point"
            inner_dict['coordinates']=  [float(longitude),float(latitude)]
            add_dict={}
            add_dict['loc']=inner_dict
            add_dict['_user_id']=idUser            
            future = self.db.negative_heatmap.insert(add_dict)
            result = yield future           
            self.db.negative_heatmap.create_index([('loc', '2dsphere')])            
        raise Return(result)

    @coroutine
    def get_heatmap(self,latitude,longitude,radius,store_type,total):
        '''
        latitude: number that contains the latitude of the center point of the heatmap
        longitude:  number that contains the longitude of the center point of the heatmap
        radius: maximun distance from the point, that the heatmap is going to contain info about
        '''
        result = {}
        compare_dict = {}
        compare_dict['type']="Point"
        compare_dict['coordinates']=  [float(longitude),float(latitude)]        
        if store_type == "positive":    
            result = yield self.db.positive_heatmap.find({'loc': { '$nearSphere' : { '$geometry' : compare_dict,'$maxDistance':float(radius),'$minDistance':0}}}).to_list(length=int(total))
        elif store_type == "negative":
            result = yield self.db.negative_heatmap.find({'loc':{'$nearSphere':{'$geometry':compare_dict,'$maxDistance':float(radius),'$minDistance':0}}}).to_list(length=int(total))
        raise Return(result)