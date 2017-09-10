package me.ialistannen.libraryhelper.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.logic.server.ApiErrorPOJO;
import me.ialistannen.libraryhelper.view.FragmentBase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A basic {@link Callback} to remove some boilerplate.
 */
public abstract class BasicFragmentServerCallback<T> implements Callback {

  private Object context;
  @StringRes
  private int dialogTitle;
  private Type pojo;
  private Predicate<Object> canToast;

  public BasicFragmentServerCallback(Object context, int dialogTitle, Type pojo) {
    this(context, dialogTitle, pojo, null);
    canToast = new Predicate<Object>() {
      @Override
      public boolean apply(Object context) {
        //noinspection SimplifiableIfStatement
        if (BasicFragmentServerCallback.this.context instanceof FragmentBase) {
          return ((FragmentBase) BasicFragmentServerCallback.this.context).isAdded();
        }
        return true;
      }
    };
  }

  /**
   * @param context The {@link Context} or {@link FragmentBase} to use for lookups and displaying
   * errors
   * @param dialogTitle The title of the dialog
   * @param pojo The pojo to deserialize to
   * @param canToast A predicate to indicate whether the callback can display an error dialog/toast
   * or if that would lead to an error.
   */
  public BasicFragmentServerCallback(Object context, int dialogTitle, Type pojo,
      Predicate<Object> canToast) {
    Preconditions.checkArgument(
        context instanceof Context || context instanceof FragmentBase,
        "The context needs to be a Context or a FragmentBase instance!"
    );
    this.context = context;
    this.dialogTitle = dialogTitle;
    this.pojo = pojo;
    this.canToast = canToast;
  }

  @Override
  public void onFailure(@NonNull Call call, @NonNull IOException e) {
    String errorType = e.getLocalizedMessage();
    if (errorType == null || errorType.isEmpty()) {
      errorType = e.getClass().getSimpleName();
    }
    if (e instanceof SocketTimeoutException) {
      showDialog(R.string.server_response_timeout);
    } else {
      showDialog(R.string.server_response_unknown_error, errorType);
    }
    onPostExecute();
  }

  @Override
  public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
    ResponseBody body = response.body();

    if (body == null) {
      showDialog(R.string.server_response_body_is_null);
      onPostExecute();
      return;
    }

    String bodyString = body.string();

    if (!response.isSuccessful()) {
      ApiErrorPOJO error = Json.fromJson(bodyString, ApiErrorPOJO.class);
      if (error == null) {
        showDialog(R.string.server_response_malformed, bodyString);
        onPostExecute();
        return;
      }
      showDialog(R.string.server_response_error_received, error.message);
      onPostExecute();
      return;
    }

    T result = Json.fromJson(bodyString, pojo);

    if (result == null) {
      showDialog(R.string.server_response_malformed, bodyString);
      onPostExecute();
      return;
    }

    onPojoReceived(result);
    onPostExecute();
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

  private Context getContext() {
    if (context instanceof Context) {
      return (Context) context;
    } else if (context instanceof FragmentBase) {
      return ((FragmentBase) context).getActivity();
    }
    throw new IllegalStateException("Context is null? Context: " + context);
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
        showDialog(getContext().getString(message, messageFormatArguments));
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
        new AlertDialog.Builder(getContext())
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
            getContext(),
            getContext().getString(message, formatArguments),
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
    if (!canToast.apply(this)) {
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
