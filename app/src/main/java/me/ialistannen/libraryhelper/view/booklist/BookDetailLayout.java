package me.ialistannen.libraryhelper.view.booklist;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.view.booklist.BookDetailList.ContextMenuCreator;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 *
 */
public class BookDetailLayout extends FrameLayout {

  private Function<LoanableBook, String> coverUrlProvider;

  {
    init();
  }

  @BindView(R.id.book_detail_list)
  BookDetailList detailList;

  private boolean animatePlaceholder;

  public BookDetailLayout(Context context) {
    super(context);
  }

  public BookDetailLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BookDetailLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setCoverUrlProvider(Function<LoanableBook, String> coverUrlProvider) {
    this.coverUrlProvider = coverUrlProvider;
  }

  private void init() {
    LayoutInflater inflater = LayoutInflater.from(getContext());

    View view = inflater.inflate(R.layout.book_detail_layout, this, false);

    ButterKnife.bind(this, view);

    detailList.setContextMenuCreator(new ContextMenuCreator() {
      @Override
      public void onCreateContextMenu(final Pair<String, String> item, ContextMenu menu, View v,
          ContextMenuInfo cm) {

        MenuItem copyValue = menu.add(R.string.book_detail_layout_context_menu_copy_value);
        copyValue.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem menu) {
            copyToClipboard(item.getKey(), item.getValue());
            return true;
          }
        });
      }
    });

    removeAllViews();

    addView(view);

    showPlaceholder(true);
  }

  private void setCover(View view, final LoanableBook book) {
    final ImageView cover = view.findViewById(R.id.cover_image_view);
    cover.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {

        CoverUtil coverUtil = CoverUtil.withContext(new Supplier<Context>() {
          @Override
          public Context get() {
            return getContext();
          }
        });
        if (coverUrlProvider != null) {
          coverUtil = coverUtil.withUrlProvider(coverUrlProvider);
        }
        coverUtil
            .withBook(book)
            .loadInto(cover);

        // only fire once
        cover.getViewTreeObserver().removeOnPreDrawListener(this);
        return true;
      }
    });
  }

  /**
   * @param book The {@link LoanableBook} to display
   */
  public void setBook(LoanableBook book) {
    showPlaceholder(false);
    setCover(this, book);
    detailList.setBook(book);

    String title = book.getData(StandardBookDataKeys.TITLE);
    ((TextView) findViewById(R.id.book_title_text_view)).setText(title);
  }

  /**
   * Sets the error state.
   *
   * <p>Placeholders will be shown, but not animated.
   */
  public void setError() {
    showPlaceholder(true);
    animatePlaceholder = false;
  }

  /**
   * Whether to show placeholders
   *
   * @param show True if placeholders should be shown
   */
  public void showPlaceholder(boolean show) {
    if (getContext() == null) {
      return;
    }
    final TextView title = findViewById(R.id.book_title_text_view);

    animatePlaceholder = show;

    if (show) {
      title.setText(getContext().getString(R.string.book_detail_layout_placeholder_title));
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        private int counter = 0;
        private boolean reverseDirection;

        @Override
        public void run() {
          if (!isShown()) {
            return;
          }
          if (animatePlaceholder) {
            postDelayed(this, 1000);
          } else {
            return;
          }

          if (counter >= 3) {
            reverseDirection = true;
          } else if (counter <= 0) {
            reverseDirection = false;
          }

          if (reverseDirection) {
            counter--;
          } else {
            counter++;
          }

          StringBuilder text = new StringBuilder(
              getContext().getString(R.string.book_detail_layout_placeholder_title)
          );

          for (int i = 0; i < counter; i++) {
            text.append(".");
          }

          title.setText(text.toString());
        }
      });
    }

    ImageView coverView = findViewById(R.id.cover_image_view);

    if (show) {
      coverView.setImageDrawable(
          ContextCompat.getDrawable(getContext(), R.drawable.book_cover_placeholder)
      );
    }
  }

  private void copyToClipboard(String key, String item) {
    ClipboardManager clipboardManager = (ClipboardManager) getContext()
        .getSystemService(Context.CLIPBOARD_SERVICE);
    clipboardManager.setPrimaryClip(
        new ClipData(key, new String[]{"text"}, new Item(item)));
    Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
  }
}
