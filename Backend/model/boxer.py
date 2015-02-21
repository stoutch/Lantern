'''	
RouteBoxer
Given a set of latitude-longitude coordinates representing a route, this module will
create a set of bounding boxes, accessible via the boxes property.
'''	

import collections
import itertools
from geoutils import *

size = collections.namedtuple('size', ['width', 'height'])
cell = collections.namedtuple('cell', ['xIdx', 'yIdx'])

class rect:
	"""Class representing a rectangle in terms of an origin point and size.
	Origin is represented as a named tuple of the form (x, y).
	Size is represented as a named tuple of the form (width, height)."""
	def __init__(self, pt, sz):
		self.origin = point(pt.x, pt.y)
		self.size = size(sz.width, sz.height)
		
		ulx = self.origin.x - self.size.width/2.0
		uly = self.origin.y + self.size.height/2.0
		self.upperLeft = point(ulx, uly)
		
		lrx = self.origin.x + self.size.width/2.0
		lry = self.origin.y - self.size.height/2.0
		self.lowerRight = point(lrx, lry)

	def __str__(self):
		return "{{origin: {0}  size: {1}}}".format(str(self.origin), str(self.size))


	#	Method to return the union of two rectangles.
	def union(self, rect2):
		minX = min(self.upperLeft.x, rect2.upperLeft.x)
		maxX = max(self.lowerRight.x, rect2.lowerRight.x)
		minY = min(self.lowerRight.y, rect2.lowerRight.y)
		maxY = max(self.upperLeft.y, rect2.upperLeft.y)
		return rect(point((minX + maxX) / 2.0, (minY + maxY) / 2.0), 
			size(math.fabs(maxX-minX), math.fabs(maxY-minY)))
			
	def boundingCoords(self):
		ul = mercPoint2Coord(self.upperLeft)
		lr = mercPoint2Coord(self.lowerRight)
		return {'ul': {'lat': ul.lat, 'lng':ul.lng}, 'lr': {'lat': lr.lat, 'lng': lr.lng}}

class grid:
	"""Class representing a grid of cell tuples; its methods help build up a 
	list of marked cells.
	Internally, grid uses a set to keep track of marked cells.
	Each marked cell is represented as a named tuple of the form (xIdx, yIdx)."""
	def __init__(self, boundingRect, edgeLength):
		self.boundingRect = boundingRect
		self.edgeLength = edgeLength
		self.upperLeftCell = self.cellForMapPoint(boundingRect.upperLeft)
		self.lowerRightCell = self.cellForMapPoint(boundingRect.lowerRight)
		self.xIndices = range(self.upperLeftCell.xIdx, self.lowerRightCell.xIdx+1) 
		self.yIndices = range(self.lowerRightCell.yIdx, self.upperLeftCell.yIdx+1)
		self.markedCells = set()
		
	def __str__(self):
		return str(self.__dict__)

	def markCell(self, acell ):
		self.markedCells.add( acell )
	
	def unmarkCell(self, acell ):
		self.markedCells.remove( acell )
		
	def markCellAndNeighbors(self, acell ):
		minX = int(max(min(self.xIndices), acell.xIdx-1))
		maxX = int(min(max(self.xIndices), acell.xIdx+2))
		minY = int(max(min(self.yIndices), acell.yIdx-1))
		maxY = int(min(max(self.yIndices), acell.yIdx+2))
		for c in map(cell._make, itertools.product(*[range(minX, maxX), range(minY, maxY)])):
			self.markCell(c)
	
	def cellMarked(self, acell):
		return acell in self.markedCells
		
	def cellForMapPoint(self, pt):
		"""Returns the cell for the given map (Mercator) point"""
		normX = pt.x - self.boundingRect.origin.x
		normY = pt.y - self.boundingRect.origin.y
		xIdx = int(round( normX / self.edgeLength ))
		yIdx = int(round( normY / self.edgeLength ))
		return cell(xIdx, yIdx)
	
	def mapPointForCell(self, acell ):
		x = acell.xIdx * self.edgeLength + self.boundingRect.origin.x
		y = acell.yIdx * self.edgeLength + self.boundingRect.origin.y
		return point(x, y)
		
	def mapRectForCell(self, acell ):
		return rect( self.mapPointForCell( acell ), 
			size(self.edgeLength, self.edgeLength) )
		
	def cellsAreAdjacent(self, cell1, cell2):
		return (((abs(cell1.xIdx - cell2.xIdx) == 1) and (cell1.yIdx == cell2.yIdx)) 
			or ((cell1.xIdx == cell2.xIdx) and (abs(cell1.yIdx - cell2.yIdx) == 1)))	
	

class RouteBoxer:
	"""Class to decompose a route of points, given in lat/lng format, into a list 
	of bounding boxes."""
	EDGE_LENGTH = 20	# Edge length in meters
	
	def __init__(self, path, upperLeft, lowerRight):
		self.path = path
		self.upperLeftCoord = upperLeft
		self.lowerRightCoord = lowerRight
		self.upperLeftMapPoint = coord2MercPoint(upperLeft)
		self.lowerRightMapPoint = coord2MercPoint(lowerRight)
		deltaX = math.fabs(self.lowerRightMapPoint.x - self.upperLeftMapPoint.x)
		deltaY = math.fabs(self.upperLeftMapPoint.y - self.lowerRightMapPoint.y)
		originX = self.upperLeftMapPoint.x + deltaX / 2.0
		originY = self.lowerRightMapPoint.y + deltaY / 2.0
		boundingBox = rect(point(originX, originY), size(deltaX, deltaY))
		self.grid = grid(boundingBox, RouteBoxer.EDGE_LENGTH)
		self.boxesX = []
		self.boxesY = []
		self.buildGrid()
		self.mergeIntersectingCells()
	
	def buildGrid(self):
		lastMapPoint = coord2MercPoint(self.path[0])
		lastCell = self.grid.cellForMapPoint(lastMapPoint)
		self.grid.markCellAndNeighbors(lastCell)
	
		for loc in self.path:
			curMapPoint = coord2MercPoint(loc)
			curCell = self.grid.cellForMapPoint(curMapPoint)
			# TODO:  test for failure to find cell
			# If the current cell is the same cell as the last one, skip it.
			if curCell == lastCell:
				continue
			self.grid.markCellAndNeighbors(curCell)
			
			# If the cell is next to the last cell, continue.
			if self.grid.cellsAreAdjacent(curCell, lastCell):
				continue
			# Otherwise, the cells are at some distance from each other.
			# If the cells share the same X or Y index, mark the intervening ones.
			if curCell.xIdx == lastCell.xIdx:
				lowerY = min(lastCell.yIdx, curCell.yIdx)
				upperY = max(lastCell.yIdx, curCell.yIdx)
				for y in range (lowerY, upperY + 1):
					self.grid.markCellAndNeighbors( cell(curCell.xIdx, y) )
			
			elif curCell.yIdx == lastCell.yIdx:
				lowerX = min(lastCell.xIdx, curCell.xIdx)
				upperX = max(lastCell.xIdx, curCell.xIdx)
				for x in range(lowerX, upperX+1):
					self.grid.markCellAndNeighbors( cell(x, curCell.yIdx) )
			else:
			# The cells lie on a line not sharing an X or Y index.
			# Calculate the slope and length of the line connecting the last and
			# current map points.
				deltaX = curMapPoint.x - lastMapPoint.x
				deltaY = curMapPoint.y - lastMapPoint.y
				length = math.sqrt(pow(deltaY, 2) + pow(deltaX, 2))
				theta = math.atan2(deltaY, deltaX)
			# Calculate the number of mapPointRange segments are in the line
			# connecting the last and current map point.  Iterate over them and 
			# mark the enclosing cells. This is equivalent to walking the connecting
			# line in segments of mapPointRange length and marking the enclosing
			# cells. Since the segment used is shorter than the sides of the cells,
			# we're guaranteed to hit almost every intervening cell. The
			# markCellAndNeighbors function takes care of the rest.
				numSegments = int(math.floor(length / RouteBoxer.EDGE_LENGTH))
				if numSegments > 1:
					for i in range(numSegments):
						nextX = lastMapPoint.x + i * RouteBoxer.EDGE_LENGTH * math.cos(theta)
						nextY = lastMapPoint.y + i * RouteBoxer.EDGE_LENGTH * math.sin(theta)
						nextMapPoint = point(nextX, nextY)
						nextCell = self.grid.cellForMapPoint(nextMapPoint)
						self.grid.markCellAndNeighbors(nextCell)
			
			lastMapPoint = curMapPoint
			lastCell = curCell			

	def mergeIntersectingCells(self):
		self.boxesX = []
		self.boxesY = []
		curMapRect= None
		for y in self.grid.yIndices:
			for x in self.grid.xIndices:
				if self.grid.cellMarked( cell(x,y) ):
					if curMapRect is None:
						curMapRect = self.grid.mapRectForCell( cell(x,y) )
					else:
						newRect = self.grid.mapRectForCell( cell(x,y) )
						curMapRect = curMapRect.union(newRect)	
				else:
					if curMapRect:
						self.boxesX.append(curMapRect)
						curMapRect = None
			if curMapRect:
				self.boxesX.append(curMapRect)
				curMapRect = None
		
		curMapRect = None
		for x in self.grid.xIndices:
			for y in self.grid.yIndices:
				if self.grid.cellMarked( cell(x,y) ):
					if curMapRect is None:
						curMapRect = self.grid.mapRectForCell( cell(x,y) )
					else:
						curMapRect = curMapRect.union(self.grid.mapRectForCell( cell(x,y) ))
				else:
					if curMapRect:
						self.boxesY.append(curMapRect)
						curMapRect = None
			if curMapRect:
				self.boxesY.append(curMapRect)
				curMapRect = None
		
	def boxes(self):
		if len(self.boxesX) < len(self.boxesY):
			return self.boxesX
		else:
			return self.boxesY
	
	def boxCoords(self):
		coords = []
		for b in self.boxes():
			coords.append(b.boundingCoords())
		return coords	
		
