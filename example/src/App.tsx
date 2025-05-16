import {
  closeCustomTabs,
  CustomTabsActivityHeightResizeBehavior,
  CustomTabsCloseButtonPosition,
  CustomTabsShareState,
  launch,
  multiply,
  SheetPresentationControllerDetent,
  ViewControllerModalPresentationStyle,
} from 'react-native-custom-tabs';
import { Text, View, StyleSheet, Button, Dimensions } from 'react-native';
import { useState, useEffect } from 'react';

export default function App() {
  const [result, setResult] = useState<number | undefined>();

  useEffect(() => {
    multiply(3, 7).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button
        title="Close"
        onPress={async () => {
          await closeCustomTabs();
        }}
      />
      <Button
        title="Launch URL"
        onPress={async () => {
          await launch(
            'https://www.google.com',
            false,
            {
              shareState: CustomTabsShareState.off,
              closeButton: {
                position: CustomTabsCloseButtonPosition.start,
              },
              partial: {
                initialHeight: Dimensions.get('window').height * 0.95,
                activityHeightResizeBehavior:
                  CustomTabsActivityHeightResizeBehavior.fixed,
              },
            },
            {
              modalPresentationStyle:
                ViewControllerModalPresentationStyle.pageSheet,
              pageSheet: {
                detents: [SheetPresentationControllerDetent.large],
                preferredCornerRadius: 16,
              },
            }
          );
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
