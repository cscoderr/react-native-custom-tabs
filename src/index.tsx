import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-custom-tabs' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const CustomTabs = NativeModules.CustomTabs
  ? NativeModules.CustomTabs
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function multiply(a: number, b: number): Promise<number> {
  return CustomTabs.multiply(a, b);
}

// const eventEmitter = new NativeEventEmitter(NativeModules.YourNativeModule); // Replace with your module name
// eventEmitter.addListener('CustomTabsLog', (message) => {
//   console.log('[Android Log]', message);
// });

export enum CustomTabsActivityHeightResizeBehavior {
  defaultBehavior = 0,
  adjustable = 1,
  fixed = 2,
}

export enum CustomTabsActivitySideSheetPosition {
  defaultPosition = 0,
  start = 1,
  end = 2,
}

export enum CustomTabsActivitySideSheetDecorationType {
  defaultDecoration = 0,
  none = 1,
  shadow = 2,
  divider = 3,
}

export enum CustomTabsActivitySideSheetRoundedCornersPosition {
  defaultPosition = 0,
  none = 1,
  top = 2,
}

export interface PartialCustomTabsConfiguration {
  initialHeight?: number;
  activityHeightResizeBehavior?: CustomTabsActivityHeightResizeBehavior;
  initialWidth?: number;
  activitySideSheetBreakpoint?: number;
  activitySideSheetMaximizationEnabled?: boolean;
  activitySideSheetPosition?: CustomTabsActivitySideSheetPosition;
  activitySideSheetDecorationType?: CustomTabsActivitySideSheetDecorationType;
  activitySideSheetRoundedCornersPosition?: CustomTabsActivitySideSheetRoundedCornersPosition;
  cornerRadius?: number;
  backgroundInteractionEnabled?: boolean;
}

export interface CustomTabsBrowserConfiguration {
  prefersDefaultBrowser?: boolean;
  fallbackCustomTabs?: string[];
  headers?: Record<string, string>;
  sessionPackageName?: string;
  prefersExternalBrowser?: boolean;
}

export enum CustomTabsCloseButtonPosition {
  start = 1,
  end = 2,
}

export interface CustomTabsCloseButton {
  icon?: string;
  position?: CustomTabsCloseButtonPosition;
}

export interface CustomTabsAnimations {
  startEnter?: string;
  startExit?: string;
  endEnter?: string;
  endExit?: string;
}

const slideInAnimation: CustomTabsAnimations = {
  startEnter: 'android:anim/slide_in_right',
  startExit: 'android:anim/slide_out_left',
  endEnter: 'android:anim/slide_in_left',
  endExit: 'android:anim/slide_out_right',
};
const fadeAnimation: CustomTabsAnimations = {
  startEnter: 'android:anim/fade_in',
  startExit: 'android:anim/fade_out',
  endEnter: 'android:anim/fade_in',
  endExit: 'android:anim/fade_out',
};

export enum CustomTabsShareState {
  browserDefault = 0,
  on = 1,
  off = 2,
}

export enum CustomTabsColorScheme {
  system = 0,
  light = 1,
  dark = 2,
}

export interface CustomTabsColorSchemeParams {
  toolbarColor?: string;
  navigationBarColor?: string;
  navigationBarDividerColor?: string;
}

export interface CustomTabsColorSchemes {
  colorScheme?: CustomTabsColorScheme;
  lightParams?: CustomTabsColorSchemeParams;
  darkParams?: CustomTabsColorSchemeParams;
  defaultParams?: CustomTabsColorSchemeParams;
}

export type AnimationPresetName = 'slideIn' | 'fade';

export interface Options {
  colorSchemes?: CustomTabsColorSchemes;
  urlBarHidingEnabled?: boolean;
  shareState?: CustomTabsShareState;
  showTitle?: boolean;
  instantAppsEnabled?: boolean;
  bookmarksButtonEnabled?: boolean;
  downloadButtonEnabled?: boolean;
  shareIdentityEnabled?: boolean;
  closeButton?: CustomTabsCloseButton;
  animations?: CustomTabsAnimations | AnimationPresetName;
  browser?: CustomTabsBrowserConfiguration;
  partial?: PartialCustomTabsConfiguration;
}

const animationPresets: Record<AnimationPresetName, CustomTabsAnimations> = {
  slideIn: slideInAnimation,
  fade: fadeAnimation,
};

function resolveAnimation(
  animation?: CustomTabsAnimations | AnimationPresetName
): CustomTabsAnimations | null {
  if (!animation) return null;

  if (typeof animation === 'string') {
    return animationPresets[animation];
  }
  return animation; // manual animation object
}

export interface PageSheetConfig {
  detents: string[];
  prefersGrabberVisible?: boolean;
  preferredCornerRadius?: number;
  prefersEdgeAttachedInCompactHeight?: boolean;
}

export enum ViewControllerModalPresentationStyle {
  /// The default presentation style chosen by the system.
  ///
  /// - Availability: **iOS13.0+**
  automatic = -2,

  /// A presentation style that indicates no adaptations should be made.
  none = -1,

  /// A presentation style in which the presented view covers the screen.
  fullScreen = 0,

  /// A presentation style that partially covers the underlying content.
  pageSheet = 1,

  /// A presentation style that displays the content centered in the screen.
  formSheet = 2,

  /// A view presentation style in which the presented view covers the screen.
  overFullScreen = 5,
}

interface SafariViewControllerOptions {
  preferredBarTintColor?: number;
  preferredControlTintColor?: number;
  barCollapsingEnabled?: boolean;
  entersReaderIfAvailable?: boolean;
  dismissButtonStyle?: number;
  modalPresentationStyle?: ViewControllerModalPresentationStyle;
  pageSheet?: PageSheetConfig;
}

export function launch(
  url: String,
  prefersDeepLink: Boolean = false,
  customTabsOptions?: Options,
  safariVCOptions?: SafariViewControllerOptions
): Promise<String> {
  if (Platform.OS === 'android') {
    const animations = resolveAnimation(customTabsOptions?.animations);
    return CustomTabs.launch(url, prefersDeepLink, {
      ...customTabsOptions,
      animations: animations,
    });
  } else {
    return CustomTabs.launchURL(url, prefersDeepLink, safariVCOptions);
  }
}

export function closeCustomTabs(): Promise<String> {
  return CustomTabs.closeAllIfPossible();
}
