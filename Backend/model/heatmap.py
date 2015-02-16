from bson.objectid import ObjectId
from tornado.gen import Return, coroutine

class _Heatmap:
    '''
    ***************************************************************************
                                HEATMAP
    ***************************************************************************
    '''

    @coroutine
    def add_point_to_heatmap(self,idUser,latitude,longitude):
        '''
        idUser: id of the user using the app.
        latitude: number that contains the latitude of the point to be added
        longitude:  number that contains the longitude of the point to be added
        '''

        add_dict = {}
        add_dict['latitude']=latitude 
        add_dict['longitude']=longitude
        future = self.db.heatmap.insert(add_dict)
        result = yield future
        raise Return(result)

     @coroutine
    def get_heatmap(self,latitude,longitude,radius):
        '''
        latitude: number that contains the latitude of the center point of the heatmap
        longitude:  number that contains the longitude of the center point of the heatmap
        radius: maximun distance from the point, that the heatmap is going to contain info about
        '''
        result = {}
        result = yield self.db.heatmap.find({'brand_id':ObjectId(brand_id)},filters).to_list()
        raise Return(result)