package com.mynotes.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import com.evilnote.filesaver.FileSaverPlugin;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(FileSaverPlugin.class);
    super.onCreate(savedInstanceState);
  }
}
