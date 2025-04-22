import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinModuleKt.initializeKoin()
        NapierProxyKt.debugBuild()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}