//
//  UISheetPresentationControllerConfiguration.swift
//  CustomTabsLauncher
//
//  Created by Tomiwa Idowu on 5/15/25.
//

import Foundation

public struct UISheetPresentationControllerConfiguration {
  var detents: [String]
  var largestUndimmedDetentIdentifier: String? = nil
  var prefersScrollingExpandsWhenScrolledToEdge: Bool? = nil
  var prefersGrabberVisible: Bool? = nil
  var prefersEdgeAttachedInCompactHeight: Bool? = nil
  var preferredCornerRadius: Double? = nil
  
  static func from(_ map: [String: Any]?) -> UISheetPresentationControllerConfiguration {
    guard let map else {
      return UISheetPresentationControllerConfiguration(detents: [])
    }
    return UISheetPresentationControllerConfiguration(
      detents: map["detents"] as? [String] ?? [],
      largestUndimmedDetentIdentifier: map["largestUndimmedDetentIdentifier"] as? String,
      prefersScrollingExpandsWhenScrolledToEdge: map["prefersScrollingExpandsWhenScrolledToEdge"] as? Bool,
      prefersGrabberVisible: map["prefersGrabberVisible"] as? Bool,
      prefersEdgeAttachedInCompactHeight: map["prefersEdgeAttachedInCompactHeight"] as? Bool,
      preferredCornerRadius: map["preferredCornerRadius"] as? Double
    )
  }
}
