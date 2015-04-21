//
//  ViewController.swift
//  MAS project
//
//  Created by ZHEN CHENG WANG on 4/4/15.
//  Copyright (c) 2015 CODEZ. All rights reserved.
//

import UIKit
import Foundation



class Initial: UIViewController, GMSMapViewDelegate {
    
    func next_screen(sender: UIButton!) {
        performSegueWithIdentifier("segue_initial1", sender: sender);
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        var view_width = self.view.bounds.size.width;
        var view_height = self.view.bounds.size.height;
        var camera = GMSCameraPosition.cameraWithLatitude(33.777442, longitude: -84.397217, zoom: 15)
        var mapView = GMSMapView.mapWithFrame(CGRectZero, camera: camera)
        mapView.camera = camera
        mapView.myLocationEnabled = true
        mapView.settings.compassButton = true

        self.view = mapView
        
        var label = UILabel(frame: CGRectMake(25, view_height - 120, view_width - 50, 40))
        label.backgroundColor = hexStringToUIColor("#5A5399");
        label.text = "Enter Destination";
        label.textAlignment = .Center;
        label.textColor = UIColor.whiteColor();
        
        var button1 = UIButton(frame: CGRectMake(25, view_height - 70, view_width - 90, 40));

        button1.setTitle("  Where do you want to go?", forState: .Normal);
        button1.setTitleColor(UIColor.grayColor(), forState: .Normal);
        button1.contentHorizontalAlignment = UIControlContentHorizontalAlignment.Left;
        button1.backgroundColor = UIColor.whiteColor();
        button1.addTarget(self, action: Selector("next_screen:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        var search_button = UIButton(frame: CGRectMake(view_width - 65, view_height - 70, 40, 40));
        search_button.setBackgroundImage(UIImage(named: "search_button.png"), forState: UIControlState.Normal);
        search_button.addTarget(self, action: Selector("next_screen:"), forControlEvents: UIControlEvents.TouchUpInside);
        
        self.view.addSubview(label);
        self.view.addSubview(button1);
        self.view.addSubview(search_button)
        
        var url : NSString = "http://173.236.254.243:8080/heatmaps/positive?lat=33.777442&lng=-84.397217&radius=5000"
        var queryURL : NSURL = NSURL(string: url as String)!
        
        let task = NSURLSession.sharedSession().dataTaskWithURL(queryURL) {(data, response, error) in
            
            var myJSON = NSString(data: data, encoding: NSUTF8StringEncoding)
            //            println(myJSON)
            
            /* Return to main thread so we can make call to Google Map SDK */
            dispatch_sync(dispatch_get_main_queue(), { () -> Void in

                var seeif = 1
                let json = JSON(data: data) // put data not the encoded one
                
                //                println(json)
                let points : Array = json["response"].array!
                
                for i in 0...points.count-1{
                    //                    println(points[i])
                    var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(points[i]["loc"]["coordinates"][1].double!, points[i]["loc"]["coordinates"][0].double!)
                    var weight : Int
                    weight = points[i]["weight"].int!
                    
                    var circ = GMSCircle(position: pointcoord, radius: 5)
                    circ.fillColor = UIColor(red: 0.8, green: 0, blue: 0, alpha: 1)
                    circ.strokeColor = UIColor.redColor()
                    circ.map = mapView;
                }
                
            })
        }
        task.resume()
        var url2 : NSString = "http://173.236.254.243:8080/heatmaps/negative?lat=33.777442&lng=-84.397217&radius=5000"
        var queryURL2 : NSURL = NSURL(string: url2 as String)!
            
        let task2 = NSURLSession.sharedSession().dataTaskWithURL(queryURL2) {(data, response, error) in
                
            var myJSON = NSString(data: data, encoding: NSUTF8StringEncoding)

                
            /* Return to main thread so we can make call to Google Map SDK */
            dispatch_async(dispatch_get_main_queue(), { () -> Void in
                let json = JSON(data: data) // put data not the encoded one
                    
                    //                println(json)
                let points : Array = json["response"].array!
                    
                for i in 0...points.count-1{
                        //                    println(points[i])
                    var pointcoord : CLLocationCoordinate2D = CLLocationCoordinate2DMake(points[i]["loc"]["coordinates"][1].double!, points[i]["loc"]["coordinates"][0].double!)
                    var weight : Float
                    weight = points[i]["weight"].float!
                    var circ = GMSCircle(position: pointcoord, radius: 5)
                    circ.fillColor = self.hexStringToUIColor("#6495ED")
                    circ.strokeColor = self.hexStringToUIColor("#6495ED")
                    circ.map = mapView;
                    
                }
            });
        };
        task2.resume()
        
        
        
        
    }
    
    
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    @IBAction func login_button(sender : AnyObject) {
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

