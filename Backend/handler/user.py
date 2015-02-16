from base import BaseHandler
import tornado.web
import json
import logging
from bson import json_util

class User(BaseHandler):
    ''' User'''

    SUPPORTED_METHODS=('GET', 'POST', 'PUT')

    @tornado.web.authenticated
    @tornado.gen.coroutine
    def get(self):
        idUser = self.current_user['_id']
        model = self.settings['model']
        response = yield model.getUser(idUser)
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

    @tornado.gen.coroutine
    def post(self):
        name = self.get_argument('name',None)#Name of the username
        email = self.get_argument('email')#Email, user-id for login
        password = self.get_argument('password')#Password
        facebook_id = self.get_argument('facebook_id', None)#Facebook id input
        birthday = self.get_argument('birthday')
        genre = self.get_argument('genre',None)
        telephone = self.get_argument('telephone',None)
        if telephone is not None:
            telephone = eval(telephone)
        response = yield self.settings['model'].insertUser(name,email,password,facebook_id,birthday,genre,telephone)
        logging.info("{0}".format(response))
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return

    @tornado.web.authenticated
    @tornado.gen.coroutine
    def put(self):
        idUser = self.current_user['_id']
        name = self.get_argument('name',None)#Complete name
        email = self.get_argument('email',None)#Email
        password = self.get_arguments('password',None)#User password.
        facebook_id = self.get_argument('facebook_id',None)#Facebook id
        birthday = self.get_argument('birthday')
        user_status = self.get_argument('user_status',None)
        telephone = self.get_argument('telephone',None)
        response = yield self.settings['model'].modifyUser(idUser,name,email,password,facebook_id,birthday,user_status,telephone)
        if response:
            self.write(json.dumps({'success': True,'response':response},default=json_util.default))
        else:
            self.write({'success': False})
        return