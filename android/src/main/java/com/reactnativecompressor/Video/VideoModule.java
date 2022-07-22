package com.reactnativecompressor.Video;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.config.StorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.reactnativecompressor.Utils.RealPathUtil;

import java.util.ArrayList;

import static com.reactnativecompressor.Utils.Utils.getRealPath;
import static com.reactnativecompressor.Video.VideoCompressorHelper.video_activateBackgroundTask_helper;
import static com.reactnativecompressor.Video.VideoCompressorHelper.video_deactivateBackgroundTask_helper;
import static com.reactnativecompressor.Video.VideoCompressorHelper.video_upload_helper;
import static com.reactnativecompressor.Utils.Utils.cancelCompressionHelper;

@ReactModule(name = VideoModule.NAME)
public class  VideoModule extends ReactContextBaseJavaModule {
  public static final String NAME = "VideoCompressor";
  private static final String TAG = "react-native-compessor";
  private final ReactApplicationContext reactContext;
  public VideoModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  //Video
  @ReactMethod
  public void compress(
    String fileUri,
    ReadableMap optionMap,
    String quality,
    Promise promise) {
    ArrayList<Uri> uris = new ArrayList<Uri>();
    uris.add(Uri.parse(fileUri));
    String uuid = optionMap.getString("uuid");
    VideoQuality videoQuality = null;
    switch (quality) {
      case "VERY_LOW":
        videoQuality = VideoQuality.VERY_LOW;
        break;
      case "LOW":
        videoQuality = VideoQuality.LOW;
        break;
      case "MEDIUM":
        videoQuality = VideoQuality.MEDIUM;
        break;
      case "HIGH":
        videoQuality = VideoQuality.HIGH;
        break;
      case "VERY_HIGH":
        videoQuality = VideoQuality.VERY_HIGH;
        break;
    }
    VideoCompressor.start(
      reactContext,
      uris,
      true,
      new StorageConfiguration(
        "compress.mp4",
        null,
        false
      ),
      new Configuration(videoQuality, true, null, false, false, null, null),
      new CompressionListener() {
        @Override
        public void onStart(int index) {
          // Compression start
        }

        @Override
        public void onSuccess(int index, long size, String path) {
          // On Compression success
          promise.resolve(path);
        }

        @Override
        public void onFailure(int index, String failureMessage) {
          // On Failure
          promise.reject("FAILED " + index + " " + failureMessage);
        }

        @Override
        public void onProgress(int index, float progressPercent) {
          WritableMap params = Arguments.createMap();
          WritableMap data = Arguments.createMap();
          params.putString("uuid", uuid);
          data.putString("type", "progress");
          data.putInt("index", index);
          data.putDouble("progress", progressPercent);
          params.putMap("data", data);
          sendEvent(reactContext, "videoCompressProgress", params);
        }

        @Override
        public void onCancelled(int index) {
          // On Cancelled
          promise.reject("CANCELED " + index);
        }
      }
    );
  }

  @ReactMethod
  public void cancelCompression(String uuid)) {
    VideoCompressor.cancel();
  }

  @ReactMethod
  public void upload(
    String fileUrl,
    ReadableMap options,
    Promise promise) {
    try {
      video_upload_helper(fileUrl,options,reactContext,promise);
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void activateBackgroundTask(
    ReadableMap options,
    Promise promise) {
    try {
      String response=video_activateBackgroundTask_helper(options,reactContext);
      promise.resolve(response);
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }

  @ReactMethod
  public void deactivateBackgroundTask(
    ReadableMap options,
    Promise promise) {
    try {
      String response=video_deactivateBackgroundTask_helper(options,reactContext);
      promise.resolve(response);
    } catch (Exception ex) {
      promise.reject(ex);
    }
  }
}
