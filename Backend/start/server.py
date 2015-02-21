from model.model import Model
import tornado.web
import tornado.ioloop
import tornado.options
tornado.options.parse_command_line()

from handler.base import Login, Logout
from handler.user import User
from handler.routes import Route
from handler.heatmap import Heatmap 
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
    (r"/heatmaps/([a-z]+)", Heatmap),
    (r"/routes", Route, )
    ],
    model=Model(db),
    cookie_secret=conf.secret,
    options=conf.options,
    login_url='/login')


    print "Starting MAS Server at", port
    application.listen(port)
    tornado.ioloop.IOLoop.instance().start()