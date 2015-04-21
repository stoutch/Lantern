from base import BaseHandler
import json
from bson import json_util
import tornado.web
import ast

class List_Heatmap(BaseHandler):
    SUPPORTED_METHODS='POST'
    #@tornado.web.authenticated
    @tornado.gen.coroutine
    def post(self,query="default"):
        #idUser = self.current_user['_id']        
        idUser = 1
        list_of_elements = ast.literal_eval(self.get_argument("list_of_elements"))
        model = self.settings['model']
        response = {}
        if list_of_elements:
            response = yield model.add_list_of_points_to_heatmap(idUser,list_of_elements)
        if response:
            self.write(json.dumps({'success': True,'responses':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

