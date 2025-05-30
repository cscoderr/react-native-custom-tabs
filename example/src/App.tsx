import ReactNativeCustomTabs, {
  CustomTabsActivityHeightResizeBehavior,
  CustomTabsCloseButtonPosition,
  CustomTabsShareState,
  SheetPresentationControllerDetent,
  ViewControllerModalPresentationStyle,
} from 'react-native-custom-tabs';
import { View, StyleSheet, Button, Dimensions } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <Button
        title="Close"
        onPress={async () => {
          const response = await ReactNativeCustomTabs.close();
          console.log(response);
        }}
      />
      <View style={{ height: 20 }} />
      <Button
        title="Launch URL"
        onPress={async () => {
          setTimeout(async () => {
            console.log('Closing after 100ms');
            const res = await ReactNativeCustomTabs.close();
            console.log(res);
          }, 100);
          const res = await ReactNativeCustomTabs.launch(
            'https://www.google.com',
            {
              customTabsOptions: {
                shareState: CustomTabsShareState.off,
                closeButton: {
                  position: CustomTabsCloseButtonPosition.start,
                },
                partial: {
                  initialHeight: Dimensions.get('window').height * 0.98,
                  activityHeightResizeBehavior:
                    CustomTabsActivityHeightResizeBehavior.adjustable,
                },
              },
              safariVCOptions: {
                modalPresentationStyle:
                  ViewControllerModalPresentationStyle.pageSheet,
                pageSheet: {
                  detents: [
                    SheetPresentationControllerDetent.medium,
                    SheetPresentationControllerDetent.large,
                  ],
                  preferredCornerRadius: 16,
                },
              },
            }
          );
          console.log(res);
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
