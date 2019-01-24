package matt.wltr.labs.flysightviewer.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {

    private static final int BUFFER_SIZE = 1024 * 2;

    public static void copy(InputStream inputStream, OutputStream outputStream) throws Exception {

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER_SIZE);

        int n;

        try {
            while ((n = bufferedInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                bufferedOutputStream.write(buffer, 0, n);
            }
            bufferedOutputStream.flush();
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                Log.e(IoUtils.class.getSimpleName(), e.getMessage(), e);
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                Log.e(IoUtils.class.getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
