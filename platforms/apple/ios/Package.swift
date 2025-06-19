// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "UsageControlApp",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "UsageControlCore",
            targets: ["UsageControlCore"]
        ),
    ],
    dependencies: [
        // Add external dependencies here
    ],
    targets: [
        .target(
            name: "UsageControlCore",
            dependencies: [],
            path: "UsageControlApp"
        ),
        .testTarget(
            name: "UsageControlAppTests",
            dependencies: ["UsageControlCore"],
            path: "UsageControlAppTests"
        ),
    ]
)