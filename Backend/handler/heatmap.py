from base import BaseHandler
import json
from bson import json_util
import tornado.web

class Heatmap(BaseHandler):

#   @tornado.web.authenticated
    @tornado.gen.coroutine
    def get(self):
#TODO: Verify if the person is
#        idUser = self.current_user['_id']
        latitude = self.get_argument("lat")
        longitude = self.get_argument("lng")
        radius = self.get_argument("radius",25)
        model = self.settings['model']
        response = yield model.get_heatmap(latitude,longitude,radius)
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

    '''
        lat: number with the latitude of the position of the person.
        lng: number with the longitude of the position of the person.
    '''
    @tornado.web.authenticated
    @tornado.gen.coroutine
    def post(self):
        idUser = self.current_user['_id']        
        latitude = self.get_argument("lat")
        longitude = self.get_argument("lng")
        model = self.settings['model']
        response = yield model.add_point_to_heatmap(idUser,latitude,longitude)
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

