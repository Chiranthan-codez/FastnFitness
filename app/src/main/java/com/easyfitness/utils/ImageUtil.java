package com.easyfitness.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.easyfitness.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.BuildConfig;

public class ImageUtil {

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_GALERY_PHOTO = 2;
    public static final int REQUEST_DELETE_IMAGE = 3;

    private Fragment mF;
    private String mFilePath;
    private ImageView imgView;
    private OnDeleteImageListener mDeleteImageListener;

    public ImageUtil() {}

    public ImageUtil(ImageView view) {
        imgView = view;
    }

    /* ---------------- IMAGE DISPLAY ---------------- */

    public static void setThumb(ImageView imageView, String path) {
        try {
            if (path == null || path.isEmpty()) return;

            File f = new File(path);
            if (!f.exists() || f.isDirectory()) return;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            Bitmap rotated = ExifUtil.rotateBitmap(path, bitmap);

            imageView.setImageBitmap(rotated);
            imageView.setScaleType(ScaleType.CENTER_CROP);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPic(ImageView imageView, String path) {
        try {
            if (path == null) return;

            File f = new File(path);
            if (!f.exists() || f.isDirectory()) return;

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            Bitmap rotated = ExifUtil.rotateBitmap(path, bitmap);

            imageView.setImageBitmap(rotated);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ScaleType.CENTER_CROP);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveThumb(String realPath) {
        String r;
        r= realPath;
    }

    /* ---------------- FILE HANDLING ---------------- */

    public String getFilePath() {
        return mFilePath;
    }

    public ImageView getView() {
        return imgView;
    }

    public void setView(ImageView view) {
        imgView = view;
    }

    public void setOnDeleteImageListener(OnDeleteImageListener listener) {
        mDeleteImageListener = listener;
    }

    /* ---------------- IMAGE SOURCE DIALOG ---------------- */

    public boolean CreatePhotoSourceDialog(Fragment fragment) {

        mF = fragment;
        requestPermissionForWriting(fragment);

        String[] options = {
            fragment.getString(R.string.camera),
            fragment.getString(R.string.gallery),
            "Remove Image"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());
        builder.setItems(options, (dialog, which) -> {

            if (which == 0) {
                dispatchTakePictureIntent(fragment);
            } else if (which == 1) {
                getGalleryPicture(fragment);
            } else if (which == 2) {
                if (mDeleteImageListener != null) {
                    mDeleteImageListener.onDeleteImage(ImageUtil.this);
                }
            }
        });

        builder.show();
        return true;
    }

    /* ---------------- CAMERA ---------------- */

    private void dispatchTakePictureIntent(Fragment fragment) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(fragment.getActivity().getPackageManager()) == null) {
            return;
        }

        File photoFile;
        try {
            photoFile = createImageFile();
            mFilePath = photoFile.getAbsolutePath();
        } catch (IOException e) {
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(
            fragment.getActivity(),
            BuildConfig.APPLICATION_ID + ".provider",
            photoFile
        );

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        fragment.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    /* ---------------- GALLERY ---------------- */

    private void getGalleryPicture(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, REQUEST_PICK_GALERY_PHOTO);
    }

    public void galleryAddPic(Fragment fragment, String file) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(file)));
        fragment.getActivity().sendBroadcast(scanIntent);
    }

    /* ---------------- FILE CREATION ---------------- */

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/DCIM/");
        if (!storageDir.exists()) storageDir.mkdirs();

        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /* ---------------- PERMISSIONS ---------------- */

    private void requestPermissionForWriting(Fragment fragment) {
        if (ContextCompat.checkSelfPermission(
            fragment.getActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                fragment.getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                102
            );
        }
    }

    /* ---------------- FILE COPY / MOVE ---------------- */

    public File moveFile(File file, File dir) throws IOException {
        return copyFile(file, dir, true);
    }

    public File copyFile(File file, File dir, boolean move) throws IOException {
        File newFile = new File(dir, file.getName());

        try (FileChannel in = new FileInputStream(file).getChannel();
             FileChannel out = new FileOutputStream(newFile).getChannel()) {

            in.transferTo(0, in.size(), out);
            if (move) file.delete();
        }

        return newFile;
    }

    public String getThumbPath(String img) {
        if (img == null) return null;

        File f = new File(img);
        return f.getParent() + "/thumb_" + f.getName();

    }

    /* ---------------- CALLBACK ---------------- */

    public interface OnDeleteImageListener {
        void onDeleteImage(ImageUtil imgUtil);
    }
}
