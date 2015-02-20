from user import _User
from heatmap import _Heatmap
from routes import _Route

class Model(_User, _Heatmap, _Route):
    ''' Contains methods to access the underlying mongodb collections
    '''
    def __init__(self, db):
        self.db = db
