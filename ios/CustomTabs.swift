import SafariServices

@objc(CustomTabs)
class CustomTabs: NSObject {
  private let launcher: CustomTabsLauncher = CustomTabsLauncher()


  @objc(multiply:withB:withResolver:withRejecter:)
  func multiply(a: Float, b: Float, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    resolve(a * b)
  }
  
  @objc(launchURL:prefersDeepLink:options:withResolver:withRejecter:)
  func launchURL(
    _ urlString: String,
    prefersDeepLink: Bool,
    options: [String: Any]?,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    let url = URL(string: urlString)!
    if prefersDeepLink {
      launcher.open(url, options: [.universalLinksOnly: true]) { [weak self] opened in
        if opened {
          resolve(nil)
        } else {
          self?.launchURL(url, options: options, resolve: resolve, reject: reject)
        }
      }
    } else {
      launchURL(
        url,
        options: options,
        resolve: resolve,
        reject: reject
      )
    }
  }
  
  @objc(closeAllIfPossible:withRejecter:)
  func closeAllIfPossible(
    resolve: @escaping RCTPromiseResolveBlock,
    reject: RCTPromiseRejectBlock
  ) {
    launcher.dismissAll {
      resolve(nil)
    }
  }
  
  @objc(mayLaunchURLs:withResolver:withRejecter:)
  func mayLaunchURLs(
    _ urlStrings: [String],
    resolve: RCTPromiseResolveBlock,
    reject: RCTPromiseRejectBlock
  ) {
    let urls = urlStrings.map { URL(string: $0)! }
    guard let sessionId = launcher.prewarmConnections(to: urls) else {
      resolve(nil)
      return
    }
    resolve(sessionId)
  }
  
  @objc(invalidateSession:withResolver:withRejecter:)
  func invalidateSession(
    _ sessionId: String,
    resolve: RCTPromiseResolveBlock,
    reject: RCTPromiseRejectBlock
  ) {
    launcher.invalidatePrewarmingSession(withId: sessionId)
  }
 
  private func launchURL(
    _ url: URL,
    options: [String: Any]?,
    resolve: @escaping RCTPromiseResolveBlock,
    reject: @escaping RCTPromiseRejectBlock
  ) {
    var result: String?
    guard let options else {
      launcher.open(url) { opened in
        opened ? resolve(nil) : reject("E_BROWSER", "Failed to launch external browser.", nil)
      }
      return
    }
    print(options);
    let safariOptions = SFSafariViewControllerOptions.from(options)
    let safariViewController = SFSafariViewController.make(url: url, options: safariOptions)
    
    launcher.present(safariViewController) { presented in
      presented ? resolve(nil) : reject("E_SAFARI", "Failed to launch SFSafariViewController.", nil)
    }
  }
}
