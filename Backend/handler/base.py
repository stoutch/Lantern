import tornado.web
import tornado.auth
import json
import logging
import config
import urllib
import re
import datetime
from bson.json_util import dumps, loads, default

class BaseHandler(tornado.web.RequestHandler):
    '''
        Base for all MAS handlers. Contains useful decorators
    '''
    SUPPORTED_METHODS='GET'

    def set_default_headers(self):
        self.set_header('Access-Control-Allow-Methods', ",".join(self.SUPPORTED_METHODS))
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
        password = self.get_argument('password')
        u = yield model.login(login, password)
        if u:
            self.set_secure_cookie('login', value=dumps(u))
            self.write(dumps({'success': True,'response':u},default=default))
        else:
            self.write(dumps({'success': False},default=default))


   
class Logout(BaseHandler):
    SUPPORTED_METHODS='GET'
    @tornado.gen.coroutine
    def get(self):
        self.clear_cookie("login")
        self.write(dumps({'sucess':True},default=default))
        return


class GaTechLogin(BaseHandler):
    '''
        Login handler
    '''
    SUPPORTED_METHODS='GET'

    @tornado.gen.coroutine
    def get(self):
	#redirect to cas server
        redirect_url = config.envs['prod'].cas_server + '/login?service=' + config.envs['prod'].service_url
        self.redirect( redirect_url ) 

class GatechLogout(BaseHandler):

    @tornado.gen.coroutine
    def get(self):
        self.redirect("https://login.gatech.edu/cas/login") 
        return

class CASLogin(BaseHandler):
    SUPPORTED_METHODS="GET"
    def get( self ):
        #what you finally get
        userid = None
        try:
            server_ticket = self.get_argument( 'ticket' )
        except Exception, e:
            print 'there is not server ticket in request argumets!'
            raise HTTPError( 404 )
        #validate the ST
        validate_suffix = '/proxyValidate'
        validate_url = config.envs['prod'].cas_server + validate_suffix + '?service=' + urllib.quote( config.envs['prod'].service_url ) + '&ticket=' + urllib.quote( server_ticket )
        response = urllib.urlopen( validate_url ).read()
        pattern = r'<cas:user>(.*)</cas:user>'
        match = re.search( pattern, response )
        if match:
            userid = match.groups()[ 0 ]
        if not userid:
            print 'validate failed!'
            raise HTTPError( 404 )
            self.deal_with_userid( userid )
    
    def deal_with_userid( self, userid ):
        print userid  
