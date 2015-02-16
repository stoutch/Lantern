class Config:
    '''
        Configuration parameters for the MAS Server
    '''
    def __init__(self,
                 mongo_host,
                 mongo_port,
                 db_name,
                 tornado_port,
                 secret,
                 options=None):
        self.mongo_host = mongo_host
        self.mongo_port = mongo_port
        self.db_name = db_name
        self.tornado_port = tornado_port
        self.secret = secret
        self.options = None

#dev environment
dev = Config(mongo_host="localhost",
             mongo_port=27017,
             db_name="mas_dev",
             tornado_port=23436,
             secret = '0c1507d0-c020-4b3c-aed8-b3ce06b1aca6',
             options={'bypass_login' : True}
             )

#production environment
prod = Config(mongo_host="localhost",
              mongo_port=24171,
              db_name="mas",
              tornado_port=23436,
              secret = '0c1507d0-c020-4b3c-aed8-b3ce06b1aca6',
              )

# Add all environments to this hash.
# This is to be able to use them later in the startup script
# from argparse
envs = {'dev': dev, 'prod': prod}