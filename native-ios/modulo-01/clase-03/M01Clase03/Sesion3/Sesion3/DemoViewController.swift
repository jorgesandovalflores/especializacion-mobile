//
//  DemoViewController.swift
//  Sesion3
//
//  Created by Gabriel Castillo Vizcarra on 2/02/26.
//

import UIKit

class DemoViewController: UIViewController {

    @IBOutlet weak var lblTitle: UILabel!
    @IBOutlet weak var btnLogin: UIButton!
    @IBOutlet weak var switchOnOff: UISwitch!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.btnLogin.setTitle("Iniciar Sesion", for: .normal)
        self.btnLogin.tintColor = .red
        
    }


    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
