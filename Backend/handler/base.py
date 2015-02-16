import tornado.web
import tornado.auth
import json
import logging
import datetime
from bson.json_util import dumps, loads, default

class BaseHandler(tornado.web.RequestHandler):
    '''
        Base for all MAS handlers. Contains useful decorators
    '''
    SUPPORTED_METHODS='GET'

    def set_default_headers(self):
        self.set_header('Access-Control-Allow-Method', ",".join(self.SUPPORTED_METHODS))
        self.set_header('Access-Control-Allow-Headers', 'x-requested-with, session, Content-Type')
        self.set_header('Access-Control-Allow-Origin', '*')
        self.set_header('Access-Control-Allow-Credentials', 'false')

    def get_current_user(self):
        options = self.settings['options']
        if options is not None and 'bypass_login' in options and options['bypass_login']:
            logging.info("Default user 1")
            return {'_id': 1}
        user_bson = self.get_secure_cookie('login')
        if user_bson is None: user_bson = self.get_secure_cookie('session', self.request.headers.get('session'))
        return loads(user_bson) if user_bson else None

    @classmethod
    def writes_to_callback(cls, f):
        '''
            Decorator for handler methods that support writing json responses,
            and optionally to callbacks
        '''
        def dumpjson(obj, callback=None):
            def dthandler(obj):
                if isinstance(obj, datetime.datetime) or isinstance(obj, datetime.date):
                    return obj.isoformat()
                else:
                    None
            jsonDump = json.dumps(obj, default=dthandler)
            if callback is not None: jsonDump = callback + '(' + jsonDump + ')'
            return jsonDump
        def wrapper(self, *args, **kwargs):
            callback = self.get_argument('callback', None)
            result = f(self, *args, **kwargs)
            self.write(dumpjson(result, callback))
        return wrapper


class Login(BaseHandler, tornado.auth.FacebookGraphMixin):
    '''
        Login handler
    '''
    SUPPORTED_METHODS='POST'

    @tornado.gen.coroutine
    def post(self):
        model = self.settings['model']
        login = self.get_argument('login')
        password = self.get_argument('password',None)
        if password is not None:
            u = yield model.login(login, password)
            if u:
                self.set_secure_cookie('login', value=dumps(u))
                self.write(dumps({'success': True,'response':u},default=default))
            else:
                self.write(dumps({'success': False},default=default))
        else:
            generic_id =self.get_argument('id')#Id associated to the type send before
            type_login = self.get_argument('type_login')#facebook,twitter, etc.
            u = yield model.login_third_party(login,type_login,generic_id)
            if u:
                self.set_secure_cookie('login', value=dumps(u))
                self.write(dumps({'success': True,'response':u},default=default))
            else:
                self.write(dumps({'success': False},default=default))
        return

class Logout(BaseHandler):

    @tornado.gen.coroutine
    def get(self):
        self.clear_cookie("login")
        return