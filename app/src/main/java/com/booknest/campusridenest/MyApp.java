package com.booknest.campusridenest;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
// import com.booknest.campusridenest.BuildConfig;  //

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        //
    /*
    if (com.booknest.campusridenest.BuildConfig.DEBUG) {
      try {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
      } catch (Throwable ignored) {}
    }
    */
    }
}
