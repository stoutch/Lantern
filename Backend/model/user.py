import hashlib
import logging
from bson.objectid import ObjectId
from tornado.gen import Return, coroutine
from datetime import datetime

class _User:
    '''
    **********************************************************************
                                USER FUNCTIONS
    **********************************************************************
    '''
    def _hashPassword(self, password):
        return hashlib.sha256(password).hexdigest()

    @coroutine
    def _ensureUnique(self):
        '''
        This function is called before the creation of users, it ensures that the
        users are unique by the email index.
        '''
        r = yield self.db.users.ensure_index("email",unique=True,dropDups=True)
        raise Return(r)


    @coroutine
    def login(self, login, password):
        '''
        This function look for the user that is being logged in.
        '''
        hashPass = self._hashPassword(password)
        user_bson = yield self.db.users.find_one({'email': login, 'password': hashPass},{'password':0,'login_stamp':0})
        logging.info("{0}".format(user_bson))
        if user_bson is not None:
            temp = yield self.db.users.update({'_id':ObjectId(user_bson['_id'])},{'$addToSet':{'login_stamp':datetime.now()}})
            logging.info("logging stamp: {0}".format(temp))
        raise Return(user_bson)

    @coroutine
    def getUser(self,idUser):
        '''
           This function finds a user by its ID.
        '''
        user_bson = yield self.db.users.find_one({'_id': ObjectId(idUser)},{'_id':0,'password':0})
        raise Return(user_bson)


    @coroutine
    def insertUser(self,name,email,password,facebook_id,birth_day,genre,phone):
        '''
        Function to insert users to the collection
        '''
        #Create an uniqueness constraint on the users collections
        var = locals()
        yield self._ensureUnique()
        #Hash the password
        hash_pass = self._hashPassword(password)
        #Create the document to insert with the names used in the collection
        #TODO transform to bson
        #Create the document to insert with the names used in the collection
        document = {}
        list_value = ['name', 'email', 'genre','facebook_id','birth_day','phone']
        for i in list_value:
            if var[i] is not None:
                document[i]=var[i]
        document['password']=hash_pass
        document['user_status']=True
        try: ###Verify if the email already exists
            future = self.db.users.insert(document)
            result = yield future
        except: ###Error raised, duplicate index for email.
            logging.info("User already exists")
            result = None
        raise Return(result)

    '''
    function used to modify users, equivalent to the introduction to users
    '''
    @coroutine
    def modifyUser(self,
                   id_user,
                   nombre=None,
                   email=None,
                   password=None,
                   facebook_id=None,
                   birthday=None,
                   user_status=None,
                   phone=None):
        change_dict = {}
        #For every not none value, add it to the dictionary
        if nombre is not None:
            change_dict['name']=nombre
        if email is not None:
            change_dict['email']=email
        if password is not None:
            hashPass = hashlib.sha256(password).hexdigest()
            change_dict['password']=hashPass
        if facebook_id is not None:
            change_dict['facebook_id']=facebook_id
        if birthday is not None:
            change_dict['birth_day']=birthday
        if user_status is not None:
            change_dict['user_status']=user_status
        if phone is not None:
            change_dict['phone']=phone
        #update each position in the dictionary for the user
        result = yield self.db.users.update({'_id':ObjectId(id_user)},{'$set':change_dict})
        raise Return(result)
