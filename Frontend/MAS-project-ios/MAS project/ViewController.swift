//
//  ViewController.swift
//  MAS project
//
//  Created by Aditya Kadur on 3/24/15.
//  Copyright (c) 2015 CODEZ. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        var camera = GMSCameraPosition.cameraWithLatitude(37.80948,
            longitude:5.965699, zoom:2)
        var mapView = GMSMapView.mapWithFrame(CGRectZero, camera:camera)
        
        // Available map types: kGMSTypeNormal, kGMSTypeSatellite, kGMSTypeHybrid,
        // kGMSTypeTerrain, kGMSTypeNone
        
        // Set the mapType to Satellite
        mapView.mapType = kGMSTypeSatellite
        self.view = mapView
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    @IBAction func login_button(sender : AnyObject) {
    }


}

