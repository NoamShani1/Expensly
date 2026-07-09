package com.example.expensly.expensly;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Stores each user's profile picture as a JPEG in the app's private storage
 * (filesDir/avatars/&lt;hash-of-email&gt;.jpg). No permissions needed.
 */
public final class AvatarStore {

    private static final int MAX_SIZE_PX = 1024;
    private static final int JPEG_QUALITY = 88;

    private AvatarStore() {
    }

    private static File fileFor(Context context, String email) {
        File dir = new File(context.getFilesDir(), "avatars");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return new File(dir, PasswordUtils.sha256(email.trim().toLowerCase()) + ".jpg");
    }

    /**
     * Copies the picked image into private storage, downscaled so it doesn't
     * blow up memory. Returns true on success.
     */
    public static boolean save(Context context, String email, Uri source) {
        try {
            // First pass: read only the dimensions to compute a sample size.
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(source)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 1;
            int largest = Math.max(bounds.outWidth, bounds.outHeight);
            while (largest / opts.inSampleSize > MAX_SIZE_PX) {
                opts.inSampleSize *= 2;
            }

            Bitmap bitmap;
            try (InputStream in = context.getContentResolver().openInputStream(source)) {
                bitmap = BitmapFactory.decodeStream(in, null, opts);
            }
            if (bitmap == null) return false;

            try (FileOutputStream out = new FileOutputStream(fileFor(context, email))) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            }
            bitmap.recycle();
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    /** Returns the saved avatar, or null if the user hasn't set one. */
    public static Bitmap load(Context context, String email) {
        if (email == null) return null;
        File file = fileFor(context, email);
        if (!file.exists()) return null;
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}
