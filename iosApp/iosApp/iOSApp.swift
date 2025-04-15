import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinModuleKt.doInitializeKoin()
        NapierProxyKt.debugBuild()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}