//
//  navigation.swift
//  MAS project
//
//  Created by Aditya Kadur on 4/20/15.
//  Copyright (c) 2015 CODEZ. All rights reserved.
//

import UIKit
import Foundation
import CoreLocation





class navigation: UIViewController, GMSMapViewDelegate, CLLocationManagerDelegate {
    var fromRoutes : Routes.toNavigation!
    
    var myPath : GMSMutablePath!
    var positive_heatmap : Array<JSON> = []
    var negative_heatmap : Array<JSON> = []
    
    var report_buttons: Array<UIButton> = [];
    var rating_buttons: Array<UIButton> = [];
    var lighting_stars = 0;
    var rating_stars = 0;
    var lighting = UIView();
    var rating = UIView();
    var locationManager : CLLocationManager!
    var locationObj = CLLocation();
    var route_index: String?
    var view_width : CGFloat = 0.0
    var view_height : CGFloat = 0.0

    func report(sender: UIButton!) {
        lighting.removeFromSuperview()
        rating.removeFromSuperview()
        var alert = UIAlertController(title: "What would you like to report?", message: "", preferredStyle: UIAlertControllerStyle.Alert)
        
        let lightingAction = UIAlertAction(title: "Lighting", style: .Default, handler: {(action: UIAlertAction!) in
            NSLog("Reporting lighting");
            var x_gap = CGFloat(50)
            var box_width = self.view_width - 2 * x_gap
            var label_height = CGFloat(40);
            var inner_padding = CGFloat(10)
            var upper_padding = CGFloat(10) + label_height
            var star_size = (box_width - 2 * inner_padding) / 5
            var box_height = CGFloat(100) + star_size;
            
            self.lighting.backgroundColor = UIColor.whiteColor();
            self.lighting.frame = CGRectMake(x_gap, 120.0, box_width, box_height);
            self.lighting.layer.cornerRadius = 10;
            self.lighting.layer.masksToBounds = true;

        
            var label = UILabel(frame: CGRectMake(0, 0, box_width, label_height))
            label.backgroundColor = self.hexStringToUIColor("#5a5399");
            label.text = "Lighting";
            label.textColor = UIColor.whiteColor()
            label.textAlignment = .Center
        
            var one = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
            one.frame = CGRectMake(inner_padding, upper_padding, star_size, star_size);
            one.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            one.tag = 1;
            one.addTarget(self, action: Selector("report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);
            var two = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
            two.frame = CGRectMake(inner_padding + star_size, upper_padding, star_size, star_size);
            two.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            two.tag = 2;
            two.addTarget(self, action: Selector("report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);
            var three = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
            three.frame = CGRectMake(inner_padding + 2 * star_size, upper_padding, star_size, star_size);
            three.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            three.tag = 3;
            three.addTarget(self, action: Selector("report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);
            var four = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
            four.frame = CGRectMake(inner_padding + 3 * star_size, upper_padding, star_size, star_size);
            four.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            four.tag = 4;
            four.addTarget(self, action: Selector("report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);
            var five = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
            five.frame = CGRectMake(inner_padding + 4 * star_size, upper_padding, star_size, star_size);
            five.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            five.tag = 5;
            five.addTarget(self, action: Selector("report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);

            var ok_button = UIButton(frame: CGRectMake(0, upper_padding + star_size + 10, box_width/2, label_height));
            ok_button.setTitle("Ok", forState: .Normal);
            ok_button.setTitleColor(UIColor.blackColor(), forState: .Normal);
            ok_button.titleLabel!.textAlignment = .Center;
            ok_button.backgroundColor = UIColor.whiteColor();
            ok_button.addTarget(self, action: Selector("send_lighting_report:"), forControlEvents: UIControlEvents.TouchUpInside);

            var cancel_button = UIButton(frame: CGRectMake(box_width/2, upper_padding + star_size + 10, box_width/2, label_height));

            cancel_button.setTitle("Cancel", forState: .Normal);
            cancel_button.setTitleColor(UIColor.blackColor(), forState: .Normal);
            cancel_button.titleLabel!.textAlignment = .Center;
            cancel_button.backgroundColor = UIColor.whiteColor();
            cancel_button.addTarget(self, action: Selector("close_report_lighting:"), forControlEvents: UIControlEvents.TouchUpInside);
        
            self.report_buttons = [];
            self.report_buttons.append(one);
            self.report_buttons.append(two);
            self.report_buttons.append(three);
            self.report_buttons.append(four);
            self.report_buttons.append(five);

            self.lighting.addSubview(label);
            self.lighting.addSubview(one);
            self.lighting.addSubview(two);
            self.lighting.addSubview(three);
            self.lighting.addSubview(four);
            self.lighting.addSubview(five);
            self.lighting.addSubview(ok_button);
            self.lighting.addSubview(cancel_button);
        
            self.view.addSubview(self.lighting);
            
        });
        let policeAction = UIAlertAction(title: "Police Presence", style: .Default, handler: {(action: UIAlertAction!) in
            NSLog("report police presence");
            var coord = self.locationObj.coordinate
            var lat = coord.latitude
            var long = coord.longitude
            var url = "http://173.236.254.243:8080/heatmaps/?lat=" + lat.description + "&lng=" + long.description + "&type=police_tower&value=10";
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
        });
        let cancelAction = UIAlertAction(title: "Cancel", style: .Cancel) { (_) in }
        alert.addAction(lightingAction)
        alert.addAction(policeAction)
        alert.addAction(cancelAction)

        self.presentViewController(alert, animated: true, completion: nil)

    }

    override func viewDidLoad() {
        self.route_index = self.fromRoutes.iPick
        self.myPath = self.fromRoutes.pPick
        self.positive_heatmap = self.fromRoutes.hPmap
        self.negative_heatmap = self.fromRoutes.hNmap
        super.viewDidLoad()
        NSLog(route_index!);
//        println(self.myPath.description)
//        println("path end")
        
        self.view_width = self.view.frame.size.width;
        self.view_height = self.view.frame.size.height;
        
        self.locationManager = CLLocationManager()
        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        self.locationManager.requestAlwaysAuthorization()
        self.locationManager.startUpdatingLocation()
        
        var camera = GMSCameraPosition.cameraWithLatitude(33.777442, longitude: -84.397217, zoom: 15)
        var mapView = GMSMapView.mapWithFrame(CGRectZero, camera: camera)
        mapView.camera = camera
        mapView.myLocationEnabled = true
        self.view = mapView
        
        /* Render Route: */
        let polycolor = UIColor.greenColor()
        var polyline = GMSPolyline(path : self.myPath)
        polyline.spans = [GMSStyleSpan(color: polycolor)]
        polyline.strokeWidth = CGFloat(6.0)
        polyline.map = mapView
        
        // Render positive heatmap
        if(positive_heatmap.count > 0) {
            for i in 0...positive_heatmap.count-1{
                var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(positive_heatmap[i]["loc"]["coordinates"][1].double!, positive_heatmap[i]["loc"]["coordinates"][0].double!)
                var weight : Int
                weight = positive_heatmap[i]["weight"].int!
            
                var circ = GMSCircle(position: pointcoord, radius: 5)
                circ.fillColor = UIColor(red: 0.8, green: 0, blue: 0, alpha: 1)
                circ.strokeColor = UIColor.redColor()
                circ.map = mapView;
        }
        }
        
        // Render negative heatmap
        if(negative_heatmap.count > 0) {
            for i in 0...negative_heatmap.count-1{
                var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(negative_heatmap[i]["loc"]["coordinates"][1].double!, negative_heatmap[i]["loc"]["coordinates"][0].double!)
                var weight : Int
                weight = negative_heatmap[i]["weight"].int!
                var circ = GMSCircle(position: pointcoord, radius: 5)
                circ.fillColor = self.hexStringToUIColor("#6495ED")
                circ.strokeColor = self.hexStringToUIColor("#6495ED")
                circ.map = mapView;
            }
        }
        
//        NSLog("Here we are");
        var button1 = UIButton(frame: CGRectMake(25, view_height - 60, view_width/2 - 35, 40));
        button1.setTitle("Report", forState: .Normal);
        button1.setTitleColor(UIColor.whiteColor(), forState: .Normal);
        button1.titleLabel!.textAlignment = .Center;
        button1.backgroundColor = hexStringToUIColor("#f62afd");
        button1.addTarget(self, action: Selector("report:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        self.view.addSubview(button1);
        var button2 = UIButton(frame: CGRectMake(view_width/2 + 10, view_height - 60, view_width/2 - 35, 40));
        button2.setTitle("End", forState: .Normal);
        button2.setTitleColor(UIColor.whiteColor(), forState: .Normal);
        button2.titleLabel!.textAlignment = .Center;
        button2.backgroundColor = hexStringToUIColor("#5a5399");
        button2.addTarget(self, action: Selector("end_navigation:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        self.view.addSubview(button2);
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func report_lighting(sender: UIButton!) {
        if(sender.tag == 1)
        {
//            NSLog("1 star");
            report_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[1].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[2].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            lighting_stars = 1;
        }
        else if(sender.tag == 2)
        {
//            NSLog("2 stars");
            report_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[2].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            lighting_stars = 2;
        }
        if(sender.tag == 3)
        {
//            NSLog("3 stars");
            report_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            report_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            lighting_stars = 3;
        }
        if(sender.tag == 4)
        {
//            NSLog("4 stars");
            report_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[3].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            lighting_stars = 4;
        }
        if(sender.tag == 5)
        {
//            NSLog("5 stars");
            report_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[3].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            report_buttons[4].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            lighting_stars = 5;
        }
    }
    
    func send_lighting_report(sender: UIButton!) {
        lighting.removeFromSuperview();
//        NSLog("sending lighting report");
        var value = 4*lighting_stars - 10;
        var coord = locationObj.coordinate
        var lat = coord.latitude
        var long = coord.longitude
        //http://173.236.254.243:8080/heatmaps/?lat=32.725371&lng=-117.160721&type=lighting&value=10
        var url = "http://173.236.254.243:8080/heatmaps/?lat=" + lat.description + "&lng=" + long.description + "&type=lighting&value=" + value.description;
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
        lighting_stars = 0;
    }
    
    func close_report_lighting(sender: UIButton) {
        lighting.removeFromSuperview();
        lighting_stars = 0;
//        NSLog("cancelled lighting report");
    }
    
    func end_navigation(sender: UIButton!) {
        
        lighting.removeFromSuperview()
        
        var x_gap = CGFloat(50)
        var box_width = self.view_width - 2 * x_gap
        var label_height = CGFloat(40);
        var inner_padding = CGFloat(10)
        var upper_padding = CGFloat(10) + label_height
        var star_size = (box_width - 2 * inner_padding) / 5
        var box_height = CGFloat(100) + star_size;
        
        self.rating.backgroundColor = UIColor.whiteColor();
        self.rating.frame = CGRectMake(x_gap, 120.0, box_width, box_height);
        self.rating.layer.cornerRadius = 10;
        self.rating.layer.masksToBounds = true;
        
        var label = UILabel(frame: CGRectMake(0, 0, box_width, label_height))
        label.backgroundColor = self.hexStringToUIColor("#5a5399");
        label.text = "Please rate the route";
        label.textColor = UIColor.whiteColor()
        label.textAlignment = .Center

        var one = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
        one.frame = CGRectMake(inner_padding, upper_padding, star_size, star_size);
        one.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
        one.tag = 1;
        one.addTarget(self, action: Selector("set_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        var two = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
        two.frame = CGRectMake(inner_padding + star_size, upper_padding, star_size, star_size);
        two.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
        two.tag = 2;
        two.addTarget(self, action: Selector("set_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        
        var three = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
        three.frame = CGRectMake(inner_padding + 2 * star_size, upper_padding, star_size, star_size);
        three.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
        three.tag = 3;
        three.addTarget(self, action: Selector("set_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        var four = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
        four.frame = CGRectMake(inner_padding + 3 * star_size, upper_padding, star_size, star_size);
        four.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
        four.tag = 4;
        four.addTarget(self, action: Selector("set_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        var five = UIButton.buttonWithType(UIButtonType.Custom) as! UIButton;
        five.frame = CGRectMake(inner_padding + 4 * star_size, upper_padding, star_size, star_size);
        five.setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
        five.tag = 5;
        five.addTarget(self, action: Selector("set_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        var ok_button = UIButton(frame: CGRectMake(0, upper_padding + star_size + 10, box_width/2, label_height));
        ok_button.setTitle("Ok", forState: .Normal);
        ok_button.setTitleColor(UIColor.blackColor(), forState: .Normal);
        ok_button.titleLabel!.textAlignment = .Center;
        ok_button.backgroundColor = UIColor.whiteColor();
        ok_button.addTarget(self, action: Selector("send_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        var cancel_button = UIButton(frame: CGRectMake(box_width/2, upper_padding + star_size + 10, box_width/2, label_height));
        
        cancel_button.setTitle("Cancel", forState: .Normal);
        cancel_button.setTitleColor(UIColor.blackColor(), forState: .Normal);
        cancel_button.titleLabel!.textAlignment = .Center;
        cancel_button.backgroundColor = UIColor.whiteColor();
        cancel_button.addTarget(self, action: Selector("cancel_rating:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        self.rating_buttons = [];
        self.rating_buttons.append(one);
        self.rating_buttons.append(two);
        self.rating_buttons.append(three);
        self.rating_buttons.append(four);
        self.rating_buttons.append(five);
        
        self.rating.addSubview(label);
        self.rating.addSubview(one);
        self.rating.addSubview(two);
        self.rating.addSubview(three);
        self.rating.addSubview(four);
        self.rating.addSubview(five);
        self.rating.addSubview(ok_button);
        self.rating.addSubview(cancel_button);
        
        self.view.addSubview(self.rating);
        
    }
    
    func set_rating(sender: UIButton!) {
        if(sender.tag == 1)
        {
//            NSLog("1 star");
            rating_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[1].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[2].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_stars = 1;
        }
        else if(sender.tag == 2)
        {
//            NSLog("2 stars");
            rating_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[2].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_stars = 2;
        }
        if(sender.tag == 3)
        {
//            NSLog("3 stars");
            rating_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[3].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_stars = 3;
        }
        if(sender.tag == 4)
        {
//            NSLog("4 stars");
            rating_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[3].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[4].setBackgroundImage(UIImage(named: "unselected.png"), forState: UIControlState.Normal);
            rating_stars = 4;
        }
        if(sender.tag == 5)
        {
//            NSLog("5 stars");
            rating_buttons[0].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[1].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[2].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[3].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_buttons[4].setBackgroundImage(UIImage(named: "selected.png"), forState: UIControlState.Normal);
            rating_stars = 5;
        }

    }
    
    func send_rating(sender: UIButton!) {
        var value = 4*rating_stars - 10;
        // http://173.236.254.243:8080/routes/rate/1428192333326?rating=10&day=true
        var url = "http://173.236.254.243:8080/routes/rate/" + route_index! + "?rating=" + value.description + "&day=true"
//        NSLog(url);
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
        rating_stars = 0;
        performSegueWithIdentifier("segue_end", sender: sender);
    }
    
    func cancel_rating(sender: UIButton!) {
        rating_stars = 0;
        rating.removeFromSuperview();
    }
    
    func locationManager(manager: CLLocationManager!, didFailWithError error: NSError!) {
        NSLog("Error getting location");
    }
    
    func locationManager(manager: CLLocationManager!, didUpdateLocations locations: [AnyObject]!) {
            var locationArray = locations as NSArray
            self.locationObj = locationArray.lastObject as! CLLocation
        var point = locationObj.coordinate;
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