from user import _User
from heatmap import _Heatmap
from routes import _Route
from selectroute import _SelectRoute
import logging 

class Model(_User, _Heatmap, _Route,_SelectRoute):
    ''' Contains methods to access the underlying mongodb collections
    '''
    def __init__(self, db):
        self.db = db
