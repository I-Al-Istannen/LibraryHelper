package me.ialistannen.libraryhelper.view.booklist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.util.Objects;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A small util to help with getting cover images.
 */
class CoverUtil {

  private LoanableBook book;
  private Supplier<Context> contextSupplier;
  private Function<LoanableBook, String> coverUrlProvider;

  private CoverUtil(final Supplier<Context> contextSupplier) {
    this.contextSupplier = contextSupplier;

    coverUrlProvider = new Function<LoanableBook, String>() {
      @Override
      public String apply(LoanableBook book) {
        if (contextSupplier.get() == null) {
          return null;
        }
        Isbn isbn = book.getData(StandardBookDataKeys.ISBN);
        return HttpUtil.getServerUrlFromSettings(contextSupplier.get(), EndpointType.COVER)
            .url()
            .toExternalForm()
            + "/" + isbn.getDigitsAsString() + ".jpg";
      }
    };
  }

  static CoverUtil withContext(Supplier<Context> context) {
    return new CoverUtil(context);
  }

  CoverUtil withBook(LoanableBook book) {
    this.book = Objects.requireNonNull(book, "book can not be null!");

    return this;
  }

  /**
   * @param coverUrlProvider The new provider for cover urls
   * @return This instance
   */
  CoverUtil withUrlProvider(Function<LoanableBook, String> coverUrlProvider) {
    this.coverUrlProvider = Objects
        .requireNonNull(coverUrlProvider, "coverUrlProvider can not be null!");

    return this;
  }

  /**
   * @param imageView The {@link ImageView} to load the image to
   */
  void loadInto(final ImageView imageView) {
    if (book == null) {
      throw new IllegalStateException("No book set!");
    }

    String url = coverUrlProvider.apply(book);
    if (url == null) {
      Log.i("BookDetailLayout", "Cover url for book: " + book + " was null!");
      return;
    }

    final Context context = contextSupplier.get();

    Request request = new Request.Builder()
        .url(url)
        .build();
    HttpUtil.makeCall(request, context, new Callback() {
      @Override
      public void onFailure(@Nullable Call call, @Nullable IOException e) {
        // ignore it
        Log.i("BookDetailLayout", "Request failed!", e);
      }

      @Override
      public void onResponse(@Nullable Call call, @Nullable Response response)
          throws IOException {
        if (response == null || CoverUtil.this.contextSupplier == null) {
          return;
        }
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
          return;
        }

        if (contextSupplier.get() == null) {
          return;
        }

        byte[] body = responseBody.bytes();
        final Bitmap bitmap = BitmapFactory.decodeByteArray(body, 0, body.length);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            imageView.setScaleType(ScaleType.CENTER_INSIDE);
            imageView.setImageBitmap(bitmap);
          }
        });
      }
    });
  }
}
