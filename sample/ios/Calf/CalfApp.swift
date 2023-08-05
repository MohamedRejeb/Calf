//
//  CalfApp.swift
//  Calf
//
//  Created by Mohamed Ben Rejeb on 2/8/2023.
//

import SwiftUI
import Common

@main
struct CalfApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return Main_iosKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates the state of the specified view controller with new information from SwiftUI.
    }
}
