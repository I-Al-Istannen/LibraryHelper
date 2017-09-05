package me.ialistannen.libraryhelper.view.booklist;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
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
import me.ialistannen.libraryhelper.view.FragmentBase;
import me.ialistannen.libraryhelper.view.booklist.BookDetailList.ContextMenuCreator;
import me.ialistannen.libraryhelpercommon.book.LoanableBook;

/**
 * A fragment to display detail information about a book.
 */
public class BookDetailFragment extends FragmentBase {

  @BindView(R.id.book_detail_list)
  BookDetailList detailList;

  private boolean coverAdded;
  private LoanableBook book;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    coverAdded = false;

    View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

    ButterKnife.bind(this, view);

    if (book != null) {
      setCover(view);
      detailList.setBook(book);

      String title = book.getData(StandardBookDataKeys.TITLE);
      ((TextView) view.findViewById(R.id.book_title_text_view)).setText(title);
    }

    detailList.setContextMenuCreator(new ContextMenuCreator() {
      @Override
      public void onCreateContextMenu(final Pair<String, String> item, ContextMenu menu, View v,
          ContextMenuInfo cm) {

        MenuItem copyValue = menu.add(R.string.book_detail_fragment_context_menu_copy_value);
        copyValue.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem menu) {
            copyToClipboard(item.getKey(), item.getValue());
            return true;
          }
        });
      }
    });

    return view;
  }

  private void setCover(View view) {
    final ImageView cover = view.findViewById(R.id.cover_image_view);
    cover.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {
        if (coverAdded) {
          return true;
        }

        Picasso.with(getFragmentHolderActivity())
            .load(buildCoverImageUrl(book))
            .resize(cover.getWidth(), cover.getHeight())
            .centerInside()
            .into(cover);
        coverAdded = true;
        return true;
      }
    });
  }

  public void setBook(LoanableBook book) {
    this.book = book;
  }

  private String buildCoverImageUrl(LoanableBook book) {
    Isbn isbn = book.getData(StandardBookDataKeys.ISBN);
    return HttpUtil.getServerUrlFromSettings(getFragmentHolderActivity(), EndpointType.COVER)
        .url()
        .toExternalForm()
        + "/" + isbn.getDigitsAsString() + ".jpg";
  }

  private void copyToClipboard(String key, String item) {
    ClipboardManager clipboardManager = (ClipboardManager) getFragmentHolderActivity()
        .getSystemService(Context.CLIPBOARD_SERVICE);
    clipboardManager.setPrimaryClip(
        new ClipData(key, new String[]{"text"}, new Item(item)));
    Toast.makeText(getFragmentHolderActivity(), "Copied!", Toast.LENGTH_SHORT).show();
  }
}