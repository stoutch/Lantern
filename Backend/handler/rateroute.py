from base import BaseHandler
import json
from bson import json_util
import tornado.web

class RateRoute(BaseHandler):
    SUPPORTED_METHODS='POST'
#TODO: User should be logged
#    @tornado.web.authenticated
    @tornado.gen.coroutine
    def post(self,route_index):
#        idUser = self.current_user['_id']
        idUser = 1
        model = self.settings['model']
        rating = self.get_argument("rating")
        day = self.get_argument("day",True)
        response = yield model.rate_route(idUser,route_index,rating,day)
        if response:
            self.write(json.dumps({'success': True},default=json_util.default))
        else:
            self.write({'success': False})
        return
