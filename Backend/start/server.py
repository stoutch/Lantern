from model.model import Model
import logging
import tornado.web
import tornado.ioloop
import tornado.options
from tornado.options import define, options

from handler.base import Login, Logout,CASLogin
from handler.user import User
from handler.routes import Route
from handler.rateroute import RateRoute
from handler.heatmap import Heatmap 
from handler.list_heatmap import List_Heatmap
from handler.selectroute import SelectRoute
import motor

class Stop(tornado.web.RequestHandler):
    def get(self):
        ioloop = tornado.ioloop.IOLoop.instance()
        ioloop.add_callback(lambda x : x.stop(), ioloop)

def start(conf):
    motorClient = motor.MotorClient(conf.mongo_host, conf.mongo_port)
    db = motorClient[conf.db_name]
    port = conf.tornado_port

    application = tornado.web.Application([
    (r"/users",User, ),
    (r"/login", Login, ),
    (r"/logout",Logout, ),
    (r"/cas_login",CASLogin,),
    (r"/heatmaps/([a-z]*)", Heatmap),
    (r"/bulk_heatmap", List_Heatmap),
    (r"/routes", Route, ),
    (r"/routes/select/([0-9]+)",SelectRoute,),
    (r"/routes/rate/([0-9]+)",RateRoute,),
    ],
    model=Model(db),
    cookie_secret=conf.secret,
    options=conf.options,
    login_url='/login')
    tornado.options.options.log_file_prefix='/home/student/Backend/start/MASserver.log'
    tornado.options.parse_command_line() 


    print "Starting MAS Server at", port
    application.listen(port)
    tornado.ioloop.IOLoop.instance().start()
