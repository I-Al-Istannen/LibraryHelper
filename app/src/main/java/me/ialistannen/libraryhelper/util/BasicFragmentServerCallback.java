package me.ialistannen.libraryhelper.util;

import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;
import java.io.IOException;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.FragmentBase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A basic {@link Callback} to remove some boilerplate.
 */
public abstract class BasicFragmentServerCallback<T> implements Callback {

  private FragmentBase context;
  @StringRes
  private int dialogTitle;
  private Class<T> pojo;

  public BasicFragmentServerCallback(FragmentBase context, int dialogTitle, Class<T> pojo) {
    this.context = context;
    this.dialogTitle = dialogTitle;
    this.pojo = pojo;
  }

  @Override
  public void onFailure(@NonNull Call call, @NonNull IOException e) {
    showDialog(e.getLocalizedMessage());
  }

  @Override
  public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
    ResponseBody body = response.body();

    if (body == null) {
      showDialog(R.string.server_response_body_is_null);
      onPostExecute();
      return;
    }
    if (onRawData(body)) {
      onPostExecute();
      return;
    }

    String bodyString = body.string();
    T result = Json.getGson().fromJson(bodyString, pojo);

    if (result == null) {
      showDialog(R.string.server_response_malformed, bodyString);
      onPostExecute();
      return;
    }

    onPojoReceived(result);
    onPostExecute();
  }

  /**
   * Called with the raw {@link ResponseBody}. Allows you to do more raw processing.
   *
   * @param body The {@link ResponseBody}
   * @return True if you handled it, false if I should try to decode it further into JSON
   */
  protected boolean onRawData(@NonNull ResponseBody body) {
    return false;
  }

  /**
   * Called when gson successfully decoded a pojo.
   *
   * @param pojo The pojo that gson decoded
   */
  protected abstract void onPojoReceived(T pojo);

  /**
   * Called after the callback has finished.
   *
   * <p>Will *always* be called, no matter *how* the callback exits.
   */
  protected void onPostExecute() {
  }

  /**
   * @param message The message to display
   * @param messageFormatArguments The format arguments for the message
   */
  protected void showDialog(@StringRes final int message,
      final Object... messageFormatArguments) {

    doSyncIfAdded(new Runnable() {
      @Override
      public void run() {
        showDialog(context.getString(message, messageFormatArguments));
      }
    });
  }

  /**
   * @param message The message to display
   */
  protected void showDialog(final String message) {
    doSyncIfAdded(new Runnable() {
      @Override
      public void run() {
        new AlertDialog.Builder(context.getActivity())
            .setTitle(dialogTitle)
            .setMessage(message)
            .create()
            .show();
      }
    });
  }

  /**
   * @param message The message id to toast
   * @param formatArguments The format arguments to pass to getString
   */
  protected void toast(@StringRes final int message, final Object... formatArguments) {
    doSyncIfAdded(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(
            context.getActivity(),
            context.getString(message, formatArguments),
            Toast.LENGTH_SHORT
        ).show();
      }
    });
  }

  /**
   * Executes the code if the fragment is added to an activity.
   *
   * @param runnable The code to execute
   */
  protected void doSyncIfAdded(Runnable runnable) {
    if (!context.isAdded()) {
      return;
    }
    // Do not post if we are already on the main thread
    if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
      runnable.run();
      return;
    }
    new Handler(Looper.getMainLooper()).post(runnable);
  }
}
