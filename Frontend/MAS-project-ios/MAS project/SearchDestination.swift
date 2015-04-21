//
//  SearchDestination.swift
//  MAS project
//
//  Created by ZHEN CHENG WANG on 4/1/15.
//  Copyright (c) 2015 CODEZ. All rights reserved.
//

import Foundation

import UIKit

class SearchDestination: UIViewController {
    
    @IBOutlet weak var inputDest : UITextField!
    @IBOutlet weak var sendButton : UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    //    @IBAction func sendDest(sender : AnyObject) {
    //    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "segueTest") {
            
            
            
            
            //            // doing http
            //            var svc = segue.destinationViewController as! Routes;
            //            svc.destString = inputDest.text
            //            println("inputdest:\(inputDest.text)")
            
            
            
            var svc = segue.destinationViewController as! Routes;
            
            svc.destString = inputDest.text // destCoord.latitude.description + "," + destCoord.longitude.description
//            println("destString:\(svc.destString)")
            
            
        }
        
    }
    
    
}

