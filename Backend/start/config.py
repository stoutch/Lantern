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
		 google_api_key,
                 box_edge_size,
                 cas_server,
                 service_url,
                 options=None):
        self.mongo_host = mongo_host
        self.mongo_port = mongo_port
        self.db_name = db_name
        self.tornado_port = tornado_port
        self.secret = secret
        self.google_api_key=google_api_key
        self.box_edge_size=box_edge_size
        self.cas_server=cas_server
        self.service_url=service_url
        self.options = None

#dev environment
dev = Config(mongo_host="localhost",
             mongo_port=27017,
             db_name="mas_dev",
             tornado_port=8080,
             secret = '0c1507d0-c020-4b3c-aed8-b3ce06b1aca6',
             google_api_key = "AIzaSyDvKcr67ZmGSUrdOi6TNAjR1cs6VRe1I90",
             box_edge_size = 20,
             cas_server = "https://login.gatech.edu/cas",
             service_url= "http://173.236.254.243:8080/cas_login",
             options={'bypass_login' : True}
             )

#production environment
prod = Config(mongo_host="localhost",
              mongo_port=24171,
              db_name="mas",
              tornado_port=8080,
              secret = '0c1507d0-c020-4b3c-aed8-b3ce06b1aca6',
	      google_api_key = "AIzaSyDvKcr67ZmGSUrdOi6TNAjR1cs6VRe1I90",
	      box_edge_size = 20,
              cas_server = "https://login.gatech.edu/cas",
	      service_url= "http://173.236.254.243:8080/cas_login",
              )

#percentual weight of the different type of elements in the map
weights = {"lighting":10,
           "distance":25,
           "people":50,
           "police_tower":15,
           "police_station":15,
           "incident":-15,
           "security_rating":15,
           "scenic_rating":0,
	  }

ttl_index = {"lighting":"lighting_index",
            "people":"people_index",
            "police_tower":"tower_index",
            "police_station":"police_index",
            "incident":"incident_index",
            "security_rating":"security_index",
            "scenic_rating":"scenic_index",
            }

ttl_dictionary = {"police_tower":1*24*3600,
             "incident":1*30*24*3600,
             "security_rating":1*30*24*3600,
             "scenic_rating":1*30*24*3600,
             "people":1*24*3600}



# Add all environments to this hash.
# This is to be able to use them later in the startup script
# from argparse
envs = {'dev': dev, 'prod': prod}
