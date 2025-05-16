//
//  CustomTabsLauncher.swift
//
//
//  Created by Tomiwa Idowu on 5/15/25.
//
import SafariServices
import UIKit

open class CustomTabsLauncher {
  private var prewarmingTokenCache = [String: Any]()
  
  open func open(
    _ url: URL,
    options: [UIApplication.OpenExternalURLOptionsKey: Any] = [:],
    completionHandler completion: ((Bool) -> Void)? = nil
  ) {
    UIApplication.shared.open(url, options: options, completionHandler: completion)
  }
  
  open func present(_ viewControllerToPresent: UIViewController, completion: ((Bool) -> Void)? = nil) {
    DispatchQueue.main.async {
      if let topViewController = UIWindow.keyWindow?.topViewController() {
        topViewController.present(viewControllerToPresent, animated: true) {
          completion?(true)
        }
      } else {
        completion?(false)
      }
    }
//    DispatchQueue.main.async {
//      if let topViewController = UIApplication.shared.keyWindow?.rootViewController {
//        topViewController.present(viewControllerToPresent, animated: true) {
//          completion?(true)
//        }
//        
//      } else {
//        completion?(false)
//      }
//    }
  }
  
  open func dismissAll(completion: (() -> Void)? = nil) {
    DispatchQueue.main.async {
      guard let rootViewController = UIWindow.keyWindow?.rootViewController else {
        completion?()
        return
      }
      
      var presentedViewController = rootViewController.presentedViewController
      var presentedViewControllers = [UIViewController]()
      while presentedViewController != nil {
        if presentedViewController is SFSafariViewController {
          presentedViewControllers.append(presentedViewController!)
        }
        presentedViewController = presentedViewController!.presentedViewController
      }
      recursivelyDismissViewControllers(
        presentedViewControllers,
        animated: true,
        completion: completion
      )
    }
  }
  
  open func prewarmConnections(to urls: [URL]) -> String? {
    guard #available(iOS 15.0, *) else {
      return nil
    }
    
    let id = UUID().uuidString
    let newToken = SFSafariViewController.prewarmConnections(to: urls)
    prewarmingTokenCache[id] = newToken
    return id
  }
  
  open func invalidatePrewarmingSession(withId sessionId: String) {
    guard #available(iOS 15.0, *) else {
      return
    }
    
    guard
      let id = UUID(uuidString: sessionId)?.uuidString,
      let token = prewarmingTokenCache[id] as? SFSafariViewController.PrewarmingToken
    else {
      return
    }
    token.invalidate()
    prewarmingTokenCache.removeValue(forKey: id)
  }
}

private extension UIWindow {
  static var keyWindow: UIWindow? {
    // For iOS 13+, get the key window from connected scenes
    if #available(iOS 13.0, *) {
      return UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow }
    } else {
      // For earlier iOS versions
      return UIApplication.shared.keyWindow
    }
  }
  
  func topViewController() -> UIViewController? {
    recursivelyFindTopViewController(from: rootViewController)
  }
}

private func recursivelyFindTopViewController(from viewController: UIViewController?) -> UIViewController? {
  if let navigationController = viewController as? UINavigationController {
    recursivelyFindTopViewController(from: navigationController.visibleViewController)
  } else if let tabBarController = viewController as? UITabBarController,
            let selected = tabBarController.selectedViewController
  {
    recursivelyFindTopViewController(from: selected)
  } else if let presentedViewController = viewController?.presentedViewController {
    recursivelyFindTopViewController(from: presentedViewController)
  } else {
    viewController
  }
}

private func recursivelyDismissViewControllers(
  _ viewControllers: [UIViewController],
  animated flag: Bool,
  completion: (() -> Void)? = nil
) {
  var viewControllers = viewControllers
  guard let vc = viewControllers.popLast() else {
    completion?()
    return
  }
  
  vc.dismiss(animated: flag) {
    if viewControllers.isEmpty {
      completion?()
    } else {
      recursivelyDismissViewControllers(viewControllers, animated: flag, completion: completion)
    }
  }
}

