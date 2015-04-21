from base import BaseHandler
import json
from bson import json_util
import tornado.web

class SelectRoute(BaseHandler):
    SUPPORTED_METHODS='POST'
#TODO: User should be logged
#    @tornado.web.authenticated
    @tornado.gen.coroutine
    def post(self,route_index):
#        idUser = self.current_user['_id']
        idUser = 1
        model = self.settings['model']
        response = yield model.set_route(idUser,route_index)
        if response:
            self.write(json.dumps({'success': True},default=json_util.default))
        else:
            self.write({'success': False})
        return
