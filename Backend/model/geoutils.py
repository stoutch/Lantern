'''
Data types and functions translating between the (x,y) coordinate system and  
latitude/longitude coordinates.
'''
import collections
import math

EARTH_RADIUS = 6378137

point = collections.namedtuple('point', ['x', 'y'])
coordinate = collections.namedtuple('coordinate', ['lat', 'lng'])

def midDistance(coord1,coord2):
    midPoint = point((lng2x(coord1.lng)+lng2x(coord2.lng))/2,(lat2y(coord1.lat)+lat2y(coord2.lat))/2)
    return mercPoint2Coord(midPoint)

def coordDistance(coord1,coord2):
    dlon=coord2.lng-coord1.lng
    dlat=coord2.lng-coord1.lng
    a=(math.pow(math.sin(dlat/2),2)+math.cos(coord1.lat)*math.cos(coord2.lat)*(math.pow(math.sin(dlon/2),2)))
    c=2*math.atan2(math.sqrt(a),math.sqrt(1-a))
    d=EARTH_RADIUS*c
    return math.fabs(d)

def deg2rad(deg):
	return deg * math.pi / 180.0
	
def rad2deg(rad):
	return rad * 180.0 / math.pi
	
def x2lng(x):
	return rad2deg( x/EARTH_RADIUS )
	
def lng2x(lng):
	return deg2rad(lng) * EARTH_RADIUS

def y2lat(y):
	return rad2deg( (2.0*math.atan(math.exp(y / EARTH_RADIUS))-math.pi/2.0) )

def lat2y(lat):
	return math.log( math.tan ( math.pi / 4.0 + deg2rad(lat) / 2.0 ) ) * EARTH_RADIUS
#  return 180.0/math.pi*math.log(math.tan(math.pi/4.0+a*(math.pi/180.0)/2.0))

def coord2MercPoint(coord):
	return point(lng2x(coord.lng), lat2y(coord.lat))
	
def mercPoint2Coord(pt):
	return coordinate( y2lat(pt.y), x2lng(pt.x))
