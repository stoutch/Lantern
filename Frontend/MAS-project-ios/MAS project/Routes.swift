//
//  Routes.swift
//  MAS project
//
//  Created by ZHEN CHENG WANG on 3/29/15.
//  Copyright (c) 2015 CODEZ. All rights reserved.
//

import UIKit
import MapKit

class Routes: UIViewController, GMSMapViewDelegate, CLLocationManagerDelegate{ // last get current location
    
    //    struct myvar {
    //        var t : Int = 1
    //    }
    
    /* Globals: */
    var mapView : GMSMapView!
    var polylineSet : Array<GMSPolyline> = Array<GMSPolyline>()
    
    var manager : CLLocationManager!
    var startLat : String!
    var startLng : String!
    var startLatLng : String!
    var startMutex : Bool = true
    
    var destString: String?
    var destLat: String!
    var destLng: String!
    
    var taplat : Double = 0.0
    var taplng : Double = 0.0
    var setRouteFlag = 0
    var routes = Array<Array<CLLocationCoordinate2D>>()
    var scores = Array<Int>()
    var indices = Array<String>()
    var distance = Array<String>()
    var pathList : Array<GMSMutablePath> = Array<GMSMutablePath>()
    var pathPicked : Int = 0
    var indexPicked : String = "0"
    
    var view_width = CGFloat(0)
    var view_height = CGFloat(0)
    var label = UILabel()
    
    var Pmap : Array<JSON>!
    var Nmap : Array<JSON>!
    
    struct toNavigation{
        var iPick : String
        var pPick : GMSMutablePath
        var hPmap : Array<JSON>
        var hNmap : Array<JSON>
    }
    
    func next_screen(sender: UIButton!) {
        var url = "http://173.236.254.243:8080/routes/select/";
        url += indexPicked;
        let request = NSMutableURLRequest(URL: NSURL(string: url)!)
        request.HTTPMethod = "POST"

        let task = NSURLSession.sharedSession().dataTaskWithRequest(request) {
            data, response, error in
            
            if error != nil {
                println("error=\(error)")
                return
            }
            
            println("response = \(response)")
            
            let responseString = NSString(data: data, encoding: NSUTF8StringEncoding)
            println("responseString = \(responseString)")
        }
        task.resume()
        performSegueWithIdentifier("segue_route", sender: sender);
    }
    
    func locationManager(manager: CLLocationManager!, didUpdateLocations locations: [AnyObject]!) {
        if(startMutex){

            var destCoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(33.772360, -84.394838)//= geometry["location"]
            var destQuery = self.destString!.componentsSeparatedByString(" ") //split(self.destString as String!){$0 == " "}
            
            var queryWrapper = join("+", destQuery)
            var destURL : NSString = "https://maps.googleapis.com/maps/api/geocode/json?address="+queryWrapper
            //        var destURL : NSString = "https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA"
            var destStr : NSString = destURL.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
            var destGeoURL : NSURL = NSURL(string: destStr as String)!
            
            
//            println("outter most")
            let geocoderTask = NSURLSession.sharedSession().dataTaskWithURL(destGeoURL) {(dataGeo, response, error) in
                //                println(NSString(data: dataGeo, encoding: NSUTF8StringEncoding))
                // main thread:
                //                dispatch_async(dispatch_get_main_queue(), { () -> Void in
//                println("in dest")
                var geoReply = JSON(data: dataGeo)
                //                    println(geoReply)
                var results = geoReply["results"].array!

                destCoord = CLLocationCoordinate2DMake(results[0]["geometry"]["location"]["lat"].double!, results[0]["geometry"]["location"]["lng"].double!)

                self.destLat = "\(destCoord.latitude)"
                self.destLng = "\(destCoord.longitude)"

                var startCoord : NSString = locations.description
                var coordHead = startCoord.rangeOfString("<").location+2 // <+
                var coordTail = startCoord.rangeOfString(">").location-1 // >
                var coordLength = coordTail - coordHead
                var startString : String = startCoord as String!
                self.startLatLng = startCoord.substringWithRange(NSRange(location:coordHead, length:coordLength))

                var startArr = split(self.startLatLng) {$0 == ","}
                self.startLat = startArr[0]
                self.startLng = startArr[1]
                self.startMutex = false

                var encodeDest = "{\"lat\":"+self.destLat+",\"lng\":"+self.destLng+"}"
                var encodeStart = "{\"lat\":"+self.startLat+",\"lng\":"+self.startLng+"}"

                var url : NSString = "http://173.236.254.243:8080/routes?dest="+encodeDest+"&start="+encodeStart
                var urlStr : NSString = url.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
                var queryURL : NSURL = NSURL(string: urlStr as String)!
                
                
                let task = NSURLSession.sharedSession().dataTaskWithURL(queryURL) {(data, response, error) in
                    
                    var myJSON = NSString(data: data, encoding: NSUTF8StringEncoding)
                    println(myJSON)
                    /* Return to main thread so we can make call to Google Map SDK */
                    dispatch_sync(dispatch_get_main_queue(), { () -> Void in
                        var seeif = 1
                        //                    println("from outside:\(self.destLat)")
                        let json = JSON(data: data) // put data not the encoded one
                        let routesList : Array = json["response"]["routes"].array!
                        let routeScore : Array = json["response"]["score"].array!
                        let routeIndex : Array = json["response"]["route_index"].array!
                        let routeposHeatmap : Array = json["response"]["heatmaps"]["positive"].array!
                        let routenegHeatmap : Array = json["response"]["heatmaps"]["negative"].array!
                        
                        self.Pmap = routeposHeatmap
                        self.Nmap = routenegHeatmap
                        
                        //                                             Display heatmaps
                        if(routeposHeatmap.count > 0) {
                            for i in 0...routeposHeatmap.count-1{
                                var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(routeposHeatmap[i]["loc"]["coordinates"][1].double!, routeposHeatmap[i]["loc"]["coordinates"][0].double!)

                                var circ = GMSCircle(position: pointcoord, radius: 5)
                                circ.fillColor = UIColor(red: 0.8, green: 0, blue: 0, alpha: 1)
                                circ.strokeColor = UIColor.redColor()
                                circ.map = self.mapView;
                            }
                        }
                        
                        if(routenegHeatmap.count > 0) {
                            for i in 0...routenegHeatmap.count-1{
                                var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(routenegHeatmap[i]["loc"]["coordinates"][1].double!, routenegHeatmap[i]["loc"]["coordinates"][0].double!)

                                var circ = GMSCircle(position: pointcoord, radius: 5)
                                circ.fillColor = self.hexStringToUIColor("#6495ED")
                                circ.strokeColor = self.hexStringToUIColor("#6495ED")
                                circ.map = self.mapView;
                            }
                        }
                        
                        var max_score = Int.min

                        for i in 0...routesList.count-1{
                            var score = Int(routeScore[i].double! * 100)
                            self.scores.append(score)
                            
                            var route_string = ""
                            if let route_id = routeIndex[i].number {
//                                self.indices.append(routeIndex[i].string!)
                                self.indices.append(route_id.description);
                                route_string = route_id.description;
                            }
                            else {
                                println(routeIndex[i]);
                            }
                            if(max_score < score)
                            {
                                max_score = score
                                self.indexPicked = route_string
                                self.pathPicked = i
                            }
                            var dis = ""
                            if let dis = routesList[i]["legs"]["duration"]["text"].string {
//                            self.time.append().string!)
                                println(dis);
                            }
                        }
                        
                        self.routes = Array<Array<CLLocationCoordinate2D>>()
                        
                        /* Paths to render */
                        self.pathList = Array<GMSMutablePath>()
                        
                        for i in 0...routesList.count-1{
                            var currRoute = routesList[i]["legs"][0] // no waypoints set
                            var currSteps = currRoute["steps"].array!
                            var routesItem = Array<CLLocationCoordinate2D>()
                            
                            var currPath = GMSMutablePath()
                            for j in 0...currSteps.count-1{ // notice the index
                                var currStep = currSteps[j]
                                
                                var currStart = currStep["start_location"]
                                var testcurr = currStep["start_location"]["lat"]
                                //                        println(testcurr)
                                var startCoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(currStart["lat"].double!, currStart["lng"].double!)
                                routesItem.append(startCoord)
                                
                                var currEnd = currStep["end_location"]
                                var endCoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(currEnd["lat"].double!, currEnd["lng"].double!)
                                routesItem.append(endCoord)
                                
                                currPath.addCoordinate(startCoord)
                                currPath.addCoordinate(endCoord)
                            }
                            self.routes.append(routesItem)
                            self.pathList.append(currPath)
                        }
                        
                        
                        /* Render paths on map */
                        var R = CGFloat(20)
                        var G = CGFloat(20)
                        var B = CGFloat(20)

                        for i in 0...self.pathList.count-1{
                            var polycolor = UIColor(red: R/255.0, green: G/255.0, blue: B/255.0, alpha: 1.0)
                            if(i == self.pathPicked)
                            {
                                polycolor = UIColor.greenColor()
                            }
                            var polyline = GMSPolyline(path : self.pathList[i])
                            polyline.spans = [GMSStyleSpan(color: polycolor)]
                            polyline.strokeWidth = CGFloat(6.0)
                            polyline.map = self.mapView
                            self.polylineSet.append(polyline)
                            
                            R = CGFloat(R+50)
                            G = CGFloat(G+50)
                            B = CGFloat(B+50)
                        }
                        
                        self.polylineSet[self.pathPicked].spans = [GMSStyleSpan(color: UIColor.greenColor())]
                        
                        self.label.frame = CGRectMake(0, self.view_height - 40, self.view_width, 40)
                        self.label.backgroundColor = self.hexStringToUIColor("#F3F0ED");
                        self.label.text = "Score: " + self.scores[self.pathPicked].description;
                        self.label.textAlignment = .Center;
                        self.label.textColor = UIColor.grayColor();
                        
                        var button1 = UIButton(frame: CGRectMake(0, self.view_height - 80, self.view_width, 40));
                        button1.setTitle("Start Navigation", forState: .Normal);
                        button1.setTitleColor(UIColor.whiteColor(), forState: .Normal);
                        button1.titleLabel!.textAlignment = .Center;
                        button1.backgroundColor = self.hexStringToUIColor("#5A5399");
                        button1.addTarget(self, action: Selector("next_screen:"), forControlEvents: UIControlEvents.TouchUpInside);
                        
                        self.view.addSubview(self.label);
                        self.view.addSubview(button1);
                    })
                }
                task.resume()
            }
            geocoderTask.resume()
        }
    }
    
    override func viewDidLoad() {
        
        
        
        super.viewDidLoad()
        view_width = self.view.frame.size.width;
        view_height = self.view.frame.size.height;
//        println(self.destString)
        
        var camera = GMSCameraPosition.cameraWithLatitude(33.777442, longitude: -84.397217, zoom: 15) // coc 33.777442, longitude: -84.397217, zoom: 14
        mapView = GMSMapView.mapWithFrame(CGRectZero, camera: camera)
        mapView.myLocationEnabled = true
        self.view = mapView
        
        /* Get start coordinate */
        self.manager = CLLocationManager()
        self.manager.delegate = self
        self.manager.desiredAccuracy = kCLLocationAccuracyBest
        self.manager.requestAlwaysAuthorization()
        self.manager.startUpdatingLocation()
        
        mapView.delegate = self
        
    }
    
    // the function name need to be mapView
    func mapView(mapView: GMSMapView!, didLongPressAtCoordinate coord: CLLocationCoordinate2D){
        var pressLat = coord.latitude
        var pressLng = coord.longitude

            taplat = coord.latitude
            taplng = coord.longitude
            setRouteFlag = 1
        
            var candidates = Array<Int>()
            var bestCandidate : Int = -1
            var bestCandidateScore : Int = Int.min
            var bestInd : Int = 0
            var bestScore : Int = Int.min
            for i in 0...self.scores.count-1{
//                println(self.scores[i])
                if self.scores[i]>bestScore{
                    bestScore = self.scores[i]
                    bestInd = i
                }
                var currPath : GMSPath = self.pathList[i]
                if GMSGeometryIsLocationOnPathTolerance(coord, currPath, false, CLLocationDistance(10.0)){
                    candidates.append(i)
                    if self.scores[i]>bestCandidateScore{
                        bestCandidateScore = self.scores[i]
                        bestCandidate = i
                    }
                }
            }
//            println("bestCandidate:\(bestCandidate)")
//            println("bestInd:\(bestInd)")
        
            if candidates.count==0{
                self.pathPicked = bestInd
            }
            else{
                self.pathPicked = bestCandidate
            }
            
            self.indexPicked = self.indices[self.pathPicked]
//            mapView.clear()
            for i in 0...self.pathList.count-1{
                if i==self.pathPicked {
                    self.polylineSet[i].spans = [GMSStyleSpan(color: UIColor.greenColor())]
                }
                else {
                    self.polylineSet[i].spans = [GMSStyleSpan(color: UIColor.grayColor())]
                }
            }
            self.polylineSet[self.pathPicked].spans = [GMSStyleSpan(color: UIColor.greenColor())]
        
    
        
            self.label.text = "Score: " + self.scores[self.pathPicked].description;
        
//            println("pathPicked:\(self.pathPicked)")

    }
    
    /* Get the JSON file from server: */
    func getJSON(origin: String, destination: String, completionHandler: (String?, NSError?) -> Void ) -> NSURLSessionTask {
        // do calculations origin and destiantion with google distance matrix api
        
        
        var url : NSString = "http://173.236.254.243:8080/routes?dest={\"lat\":33.772601,\"lng\":-84.394774}&start={\"lat\":33.777442,\"lng\":-84.397217}" // coc to tower
        var urlStr : NSString = url.stringByAddingPercentEscapesUsingEncoding(NSUTF8StringEncoding)!
        var queryURL : NSURL = NSURL(string: urlStr as String)!
        let urlSession = NSURLSession.sharedSession()
        
        let task = urlSession.dataTaskWithURL(queryURL) { data, response, error -> Void in
            if error != nil {
                // If there is an error in the web request, print it to the console
                // println(error.localizedDescription)
                completionHandler(nil, error)
                return
            }
            completionHandler(NSString(data: data, encoding: NSUTF8StringEncoding)! as String,nil)
            
        }
        task.resume()
        return task
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "segue_route") {
            var svc = segue.destinationViewController as! navigation;

            var navigationWrapper = toNavigation(iPick: indexPicked, pPick: self.pathList[self.pathPicked], hPmap: self.Pmap, hNmap: self.Nmap)
            svc.fromRoutes = navigationWrapper

        }
    }
    
    func hexStringToUIColor (hex:String) -> UIColor {
        var cString:String = hex.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet() as NSCharacterSet).uppercaseString
        
        if (cString.hasPrefix("#")) {
            cString = cString.substringFromIndex(advance(cString.startIndex, 1))
        }
        
        if (count(cString) != 6) {
            return UIColor.grayColor()
        }
        
        var rgbValue:UInt32 = 0
        NSScanner(string: cString).scanHexInt(&rgbValue)
        
        return UIColor(
            red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
            green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
            blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
            alpha: CGFloat(1.0)
        )
    }
}