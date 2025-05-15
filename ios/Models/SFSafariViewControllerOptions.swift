//
//  SFSafariViewControllerOptions.swift
//  CustomTabsLauncher
//
//  Created by Tomiwa Idowu on 5/15/25.
//

import Foundation

public struct SFSafariViewControllerOptions {
  var preferredBarTintColor: Int64? = nil
  var preferredControlTintColor: Int64? = nil
  var barCollapsingEnabled: Bool? = nil
  var entersReaderIfAvailable: Bool? = nil
  var dismissButtonStyle: Int64? = nil
  var modalPresentationStyle: Int64? = nil
  var pageSheet: UISheetPresentationControllerConfiguration? = nil
  
  static func from(_ map: [String: Any]?) -> SFSafariViewControllerOptions {
    guard let map else {
      return SFSafariViewControllerOptions()
    }
    return SFSafariViewControllerOptions(
      preferredBarTintColor: map["preferredBarTintColor"] as? Int64,
      preferredControlTintColor: map["preferredControlTintColor"] as? Int64,
      barCollapsingEnabled: map["barCollapsingEnabled"] as? Bool,
      entersReaderIfAvailable: map["entersReaderIfAvailable"] as? Bool,
      dismissButtonStyle: map["dismissButtonStyle"] as? Int64,
      modalPresentationStyle: map["modalPresentationStyle"] as? Int64,
      pageSheet: UISheetPresentationControllerConfiguration.from(map["pageSheet"] as? [String: Any])
    )
  }
}
