//
//  ViewController.swift
//  interesting
//
//  Created by Johan Widmann on 7/20/17.
//  Copyright © 2017 Johan. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    
    @IBOutlet weak var initAmt: UITextField!
    @IBOutlet weak var intAmt: UITextField!
    @IBOutlet weak var perAmt: UITextField!
    @IBOutlet weak var fin: UILabel!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func whenTap(_ sender: Any) {
        view.endEditing(true)
    }
    
    @IBAction func finAmt(_ sender: Any) {
        let initial = Double(initAmt.text!) ?? 0
        let interest = Double(intAmt.text!) ?? 0
        let period = Double(perAmt.text!) ?? 0
        let total = initial * pow((1 + interest), period)
        fin.text = "$\(total)"
    }
    
    
}

