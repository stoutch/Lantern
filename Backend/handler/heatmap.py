from base import BaseHandler
import json
from bson import json_util
import tornado.web

class Heatmap(BaseHandler):
    SUPPORTED_METHODS='POST,GET'
    #@tornado.web.authenticated
    @tornado.gen.coroutine
    def get(self,query):
        '''
            Get function for the query to the server
            @lat: number with the latitude of the position of the person.
            @lng: number with the longitude of the position of the person.
            @radius: radius of the heatmap to be send back
        '''
        #TODO: Verify if the person is
        #idUser = self.current_user['_id']
        latitude = self.get_argument("lat")
        longitude = self.get_argument("lng")
        radius = self.get_argument("radius",25000)
        length = self.get_argument("total",50)
        model = self.settings['model']
        response = {}
        if query == "positive":
            response = yield model.get_heatmap(latitude,longitude,radius,query,length)
        elif query == "negative":
            response = yield model.get_heatmap(latitude,longitude,radius,query,length)
        else:  
            self.write({'success': False,'error':"Query "+query+" not defined yet"})
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

    #@tornado.web.authenticated
    @tornado.gen.coroutine
    def post(self,query):
        '''
            lat: number with the latitude of the position of the person.
            lng: number with the longitude of the position of the person.
        '''
        #idUser = self.current_user['_id']        
        idUser = 1
        latitude = self.get_argument("lat")
        longitude = self.get_argument("lng")
        model = self.settings['model']
        response = {}
        if query == "positive":
            response = yield model.add_point_to_heatmap(idUser,latitude,longitude,query)
        elif query == 'negative':
            response = yield model.add_point_to_heatmap(idUser,latitude,longitude,query)
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

