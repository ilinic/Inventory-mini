/*
    Author: Artem Mouraviev ilinic8@mail.ru
 */
package com.mouraviev.inventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class BarcodeActivity extends AppCompatActivity
        implements BarcodeTracker.BarcodeGraphicTrackerCallback {

    private static final String TAG = "Inventory mini";
    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final long VIBRATE_QR_MSEC = 200;
    private static final long VIBRATE_CHANGE_MSEC = 100;
    private OkHttpClient httpClient;
    private TextView descTextView;
    private EditText codeEdit, deltaEdit;
    private String curCode = "";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_barcode);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        httpClient = new OkHttpClient.Builder()
                .callTimeout(2, TimeUnit.SECONDS)
                .build();

        mPreview = findViewById(R.id.preview);
        mPreview.setMinimumHeight(mPreview.getWidth());

        descTextView = findViewById(R.id.descText);
        descTextView.setShadowLayer(50.0f, 0.0f, 0.0f, Color.WHITE);

        codeEdit = findViewById(R.id.codeEdit);
        codeEdit.setVisibility(View.INVISIBLE);

        deltaEdit = findViewById(R.id.deltaEdit);

        descTextView.setText("");
        deltaEdit.setText("-1");

        findViewById(R.id.inventoryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.historyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.enterCodeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeEdit.getVisibility() == View.INVISIBLE) {
                    codeEdit.setText("");
                    codeEdit.setVisibility(View.VISIBLE);
                } else
                    codeEdit.setVisibility(View.INVISIBLE);
            }
        });

        codeEdit.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    processQRCode(codeEdit.getText().toString());

                    InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(codeEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.plusBtn).setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curDelta = Integer.parseInt(deltaEdit.getText().toString());
                curDelta++;
                deltaEdit.setText(String.valueOf(curDelta));
                deltaEdit.invalidate();
            }
        }));

        findViewById(R.id.minusBtn).setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int curDelta = Integer.parseInt(deltaEdit.getText().toString());
                curDelta--;
                deltaEdit.setText(String.valueOf(curDelta));
                deltaEdit.invalidate();
            }
        }));

        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAction(deltaEdit.getText().toString());
            }
        });

        boolean autoFocus = true;
        boolean useFlash = false;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }
    }
/*

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
*/

    void processQRCode(String code) {

        Request request;

        curCode = code;

        try {
            request = new Request.Builder()
                    .url(MainActivity.site + "/get_product?uid=" + MainActivity.userId + "&prodid=" + code)
                    .build();

            httpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            showToast("Ошибка. Проверьте соединение и данные");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            Gson gson = new Gson();

                            Map<String, String> responseMap = gson.fromJson(responseBody.string(), new
                                    TypeToken<Map<String, String>>() {
                                    }.getType());

                            if (!responseMap.get("prodid").equals(curCode))
                                return;

                            if (responseMap.get("err").equals("1")) {
                                showToast("Ошибка авторизации. Проверьте соединение и данные");
                                showProdInfo("");
                                curCode = "";
                                return;
                            }

                            if (responseMap.get("err").equals("2")) {
                                showProdInfo("Не найден №" + responseMap.get("prodid"));
                                showToast("Не найден №" + responseMap.get("prodid") + ". Проверьте данные");
                                curCode = "";
                                return;
                            }

                            if (!responseMap.get("err").equals("0")) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                showProdInfo("");
                                curCode = "";
                                return;
                            }

                            showProdInfo("\u2211" + responseMap.get("count") + " №" + responseMap.get("prodid") + " " + responseMap.get("prodname"));
                        }
                    });

        } catch (Exception e) {
            showToast("Ошибка. Проверьте соединение и данные");
        }
    }

    void sendAction(String delta) {

        if (curCode.isEmpty()) {
            showToast("Не указан продукт");
            return;
        }

        Request request;

        try {
            request = new Request.Builder()
                    .url(MainActivity.site + "/act?uid=" + MainActivity.userId + "&prodid=" + curCode + "&delta=" + delta)
                    .build();

            httpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            showToast("Ошибка. Проверьте соединение и данные");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            if (!response.isSuccessful()) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            ResponseBody responseBody = response.body();

                            if (responseBody == null) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            Gson gson = new Gson();

                            Map<String, String> responseMap = gson.fromJson(responseBody.string(), new
                                    TypeToken<Map<String, String>>() {
                                    }.getType());

                            if (responseMap.get("err").equals("1")) {
                                showToast("Ошибка авторизации. Проверьте соединение и данные");
                                return;
                            }

                            if (responseMap.get("err").equals("2")) {
                                showProdInfo("Не найден №" + responseMap.get("prodid"));
                                return;
                            }

                            if (!responseMap.get("err").equals("0")) {
                                showToast("Ошибка. Проверьте соединение и данные");
                                return;
                            }

                            if (responseMap.get("prodid").equals(curCode)) {
                                deltaOK();
                                vibrate(VIBRATE_CHANGE_MSEC);
                                showProdInfo("\u2211" + responseMap.get("count") + " №" + responseMap.get("prodid") + " " + responseMap.get("prodname"));
                            } else
                                showToast("Изменен: \u2211" + responseMap.get("count") + " №" + responseMap.get("prodid") + " " + responseMap.get("prodname"));
                        }
                    });

        } catch (Exception e) {
            showToast("Ошибка. Проверьте соединение и данные");
        }
    }

    private void deltaOK() {

        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        /*Toast toast = Toast.makeText(getApplicationContext(), "Изменен", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();*/
                        deltaEdit.setText("-1");
                    }
                });
    }

    @Override
    public void onDetectedQrCode(Barcode barcode) {
        if (barcode != null) {

            if (barcode.displayValue.equals(curCode))
                return;

            final String barcodeText = barcode.displayValue;

            vibrate(VIBRATE_QR_MSEC);

            processQRCode(barcodeText);
        }
    }

    private void vibrate(long len) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(len, VibrationEffect.DEFAULT_AMPLITUDE));
        else
            //deprecated in API 26
            v.vibrate(len);
    }

    // Handles the requesting of the camera permission.
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    /**
     * Creates and starts the camera.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error,
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
                .setRequestedFps(24.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    // Restarts the camera
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    // Stops the camera
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = true;
            boolean useFlash = false;
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Inventory mini")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    void showToast(final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                });
    }

    void showProdInfo(final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        descTextView.setText(msg);
                        deltaEdit.setText("-1");
                    }
                });
    }


}
