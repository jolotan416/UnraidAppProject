import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinModuleKt.doInitializeKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}