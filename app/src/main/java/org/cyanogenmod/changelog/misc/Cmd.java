package org.cyanogenmod.changelog.misc;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Cmd {
    private static String TAG = "Cmd";

    public static String exec(String... strings) {
        String output = "";
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            Process process = Runtime.getRuntime().exec("sh");
            outputStream = new DataOutputStream(process.getOutputStream());
            inputStream = process.getInputStream();

            for (String s : strings) {
                outputStream.writeBytes(s + "\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Log.e(TAG, "", e);
            }

            output = readFully(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            CloseSilently(outputStream, inputStream);
        }

        return output;
    }

    private static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        return byteArrayOutputStream.toString("UTF-8");
    }

    private static void CloseSilently(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                try {
                    Log.d(TAG, "Closing: " + String.valueOf(object));

                    if (object instanceof Closeable) {
                        ((Closeable) object).close();
                        return;
                    }

                    Log.d(TAG, "cannot close: " + String.valueOf(object));
                    throw new RuntimeException("cannot close " + object);
                } catch (Throwable e) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }
}