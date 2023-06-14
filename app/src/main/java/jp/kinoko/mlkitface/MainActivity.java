package jp.kinoko.mlkitface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView imageView;
    private Canvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        findViewById(R.id.galleryButton).setOnClickListener(v -> dispatchPickPictureIntent());
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                processImage(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processImage(Bitmap imageBitmap) {
        imageView.setImageBitmap(imageBitmap);

        // 顔検出オプションを設定
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        // 顔検出器を初期化
//        LocalModel localModel = new LocalModel.Builder().build();
        FaceDetector faceDetector = FaceDetection.getClient(options);

        // 画像をML KitのInputImage形式に変換
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        // 顔検出を実行
        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    // 検出された顔の数を取得
                    int faceCount = faces.size();
                    Toast.makeText(MainActivity.this, "Detected " + faceCount + " faces", Toast.LENGTH_SHORT).show();

                    Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    for (Face face : faces) {
                        float left = face.getBoundingBox().left;
                        float top = face.getBoundingBox().top;
                        float right = face.getBoundingBox().right;
                        float bottom = face.getBoundingBox().bottom;
                        Log.d("KINOKO", "Face detected at: " + left + ", " + top + ", " + right + ", " + bottom);
                        // 枠を描画する処理を追加する
                        // ...
                        Paint paint = new android.graphics.Paint();
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(4);
                        paint.setColor(android.graphics.Color.RED);

                        canvas = new Canvas(mutableBitmap);
                        canvas.drawRect((int)left, (int)top, (int)right, (int)bottom, paint);

                        imageView.setImageBitmap(mutableBitmap);
                    }
                })
                .addOnFailureListener(e -> {
                    // エラーメッセージを表示
                    Toast.makeText(MainActivity.this, "Face detection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
