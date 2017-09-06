package me.ialistannen.libraryhelper.view.booklist;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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
import com.squareup.picasso.Picasso;
import me.ialistannen.isbnlookuplib.book.StandardBookDataKeys;
import me.ialistannen.isbnlookuplib.isbn.Isbn;
import me.ialistannen.isbnlookuplib.util.Pair;
import me.ialistannen.libraryhelper.R;
import me.ialistannen.libraryhelper.util.HttpUtil;
import me.ialistannen.libraryhelper.util.HttpUtil.EndpointType;
import me.ialistannen.libraryhelper.view.booklist.BookDetailList.ContextMenuCreator;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 *
 */
public class BookDetailLayout extends FrameLayout {

  {
    init();
  }

  @BindView(R.id.book_detail_list)
  BookDetailList detailList;

  private boolean coverAdded;
  private boolean showPlaceholder;

  public BookDetailLayout(Context context) {
    super(context);
  }

  public BookDetailLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public BookDetailLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private void init() {
    coverAdded = false;

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
        if (coverAdded) {
          return true;
        }

        Picasso.with(getContext())
            .load(buildCoverImageUrl(book))
            .resize(cover.getWidth(), cover.getHeight())
            .centerInside()
            .into(cover);
        coverAdded = true;
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

  public void showPlaceholder(boolean show) {
    if (getContext() == null) {
      return;
    }
    showPlaceholder = show;
    final TextView title = findViewById(R.id.book_title_text_view);

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
          if (showPlaceholder) {
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
      //noinspection deprecation - As we are having a min of 19, not 23
      coverView.setBackgroundColor(getResources().getColor(R.color.colorPlaceholder));
    } else {
      coverView.setBackgroundColor(Color.TRANSPARENT);
    }
  }

  private String buildCoverImageUrl(LoanableBook book) {
    Isbn isbn = book.getData(StandardBookDataKeys.ISBN);
    return HttpUtil.getServerUrlFromSettings(getContext(), EndpointType.COVER)
        .url()
        .toExternalForm()
        + "/" + isbn.getDigitsAsString() + ".jpg";
  }

  private void copyToClipboard(String key, String item) {
    ClipboardManager clipboardManager = (ClipboardManager) getContext()
        .getSystemService(Context.CLIPBOARD_SERVICE);
    clipboardManager.setPrimaryClip(
        new ClipData(key, new String[]{"text"}, new Item(item)));
    Toast.makeText(getContext(), "Copied!", Toast.LENGTH_SHORT).show();
  }
}
